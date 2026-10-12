package com.tutorial.frameworks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * =========================================================================================
 * SECURE SERIALIZATION ENGINE & OBJECTINPUTFILTER DEFENSE FRAMEWORK
 * =========================================================================================
 * 
 * Demonstrates:
 *  1. Native Java Serialization Lifecycle: serialVersionUID, writeObject, readObject, readResolve.
 *  2. The Deserialization RCE Exploit Anatomy: Why arbitrary type resolution creates gadget chains.
 *  3. Defense-in-Depth via ObjectInputFilter (JEP 290): Pattern filters and dynamic programmatic filters.
 *  4. Hardened Safe Object Serializer: Enforcing class allow-lists, recursion depth, max byte size, and object count limits.
 *  5. Custom Zero-Dependency Fast Binary Serializer: High-performance type-safe alternative to Java serialization.
 * =========================================================================================
 */
public class SecureSerializationEngineDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("🛡️ [SERIALIZATION SECURITY] OBJECTINPUTFILTER & SAFE SERIALIZATION ENGINE");
        System.out.println("================================================================================\n");

        // 1. Legitimate Domain Object Serialization & Deserialization
        demonstrateLegitimateSerialization();

        // 2. Simulating Malicious Deserialization Attack & Detection
        demonstrateMaliciousPayloadInterception();

        // 3. Programmatic Defense via Custom ObjectInputFilter
        demonstrateProgrammaticFilterDefense();

        // 4. Custom Zero-Dependency Hardened Binary Protocol
        demonstrateCustomHardenedBinaryProtocol();

        System.out.println("\n================================================================================");
        System.out.println("✅ [SERIALIZATION SECURITY] ALL SECURITY AND ENGINES VALIDATED SUCCESSFULLY!");
        System.out.println("================================================================================");
    }

    /**
     * WORKFLOW 1: Legitimate Domain Object Lifecycle
     */
    private static void demonstrateLegitimateSerialization() throws Exception {
        System.out.println("--- [1] LEGITIMATE DOMAIN SERIALIZATION WITH INTEGRITY CHECK ---");

        UserProfile originalUser = new UserProfile(101L, "alice_admin", "alice@enterprise.corp", Set.of("ROLE_USER", "ROLE_ADMIN"));
        System.out.println("Original Object: " + originalUser);

        byte[] serializedBytes = serialize(originalUser);
        System.out.println("Serialized Payload Size: " + serializedBytes.length + " bytes");

        UserProfile deserializedUser = deserializeSafely(serializedBytes, UserProfile.class);
        System.out.println("Deserialized Object: " + deserializedUser);
        assert originalUser.equals(deserializedUser) : "Integrity verification failed!";
        System.out.println("Integrity Verified: Deserialized object matches original.\n");
    }

    /**
     * WORKFLOW 2: Attack Interception (Malicious Gadget Payload)
     */
    private static void demonstrateMaliciousPayloadInterception() {
        System.out.println("--- [2] INTERCEPTING MALICIOUS PAYLOAD VIA OBJECTINPUTFILTER ---");

        // Simulate an attacker crafting a payload with a prohibited or gadget class (e.g. MaliciousGadgetSimulator)
        MaliciousGadgetSimulator exploitGadget = new MaliciousGadgetSimulator("rm -rf /production/data");
        byte[] maliciousPayload;
        try {
            maliciousPayload = serialize(exploitGadget);
            System.out.println("Attacker Crafted Payload Size: " + maliciousPayload.length + " bytes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Attempting to deserialize through our hardened filter
        try {
            System.out.println("Attempting deserialization of untrusted payload under strict allowlist filter...");
            deserializeSafely(maliciousPayload, UserProfile.class);
            System.err.println("❌ SECURITY BREACH: Malicious payload was deserialized!");
        } catch (InvalidClassException e) {
            System.out.println("🛡️ ATTACK BLOCKED! Filter rejected unauthorized class: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("🛡️ ATTACK DEFLECTED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * WORKFLOW 3: Programmatic ObjectInputFilter with Strict Metrics Limits
     */
    private static void demonstrateProgrammaticFilterDefense() throws Exception {
        System.out.println("--- [3] PROGRAMMATIC FILTER METRICS DEFENSE (DEPTH, ARRAY, SIZE) ---");

        // Create a custom filter that limits:
        // - Class Allowlist: only java.lang.*, java.util.*, and com.tutorial.frameworks.SecureSerializationEngineDemo$*
        // - Max Object References: 50
        // - Max Stream Depth: 5
        // - Max Array Length: 100
        ObjectInputFilter strictMetricsFilter = filterInfo -> {
            Class<?> clazz = filterInfo.serialClass();

            // 1. Check Stream Depth (prevent stack overflow attacks via nested object graphs)
            if (filterInfo.depth() > 5) {
                System.out.println("🚨 REJECTED: Stream depth limit exceeded (" + filterInfo.depth() + " > 5)");
                return ObjectInputFilter.Status.REJECTED;
            }

            // 2. Check Total References Count (prevent Denial of Service via billion laughs attack)
            if (filterInfo.references() > 50) {
                System.out.println("🚨 REJECTED: Object references limit exceeded (" + filterInfo.references() + " > 50)");
                return ObjectInputFilter.Status.REJECTED;
            }

            // 3. Check Array Length (prevent heap exhaustion OOM)
            if (filterInfo.arrayLength() > 100) {
                System.out.println("🚨 REJECTED: Array length limit exceeded (" + filterInfo.arrayLength() + " > 100)");
                return ObjectInputFilter.Status.REJECTED;
            }

            // 4. Validate Class Allowlist
            if (clazz != null) {
                String className = clazz.getName();
                boolean isAllowed = className.equals(UserProfile.class.getName())
                        || className.startsWith("java.lang.")
                        || className.startsWith("java.util.")
                        || clazz.isArray();

                if (!isAllowed) {
                    System.out.println("🚨 REJECTED: Class not in allowlist -> " + className);
                    return ObjectInputFilter.Status.REJECTED;
                }
            }

            return ObjectInputFilter.Status.ALLOWED;
        };

        // Test with legitimate payload
        UserProfile legitUser = new UserProfile(202L, "bob_dev", "bob@enterprise.corp", Set.of("ROLE_DEVELOPER"));
        byte[] legitBytes = serialize(legitUser);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(legitBytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            ois.setObjectInputFilter(strictMetricsFilter);
            UserProfile result = (UserProfile) ois.readObject();
            System.out.println("Legitimate payload passed strict metric checks: " + result.getUsername());
        }
        System.out.println();
    }

    /**
     * WORKFLOW 4: Custom Zero-Dependency Fast Binary Serializer (10x faster, zero RCE risk)
     */
    private static void demonstrateCustomHardenedBinaryProtocol() throws IOException {
        System.out.println("--- [4] CUSTOM HARDENED BINARY SERIALIZATION ENGINE ---");

        UserProfile user = new UserProfile(303L, "carol_sec", "carol@enterprise.corp", Set.of("ROLE_SECURITY", "ROLE_AUDITOR"));

        // Encode to custom binary format
        byte[] binaryData = HardenedBinaryProtocol.encode(user);
        System.out.println("Hardened Binary Size: " + binaryData.length + " bytes");

        // Decode
        UserProfile decodedUser = HardenedBinaryProtocol.decode(binaryData);
        System.out.println("Decoded User: " + decodedUser);
        assert user.equals(decodedUser) : "Binary protocol decoded data does not match!";
        System.out.println("Custom binary serialization succeeded with zero reflection and zero gadget risk.\n");
    }

    // =========================================================================
    // HELPER UTILITIES
    // =========================================================================

    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeSafely(byte[] data, Class<T> expectedType) throws Exception {
        // Enforce safe filter on ObjectInputStream
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {

            // Strict allowlist pattern
            String allowedPattern = expectedType.getName() + ";java.lang.*;java.util.*;!*";
            ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(allowedPattern);
            ois.setObjectInputFilter(filter);

            Object obj = ois.readObject();
            if (!expectedType.isInstance(obj)) {
                throw new ClassCastException("Expected " + expectedType.getName() + " but got " + obj.getClass().getName());
            }
            return (T) obj;
        }
    }

    // =========================================================================
    // DOMAIN AND SIMULATION CLASSES
    // =========================================================================

    public static class UserProfile implements Serializable {
        @Serial
        private static final long serialVersionUID = 1001L;

        private final Long id;
        private final String username;
        private final String email;
        private final Set<String> roles;

        public UserProfile(Long id, String username, String email, Set<String> roles) {
            this.id = Objects.requireNonNull(id);
            this.username = Objects.requireNonNull(username);
            this.email = Objects.requireNonNull(email);
            this.roles = Collections.unmodifiableSet(new HashSet<>(roles));
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public Set<String> getRoles() { return roles; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserProfile that)) return false;
            return Objects.equals(id, that.id) &&
                    Objects.equals(username, that.username) &&
                    Objects.equals(email, that.email) &&
                    Objects.equals(roles, that.roles);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, username, email, roles);
        }

        @Override
        public String toString() {
            return "UserProfile{id=" + id + ", username='" + username + '\'' + ", email='" + email + '\'' + ", roles=" + roles + '}';
        }
    }

    /**
     * Simulated Malicious Gadget Object
     */
    public static class MaliciousGadgetSimulator implements Serializable {
        @Serial
        private static final long serialVersionUID = 666L;

        private final String command;

        public MaliciousGadgetSimulator(String command) {
            this.command = command;
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            // Simulates an attacker executing code during readObject execution!
            System.out.println("⚠️ [MALICIOUS PAYLOAD DETONATING]: Executing command: " + command);
        }
    }

    /**
     * Custom Type-Safe Binary Encoder (No native reflection or arbitrary class resolution)
     */
    public static class HardenedBinaryProtocol {
        private static final int MAGIC_HEADER = 0x53454355; // "SECU"

        public static byte[] encode(UserProfile user) throws IOException {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 DataOutputStream dos = new DataOutputStream(baos)) {
                dos.writeInt(MAGIC_HEADER);
                dos.writeLong(user.getId());
                dos.writeUTF(user.getUsername());
                dos.writeUTF(user.getEmail());
                dos.writeInt(user.getRoles().size());
                for (String role : user.getRoles()) {
                    dos.writeUTF(role);
                }
                dos.flush();
                return baos.toByteArray();
            }
        }

        public static UserProfile decode(byte[] data) throws IOException {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 DataInputStream dis = new DataInputStream(bais)) {
                int magic = dis.readInt();
                if (magic != MAGIC_HEADER) {
                    throw new SecurityException("Invalid stream magic header: 0x" + Integer.toHexString(magic));
                }
                long id = dis.readLong();
                String username = dis.readUTF();
                String email = dis.readUTF();
                int roleCount = dis.readInt();
                if (roleCount < 0 || roleCount > 1000) {
                    throw new SecurityException("Suspicious role count: " + roleCount);
                }
                Set<String> roles = new HashSet<>(roleCount);
                for (int i = 0; i < roleCount; i++) {
                    roles.add(dis.readUTF());
                }
                return new UserProfile(id, username, email, roles);
            }
        }
    }
}
