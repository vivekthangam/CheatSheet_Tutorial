package com.cheatsheet.collections.securitylab;

import java.util.*;

/**
 * Security Vulnerabilities & Mitigations in Collections & Streams (SecurityLab)
 */
public class CollectionsSecurityLab {

    // =========================================================================
    // VULNERABILITY 1: Hash Collision Denial-of-Service (HashDoS) Defense
    // =========================================================================

    public static final class SecureComparableKey implements Comparable<SecureComparableKey> {
        private final String key;

        public SecureComparableKey(String key) {
            this.key = Objects.requireNonNull(key);
        }

        public String getKey() {
            return key;
        }

        // 🛡️ MITIGATION: Implementing Comparable enables HashMap to treeify colliding bins into Red-Black Tree (O(log n))
        @Override
        public int compareTo(SecureComparableKey other) {
            return this.key.compareTo(other.key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SecureComparableKey that)) return false;
            return Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    // =========================================================================
    // VULNERABILITY 2: Mutable State Exposure via Unmodifiable Wrappers
    // =========================================================================

    public static class UnmodifiableWrapperLeak {
        private final List<String> internalRoles = new ArrayList<>();

        public void addRole(String role) {
            internalRoles.add(role);
        }

        // 🚨 VULNERABLE: Collections.unmodifiableList still mutates when internalRoles changes underneath!
        public List<String> vulnerableGetRoles() {
            return Collections.unmodifiableList(internalRoles);
        }

        // 🛡️ MITIGATION: List.copyOf creates an immutable snapshot decoupled from internal mutations
        public List<String> hardenedGetRoles() {
            return List.copyOf(internalRoles);
        }
    }
}
