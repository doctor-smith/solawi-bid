# IQL Registry DSL

The IQL Registry DSL describes the structure of your domain model for the IQL compiler.

It registers:

* entities
* database tables
* fields
* relations
* many-to-many mapping tables
* additional joins for many-to-many relations
* custom field-type mappings
* field-name strategies
* other module registries

The resulting `Registry` is used by the IQL compiler to resolve fields, relation paths, database columns, and relation joins.

---

# 11. Additional Joins in Many-to-Many Relations

A many-to-many relation can require additional tables to be joined when the mapping table contains a reference to another entity.

This is useful when the mapping itself carries additional context.

For example:

```text
Users
   |
   v
UserRoleContext
   |
   ├── roleId
   └── contextId
          |
          v
      Contexts
```

The actual role relation is:

```text
Users.id
    =
UserRoleContext.userId

UserRoleContext.roleId
    =
Roles.id
```

But the mapping also references `Contexts`:

```text
UserRoleContext.contextId
    =
Contexts.id
```

The additional relation can be declared with `join()`:

```kotlin
entity("user", UsersTable) {

    manyToMany(
        "roleContexts",
        RolesTable,
        UserRoleContext
    ) {

        source(
            UsersTable.id references UserRoleContext.userId
        )

        target(
            RolesTable.id references UserRoleContext.roleId
        )

        join(
            "context",
            ContextsTable.id references UserRoleContext.contextId
        )
    }
}
```

The first argument of `join()` is the **IQL entity name**:

```kotlin
join(
    "context",
    ...
)
```

It is not the database table name.

The `target` describes the primary target of the many-to-many relation.

The `join` describes an additional entity that is connected through the mapping table.

Conceptually:

```text
             ┌──────────────┐
             │     Role     │
             └──────▲───────┘
                    │ roleId
                    │
┌──────┐      ┌─────┴────────────┐
│ User │─────►│ UserRoleContext  │
└──────┘ user │                  │
              │ contextId        │
              └──────┬───────────┘
                     │
                     ▼
              ┌──────────────┐
              │   Context    │
              └──────────────┘
```

The additional join is part of the metadata of the many-to-many relation.

It is used by the compiler both when constructing the SQL join and when resolving fields that belong to the additional entity.

For example:

```kotlin
where {
    any("roleContexts") {
        p("context.name") eq "production"
    }
}
```

Here:

```text
roleContexts
     ↓
   Role
     ↓
 context
     ↓
   name
```

The `context` part is **not necessarily a normal relation registered on `role`**.

It may instead be resolved through the `relationJoin` declared on `roleContexts`.

---

# 11.1 RelationJoin Field Resolution

Additional joins participate in relation-path resolution.

Consider:

```kotlin
manyToMany(
    "roleContexts",
    RolesTable,
    UserRoleContext
) {

    source(
        UsersTable.id references UserRoleContext.userId
    )

    target(
        RolesTable.id references UserRoleContext.roleId
    )

    join(
        "context",
        ContextsTable.id references UserRoleContext.contextId
    )
}
```

The following path:

```kotlin
p("context.name")
```

can therefore resolve to:

```text
role
  ↓
context
  ↓
name
```

where `context` is supplied by the many-to-many `relationJoin`.

The compiler then generates the corresponding mapping-table join:

```text
Roles
   |
   | role_id
   v
UserRoleContext
   |
   | context_id
   v
Contexts
```

This means that a relation join is not merely SQL metadata. It can also provide a path through which fields can be resolved.

---

# 11.2 RelationJoin Priority

When resolving a relation name after entering the target entity of a many-to-many relation, the compiler uses the following priority:

```text
1. Normal relation on the current entity
2. Additional relationJoin of the preceding many-to-many relation
```

A normal relation therefore always wins over a `relationJoin` with the same name.

For example:

```kotlin
entity("role", RolesTable) {

    manyToOne(
        "context",
        ContextsTable
    ) {
        RolesTable.contextId references ContextsTable.id
    }
}
```

and:

```kotlin
manyToMany(
    "roleContexts",
    RolesTable,
    UserRoleContext
) {

    ...

    join(
        "context",
        ContextsTable.id references UserRoleContext.contextId
    )
}
```

Both provide a possible `context` path.

For:

```kotlin
any("roleContexts") {
    p("context.name") eq "development"
}
```

