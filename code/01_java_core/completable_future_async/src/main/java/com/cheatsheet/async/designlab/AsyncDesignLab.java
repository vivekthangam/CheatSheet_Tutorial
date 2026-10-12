package com.cheatsheet.async.designlab;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Architectural Anti-Patterns & Refactorings in Asynchronous Pipelines (DesignLab)
 */
public class AsyncDesignLab {

    // =========================================================================
    // ANTI-PATTERN 1: Blocking Inside thenApply vs Monadic thenCompose
    // =========================================================================

    public record Account(String id, String name) {}
    public record Balance(String accountId, long amountCents) {}
    public record AccountOverview(Account account, Balance balance) {}

    public static class MonadicChainingRefactor {
        // ⚠️ ANTI-PATTERN: Calling join() inside thenApply turns non-blocking pipeline into blocking
        public CompletableFuture<AccountOverview> vulnerableCompose(
                CompletableFuture<Account> accountFut,
                java.util.function.Function<String, CompletableFuture<Balance>> balanceFetcher
        ) {
            return accountFut.thenApply(account -> {
                Balance bal = balanceFetcher.apply(account.id()).join(); // ❌ BLOCKING JOIN!
                return new AccountOverview(account, bal);
            });
        }

        // 🏗️ REFACTOR: Use monadic thenCompose to chain asynchronous stages cleanly
        public CompletableFuture<AccountOverview> hardenedCompose(
                CompletableFuture<Account> accountFut,
                java.util.function.Function<String, CompletableFuture<Balance>> balanceFetcher
        ) {
            return accountFut.thenCompose(account ->
                    balanceFetcher.apply(account.id())
                            .thenApply(bal -> new AccountOverview(account, bal))
            );
        }
    }

    // =========================================================================
    // ANTI-PATTERN 2: Coordinated Speculative Race with Winning Cancellation
    // =========================================================================

    public static class SpeculativeRacingCoordinator {
        // 🏗️ REFACTOR: Coordinated speculative race that cancels losing tasks immediately
        public static <T> CompletableFuture<T> raceWithCancellation(List<CompletableFuture<T>> futures) {
            CompletableFuture<T> promise = new CompletableFuture<>();
            for (CompletableFuture<T> f : futures) {
                f.thenAccept(result -> {
                    if (promise.complete(result)) {
                        // Winner found! Cancel all competitors
                        futures.forEach(other -> {
                            if (other != f) {
                                other.cancel(true);
                            }
                        });
                    }
                }).exceptionally(ex -> null);
            }
            return promise;
        }
    }
}
