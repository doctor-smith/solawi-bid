# Permission write paths: reachability and callers

Baseline: `feature/SMA-594-legal-entity-tenancy` at merge commit `0265c8a4`.

This note separates HTTP reachability from internal repository use. It is a
code-and-test inventory, not an authorization redesign.

## `PutUserRoleContext`

- `Application.setupRouting` registers `permissions` behind JWT authentication
  (`application/routing/routing.kt:46-48`).
- `PUT permissions/user/user-role-context` is registered and invokes the
  `PutUserRoleContext` action without an `IsGranted` action
  (`module/permission/routing/routing.kt:34-37`).
- The action calls `Transaction.putUserRoleContext` directly. The repository
  replaces all role bindings for the body `userId` and `contextId`; it contains
  no actor, owner, membership, or entitlement check
  (`module/permission/repository/put-user-role-context.kt:29-35`).
- This is a live application flow, not merely an unused API declaration. The
  private application/organization management UI dispatches
  `putUsersRoleContext` when its permissions dialog is submitted
  (`private-application-organiation-management.kt:265-272`). The frontend
  action maps that operation to the `PutUserRoleContext` endpoint
  (`put-users-role-context.kt:27-43`). Existing E2E page objects also wait for
  this endpoint while granting permissions.
- No other production caller of `putUserRoleContext` was found. Organization
  member management modifies `UserRoleContext` independently and therefore is
  a separate write path.

Conclusion: the HTTP route is registered, frontend-used, and reaches the
repository. The route now requires `MANAGE_USER_ROLE_CONTEXTS` in the payload
context before invoking the action.

## `PutRoleRightContext`

- `PUT permissions/contexts/role-right-context` is registered behind JWT
  authentication (`module/permission/routing/routing.kt:63-67`).
- Its route requires `MANAGE_ROLE_RIGHT_CONTEXTS` in the payload context before
  invoking the action. The following action invokes
  `Transaction.putRoleRightContext`, whose repository replaces rights for the
  supplied role and context without an actor-level check
  (`module/permission/repository/put-role-right-context.kt:19-27`).
- The endpoint is present in the shared API registry, but no production
  frontend dispatch of `PutRoleRightContext` was found.
- The same repository is used internally by
  `updateStandardApplicationContext` and `updateStandardModuleContext`
  (`service.update-standard-application-context.kt:15-46`). The application
  service is reached through a separate route protected by
  `IsGranted("UPDATE_APPLICATION")`
  (`module/application/routing/routing.kt:115-119`). The module helper has no
  production caller at this baseline.

Conclusion: the repository itself is intentionally authorization-agnostic.
One service caller has an upstream `UPDATE_APPLICATION` guard, while the direct
permission HTTP route uses `MANAGE_ROLE_RIGHT_CONTEXTS`. The two entry paths
must not be assessed as one authorization boundary.

## Executable characterization

`PermissionRoutingTests` contains two negative API tests using the ordinary
`unautorized@solyton.org` account. They submit the already existing assignment
state and verify both `403` and an unchanged observable assignment state. The
guard derives its authorization context from the payload rather than trusting
the request header.
