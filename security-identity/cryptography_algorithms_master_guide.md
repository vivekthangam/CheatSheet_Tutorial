[🏠 Back to Home](README.md) | [🔐 Java & Spring Cryptography Guide](java_spring_cryptography_master_guide.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)

# 🛡️ The Master Cryptography Algorithms Encyclopedia & Engineering Manual

An exhaustive, production-grade handbook covering **every foundational, modern, and advanced cryptographic algorithm**. Includes mathematical foundations, why each exists, pros and cons, real-world industry adoption, step-by-step implementation blueprints (Java, Node.js, Python), and deep dives into **Post-Quantum Cryptography (PQC)**, **Zero-Knowledge Proofs (ZKP)**, **Fully Homomorphic Encryption (FHE)**, and **Multi-Party Computation (MPC)**.

---

## 📑 Table of Contents

1. [🧠 The Grand Taxonomy & Core Mental Models of Cryptography](#-the-grand-taxonomy--core-mental-models-of-cryptography)
2. [📊 Master Cryptographic Algorithms Decision Matrix](#-master-cryptographic-algorithms-decision-matrix)
3. [🔒 Part 1: Symmetric Key Encryption & Block Cipher Modes](#-part-1-symmetric-key-encryption--block-cipher-modes)
   - [1.1 AES (Advanced Encryption Standard / Rijndael)](#11-aes-advanced-encryption-standard--rijndael)
   - [1.2 Block Cipher Modes of Operation: ECB, CBC, CTR, GCM (AEAD), XTS](#12-block-cipher-modes-of-operation)
   - [1.3 ChaCha20-Poly1305 (Modern Stream Cipher)](#13-chacha20-poly1305-modern-stream-cipher)
   - [1.4 Legacy & Historical Ciphers: DES, 3DES, Blowfish, Twofish](#14-legacy--historical-ciphers-des-3des-blowfish-twofish)
4. [🔑 Part 2: Asymmetric Encryption & Key Exchange Protocols](#-part-2-asymmetric-encryption--key-exchange-protocols)
   - [2.1 RSA (Rivest–Shamir–Adleman) & OAEP](#21-rsa-rivestshamiradleman--oaep)
   - [2.2 Diffie-Hellman (DH) & Ephemeral Diffie-Hellman (DHE)](#22-diffie-hellman-dh--ephemeral-diffie-hellman-dhe)
   - [2.3 Elliptic Curve Cryptography (ECC): ECDH, X25519, NIST P-256, secp256k1](#23-elliptic-curve-cryptography-ecc-ecdh-x25519-nist-p-256-secp256k1)
5. [✍️ Part 3: Digital Signatures & Asymmetric Authentication](#️-part-3-digital-signatures--asymmetric-authentication)
   - [3.1 RSA-PSS (Probabilistic Signature Scheme)](#31-rsa-pss-probabilistic-signature-scheme)
   - [3.2 ECDSA (Elliptic Curve Digital Signature Algorithm)](#32-ecdsa-elliptic-curve-digital-signature-algorithm)
   - [3.3 EdDSA / Ed25519 (RFC 8032)](#33-eddsa--ed25519-rfc-8032)
   - [3.4 BLS Signatures (Boneh–Lynn–Shacham - Signature Aggregation)](#34-bls-signatures-bonehlynnshacham---signature-aggregation)
6. [🧩 Part 4: Cryptographic Hash Functions](#-part-4-cryptographic-hash-functions)
   - [4.1 Broken & Deprecated Hashes: MD5 & SHA-1](#41-broken--deprecated-hashes-md5--sha-1)
   - [4.2 SHA-2 Family (SHA-256, SHA-384, SHA-512)](#42-sha-2-family-sha-256-sha-384-sha-512)
   - [4.3 SHA-3 Family & Keccak (Sponge Construction & SHAKE)](#43-sha-3-family--keccak-sponge-construction--shake)
   - [4.4 BLAKE2 & BLAKE3 (High-Throughput Tree Hashing)](#44-blake2--blake3-high-throughput-tree-hashing)
7. [🛡️ Part 5: Message Authentication Codes (MAC)](#️-part-5-message-authentication-codes-mac)
   - [5.1 HMAC (Hash-Based Message Authentication Code)](#51-hmac-hash-based-message-authentication-code)
   - [5.2 Poly1305 & KMAC](#52-poly1305--kmac)
8. [🧂 Part 6: Password Hashing & Key Derivation Functions (KDF)](#-part-6-password-hashing--key-derivation-functions-kdf)
   - [6.1 Argon2 (Argon2id, Argon2d, Argon2i)](#61-argon2-argon2id-argon2d-argon2i)
   - [6.2 BCrypt](#62-bcrypt)
   - [6.3 PBKDF2 & Scrypt](#63-pbkdf2--scrypt)
   - [6.4 HKDF (HMAC-Based Extract-and-Expand Key Derivation)](#64-hkdf-hmac-based-extract-and-expand-key-derivation)
9. [🚀 Part 7: Advanced Cryptography (PQC, ZKP, FHE, MPC)](#-part-7-advanced-cryptography-pqc-zkp-fhe-mpc)
   - [7.1 Post-Quantum Cryptography (NIST FIPS 203, 204, 205: ML-KEM, ML-DSA, SLH-DSA)](#71-post-quantum-cryptography-pqc)
   - [7.2 Zero-Knowledge Proofs (ZKP: zk-SNARKs, zk-STARKs, Bulletproofs)](#72-zero-knowledge-proofs-zkp)
   - [7.3 Homomorphic Encryption (Partially & Fully Homomorphic: Paillier, BFV, CKKS)](#73-homomorphic-encryption-fhe)
   - [7.4 Multi-Party Computation (MPC) & Shamir's Secret Sharing](#74-multi-party-computation-mpc--secret-sharing)
10. [🗺️ Part 8: The "Which Algorithm Should I Use?" Architectural Decision Flowchart](#️-part-8-the-which-algorithm-should-i-use-architectural-decision-flowchart)
11. [🎓 Part 9: Staff-Level Cryptographic Security Traps & Interview Bank](#-part-9-staff-level-cryptographic-security-traps--interview-bank)

---

# 🧠 The Grand Taxonomy & Core Mental Models of Cryptography

Cryptography is the mathematical science of securing information against adversaries. It is organized into **Four Foundational Objectives**:

```
                                  CRYPTOGRAPHY
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
  CONFIDENTIALITY                  INTEGRITY                   AUTHENTICITY &
 (Encryption/Ciphers)           (Hashing & MAC)               NON-REPUDIATION
         │                             │                             │
   ┌─────┴─────┐                 ┌─────┴─────┐                       │
   ▼           ▼                 ▼           ▼                       ▼
Symmetric  Asymmetric        Fast Hashes    MACs             Digital Signatures
 (AES,       (RSA,            (SHA-256,    (HMAC,             (Ed25519, ECDSA,
ChaCha20)    ECC)              BLAKE3)    Poly1305)               RSA-PSS)
                                 │
                                 ▼
                          Password Hashes (KDF)
                           (Argon2id, BCrypt)
```

1. **Confidentiality**: Only authorized recipients can read the plaintext (e.g., AES-256-GCM, RSA-OAEP).
2. **Integrity**: Any unauthorized modification to the data in transit or storage is immediately detected (e.g., SHA-256, HMAC).
3. **Authenticity**: Proves beyond doubt that the data originated from the claimed sender (e.g., Digital Signatures, HMAC).
4. **Non-Repudiation**: The author cannot deny having authored or signed the message (e.g., Ed25519, RSA-PSS).

---

# 📊 Master Cryptographic Algorithms Decision Matrix

| Algorithm | Category | Key / Block Size | Security Strength | Performance | Primary Real-World Application | Current Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **AES-256-GCM** | Symmetric AEAD | 256-bit key, 128-bit block | Top Tier (Post-Quantum Resilient) | $>5\text{ GB/s}$ (AES-NI hardware) | TLS 1.3, Database columns, Disk encryption | **Gold Standard** |
| **ChaCha20-Poly1305** | Symmetric AEAD | 256-bit key, 64-byte stream | Top Tier | Extreme on mobile/ARM without AES-NI | WireGuard VPN, Android TLS 1.3 | **Gold Standard** |
| **RSA-4096 (OAEP)** | Asymmetric Cipher | 4096-bit key | High (Breaks under Quantum Shor's) | Slow ($\approx 1\text{ ms}$) | Legacy email encryption (S/MIME, PGP) | Legacy / Deprecating |
| **X25519 (ECDH)** | Key Exchange | 256-bit key | High | Fast ($<50\ \mu\text{s}$) | TLS 1.3 Handshake, Signal Protocol | **Gold Standard** |
| **Ed25519 (EdDSA)** | Digital Signature | 256-bit key, 64-byte sig | Highest (Side-channel immune) | Ultra-fast ($>50,000\text{ sigs/s}$) | SSH keys, Git commit signing, Crypto (Solana) | **Gold Standard** |
| **ECDSA (secp256k1)** | Digital Signature | 256-bit key, 64-byte sig | High | Fast | Bitcoin, Ethereum transactions | Standard in Web3 |
| **SHA-256** | Hash Function | 256-bit output | High (Collision resistant) | Very Fast ($500\text{ MB/s}$) | Git commit hashes, Blockchains, TLS certs | Standard |
| **BLAKE3** | Tree Hash | 256-bit output | High | Extreme ($6.8\text{ GB/s}$ SIMD) | Distributed file systems, package managers | Modern High-Perf |
| **HMAC-SHA256** | Message Auth (MAC) | Variable key, 256-bit tag | High | Very Fast | Stripe/GitHub Webhooks, AWS SigV4, JWTs | **Gold Standard** |
| **Argon2id** | Password Hash (KDF) | Configurable (Memory-hard) | Winner of PHC | Tunable ($>50\text{ ms}$) | Linux LUKS2, 1Password, Bitwarden | **Gold Standard** |
| **BCrypt** | Password Hash | 72-byte max pass, 184-bit salt | Strong | Tunable ($100\text{ ms}$) | Spring Security, Rails, Django auth | Legacy Standard |
| **ML-KEM (Kyber)** | Post-Quantum KEM | 768 / 1024-bit security | Quantum-Proof (Module Lattices) | Fast ($<100\ \mu\text{s}$) | Hybrid TLS 1.3 (Chrome + Cloudflare) | **NIST FIPS 203 (2024)** |
| **ML-DSA (Dilithium)** | Post-Quantum Signature | Matrix Lattice | Quantum-Proof | Fast | Future PKI, Federal identity certs | **NIST FIPS 204 (2024)** |

---

# 🔒 Part 1: Symmetric Key Encryption & Block Cipher Modes

Symmetric encryption uses a single shared secret key for both encryption and decryption.

---

## 1.1 AES (Advanced Encryption Standard / Rijndael)

### Why We Need It
Prior to AES, the world relied on DES (56-bit key), which became vulnerable to brute force by the late 1990s. In 2001, NIST selected the **Rijndael cipher** (designed by Vincent Rijmen and Joan Daemen) as AES. It is the global foundation of electronic commerce, government security, and data protection.

### How It Works Internally
AES is a **Substitution-Permutation Network (SPN)** operating on 128-bit (16-byte) blocks arranged in a $4 \times 4$ byte state matrix. Depending on the key size, it executes multiple rounds:
- **AES-128**: 10 rounds
- **AES-192**: 12 rounds
- **AES-256**: 14 rounds

Each round executes four deterministic mathematical transformations:
1. `SubBytes`: Non-linear byte substitution using the Rijndael S-Box (inversion over Galois Field $\text{GF}(2^8)$).
2. `ShiftRows`: Cyclically shifts row $i$ by $i$ positions to the left.
3. `MixColumns`: Matrix multiplication over $\text{GF}(2^8)$ to diffuse byte values across the column.
4. `AddRoundKey`: Bitwise XOR of the state matrix with the derived round key.

```
Plaintext (16 bytes) ──► [ AddRoundKey ] ──► [ 10/12/14 Rounds: SubBytes -> ShiftRows -> MixColumns -> AddRoundKey ] ──► Ciphertext
```

### Pros & Cons
- **Pros**:
  - Extremely fast: modern x86 and ARM processors feature dedicated hardware instructions (`AES-NI`, `ARMv8 Crypto`) encrypting at $>5\text{ GB/s}$ per core.
  - Immune to all practical cryptanalytic attacks after 25+ years of analysis.
  - AES-256 offers 128-bit post-quantum security against Grover's algorithm.
- **Cons**:
  - Fixed 128-bit block size requires a mode of operation to encrypt larger streams.
  - Vulnerable to cache-timing side-channel attacks if implemented with software lookup tables instead of constant-time AES-NI instructions.

### Real-World Adoption
- TLS 1.3 internet traffic encryption.
- Full Disk Encryption: BitLocker (Windows), FileVault (macOS), LUKS (Linux).
- Database transparent data encryption (TDE): AWS RDS, PostgreSQL, MongoDB.

### Step-by-Step Usage: AES-256-GCM in Java 21
```java
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BITS = 128; // Authentication Tag
    private static final int IV_LENGTH_BYTES = 12;  // Standard 96-bit Nonce

    // Step 1: Generate a cryptographically secure 256-bit AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }

    // Step 2: Encrypt with random IV and return combined IV + Ciphertext
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv); // Mandatory: Unique IV every call!

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Pack IV + Ciphertext together
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        buffer.put(iv).put(ciphertext);
        return Base64.getEncoder().encodeToString(buffer.array());
    }

    // Step 3: Decrypt and authenticate
    public static String decrypt(String base64Payload, SecretKey key) throws Exception {
        byte[] payload = Base64.getDecoder().decode(base64Payload);
        ByteBuffer buffer = ByteBuffer.wrap(payload);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        buffer.get(iv);
        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] plaintext = cipher.doFinal(ciphertext); // Throws AEADBadTagException if tampered!

        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
```

---

## 1.2 Block Cipher Modes of Operation

A raw block cipher only encrypts exactly 16 bytes. Modes of operation determine how to encrypt arbitrary-length data.

| Mode | Authenticated (AEAD)? | Parallelizable? | Streaming? | Security Rating | Major Flaw / Critical Vulnerability |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ECB** (Electronic Codebook) | No | Yes | No | **BROKEN / INSECURE** | Identical plaintext blocks produce identical ciphertext blocks (Leaking structural patterns). |
| **CBC** (Cipher Block Chaining) | No | No (Sequential) | No | **VULNERABLE** | Padding Oracle attacks (POODLE, Lucky Thirteen) unless paired with HMAC-SHA256. |
| **CTR** (Counter Mode) | No | Yes | Yes | Medium | Turns block cipher into stream cipher; disastrous if Counter/Nonce is reused. |
| **GCM** (Galois/Counter Mode) | **Yes (AEAD)** | **Yes (SIMD)** | **Yes** | **GOLD STANDARD** | Nonce reuse leaks the authentication key $H$, permitting message forgery. |
| **XTS** (XEX Tweakable Ciphertext Stealing)| No | Yes | No | **STANDARD (Disks)**| Designed strictly for block storage devices (BitLocker, LUKS); not for network protocols. |

---

## 1.3 ChaCha20-Poly1305 (Modern Stream Cipher)

### Why We Need It
While AES is ultra-fast with hardware instructions, low-power mobile devices, smart watches, and microcontrollers often lack dedicated `AES-NI` hardware silicon. Software-based AES on such devices is slow and vulnerable to cache-timing attacks. Daniel J. Bernstein (DJB) designed **ChaCha20** paired with **Poly1305** to deliver high-speed, constant-time software encryption without hardware acceleration.

### How It Works Internally
- **ChaCha20**: A stream cipher based on a 512-bit state (16 32-bit words) using an **ARX network (Addition, Rotation, XOR)**. It executes 20 rounds (10 column rounds, 10 diagonal rounds) using only basic integer addition, XOR, and bitwise rotations.
- **Poly1305**: A high-speed one-time authenticator evaluating a polynomial over the prime field $2^{130} - 5$.

### Pros & Cons
- **Pros**:
  - Immune to cache-timing attacks in pure software.
  - Faster than software AES on ARM CPUs (e.g. Raspberry Pi, older Android phones).
  - Adopted as an official IETF standard (RFC 8439).
- **Cons**:
  - On desktop and server CPUs with `AES-NI`, AES-GCM is typically $2\times$ to $4\times$ faster.

### Real-World Adoption
- Default cipher in **WireGuard VPN**.
- Mandatory cipher in **TLS 1.3** and **SSH**.
- Android OS internal file-based encryption.

### Step-by-Step Usage: Node.js Crypto API
```javascript
const crypto = require('crypto');

function encryptChaCha20(plaintext, key) {
  // Key must be exactly 32 bytes (256 bits)
  // Nonce (IV) must be exactly 12 bytes (96 bits)
  const nonce = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv('chacha20-poly1305', key, nonce, { authTagLength: 16 });

  let ciphertext = cipher.update(plaintext, 'utf8', 'hex');
  ciphertext += cipher.final('hex');
  const authTag = cipher.getAuthTag().toString('hex');

  return {
    nonce: nonce.toString('hex'),
    ciphertext,
    authTag
  };
}

function decryptChaCha20(encryptedData, key) {
  const decipher = crypto.createDecipheriv(
    'chacha20-poly1305',
    key,
    Buffer.from(encryptedData.nonce, 'hex'),
    { authTagLength: 16 }
  );

  decipher.setAuthTag(Buffer.from(encryptedData.authTag, 'hex'));
  let decrypted = decipher.update(encryptedData.ciphertext, 'hex', 'utf8');
  decrypted += decipher.final('utf8'); // Throws Error if tampered!
  return decrypted;
}

// Execution
const key = crypto.randomBytes(32);
const encrypted = encryptChaCha20("Secret Payload", key);
console.log("Decrypted:", decryptChaCha20(encrypted, key));
```

---

## 1.4 Legacy & Historical Ciphers: DES, 3DES, Blowfish, Twofish

1. **DES (Data Encryption Standard - 1977)**:
   - 56-bit key, 64-bit block size.
   - *Why Broken*: 56-bit keyspace can be brute-forced in under 24 hours using specialized hardware (EFF Deep Crack). **Status: Broken / Never use.**
2. **3DES (Triple DES - EDE)**:
   - Encrypts with Key 1, Decrypts with Key 2, Encrypts with Key 3 ($E_{K3}(D_{K2}(E_{K1}(P)))$).
   - *Why Retired*: 64-bit block size makes it vulnerable to **Sweet32 collision attacks** after encrypting $\approx 32\text{ GB}$ of data under the same key. **Status: Deprecated by NIST in 2023.**
3. **Blowfish (1993)**:
   - Designed by Bruce Schneier. Fast, unpatented Feistel cipher.
   - *Limitation*: 64-bit block size also suffers from Sweet32 collisions. Succeeded by Twofish.
4. **Twofish (1998)**:
   - 128-bit block size, up to 256-bit key. AES finalist. Highly secure, but slower than Rijndael on hardware.

---

# 🔑 Part 2: Asymmetric Encryption & Key Exchange Protocols

Asymmetric cryptography solves the fundamental **Key Distribution Problem**: how two parties communicate securely without sharing a prior secret key.

---

## 2.1 RSA (Rivest–Shamir–Adleman) & OAEP

### Why We Need It
Invented in 1977 by Ron Rivest, Adi Shamir, and Leonard Adleman, RSA was the first practical public-key cryptosystem enabling both public-key encryption and digital signatures.

### How It Works Internally
RSA relies on the **Factoring Problem**: multiplying two large prime numbers ($p$ and $q$) is computationally trivial ($N = p \times q$), but factoring $N$ back into $p$ and $q$ is computationally infeasible for classical computers when $N$ is 2048 to 4096 bits.

1. Select two distinct prime numbers $p$ and $q$.
2. Compute modulus: $N = p \times q$.
3. Compute Euler's totient: $\phi(N) = (p - 1)(q - 1)$.
4. Choose public exponent $e$ such that $\gcd(e, \phi(N)) = 1$ (Standard: $e = 65537$).
5. Compute private exponent $d \equiv e^{-1} \pmod{\phi(N)}$.
6. **Encryption**: $C \equiv M^e \pmod N$.
7. **Decryption**: $M \equiv C^d \pmod N$.

### Critical Requirement: OAEP (Optimal Asymmetric Encryption Padding)
Never use "Textbook RSA" ($M^e \pmod N$) or PKCS#1 v1.5 padding! They are vulnerable to **Bleichenbacher's Million Message Attack** (padding oracle). Always use **RSA-OAEP (RFC 8017)** with SHA-256.

### Pros & Cons
- **Pros**: Universally supported across every operating system, HSM, and smartcard on earth.
- **Cons**:
  - Very slow: modular exponentiation with 4096-bit numbers is computationally heavy.
  - Large keys: 4096-bit RSA keys consume significant bandwidth compared to 256-bit ECC keys.
  - **Quantum Vulnerable**: Shor's polynomial-time factoring algorithm will completely break RSA once fault-tolerant quantum computers exist.

---

## 2.2 Diffie-Hellman (DH) & Ephemeral Diffie-Hellman (DHE)

### Why We Need It
Diffie-Hellman (1976) allows two parties (Alice and Bob) to negotiate a shared secret key over an insecure, public channel without transmitting the secret itself.

### How It Works Internally (Discrete Logarithm Problem)
1. Alice and Bob agree publicly on a prime modulus $p$ and generator $g$.
2. Alice picks private secret $a$ and sends public value $A = g^a \pmod p$.
3. Bob picks private secret $b$ and sends public value $B = g^b \pmod p$.
4. Alice calculates: $S = B^a \pmod p = (g^b)^a = g^{ab} \pmod p$.
5. Bob calculates: $S = A^b \pmod p = (g^a)^b = g^{ab} \pmod p$.
6. Both now share identical secret $S$ without eavesdroppers being able to compute $g^{ab}$.

### Ephemeral Diffie-Hellman (DHE) & Forward Secrecy
If static private keys are used, an adversary who records encrypted traffic today and steals the server's private key 5 years later can decrypt all historical traffic.
**Ephemeral DH (DHE / ECDHE)** generates fresh, one-time temporary keypairs for **every single session**. Stealing tomorrow's server key cannot decrypt yesterday's recorded traffic. This property is **Perfect Forward Secrecy (PFS)**.

---

## 2.3 Elliptic Curve Cryptography (ECC): ECDH, X25519, NIST P-256, secp256k1

### Why We Need It
To match the security of a 3072-bit RSA key, an elliptic curve requires only a **256-bit key**. This yields smaller certificates, faster network handshakes, lower memory consumption, and dramatically reduced CPU drain.

### How It Works Internally
ECC operates on points $(x, y)$ satisfying the Weierstrass curve equation over a finite field:
$$y^2 = x^3 + ax + b \pmod p$$
- Point Addition and Point Doubling form an abelian group.
- **Scalar Multiplication**: Given a base point $G$ and an integer private key $k$, computing the public key point $P = k \cdot G$ is easy.
- **Elliptic Curve Discrete Logarithm Problem (ECDLP)**: Given $P$ and $G$, finding $k$ is practically impossible for properly chosen curves.

### The Major Curves in Production
1. **Curve25519 / X25519 (Montgomery Curve)**:
   - Designed by DJB. Immune to side-channel attacks and invalid curve attacks.
   - The default key exchange mechanism in TLS 1.3, OpenSSH, and Signal.
2. **NIST P-256 (secp256r1 / prime256v1)**:
   - US Government standard curve. Ubiquitous in web PKI certificates, Apple Pay, and FIDO2/WebAuthn tokens.
3. **secp256k1 (Koblitz Curve)**:
   - Features efficient endomorphisms. Selected by Satoshi Nakamoto for **Bitcoin**, and later adopted by **Ethereum**.

### Step-by-Step Usage: High-Speed X25519 Key Exchange in Python
```python
from cryptography.hazmat.primitives.asymmetric import x25519

# Step 1: Alice generates private/public keypair
alice_private_key = x25519.X25519PrivateKey.generate()
alice_public_key = alice_private_key.public_key()

# Step 2: Bob generates private/public keypair
bob_private_key = x25519.X25519PrivateKey.generate()
bob_public_key = bob_private_key.public_key()

# Step 3: Alice and Bob exchange public keys over the network and compute shared secret
alice_shared_key = alice_private_key.exchange(bob_public_key)
bob_shared_key = bob_private_key.exchange(alice_public_key)

# Verification
assert alice_shared_key == bob_shared_key
print(f"Negotiated 256-bit Shared Secret: {alice_shared_key.hex()[:32]}...")
```

---

# ✍️ Part 3: Digital Signatures & Asymmetric Authentication

Digital signatures guarantee **Authenticity** and **Non-Repudiation**.

---

## 3.1 RSA-PSS (Probabilistic Signature Scheme)
- Replaces legacy RSA PKCS#1 v1.5 signatures.
- Incorporates randomized salt into the signature generation process, providing provable security in the Random Oracle Model.
- Standard for high-assurance document signing and TLS 1.3 certificates.

---

## 3.2 ECDSA (Elliptic Curve Digital Signature Algorithm)
- Generates a signature pair $(r, s)$ using elliptic curve point multiplication.
- **THE FATAL TRAP (Nonce Reuse)**:
  - ECDSA requires generating a random integer $k$ (nonce) for every signature.
  - **If the same nonce $k$ is used to sign two different messages, the signer's private key can be extracted instantly via basic algebra!** (This exact vulnerability allowed hackers to extract the private signing key of the Sony PlayStation 3 in 2010).
  - Modern implementation standard: **RFC 6979** (Deterministic ECDSA), which derives $k$ pseudo-randomly from the message hash and private key.

---

## 3.3 EdDSA / Ed25519 (RFC 8032)

### Why We Need It
EdDSA (Edwards-curve Digital Signature Algorithm) running on **Curve25519 (Ed25519)** solves every practical flaw of ECDSA:
1. **Deterministic by Design**: Never relies on an external random number generator during signing; nonce reuse is mathematically impossible.
2. **Constant-Time Execution**: Immune to cache-timing and branch-prediction side-channel attacks.
3. **Extreme Performance**: Verifies $>50,000$ signatures per second on standard server hardware.

### Step-by-Step Usage: Ed25519 in Java 21
```java
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Ed25519Signer {
    public static void main(String[] args) throws Exception {
        // 1. Generate Ed25519 Keypair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = kpg.generateKeyPair();

        byte[] payload = "Authorize Wire Transfer of $50,000".getBytes(StandardCharsets.UTF_8);

        // 2. Sign the message
        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(keyPair.getPrivate());
        signer.update(payload);
        byte[] signature = signer.sign();

        // 3. Verify the signature
        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(payload);
        boolean isValid = verifier.verify(signature);

        System.out.println("Signature Valid: " + isValid);
    }
}
```

---

## 3.4 BLS Signatures (Boneh–Lynn–Shacham)

### Why We Need It
In Proof-of-Stake blockchains with hundreds of thousands of validators (e.g. Ethereum), verifying 500,000 individual ECDSA signatures per block would freeze the network. **BLS Signatures** utilize bilinear pairings on elliptic curves (e.g. BLS12-381) to **aggregate thousands of distinct signatures into a single 48-byte signature**. A validator can verify that 100,000 nodes signed a block in a single mathematical check!

---

# 🧩 Part 4: Cryptographic Hash Functions

A cryptographic hash function maps arbitrary-length input data to a fixed-size byte string.

### Properties of Secure Cryptographic Hashes:
1. **Pre-image Resistance (One-Way)**: Given hash $H$, it is computationally impossible to find message $M$ such that $\text{hash}(M) = H$.
2. **Second Pre-image Resistance (Weak Collision Resistance)**: Given $M_1$, it is impossible to find $M_2 \neq M_1$ such that $\text{hash}(M_1) = \text{hash}(M_2)$.
3. **Collision Resistance (Strong Collision Resistance)**: It is impossible to find *any* two arbitrary messages $M_1 \neq M_2$ such that $\text{hash}(M_1) = \text{hash}(M_2)$.
4. **Avalanche Effect**: Flipping a single bit in the input flips $\approx 50\%$ of the output bits randomly.

---

## 4.1 Broken Hashes: MD5 & SHA-1
- **MD5 (128-bit output - 1991)**: Practical collisions can be generated on a smartphone in $<1\text{ second}$. Used in 2012 by the Flame cyberwarfare malware to forge Microsoft Windows Update digital certificates. **Status: Cryptographically Dead.**
- **SHA-1 (160-bit output - 1995)**: Broken by Google in 2017 (SHAttered attack) producing two distinct PDFs with identical SHA-1 hashes. **Status: Deprecated / Prohibited.**

---

## 4.2 SHA-2 Family (SHA-256, SHA-384, SHA-512)
- Designed by the NSA and published by NIST in 2001.
- Built on the **Merkle–Damgård construction** with the Davies-Meyer compression function.
- **Length Extension Vulnerability**:
  - Because Merkle-Damgård exposes internal state at the end of every block, an attacker who knows $\text{hash}(Secret || Message)$ can append extra data and compute a valid hash **without knowing the Secret**!
  - *Fix*: Never use raw $\text{SHA-256}(Key || Message)$ for authentication. Always use **HMAC-SHA256**.

---

## 4.3 SHA-3 Family & Keccak (Sponge Construction)
- Winner of the 9-year NIST public competition (2015).
- Built on the **Sponge Construction** (Keccak permutation) consisting of an **Absorbing Phase** and a **Squeezing Phase**.
- **Immune to Length Extension Attacks** by design.
- Supports variable-length outputs: **SHAKE128** and **SHAKE256** (Extendable-Output Functions - XOF).

---

## 4.4 BLAKE2 & BLAKE3 (High-Throughput Tree Hashing)
- Designed by Jack O'Connor, Jean-Philippe Aumasson, Samuel Neves, and Zooko Wilcox (2020).
- **BLAKE3**: Operates as a binary Merkle Tree over 1KB chunks.
- Highly parallelizable: utilizes AVX-512 and NEON SIMD lanes to hash at **6.8 GB/s**—faster than the transfer rate of standard PCIe NVMe SSDs!

---

# 🛡️ Part 5: Message Authentication Codes (MAC)

A MAC authenticates a message using a shared secret key, verifying both **Integrity** and **Authenticity**.

---

## 5.1 HMAC (Hash-Based Message Authentication Code - RFC 2104)

### How It Works Internally
HMAC resolves the Length Extension vulnerability of Merkle-Damgård hashes by applying a nested two-pass hashing scheme:
$$\text{HMAC}(K, M) = H\Big((K' \oplus \text{opad}) \mathbin{\Vert} H\big((K' \oplus \text{ipad}) \mathbin{\Vert} M\big)\Big)$$
- $\text{ipad} = 0\text{x}3636\dots$ (Inner Pad)
- $\text{opad} = 0\text{x}5\text{C}5\text{C}\dots$ (Outer Pad)

### Step-by-Step Usage: Webhook Signature Verification in Node.js
```javascript
const crypto = require('crypto');

function verifyWebhook(payloadString, signatureHeader, secretKey) {
  // Step 1: Compute expected HMAC-SHA256
  const computedHmac = crypto
    .createHmac('sha256', secretKey)
    .update(payloadString)
    .digest('hex');

  // Step 2: Constant-Time Comparison to defeat Remote Timing Attacks!
  // NEVER use: if (computedHmac === signatureHeader)
  const trustedBuffer = Buffer.from(computedHmac, 'hex');
  const untrustedBuffer = Buffer.from(signatureHeader, 'hex');

  if (trustedBuffer.length !== untrustedBuffer.length) {
    return false;
  }
  return crypto.timingSafeEqual(trustedBuffer, untrustedBuffer);
}
```

---

# 🧂 Part 6: Password Hashing & Key Derivation Functions (KDF)

Standard cryptographic hashes (like SHA-256) are **anti-patterns for passwords**. Modern GPU clusters calculate billions of SHA-256 hashes per second. Password hashes must be **intentionally slow, memory-hard, and computationally expensive**.

---

## 6.1 Argon2 (Winner of the Password Hashing Competition)

### Why We Need It
Argon2 was selected in 2015 as the premier password hashing standard, offering customizable memory parameters, time iterations, and parallelism threads.

### Variants
1. **Argon2d**: Maximizes resistance against GPU cracking by using data-dependent memory lookups. Vulnerable to cache-timing side-channel attacks.
2. **Argon2i**: Uses data-independent memory lookups to defeat cache-timing attacks. Ideal for password-based key derivation.
3. **Argon2id (RECOMMENDED)**: Hybrid approach. Uses Argon2i for the first pass over memory and Argon2d for subsequent passes, defeating both GPU cracking and side-channel attacks.

### Parameter Guidelines (OWASP 2024):
- **Memory**: $64\text{ MB}$ ($65,536\text{ KB}$)
- **Iterations (Time)**: $3$ passes
- **Parallelism**: $1$ to $4$ threads

---

## 6.2 BCrypt
- Designed in 1999 by Niels Provos and David Mazières based on the **Eksblowfish** cipher.
- Uses an expensive key setup phase parameterized by a work factor ($2^{\text{cost}}$ iterations).
- **The Hard Limit**: BCrypt truncates passwords silently at **72 bytes**. Passwords exceeding 72 characters must be pre-hashed with SHA-256 before feeding into BCrypt.

---

## 6.3 HKDF (HMAC-based Extract-and-Expand Key Derivation - RFC 5869)
Used when you already have high-entropy input keying material (such as a Diffie-Hellman shared secret) and need to derive multiple cryptographically independent subkeys (e.g. an encryption key, a MAC key, and an IV).
- **Step 1 (Extract)**: Condenses input material into a pseudorandom key (PRK) using HMAC and a salt.
- **Step 2 (Expand)**: Expands the PRK into arbitrary-length subkeys using contextual application info strings.

---

# 🚀 Part 7: Advanced Cryptography (PQC, ZKP, FHE, MPC)

---

## 7.1 Post-Quantum Cryptography (PQC)

### The Threat: Shor's Algorithm
Peter Shor proved in 1994 that a sufficiently large fault-tolerant quantum computer can factor large integers and solve discrete logarithms in polynomial time ($\mathcal{O}((\log N)^3)$). **A quantum computer will break RSA, ECC, Diffie-Hellman, and ECDSA.**

Symmetric ciphers (AES-256) and hash functions (SHA-256) remain secure against Grover's quantum search algorithm, merely requiring key sizes to be doubled (AES-256 retains 128 bits of quantum security).

### NIST Post-Quantum Standards (Finalized August 2024)
NIST released the official Federal Information Processing Standards (FIPS):

```
┌────────────────────────────────────────────────────────────────────────┐
│                        NIST POST-QUANTUM STANDARDS                     │
│                                                                        │
│  1. FIPS 203 (ML-KEM / CRYSTALS-Kyber):                                │
│     - General Key Encapsulation (Replaces RSA / ECDH Key Exchange)     │
│     - Hard mathematical problem: Module Learning With Errors (M-LWE)   │
│                                                                        │
│  2. FIPS 204 (ML-DSA / CRYSTALS-Dilithium):                            │
│     - Primary General-Purpose Digital Signature Standard               │
│     - Hard mathematical problem: Module Learning With Errors (M-LWE)   │
│                                                                        │
│  3. FIPS 205 (SLH-DSA / SPHINCS+):                                     │
│     - Stateless Hash-Based Digital Signature                           │
│     - Fallback signature relying purely on SHA-256 / SHAKE security   │
└────────────────────────────────────────────────────────────────────────┘
```

### Real-World Adoption: Hybrid Key Exchange (X25519 + Kyber768)
Today, production browsers (Google Chrome) and edge CDNs (Cloudflare) negotiate **Hybrid TLS 1.3** (`X25519Kyber768Draft00`). The client performs both a classical X25519 exchange and a quantum-resistant ML-KEM exchange, combining secrets. If Kyber is ever compromised, classical X25519 protects the connection; if quantum computers emerge, Kyber protects the connection.

---

## 7.2 Zero-Knowledge Proofs (ZKP)

A Zero-Knowledge Proof allows a **Prover** to prove to a **Verifier** that a statement is mathematically true without revealing any information beyond the statement's validity.

### The 3 Mathematical Properties:
1. **Completeness**: If the statement is true and both parties follow the protocol, the verifier will always be convinced.
2. **Soundness**: A dishonest prover cannot convince the verifier of a false statement (except with negligible probability).
3. **Zero-Knowledge**: The verifier learns nothing except that the statement is true (no secret witness data is leaked).

### zk-SNARKs vs zk-STARKs

| Dimension | zk-SNARKs (Groth16, PLONK) | zk-STARKs (Scalable Transparent ARKs) |
| :--- | :--- | :--- |
| **Proof Size** | Ultra-compact ($\approx 200 - 400\text{ bytes}$) | Larger ($\approx 10 - 100\text{ KB}$) |
| **Verification Speed** | Constant time ($<5\text{ ms}$) | Fast ($<20\text{ ms}$) |
| **Trusted Setup?** | **Required** (Toxic waste ceremony in Groth16) | **No Trusted Setup** (Uses public randomness) |
| **Quantum Resistance**| Vulnerable (Uses Elliptic Curve Pairings) | **Quantum Resistant** (Uses Collision-Resistant Hashes) |
| **Real-World Use** | Zcash, Tornado Cash, Aztec | StarkNet, dYdX, Polygon Miden |

### Real-World Production Applications
1. **Confidential Transactions & Privacy Blockchains**: Proving a wallet has sufficient balance to transfer tokens without revealing the sender, receiver, or transaction amount.
2. **zk-Rollups (Layer-2 Scaling)**: Bundling 10,000 off-chain transactions into a single cryptographic proof verified on Ethereum mainnet in a single transaction.
3. **Identity Verification & Age Proofs**: Proving via cryptographic passport chips that "User is older than 21" without revealing date of birth, name, or nationality.

---

## 7.3 Homomorphic Encryption (FHE)

### The Vision: Computing on Encrypted Data
In traditional systems, data must be decrypted in memory before it can be processed by a CPU or machine learning model. If the server is compromised, all plaintext is exposed.
**Fully Homomorphic Encryption (FHE)** allows a third party to compute arbitrary mathematical functions over encrypted data without ever decrypting it:
$$\text{Enc}(A) \otimes \text{Enc}(B) = \text{Enc}(A \times B)$$

```
Client (Has Private Key)                Cloud Server (Has Public Evaluation Key)
┌──────────────────────┐                ┌───────────────────────────────────────┐
│ Encrypts Medical Data│ ──Ciphertext─► │ Executes ML Diagnostics on Ciphertext │
│                      │                │ (Cannot read patient records!)        │
│ Decrypts Diagnosis   │ ◄──Result───── │ Returns Encrypted Diagnostic Result   │
└──────────────────────┘                └───────────────────────────────────────┘
```

### Major FHE Schemes:
1. **Paillier (Partially Homomorphic)**: Supports addition of encrypted ciphertexts. Extremely fast; used in electronic voting.
2. **BFV / BGV (Somewhat / Fully Homomorphic)**: Supports exact arithmetic on encrypted integers.
3. **CKKS (Cheon-Kim-Kim-Song)**: Supports approximate arithmetic over encrypted complex numbers and floating-point tensors. The premier scheme for **Privacy-Preserving Machine Learning (PPML)** and encrypted AI inference.

---

## 7.4 Multi-Party Computation (MPC) & Shamir's Secret Sharing

### Shamir's Secret Sharing ($k$-of-$n$ Threshold Scheme)
Adi Shamir proved that any secret number $S$ can be divided into $n$ distinct shares such that:
- Any $k$ shares ($k \le n$) can reconstruct the secret completely.
- Any $k - 1$ shares reveal **zero mathematical information** about the secret.

It uses polynomial interpolation over finite fields:
$$f(x) = S + a_1 x + a_2 x^2 + \dots + a_{k-1} x^{k-1} \pmod p$$
The secret is the y-intercept $f(0) = S$. Any $k$ points $(x_i, f(x_i))$ determine the unique degree-$(k-1)$ polynomial via **Lagrange Interpolation**.

### Threshold Signatures (TSS - MPC Wallets)
Instead of having a single private key stored on an AWS server or phone, a private key is split across 3 independent servers:
- Server 1 (User Phone), Server 2 (Enterprise Security Server), Server 3 (Backup Cloud).
- To sign an Ethereum or Bitcoin transaction, Server 1 and Server 2 coordinate an MPC protocol to generate a valid ECDSA signature **without ever combining the private key shares in memory on any machine!**

---

# 🗺️ Part 8: The "Which Algorithm Should I Use?" Architectural Decision Flowchart

```
What is your engineering objective?
 │
 ├── Encrypt Data at Rest / In Transit (Confidentiality)
 │    ├── Symmetric (Shared Secret Available):
 │    │    ├── Have modern x86/ARM server with hardware AES? ────► [ AES-256-GCM ]
 │    │    └── Mobile / Embedded / IoT device? ──────────────────► [ ChaCha20-Poly1305 ]
 │    │
 │    └── Asymmetric (Encrypting for a remote recipient):
 │         ├── Modern Standard: ───────────────────────────────► [ Hybrid: X25519 + AES-256-GCM ]
 │         └── Post-Quantum Ready: ────────────────────────────► [ Hybrid: X25519 + ML-KEM (Kyber) ]
 │
 ├── Digital Signatures (Authenticity & Non-Repudiation)
 │    ├── Standard Web / Cloud: ───────────────────────────────► [ Ed25519 (EdDSA) ]
 │    ├── Web3 / Bitcoin / Ethereum: ──────────────────────────► [ ECDSA (secp256k1) with RFC 6979 ]
 │    ├── Consolidating 10,000+ Signatures (Consensus): ───────► [ BLS Signatures (BLS12-381) ]
 │    └── Post-Quantum Compliant: ─────────────────────────────► [ ML-DSA (Dilithium) ]
 │
 ├── Message Integrity & Webhook Tamper Detection
 │    └── Shared secret exists: ───────────────────────────────► [ HMAC-SHA256 ] (Use Constant-Time Compare!)
 │
 ├── High-Speed Data Fingerprinting / Deduplication
 │    └── Extreme throughput needed (>5 GB/s): ────────────────► [ BLAKE3 ]
 │
 └── Password Storage / Key Derivation
      ├── Storing User Passwords: ─────────────────────────────► [ Argon2id (Memory: 64MB, Passes: 3) ]
      ├── Legacy Framework Compatibility: ─────────────────────► [ BCrypt (Cost Factor 12+) ]
      └── Deriving Subkeys from Master Secret: ────────────────► [ HKDF (RFC 5869) ]
```

---

# 🎓 Part 9: Staff-Level Cryptographic Security Traps & Interview Bank

### 1. What is the mathematical vulnerability of AES-GCM IV reuse?
AES-GCM combines CTR mode encryption with the GHASH authenticator over Galois Field $\text{GF}(2^{128})$. If the same key $K$ and IV are used to encrypt two distinct messages:
1. The keystreams match: $C_1 \oplus C_2 = P_1 \oplus P_2$, leaking the XOR difference of plaintexts.
2. The GHASH polynomial can be solved as a system of linear equations, enabling an attacker to recover the secret hash subkey $H = \text{AES}_K(0^{128})$. Once $H$ is known, the attacker can forge authentication tags for arbitrary forged ciphertexts.

### 2. Why is `MessageDigest.isEqual()` mandatory when verifying HMAC signatures?
Standard comparison operators (`String.equals()` or `==`) abort execution at the very first mismatched byte. An attacker transmits millions of HTTP requests with varying candidate bytes, measuring response latency with nanosecond precision. Latency peaks when the first byte is correct, then the second byte, allowing full signature extraction in minutes (**Remote Timing Attack**). `MessageDigest.isEqual()` executes in constant time by bitwise-ORing differences across all bytes.

### 3. How does Bleichenbacher's attack on RSA PKCS#1 v1.5 work?
Bleichenbacher's Million Message attack exploits chosen-ciphertext oracle feedback. In PKCS#1 v1.5, decrypted plaintext must begin with the byte sequence `0x00 0x02`. If a server returns an explicit error or timing difference when an altered ciphertext $C' = C \cdot s^e \pmod N$ decrypts to an invalid padding block, an attacker can iteratively narrow down the plaintext bounds by testing candidate values of $s$, decrypting the message without knowing the private key. RSA-OAEP eliminates this vulnerability.

### 4. Explain the difference between statistical randomness and cryptographic entropy.
Statistical randomness (e.g. `java.util.Random`, Mersenne Twister) ensures uniform distribution over a sample space, but outputs are mathematically deterministic and predictable once the internal state/seed is observed. Cryptographic entropy (`SecureRandom`) captures non-deterministic atmospheric, thermal, or hardware quantum noise from the OS kernel (`/dev/urandom`), satisfying unpredictability under adversarial analysis.

### 5. What is the difference between Pre-Quantum and Post-Quantum Security Levels?
A classical computer requires $2^k$ operations to brute-force a $k$-bit symmetric key. Under Grover's quantum search algorithm, a quantum computer can search an unsorted database in $\mathcal{O}(\sqrt{N})$ steps. Thus, AES-128 drops to $2^{64}$ quantum operations (theoretically vulnerable), while AES-256 drops to $2^{128}$ quantum operations (safe against quantum computers for millennia). Asymmetric algorithms (RSA, ECC) are completely broken by Shor's algorithm and drop from exponential to polynomial complexity.

---

## ⚖️ Cryptography Master Cheat Sheet

| Use Case | Recommended Algorithm | Key Size / Spec | Java / Node Provider |
| :--- | :--- | :--- | :--- |
| **Payload Encryption** | AES-GCM | 256-bit Key, 96-bit IV, 128-bit Tag | `AES/GCM/NoPadding` |
| **Mobile Encryption** | ChaCha20-Poly1305 | 256-bit Key, 96-bit Nonce | `chacha20-poly1305` |
| **Key Exchange** | X25519 (ECDH) | Curve25519 (32 bytes) | `X25519` |
| **Digital Signatures** | Ed25519 (EdDSA) | 256-bit Key, 64-byte Sig | `Ed25519` |
| **Password Storage** | Argon2id | Memory: 64MB, Passes: 3, Threads: 1 | `Argon2PasswordEncoder` |
| **Webhook Tamper Check**| HMAC-SHA256 | 256-bit Key (Constant-time compare)| `HmacSHA256` |
| **Subkey Derivation** | HKDF | RFC 5869 Extract & Expand | `HKDF` |
| **Post-Quantum KEM** | ML-KEM (Kyber-768)| FIPS 203 Standard | Bouncy Castle PQC |

---
[🏠 Back to Home](README.md) | [🔐 Java & Spring Cryptography Guide](java_spring_cryptography_master_guide.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)
