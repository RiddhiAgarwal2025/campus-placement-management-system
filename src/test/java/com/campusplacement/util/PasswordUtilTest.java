package com.campusplacement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordUtilTest {

    @Test
    @DisplayName("Hash produces valid pbkdf2_sha256 string representation")
    void testHashFormat() {
        String hash = PasswordUtil.hash("Secret@123");
        assertThat(hash).isNotNull().startsWith("pbkdf2_sha256$65536$");
        String[] parts = hash.split("\\$");
        assertThat(parts).hasSize(4);
        assertThat(parts[0]).isEqualTo("pbkdf2_sha256");
        assertThat(parts[1]).isEqualTo("65536");
        assertThat(parts[2]).isNotEmpty(); // salt
        assertThat(parts[3]).isNotEmpty(); // hash
    }

    @Test
    @DisplayName("Verify returns true for matching password")
    void testVerifySuccess() {
        String password = "CorrectPassword#2026";
        String hash = PasswordUtil.hash(password);
        assertThat(PasswordUtil.verify(password, hash)).isTrue();
        assertThat(PasswordUtil.verify(password.toCharArray(), hash)).isTrue();
    }

    @Test
    @DisplayName("Verify returns false for incorrect password")
    void testVerifyFailure() {
        String hash = PasswordUtil.hash("CorrectPassword");
        assertThat(PasswordUtil.verify("WrongPassword", hash)).isFalse();
        assertThat(PasswordUtil.verify("correctpassword", hash)).isFalse();
        assertThat(PasswordUtil.verify("", hash)).isFalse();
    }

    @Test
    @DisplayName("Verify handles null and malformed stored hashes gracefully")
    void testVerifyMalformed() {
        assertThat(PasswordUtil.verify("pass", null)).isFalse();
        assertThat(PasswordUtil.verify((String) null, "hash")).isFalse();
        assertThat(PasswordUtil.verify((char[]) null, "hash")).isFalse();
        assertThat(PasswordUtil.verify("pass", "malformed_string")).isFalse();
        assertThat(PasswordUtil.verify("pass", "pbkdf2_sha256$not_a_number$salt$hash")).isFalse();
    }

    @Test
    @DisplayName("Different salts produce different hashes for the same password")
    void testSaltRandomness() {
        String hash1 = PasswordUtil.hash("SamePassword");
        String hash2 = PasswordUtil.hash("SamePassword");
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(PasswordUtil.verify("SamePassword", hash1)).isTrue();
        assertThat(PasswordUtil.verify("SamePassword", hash2)).isTrue();
    }

    @Test
    @DisplayName("Null password throws IllegalArgumentException")
    void testNullPasswordThrows() {
        assertThatThrownBy(() -> PasswordUtil.hash((String) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
