package com.api.e_commerce.domain.security;

public interface PasswordHasher {

    String hash(String rawPassword);
}