the normal relation is selected:

```text
role
  |
  └── context
        |
        └── name
```

Only when the target entity has **no normal relation** called `context` does the compiler fall back to the `relationJoin`:

```text
role
  |
  └── relationJoin(context)
          |
          └── name
```

This priority is intentional.

It prevents an additional mapping-table join from unexpectedly shadowing an explicitly registered domain relation.

---

# 11.3 RelationJoin Does Not Become a Normal Relation

A `join()` declaration does **not** add a normal relation to the target entity.

For example:

```kotlin
join(
    "context",
    ContextsTable.id references UserRoleContext.contextId
)
```

does not mean that:

```kotlin
registry
    .getEntity("role")
    ?.relations
    ?.containsKey("context")
```

must become `true`.

Instead, the join belongs to the many-to-many relation:

```text
user
  |
  └── roleContexts
        |
        ├── role
        |
        └── context   <- relationJoin
```

This distinction is important because relation joins are scoped to the many-to-many relation through which they were declared.

---

# 11.4 RelationJoin and SQL Generation

For a many-to-many relation with an additional join, the compiler generates an `EXISTS` query containing:

```text
source
   ↕
mapping table
   ↕
target

mapping table
   ↕
additional joined entity
```

For example:

```text
Users
  |
  | Users.id = UserRoleContext.userId
  v
UserRoleContext
  |
  +---- role_id ------> Roles.id
  |
  +---- context_id ---> Contexts.id
```

The additional join is correlated through the mapping table.

Conceptually the generated query is:

```sql
EXISTS (
    SELECT ...
    FROM Roles
    INNER JOIN UserRoleContext
        ON UserRoleContext.role_id = Roles.id
    INNER JOIN Contexts
        ON UserRoleContext.context_id = Contexts.id
    WHERE
        Users.id = UserRoleContext.user_id
        AND ...
)
```

The additional join therefore remains tied to the specific many-to-many mapping row.

This is important for correctness.

For example, if a user has:

```text
role = admin
context = production
```

then:

```kotlin
p("context.name") eq "production"
```

must match that mapping row.

It must not accidentally match an unrelated context belonging to another mapping row.

---

# 11.5 Multiple Additional Joins

A many-to-many relation can define multiple additional joins:

```kotlin
manyToMany(
    "roleContexts",
    RolesTable,
    UserRoleContext
) {

    source(
        UsersTable.id references UserRoleContext.userId
    )

    target(
        RolesTable.id references UserRoleContext.roleId
    )

    join(
        "context",
        ContextsTable.id references UserRoleContext.contextId
    )

    join(
        "tenant",
        TenantsTable.id references UserRoleContext.tenantId
    )
}
```

This produces:

```text
User
  |
  v
UserRoleContext
  |
  +----> Role
  |
  +----> Context
  |
  +----> Tenant
```

Each additional join can participate in field resolution:

```kotlin
any("roleContexts") {
    p("context.name") eq "production"
}
```

or:

```kotlin
any("roleContexts") {
    p("tenant.name") eq "acme"
}
```

The compiler resolves each path against the current relation context.

---

# 12. Multiple Columns in Many-to-Many Joins

Both `source()` and `target()` can contain multiple column references.

For example:

```kotlin
manyToMany(
    "members",
    EmployeesTable,
    ProjectMembers
) {

    source(
        ProjectsTable.companyId references ProjectMembers.companyId,
        ProjectsTable.id references ProjectMembers.projectId
    )

    target(
        EmployeesTable.companyId references ProjectMembers.companyId,
        EmployeesTable.id references ProjectMembers.employeeId
    )
}
```

The references are matched by position:

```text
source[0] -> mappingSource[0]
source[1] -> mappingSource[1]

target[0] -> mappingTarget[0]
target[1] -> mappingTarget[1]
```

Additional joins work the same way:

```kotlin
join(
    "context",
    ContextsTable.tenantId references UserRoleContext.tenantId,
    ContextsTable.id references UserRoleContext.contextId
)
```

This represents a composite join:

```text
Contexts.tenant_id = UserRoleContext.tenant_id
AND
Contexts.id = UserRoleContext.context_id
```

The same column ordering is used by the compiler when constructing the join condition.

---

# 15. Relation Names vs Entity Names

The relation name and target entity name are separate concepts.

For example:

