package com.marketingsales.backend.constant;

/**
 * Application-wide user roles.
 * ADMIN               -> full access to the dashboard, user & config management
 * MARKETING_MANAGER   -> dashboard access scoped to marketing/sales operations
 * STAFF               -> mobile app access, field/sales staff
 *
 * Kept as a simple enum for this skeleton. If dynamic, DB-driven roles/permissions
 * are needed later, promote this to a Role/Permission entity + join table
 * without changing the public API surface (still expose role name as String).
 */
public enum Role {
    ADMIN,
    MARKETING_MANAGER,
    STAFF
}
