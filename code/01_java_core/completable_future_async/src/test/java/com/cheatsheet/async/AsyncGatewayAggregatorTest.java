package com.cheatsheet.async;

import com.cheatsheet.async.buglab.AsyncBugLab;
import com.cheatsheet.async.designlab.AsyncDesignLab;
import com.cheatsheet.async.engine.AsyncGatewayAggregator;
import com.cheatsheet.async.securitylab.AsyncSecurityLab;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class AsyncGatewayAggregatorTest {

    private AsyncGatewayAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new AsyncGatewayAggregator(4, 8, 100);
    }

    @AfterEach
    void tearDown() {
        aggregator.close();
    }

    @Test
    @DisplayName("Test Multi-Stage Fan-In Aggregation with allOf and Fallback Timeouts")
    void testAggregateAccountDetailsSuccess() throws Exception {
        var detailsFut = aggregator.aggregateAccountDetails("USER-123", Duration.ofSeconds(2));
        var details = detailsFut.get();

        assertNotNull(details);
        assertEquals("USER-123", details.profile().userId());
        assertEquals("JaneDoe", details.profile().username());
        assertEquals(2, details.orders().orderIds().size());
        assertEquals(785, details.credit().score());
    }

    @Test
    @DisplayName("Test Monadic Error Recovery via exceptionallyCompose Failover")
    void testSecondaryFailover() throws Exception {
        // Primary URL fails, secondary succeeds
        CompletableFuture<String> resultFut = aggregator.fetchWithSecondaryFailover(
                "http://fail-primary.api/data",
                "http://backup.api/data"
        );

        String result = resultFut.get();
        assertTrue(result.contains("backup.api"));
    }

    @Test
    @DisplayName("Test BugLab: Root Cause Exception Unwrapping")
    void testExceptionUnwrapper() {
        AsyncBugLab.CustomBusinessException bizEx = new AsyncBugLab.CustomBusinessException("Domain violation");
        CompletionException wrapped = new CompletionException(new ExecutionException(bizEx));

        assertFalse(AsyncBugLab.ExceptionUnwrapper.vulnerableIsBusinessException(wrapped));
        assertTrue(AsyncBugLab.ExceptionUnwrapper.hardenedIsBusinessException(wrapped));
        assertSame(bizEx, AsyncBugLab.ExceptionUnwrapper.unwrapRootCause(wrapped));
    }

    @Test
    @DisplayName("Test BugLab: ThreadLocal Context Propagation Across Async Boundaries")
    void testContextPropagation() throws Exception {
        AsyncBugLab.SecurityContextHolder.set("admin_user");

        var wrappedSupplier = AsyncBugLab.ContextPropagator.wrapWithContext(
                () -> AsyncBugLab.SecurityContextHolder.get()
        );

        // Run on another thread
        CompletableFuture<String> future = CompletableFuture.supplyAsync(wrappedSupplier, aggregator.getIoThreadPool());
        String contextInWorker = future.get();

        assertEquals("admin_user", contextInWorker);
        AsyncBugLab.SecurityContextHolder.clear();
    }

    @Test
    @DisplayName("Test SecurityLab: Bounded Queue Backpressure and Error Sanitization")
    void testSecurityLabBoundedQueueAndSanitization() throws Exception {
        AsyncSecurityLab.BoundedAsyncScheduler scheduler = new AsyncSecurityLab.BoundedAsyncScheduler(2);

        var okFut = scheduler.submitSecureTask(() -> "OK_DATA");
        assertEquals("OK_DATA", okFut.get());

        // Sanitized error boundary
        CompletableFuture<String> failingFut = CompletableFuture.failedFuture(new RuntimeException("DB_PASSWORD_LEAK"));
        var sanitized = AsyncSecurityLab.SanitizedAsyncBoundary.sanitizeError(failingFut);
        assertEquals("ERR_UPSTREAM_UNAVAILABLE", sanitized.get());

        scheduler.shutdown();
    }

    @Test
    @DisplayName("Test DesignLab: Monadic Chaining and Speculative Cancellation")
    void testDesignLabMonadicAndCancellation() throws Exception {
        AsyncDesignLab.MonadicChainingRefactor refactor = new AsyncDesignLab.MonadicChainingRefactor();

        CompletableFuture<AsyncDesignLab.Account> accountFut = CompletableFuture.completedFuture(
                new AsyncDesignLab.Account("ACC-1", "Corp")
        );

        var overviewFut = refactor.hardenedCompose(accountFut, id ->
                CompletableFuture.completedFuture(new AsyncDesignLab.Balance(id, 50000L))
        );

        var overview = overviewFut.get();
        assertEquals("ACC-1", overview.account().id());
        assertEquals(50000L, overview.balance().amountCents());

        // Speculative race cancellation
        CompletableFuture<String> fast = CompletableFuture.completedFuture("FAST");
        CompletableFuture<String> slow = new CompletableFuture<>();

        CompletableFuture<String> winner = AsyncDesignLab.SpeculativeRacingCoordinator.raceWithCancellation(
                List.of(fast, slow)
        );

        assertEquals("FAST", winner.get());
        assertTrue(slow.isCancelled());
    }
}
