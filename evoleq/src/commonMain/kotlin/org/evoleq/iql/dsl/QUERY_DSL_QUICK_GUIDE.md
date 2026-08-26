# IQL Query DSL

The IQL Query DSL is a Kotlin DSL for building query objects that can be compiled by an IQL query compiler.

It supports:

* field filters
* nested relation-path filters
* relation quantifiers
* expression filters
* sorting
* expression-based sorting
* pagination

A query can be serialized independently of the database implementation and passed to an IQL query compiler.

---

## 1. Basic Query

The entry point is `query`:

```kotlin
val query =
    query("user") {
        // query configuration
    }
```

The entity passed to `query` is the **root entity** of the query.

It provides the context used for resolving relative field and relation paths.

An entity can alternatively be selected inside the builder:

```kotlin
val query =
    query {
        select("user")
    }
```

---

# 2. Filtering

Filters are defined with `where`:

```kotlin
val query =
    query("user") {
        where {
            p("username") eq "alice"
        }
    }
```

`p()` creates a field path.

`field()` is an equivalent spelling:

```kotlin
p("username") eq "alice"
```

```kotlin
field("username") eq "alice"
```

---

# 3. Field Paths

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
application.defaultContext.name
```

The path may contain multiple relation levels:

```kotlin
query("application") {
    where {
        p("modules.defaultContext.root.name") eq "GLOBAL"
    }
}
```

The compiler resolves every relation against the entity reached by the previous relation:

```text
application
    → modules
    → module
    → defaultContext
    → context
    → root
    → context
    → name
```

Relation paths can therefore mix different relation types, for example:

```text
1:N → N:1 → N:M → N:M → N:1
```

The path is resolved from the registry rather than from the database schema directly.

---

# 4. Explicit Entity Paths

An entity can be referenced explicitly when there is no current entity context:

```kotlin
p("User.name")
```

For example:

```kotlin
val filter =
    eq(
        "User.name",
        "Alice"
    )
```

When a current entity exists, the first path component is interpreted as a relation if such a relation exists.

For example, with:

```text
application
└── defaultContext → context
```

this:

```kotlin
p("defaultContext.name")
```

means a relation path.

The explicit entity form is primarily useful when no current entity is available.

---

# 5. Comparison Operators

The DSL supports:

| Operator | Example           |
| -------- | ----------------- |
| `eq`     | `p("age") eq 18`  |
| `neq`    | `p("age") neq 18` |
| `gt`     | `p("age") gt 18`  |
| `gte`    | `p("age") gte 18` |
| `lt`     | `p("age") lt 18`  |
| `lte`    | `p("age") lte 18` |

Example:

```kotlin
where {
    p("age") gt 18
    p("status") eq "ACTIVE"
    p("enabled") eq true
}
```

Multiple filters in the same `where` block are combined with `AND`.

```kotlin
query("user") {
    where {
        p("age") gte 18
        p("status") eq "ACTIVE"
    }
}
```

Conceptually:

```text
age >= 18 AND status = 'ACTIVE'
```

---

# 6. `IN`

Use `in` to compare a field against multiple values:

```kotlin
where {
    p("status") `in` listOf(
        "ACTIVE",
        "PENDING"
    )
}
```

This corresponds to an SQL `IN` condition.

`IN` also works with relation paths:

```kotlin
where {
    p("defaultContext.name") `in` listOf(
        "TEST",
        "PRODUCTION"
    )
}
```

---

# 7. NULL Checks

Use `isNull()`:

```kotlin
where {
    p("deletedAt").isNull()
}
```

Use `isNotNull()`:

```kotlin
where {
    p("deletedAt").isNotNull()
}
```

Relation paths can also be used:

```kotlin
where {
    p("defaultContext.name").isNotNull()
}
```

---

# 8. String Matching

The DSL provides helpers for SQL `LIKE` expressions.

## Contains

```kotlin
where {
    p("username") contains "alice"
}
```

Conceptually:

```text
%alice%
```

## Starts with

```kotlin
where {
    p("username") startsWith "ali"
}
```

Conceptually:

```text
ali%
```

## Ends with

```kotlin
where {
    p("username") endsWith "son"
}
```

Conceptually:

```text
%son
```

These helpers can also be used with relation paths:

```kotlin
where {
    p("defaultContext.name") startsWith "TEST"
}
```

---

# 9. Case-Insensitive Matching

Case-insensitive variants are available:

```kotlin
where {
    p("username") containsIgnoreCase "alice"
}
```

```kotlin
where {
    p("username") startsWithIgnoreCase "ali"
}
```

```kotlin
where {
    p("username") endsWithIgnoreCase "son"
}
```

The explicit form is also available:

```kotlin
where {
    p("username").contains(
        "alice",
        ignoreCase = true
    )
}
```

---

# 10. Custom `LIKE` Patterns

Use `like` when the complete pattern should be supplied:

```kotlin
where {
    p("username") like "ali%"
}
```

Case-insensitive:

```kotlin
where {
    p("username") likeIgnoreCase "ali%"
}
```

Or:

```kotlin
where {
    p("username").like(
        "ali%",
        ignoreCase = true
    )
}
```

The convenience helpers such as `contains`, `startsWith`, and `endsWith` escape `%`, `_`, and `\` automatically.

---

# 11. Logical Operators

## AND

Multiple filters in a `where` block are implicitly combined with `AND`:

```kotlin
where {
    p("active") eq true
    p("age") gte 18
}
```

Explicit `AND` is also available:

```kotlin
where {
    and {
        p("active") eq true
        p("age") gte 18
    }
}
```

## OR

Use `or` for alternatives:

```kotlin
where {
    or {
        p("status") eq "ACTIVE"
        p("status") eq "PENDING"
    }
}
```

## NOT

Use `not` to negate a filter:

```kotlin
where {
    not {
        p("active") eq false
    }
}
```

Logical operators can be combined with relation filters and nested relation paths.

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

It does not require an explicit `query("defaultContext")` or a separate nested query.

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

Relation paths may mix relation types:

```kotlin
query("application") {
    where {
        p("modules.defaultContext.roles.rights.name") eq "READ"
    }
}
```

Conceptually:

```text
application
    → modules          (1:N)
    → defaultContext   (N:1)
    → roles            (N:M)
    → rights           (N:M)
    → name