```kotlin
manyToOne(
    "defaultContext",
    ContextsTable
)
```

might target:

```text
context
```

The IQL path uses the **relation name**:

```kotlin
p("defaultContext.name") eq "TEST"
```

not:

```kotlin
p("context.name")
```

when the current entity has a relation called `defaultContext`.

The same distinction applies to many-to-many `join()` declarations.

For:

```kotlin
join(
    "context",
    ContextsTable.id references UserRoleContext.contextId
)
```

`context` is the IQL entity name used for resolving the additional join.

The database table might have a completely different name:

```text
JOIN_TEST_CONTEXTS
```

The compiler resolves:

```text
IQL entity name
       ↓
Registry entity
       ↓
Exposed table
```

Therefore registry code should consistently use the **IQL entity name** when referring to an entity.

---

# 17. Combining Relations

Relations can be mixed freely in a path.

For example:

```text
application
    -> modules
    -> defaultContext
    -> root
    -> rights
    -> roles
```

The registry does not require all relations in a path to have the same cardinality.

A path may therefore contain:

```text
1:n
n:1
n:n
1:n
n:1
```

provided every individual relation is registered correctly.

For example:

```kotlin
p(
    "modules.defaultContext.root.rights"
)
```

is resolved one relation at a time.

The important rule is that every step is resolved against the entity reached by the previous step.

For normal relations:

```text
entity
  ↓
normal relation
  ↓
target entity
```

For a many-to-many relation with additional joins:

```text
source entity
  ↓
many-to-many relation
  ↓
target entity
  ↓
normal relation
     OR
relationJoin
```

The normal relation has priority if both have the same name.

For example:

```text
user
  ↓ roleContexts
role
  ↓ context
context
```

If `role.context` is registered as a normal relation, that relation wins.

If it is not registered, the compiler can resolve `context` through the `roleContexts` relation's `relationJoin`.

---

# 20. Registry and Compiler

The registry is the metadata layer used by the compiler.

Conceptually:

```text
IQL
 │
 │ p("modules.defaultContext.name")
 ▼
Registry
 │
 ├── application
 │     └── modules -> module
 │
 ├── module
 │     └── defaultContext -> context
 │
 └── context
       └── name -> column
 │
 ▼
ExposedCompiler
 │
 ▼
SQL
```

For many-to-many relations with additional joins, the registry also provides the information required to construct the complete relation query:

```text
IQL
 │
 │ relation
 ▼
Registry
 │
 ├── source relation
 ├── mapping table
 ├── target relation
 └── additional relation joins
 │
 ▼
ExposedCompiler
 │
 ▼
EXISTS
 │
 ├── source ↔ mapping
 ├── mapping ↔ target
 └── mapping ↔ additional tables
```

The registry therefore does not execute queries itself.

It tells the compiler:

* which entities exist
* which table belongs to an entity
* which fields exist
* which Exposed column represents a field
* which field type a column has
* which relations exist
* which entity a relation targets
* which columns connect the entities
* which mapping table is required for many-to-many relations
* which additional tables have to be joined for a many-to-many relation
* which additional entities may be addressed through those joins during path resolution

---

# 22. Registry DSL Overview

The main building blocks are:

```text
registry
 │
 ├── fieldNameStrategy
 │
 ├── fieldTypes
 │     └── Exposed ColumnType -> IQL FieldType
 │
 ├── include
 │
 └── entity
       │
       ├── field
       │
       ├── manyToOne
       │
       ├── oneToMany
       │
       └── manyToMany
             ├── source
             ├── target
             └── join
```

The resulting registry provides the metadata required to resolve paths such as:

```text
name
defaultContext.name
modules.defaultContext.name
modules.defaultContext.root.name
projects.members.name
```

Many-to-many relations can additionally describe context or other related entities through `join()`:

```text
user
  └── roleContexts
        ├── role
        ├── context
        └── tenant
```

The key principles are:

> **Every relation path is resolved one step at a time, with each relation being resolved against the entity reached by the previous step.**

> **For a many-to-many relation, a normal relation on the current target entity has priority over an additional `relationJoin` with the same name.**

> **A `relationJoin` is scoped to the many-to-many relation that declares it; it does not become a normal relation of the target entity.**

> **The mapping table defines the primary source-to-target relationship, while additional `join()` declarations describe entities that are also connected through the mapping table.**
