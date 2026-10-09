package com.portafolio_manager.backend.domain.user;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {"Abc#12", "Secret#123", "Pa$$w0rd1234"})
    void shouldAcceptPasswordsMatchingRf004(String password) {
        assertThat(PasswordPolicy.isSatisfiedBy(password)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "Ab#1",           // too short
            "Abcdef#123456",  // too long
            "secret#123",     // no uppercase
            "SECRET#123",     // no lowercase
            "Secret#abc",     // no digit
            "Secret1234"      // no special character
    })
    void shouldRejectPasswordsBreakingRf004(String password) {
        assertThat(PasswordPolicy.isSatisfiedBy(password)).isFalse();
    }
}