```

The compiler builds the required SQL correlation from the complete relation path.

---

# 13. Relation Quantifiers

Quantifiers are useful when a condition should apply to related entities.

The available quantifiers are:

* `any`
* `all`
* `none`

For example, given:

```text
user → orders
```

use:

```kotlin
where {
    any("orders") {
        p("amount") gt 100
    }
}
```

This means:

> Select users having at least one order whose amount is greater than 100.

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

Quantifiers are particularly useful for `1:N` and `N:M` relationships.

---

# 14. Nested Relation Paths Inside Quantifiers

Quantifiers can themselves contain relation paths.

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

This allows quantifiers and relation-path resolution to be combined.

---

# 15. Expressions

Expressions represent values that are derived from fields or relations.

A field expression can be created with:

```kotlin
expression("username")
```

A relation expression can be created with:

```kotlin
relation("orders")
```

A relation can optionally specify an explicit entity:

```kotlin
relation(
    relation = "orders",
    entity = "order"
)
```

Expressions can then be transformed or aggregated.

---

# 16. Mapping a Relation to a Field

Use `map()` to select a field from a relation:

```kotlin
relation("orders")
    .map("amount")
```

This represents the concept:

```text
user.orders.amount
```

The mapped expression can then be aggregated.

For example:

```kotlin
relation("orders")
    .map("amount")
    .max()
```

---

# 17. Aggregations

The available aggregation functions are:

* `min()`
* `max()`
* `sum()`
* `avg()`
* `count()`

Examples:

```kotlin
relation("orders")
    .map("amount")
    .min()
```

```kotlin
relation("orders")
    .map("amount")
    .max()
```

```kotlin
relation("orders")
    .map("amount")
    .sum()
```

```kotlin
relation("orders")
    .map("amount")
    .avg()
```

Count related elements:

```kotlin
relation("orders")
    .count()
```

---

# 18. Comparing Expressions

Expressions can be compared with values:

```kotlin
expression("age") eq 18
```

Aggregated relation expressions can also be compared:

```kotlin
relation("orders")
    .map("amount")
    .max() eq 100
```

For example:

```kotlin
where {
    relation("orders")
        .map("amount")
        .max() eq 500
}
```

Expression comparisons currently support `eq` overloads for values such as:

* `JsonElement`
* `String`
* `Int`
* `Long`
* `Boolean`

---

# 19. Sorting by Fields

Ascending:

```kotlin
query("user") {
    asc("username")
}
```

Descending:

```kotlin
query("user") {
    desc("username")
}
```

The generic form is:

```kotlin
query("user") {
    orderBy(
        "username",
        SortDirection.ASC
    )
}
```

or:

```kotlin
query("user") {
    orderBy(
        "username",
        SortDirection.DESC
    )
}
```

Relation paths can also be used as sort fields where supported:

```kotlin
query("application") {
    asc("defaultContext.name")
}
```

Multiple sort fields are preserved in declaration order:

```kotlin
query("user") {
    asc("last_name")
    asc("first_name")
}
```

---

# 20. Sorting by Expressions

Expressions can be used as sort keys.

For example:

```kotlin
query("user") {
    asc(
        relation("userProfiles")
            .map("last_name")
            .max()
    )
}
```

Descending:

```kotlin
query("user") {
    desc(
        relation("userProfiles")
            .map("last_name")
            .max()
    )
}
```

The generic form is:

```kotlin
query("user") {
    orderBy(
        relation("userProfiles")
            .map("last_name")
            .max(),
        SortDirection.ASC
    )
}
```

---

# 21. Combining Relation Filters and Expression Sorting

Filtering and expression sorting can be combined:

```kotlin
val query =
    query("user") {

        where {
            p("active") eq true

            any("orders") {
                p("amount") gt 100
            }
        }

        desc(
            relation("orders")
                .map("amount")
                .max()
        )
    }
