package com.api.e_commerce.domain.model;

import com.api.e_commerce.domain.security.AdministratorPermission;

import java.util.EnumSet;
import java.util.Set;

public enum AdministratorRole {
    ADMIN(EnumSet.of(
            AdministratorPermission.ADMINISTRATOR_MANAGE,
            AdministratorPermission.CUSTOMER_READ,
            AdministratorPermission.CATEGORY_MANAGE,
            AdministratorPermission.PRODUCT_CREATE,
            AdministratorPermission.PRODUCT_IMPORT,
            AdministratorPermission.FINANCIAL_READ
    )),
    MANAGER(EnumSet.of(
            AdministratorPermission.ADMINISTRATOR_CREATE,
            AdministratorPermission.CATEGORY_MANAGE,
            AdministratorPermission.PRODUCT_CREATE,
            AdministratorPermission.PRODUCT_IMPORT,
            AdministratorPermission.FINANCIAL_READ
    ));

    private final Set<AdministratorPermission> permissions;

    AdministratorRole(Set<AdministratorPermission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public Set<AdministratorPermission> permissions() {
        return permissions;
    }
}
