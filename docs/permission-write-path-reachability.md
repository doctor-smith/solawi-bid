# Permission write paths: reachability and callers

Baseline for the application delegation section is `df18d7cd`. The right-policy write section refers to the existing integration behavior.

This note separates HTTP reachability from internal repository use. It is a
code-and-test inventory, not an authorization redesign.

## `PutUserRoleContext`

- `Application.setupRouting` registers this route behind JWT authentication.
  The action passes the authenticated actor and requested context to the
  repository, where the authorization check and write share a transaction.
- A stored organization-application booking selects its specific policy.
  The actor needs the Owner role in that context. Both the actor and target
  must be members of the organization on the stored booking.
- Only the application-specific roles on the shared allowlist can be added
  or removed by this policy. They must also exist in the context's policy.
  Existing Owner assignments and other nondelegable roles are preserved.
- For other contexts, the repository requires an exact
  `MANAGE_USER_ROLE_CONTEXTS` grant and uses the existing replacement
  contract. Organization member management is a separate write path.
- The organization's booked-application dialog offers the same explicit
  role list used by the backend.

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

`OrganizationApplicationDelegationRoutingTests` proves allowed assignment and
removal, retry without duplicate writes, and unchanged rows after rejecting
a non-owner, a nonmember target, or a request to assign Owner.