```

Conceptually:

```text
WHERE active = true
  AND EXISTS (
      order.amount > 100
  )
ORDER BY MAX(order.amount) DESC
```

The exact SQL representation depends on the compiler and relation type.

---

# 22. Pagination

Pagination is configured with `page()`:

```kotlin
query("user") {
    page(
        size = 20,
        offset = 0
    )
}
```

The default page size is `50`.

The default offset is `0`.

For example:

```kotlin
query("user") {
    page(
        size = 20,
        offset = 40
    )
}
```

means:

```text
page size = 20
offset = 40
```

Invalid pagination values are rejected:

```kotlin
page(size = 0)
```

and:

```kotlin
page(
    size = 20,
    offset = -1
)
```

---

# 23. Complete Example

A query can combine ordinary fields, relation paths, quantifiers, sorting, expressions, and pagination:

```kotlin
val query =
    query("application") {

        where {

            p("name") startsWithIgnoreCase "solawi"

            p("defaultContext.name") eq "TEST"

            any("modules") {
                p("defaultContext.name") eq "PRODUCTION"
            }

            any("defaultContext.roles") {
                p("rights.name") eq "READ"
            }

            none("modules") {
                p("name") eq "DEPRECATED"
            }
        }

        asc("name")

        page(
            size = 20,
            offset = 0
        )
    }
```

This expresses:

1. Select applications.
2. The application name starts with `solawi`, ignoring case.
3. The application's default context is `TEST`.
4. At least one module has `PRODUCTION` as its default context.
5. At least one role of the default context has the `READ` right.
6. No module is named `DEPRECATED`.
7. Sort by application name.
8. Return the first 20 results.

---

# 24. Query Construction Without an Explicit Entity

The root entity can be supplied later:

```kotlin
val query =
    query {
        select("user")

        where {
            p("active") eq true
        }

        asc("username")
    }
```

The selected entity becomes the context for resolving relative fields and relation paths.

When no entity context exists, explicit entity-qualified fields can be used:

```kotlin
p("User.name")
```

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

They can be combined:

```kotlin
any("modules") {
    p("defaultContext.name") eq "TEST"
}
```

---

# 26. Recommended Style

For simple fields, use `p()`:

```kotlin
query("user") {
    where {
        p("username") eq "alice"
    }
}
```

For a field reached through one or more relations, use a relation path:

```kotlin
query("application") {
    where {
        p("defaultContext.root.name") eq "GLOBAL"
    }
}
```

For existence conditions over collections, use quantifiers:

```kotlin
query("application") {
    where {
        any("modules") {
            p("name") eq "API"
        }
    }
}
```

For nested relation conditions, combine both:

```kotlin
query("application") {
    where {
        any("modules") {
            p("defaultContext.roles.rights.name") eq "READ"
        }
    }
}
```

For derived values, use expressions:

```kotlin
relation("orders")
    .map("amount")
    .max()
```

For simple sorting, prefer:

```kotlin
asc("last_name")
desc("created_at")
```

For complex sorting, use an expression:

```kotlin
desc(
    relation("orders")
        .map("amount")
        .max()
)
```

---

# 27. DSL Overview

The main query structure is:

```text
query
 ├── select
 ├── where
 │    ├── p / field
 │    │    ├── relation paths
 │    │    ├── eq
 │    │    ├── neq
 │    │    ├── gt / gte
 │    │    ├── lt / lte
 │    │    ├── in
 │    │    ├── isNull / isNotNull
 │    │    ├── contains
 │    │    ├── startsWith
 │    │    ├── endsWith
 │    │    └── like
 │    │
 │    ├── and
 │    ├── or
 │    ├── not
 │    ├── any
 │    ├── all
 │    └── none
 │
 ├── asc / desc
 │    ├── field
 │    └── expression
 │
 ├── orderBy
 │    ├── field
 │    └── expression
 │
 └── page
```

Expressions provide a second part of the DSL:

```text
expression("field")

relation("relation")
    └── map("field")
          ├── min()
          ├── max()
          ├── sum()
          ├── avg()
          └── count()
```

Relation paths provide direct traversal:

```text
application
    └── modules
          └── defaultContext
                └── roles
                      └── rights
                            └── name
```

A relation path may contain arbitrary supported relation types:

```text
1:N → N:1 → N:M → N:M → N:1
```

The registry resolves each step and provides the compiler with the complete path from the root entity to the final field.

Together, these APIs allow IQL to represent simple field queries as well as deeply nested relation filtering, quantified relation conditions, aggregation, expression comparison, sorting, and pagination.
