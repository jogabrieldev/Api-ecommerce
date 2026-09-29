package com.api.e_commerce.domain.model;

import com.api.e_commerce.domain.security.AdministratorPermission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdministratorRoleTest {

    @Test
    void adminShouldHaveSensitiveManagementPermissions() {
        assertTrue(AdministratorRole.ADMIN.permissions()
                .contains(AdministratorPermission.ADMINISTRATOR_MANAGE));
        assertTrue(AdministratorRole.ADMIN.permissions()
                .contains(AdministratorPermission.CUSTOMER_READ));
    }

    @Test
    void managerShouldBeLimitedToCatalogAndOwnFinancialData() {
        assertFalse(AdministratorRole.MANAGER.permissions()
                .contains(AdministratorPermission.ADMINISTRATOR_MANAGE));
        assertFalse(AdministratorRole.MANAGER.permissions()
                .contains(AdministratorPermission.CUSTOMER_READ));
        assertTrue(AdministratorRole.MANAGER.permissions()
                .contains(AdministratorPermission.CATEGORY_MANAGE));
        assertTrue(AdministratorRole.MANAGER.permissions()
                .contains(AdministratorPermission.PRODUCT_CREATE));
        assertTrue(AdministratorRole.MANAGER.permissions()
                .contains(AdministratorPermission.PRODUCT_IMPORT));
        assertTrue(AdministratorRole.MANAGER.permissions()
                .contains(AdministratorPermission.FINANCIAL_READ));
    }
}
