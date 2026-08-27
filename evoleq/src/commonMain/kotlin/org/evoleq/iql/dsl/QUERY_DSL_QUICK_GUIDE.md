# Ergänzung: Relation Paths and Many-to-Many Relation Joins

## 3. Field Paths

A field can be referenced relative to the current entity:

```kotlin
p("username")
```

For a query rooted at `user`, this refers to:

```text
user.username
```

Relations can be traversed directly in a field path.

For example, if `application` has a `defaultContext` relation:

```kotlin
query("application") {
    where {
        p("defaultContext.name") eq "TEST"
    }
}
```

This means:

```text
application
    → defaultContext
    → name
```

The path may contain multiple relation levels:

```kotlin
query("application") {
    where {
        p("modules.defaultContext.root.name") eq "GLOBAL"
    }
}
```

The compiler resolves every relation **one step at a time**.

For each step:

1. The current entity is inspected.
2. A normal registered relation with that name is preferred.
3. If no normal relation exists, a matching `relationJoin` of the previously resolved many-to-many relation may be used.
4. The resolved target entity becomes the current entity for the next path component.

Conceptually:

```text
application
    ↓ modules
module
    ↓ defaultContext
context
    ↓ root
context
    ↓ name
```

This is important because relation names are always interpreted relative to the entity currently being resolved.

---

# 12. Relation-Path Filters

A relation can be traversed directly from a field path.

For example:

```kotlin
query("application") {
    where {
        p("defaultContext.name") eq "TEST"
    }
}
```

This is a **relation-path filter**.

It does not require an explicit nested query.

Multiple relation levels are supported:

```kotlin
query("application") {
    where {
        p("modules.defaultContext.name") eq "TEST"
    }
}
```

The compiler resolves this as:

```text
application
    → modules
    → module
    → defaultContext
    → context
    → name
```

Relation paths can mix relation types:

```text
1:N → N:1 → N:M → N:M → N:1
```

provided every step can be resolved from the current entity.

---

# 13. Relation Quantifiers

Quantifiers are useful when a condition should apply to related entities.

The available quantifiers are:

* `any`
* `all`
* `none`

For example:

```kotlin
query("user") {
    where {
        any("orders") {
            p("amount") gt 100
        }
    }
}
```

This means:

> Select users having at least one related order whose amount is greater than 100.

Quantifiers are especially important for collection relations such as `1:N` and `N:M`.

---

## `none`

```kotlin
where {
    none("orders") {
        p("amount") gt 100
    }
}
```

This means:

> Select users for which no related order has an amount greater than 100.

---

## `all`

```kotlin
where {
    all("orders") {
        p("amount") gt 100
    }
}
```

This means:

> Select users for which all related orders satisfy the condition.

---

# 14. Nested Relation Paths Inside Quantifiers

A quantifier can contain a complete relation path.

For example:

```kotlin
query("application") {
    where {
        any("modules") {
            p("defaultContext.name") eq "TEST"
        }
    }
}
```

This means:

```text
application
    → modules
        → defaultContext
            → name = 'TEST'
```

Multiple levels are possible:

```kotlin
query("application") {
    where {
        any("modules") {
            p("defaultContext.roles.rights.name") eq "READ"
        }
    }
}
```

The path inside the quantifier is resolved relative to the entity represented by the quantifier relation.

So inside:

```kotlin
any("modules") {
    ...
}
```

the current entity is `module`.

Therefore:

```kotlin
p("defaultContext.name")
```

is resolved against `module`, not against `application`.

---

# 14a. Many-to-Many Relation Joins Inside Quantifiers

A many-to-many relation can define additional joined entities.

For example:

```text
user
   │
   │ rolesWithContext
   ▼
user_role_context
   │
   ├── role_id ─────→ role
   │
   └── context_id ──→ context
```

The registry may define:

```kotlin
entity("user", UsersTable) {

    manyToMany(
        "rolesWithContext",
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

The primary target of the relation is still:

```text
role
```

The `context` entity is an additional relation join.

It can nevertheless be addressed inside the relation filter:

```kotlin
query("user") {
    where {
        any("rolesWithContext") {
            p("context.name") eq "production"
        }
    }
}
```

The compiler generates the relation query conceptually as:

```text
user
 │
 └── EXISTS
      │
      ├── user → user_role_context
      ├── user_role_context → role
      └── user_role_context → context
                                  │
                                  └── name = 'production'
