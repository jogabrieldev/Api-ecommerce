package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.IdentityType;

public interface UserIdentityRegistry {

    void claim(String email, String cpf, IdentityType type);
}
