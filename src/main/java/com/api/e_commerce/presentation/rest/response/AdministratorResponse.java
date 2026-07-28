package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;

import java.time.LocalDateTime;

public record AdministratorResponse(
        Long id,
        String name,
        String email,
        AdministratorRole role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AdministratorResponse from(Administrator administrator) {
        return new AdministratorResponse(
                administrator.getId(),
                administrator.getName(),
                administrator.getEmail(),
                administrator.getRole(),
                administrator.getActive(),
                administrator.getCreatedAt(),
                administrator.getUpdatedAt()
        );
    }
}