```

The important distinction is:

```text
rolesWithContext
    ↓
primary target = role

context
    ↓
additional joined entity
```

The additional join does **not** change the primary target entity of the many-to-many relation.

---

# 14b. Relation Join Resolution

A `relationJoin` is only a fallback during path resolution.

Suppose the many-to-many relation declares:

```kotlin
join(
    "context",
    ContextsTable.id references UserRoleContext.contextId
)
```

and the target entity `role` does **not** have a normal relation named `context`.

Then:

```kotlin
any("rolesWithContext") {
    p("context.name") eq "production"
}
```

can resolve:

```text
role
   ↓ relationJoin
context
   ↓
name
```

However, if `role` has a normal relation named `context`:

```kotlin
entity("role", RolesTable) {

    manyToOne("context", ContextsTable) {
        RolesTable.contextId references ContextsTable.id
    }
}
```

then the normal relation wins.

The resolution priority is therefore:

```text
1. Normal relation on current entity
2. Matching relationJoin of the previously resolved M:N relation
3. Unknown relation → error
```

This rule is intentional.

It prevents an additional join from unexpectedly overriding an explicitly registered domain relation.

For example:

```text
role
 ├── context        ← normal relation
 │
 └── relationJoin
      └── context   ← fallback only
```

In this situation:

```kotlin
p("context.name")
```

always resolves through the normal `role.context` relation.

---

# 14c. Relation Joins Are Not Normal Relations

A `join()` declaration does not register a normal relation on the target entity.

For example:

```kotlin
manyToMany(
    "rolesWithContext",
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

does **not** mean that:

```kotlin
role.relations["context"]
```

exists.

Instead, the join is metadata belonging to:

```text
user.rolesWithContext
```

and is available while resolving the path below that many-to-many relation.

Conceptually:

```text
user
 └── rolesWithContext
       ├── role
       └── context   ← additional join, not a normal role relation
```

This distinction is important when defining registry relations.

---

# 14d. Relation Join Correlation

An additional relation join is correlated through the many-to-many mapping table.

For example:

```kotlin
join(
    "context",
    ContextsTable.id references UserRoleContext.contextId
)
```

means conceptually:

```text
UserRoleContext.context_id = Contexts.id
```

Together with the normal many-to-many mapping:

```text
Users.id = UserRoleContext.user_id

UserRoleContext.role_id = Roles.id

UserRoleContext.context_id = Contexts.id
```

the generated `EXISTS` query can therefore test conditions on both the target and the additional entity.

For example:

```kotlin
any("rolesWithContext") {
    p("name") eq "admin"
    p("context.name") eq "production"
}
```

Conceptually:

```text
EXISTS (
    role
    JOIN user_role_context
    JOIN context

    WHERE
        user.id = user_role_context.user_id
        AND
        user_role_context.role_id = role.id
        AND
        user_role_context.context_id = context.id
        AND
        role.name = 'admin'
        AND
        context.name = 'production'
)
```

The exact SQL representation depends on the compiler implementation, but the semantic relationship is the same.

---

# 14e. Multiple Additional Relation Joins

A many-to-many relation can define more than one additional joined entity.

For example:

```text
user
 └── assignments
       ├── role
       ├── context
       └── application
```

The query can then address fields from the additional entities:

```kotlin
any("assignments") {
    p("context.name") eq "production"
    p("application.name") eq "billing"
}
```

Each additional join is resolved against the relation's mapping metadata.

The primary target remains unchanged:

```text
assignments → role
```

while:

```text
context
application
```

are additional joined entities.

---

# 25. Relation Paths vs. Quantifiers

The two mechanisms solve slightly different problems.

### Relation path

Use a relation path when you want to address a field reached through a relation:

```kotlin
p("defaultContext.name") eq "TEST"
```

or:

```kotlin
p("modules.defaultContext.name") eq "TEST"
```

### Quantifier

Use a quantifier when you want to express existence or universal conditions over related entities:

```kotlin
any("modules") {
    p("name") eq "API"
}
```

```kotlin
none("modules") {
    p("name") eq "DEPRECATED"
}
```

```kotlin
all("modules") {
    p("enabled") eq true
}
```

### Quantifier + relation path

Both can be combined:

```kotlin
any("modules") {
    p("defaultContext.name") eq "TEST"
}
```

### Quantifier + many-to-many relation join

A many-to-many relation can additionally expose joined entities:

```kotlin
any("rolesWithContext") {
    p("name") eq "admin"
    p("context.name") eq "production"
}
```

This allows a single relation condition to constrain both:

```text
primary target
    role.name

additional joined entity
    context.name
```

---

# 26. Relation Resolution Rules

When resolving a relation path, the compiler follows these rules:

### Rule 1 — Start at the current entity

For:

```kotlin
query("application") {
    where {
        p("modules.defaultContext.name") eq "TEST"
    }
}
```

resolution starts at:

```text
application
```

### Rule 2 — Prefer normal relations

If the current entity contains a relation with the requested name, that relation is used.

```text
currentEntity.relations[relationName]
```

has priority.

### Rule 3 — Fall back to relationJoin

If no normal relation exists, the compiler may look at the `relationJoins` of the previously resolved many-to-many relation.

This allows:

```kotlin
any("rolesWithContext") {
    p("context.name") eq "production"
}
```

even when `role` itself has no registered `context` relation.

### Rule 4 — Move to the resolved target entity

After resolving a relation:

```text
source → relation → target
```

the target becomes the current entity for the next path component.

### Rule 5 — Unknown relations fail

If neither a normal relation nor an applicable relation join exists, the path is rejected.

For example:

```kotlin
p("modules.doesNotExist.name")
```

results in an unknown-relation error.

---

# 27. Complete Relation Example

The following example demonstrates all three cases:

```text
user
 └── rolesWithContext
       │
       ├── role
       │    └── context       ← normal relation
       │
       └── context             ← relationJoin
```

The registry contains:

```kotlin
entity("user", UsersTable) {

    manyToMany(
        "rolesWithContext",
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

entity("role", RolesTable) {

    field(RolesTable.name)

    manyToOne("context", ContextsTable) {
        RolesTable.contextId references ContextsTable.id
    }
}
```

Now:

```kotlin
any("rolesWithContext") {
    p("name") eq "admin"
}
```

resolves `name` on the primary target:

```text
role.name
```

while:

```kotlin
any("rolesWithContext") {
    p("context.name") eq "development"
}
```

resolves `context` as the **normal relation of `role`**, because normal relations have priority.

If the normal `role.context` relation does not exist, the same path can instead resolve through the many-to-many `relationJoin`:

```text
role
  ↓
relationJoin
  ↓
context
  ↓
name
```

This priority rule makes relation resolution deterministic and prevents relation joins from shadowing normal domain relations.

---

# 28. Recommended Style for Relation Joins

Use a normal relation when the relationship is a genuine domain relationship of the entity:

```kotlin
entity("role", RolesTable) {

    manyToOne("context", ContextsTable) {
        RolesTable.contextId references ContextsTable.id
    }
}
```

Use `join()` when the entity is only reachable as additional context of a many-to-many mapping:

```kotlin
manyToMany(
    "rolesWithContext",
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

A useful mental model is:

```text
normal relation
    = part of the target entity's domain model

relationJoin
    = additional table needed by a specific M:N relation
```

---

# 29. DSL Overview

The relation-related part of the DSL can therefore be understood as:

```text
query
 │
 └── where
      │
      ├── p("field")
      │
      ├── p("relation.field")
      │
      ├── p("relation.relation.field")
      │
      ├── any("relation")
      │     │
      │     └── p("field")
      │
      ├── any("manyToManyRelation")
      │     │
      │     ├── p("targetField")
      │     │
      │     └── p("additionalJoin.field")
      │
      ├── all("relation")
      │
      └── none("relation")
```

For a many-to-many relation:

```text
source
   │
   ▼
mapping table
   ├──────────► target
   │
   ├──────────► additional join
   │
   └──────────► additional join
```

Path resolution follows:

```text
normal relation
       ↓
relationJoin fallback
       ↓
unknown relation
```

The key principle is:

> **Every relation path is resolved one step at a time. A normal relation on the current entity always has priority. A `relationJoin` is only used as a fallback for additional entities connected through the previously resolved many-to-many mapping.**
