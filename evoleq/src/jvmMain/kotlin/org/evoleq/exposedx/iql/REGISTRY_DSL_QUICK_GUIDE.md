# IQL Registry DSL

The IQL Registry DSL describes the structure of your domain model for the IQL compiler.

It registers:

* entities
* database tables
* fields
* relations
* many-to-many mapping tables
* custom field-type mappings
* field-name strategies
* other module registries

The resulting `Registry` is used by the IQL compiler to resolve fields, relation paths, database columns, and relation joins.

---

# 1. Basic Registry

The entry point is `registry`:

```kotlin
val registry =
    registry {

        entity("user", UsersTable) {
            field(UsersTable.name)
        }
    }
```

The first argument of `entity()` is the IQL entity name.

The second argument is the corresponding Exposed `Table`.

```kotlin
entity(
    "user",
    UsersTable
)
```

The entity name is what is used in IQL queries:

```kotlin
query("user") {
    where {
        p("name") eq "Alice"
    }
}
```

---

# 2. Fields

Fields are registered with `field()`:

```kotlin
entity("user", UsersTable) {

    field(UsersTable.name)
    field(UsersTable.email)
    field(UsersTable.active)
}
```

The registered field connects the IQL field name with the Exposed column.

For example:

```kotlin
field(UsersTable.name)
```

allows:

```kotlin
p("name") eq "Alice"
```

The registry also uses the column's type to determine the corresponding IQL `FieldType`.

---

# 3. Field Name Strategy

The registry can define how database column names are exposed to IQL.

For example:

```kotlin
registry {
    fieldNameStrategy = FieldNameStrategy.SNAKE_CASE

    entity("user", UsersTable) {
        field(UsersTable.firstName)
        field(UsersTable.createdAt)
    }
}
```

With a snake-case strategy, database/API naming can be normalized consistently.

This is particularly useful when Kotlin properties use camelCase while database columns use snake_case:

```text
firstName  -> first_name
createdAt  -> created_at
```

The strategy is applied by the registry when resolving fields and columns.

---

# 4. Including Another Registry

Larger applications can split their registry into modules.

A registry can include another registry:

```kotlin
val applicationModuleRegistry =
    registry {

        include(permissionModuleRegistry)

        entity("application", ApplicationsTable) {
            ...
        }
    }
```

All entities, fields, and relations from the included registry become available to the resulting registry.

This allows modules to maintain their own registry definitions.

For example:

```text
permissionModuleRegistry
 ├── context
 ├── role
 ├── right
 └── userRoleContext

applicationModuleRegistry
 ├── application
 ├── module
 ├── userApplicationContext
 └── lifecycleStage

combined registry
 ├── permission entities
 └── application entities
```

---

# 5. Custom Field Types

The registry can explicitly map Exposed column types to IQL `FieldType`s.

Use `mapsTo`:

```kotlin
fieldTypes(
    DateColumnType::class mapsTo FieldType.DATE,
    DateTimeWithTimeZoneColumnType::class mapsTo FieldType.DATETIME,
    EntityIDColumnType::class mapsTo FieldType.UUID
)
```

This is useful when the Exposed type is a wrapper around another value.

For example, an Exposed `EntityIDColumnType` can represent an IQL UUID:

```text
EntityIDColumnType
        ↓
    FieldType.UUID
```

The mapping is then used when compiling comparisons:

```kotlin
p("id") eq someUuid
```

The registry knows which IQL type compiler has to handle the value.

---

# 6. Many-to-One Relations

A many-to-one relation is defined with `manyToOne()`:

```kotlin
entity("module", ModulesTable) {

    manyToOne("application", ApplicationsTable) {
        ModulesTable.applicationId references ApplicationsTable.id
    }
}
```

Conceptually:

```text
module
   |
   └── application -> application
```

The relation name is used in IQL paths:

```kotlin
p("application.name") eq "My Application"
```

The `references` expression defines how the two tables are joined.

In this example:

```text
modules.application_id = applications.id
```

---

# 7. One-to-Many Relations

The inverse relation can be registered with `oneToMany()`:

```kotlin
entity("application", ApplicationsTable) {

    oneToMany("modules", ModulesTable) {
        ApplicationsTable.id references ModulesTable.applicationId
    }
}
```

This represents:

```text
application
   |
   └── modules -> module
```

The relation can then be used by quantifiers:

```kotlin
where {
    any("modules") {
        p("name") eq "Billing"
    }
}
```

The same relation can also participate in relation paths where supported by the compiler.

---

# 8. Self-Referencing Relations

Relations can target the same entity.

For example:

```kotlin
entity("context", ContextsTable) {

    field(ContextsTable.name)

    manyToOne("root", ContextsTable) {
        ContextsTable.rootId references ContextsTable.id
    }
}
```

This produces:

```text
context
   |
   └── root -> context
```

Self-references can therefore be traversed repeatedly:

```kotlin
p("root.root.name") eq "GLOBAL"
```

Each step is resolved against the target entity of the previous relation.

---

# 9. Relation Paths

Relation paths can contain multiple relations.

For example:

```kotlin
entity("employee", EmployeesTable) {

    manyToOne("department", DepartmentsTable) {
        EmployeesTable.departmentId references DepartmentsTable.id
    }
}

entity("department", DepartmentsTable) {

    manyToOne("company", CompaniesTable) {
        DepartmentsTable.companyId references CompaniesTable.id
    }
}
```

This allows:

```kotlin
p("department.company.name") eq "Acme"
```

The registry resolves this step by step:

```text
employee
   |
   └── department
          |
          └── company
                 |
                 └── name
```

Importantly, every relation is resolved against the entity reached by the previous relation.

So:

```text
employee.department
```

resolves to `department`, and then:

```text
department.company
```

is resolved against the `department` entity.

This allows arbitrarily deep relation paths as long as every relation exists in the registry.

---

# 10. Many-to-Many Relations

Many-to-many relations require a mapping table.

Use `manyToMany()`:

```kotlin
entity("project", ProjectsTable) {

    manyToMany(
        "members",
        EmployeesTable,
        ProjectMembers
    ) {

        source(
            ProjectsTable.id references ProjectMembers.projectId
        )

        target(
            EmployeesTable.id references ProjectMembers.employeeId
        )
    }
}
```

This represents:

```text
project
   |
   |  project_members
   |
   └── members -> employee
```

The mapping table connects the source and target entities:

```text
projects.id
     |
     v
project_members.project_id

project_members.employee_id
     |
     v
employees.id
```

The registry stores this information as a `MappingInfo`.

---

# 11. Many-to-Many on the Inverse Side

The inverse side can also define the same mapping:

```kotlin
entity("employee", EmployeesTable) {

    manyToMany(
        "projects",
        ProjectsTable,
        ProjectMembers
    ) {

        source(
            EmployeesTable.id references ProjectMembers.employeeId
        )

        target(
            ProjectsTable.id references ProjectMembers.projectId
        )
    }
}
```

This allows relation traversal in both directions.

---

# 12. Mapping Tables

A many-to-many mapping contains four important pieces of information:

```text
source entity column
        ↓
mapping source column

mapping target column
        ↓
target entity column
```

For:

```kotlin
source(
    ProjectsTable.id references ProjectMembers.projectId
)

target(
    EmployeesTable.id references ProjectMembers.employeeId
)
```

the mapping is effectively:

```text
projects.id
    =
project_members.project_id

project_members.employee_id
    =
employees.id
```

The compiler can use this information to construct an `EXISTS` query for relation filters.

---

# 13. Relation Names vs Entity Names

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

This distinction is important for resolving relation paths.

---

# 14. Entity Prefixes

An IQL field can explicitly specify an entity:

```kotlin
p("User.name")
```

The registry can resolve `User` directly as an entity.

An explicit entity prefix is particularly useful when there is no current entity context.

For example:

```kotlin
query {
    where {
        p("user.name") eq "Alice"
    }
}
```

The registry/compiler can resolve `user` as an entity.

When a current entity exists, however, the first path component can also be interpreted as a relation if it matches a relation on that entity.

For example, given:

```text
application.defaultContext
```

the path:

```kotlin
p("defaultContext.name")
```

is resolved as a relation path when `defaultContext` is a relation of `application`.

---

# 15. Combining Relations

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

---

# 16. Complete Example

A small registry might look like this:

```kotlin
val registry =
    registry {

        fieldNameStrategy =
            FieldNameStrategy.SNAKE_CASE

        entity("company", Companies) {

            field(Companies.name)

            oneToMany("departments", Departments) {
                Companies.id references Departments.companyId
            }
        }

        entity("department", Departments) {

            field(Departments.name)

            manyToOne("company", Companies) {
                Departments.companyId references Companies.id
            }

            oneToMany("employees", Employees) {
                Departments.id references Employees.departmentId
            }
        }

        entity("employee", Employees) {

            field(Employees.name)

            manyToOne("department", Departments) {
                Employees.departmentId references Departments.id
            }
        }
    }
```

This describes:

```text
company
   |
   └── departments -> department
                         |
                         ├── company -> company
                         |
                         └── employees -> employee
                                              |
                                              └── department -> department
```

---

# 17. Application-Style Example

A modular registry can look like this:

```kotlin
val applicationModuleRegistry: Registry by lazy {

    registry {

        fieldNameStrategy =
            FieldNameStrategy.SNAKE_CASE

        include(permissionModuleRegistry)

        fieldTypes(
            DateColumnType::class mapsTo FieldType.DATE,
            DateTimeWithTimeZoneColumnType::class mapsTo FieldType.DATETIME,
            EntityIDColumnType::class mapsTo FieldType.UUID
        )

        entity("application", ApplicationsTable) {

            field(ApplicationsTable.name)
            field(ApplicationsTable.description)
            field(ApplicationsTable.isMandatory)
            field(ApplicationsTable.defaultContextId)

            oneToMany("modules", ModulesTable) {
                ApplicationsTable.id references ModulesTable.applicationId
            }

            manyToOne("defaultContext", ContextsTable) {
                ApplicationsTable.defaultContextId references ContextsTable.id
            }
        }

        entity("module", ModulesTable) {

            field(ModulesTable.name)
            field(ModulesTable.description)
            field(ModulesTable.isMandatory)

            manyToOne("application", ApplicationsTable) {
                ModulesTable.applicationId references ApplicationsTable.id
            }

            manyToOne("defaultContext", ContextsTable) {
                ModulesTable.defaultContextId references ContextsTable.id
            }
        }
    }
}
```

This allows paths such as:

```kotlin
p("defaultContext.name") eq "TEST"
```

and:

```kotlin
p("modules.defaultContext.name") eq "TEST"
```

The second path is resolved as:

```text
application
    ↓ modules
module
    ↓ defaultContext
context
    ↓ name
```

---

# 18. Registry and Compiler

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

---

# 19. Recommended Registry Structure

For a larger application, keep registry definitions close to their module:

```text
permission
 └── permissionModuleRegistry

application
 └── applicationModuleRegistry
```

Then compose them:

```kotlin
registry {
    include(permissionModuleRegistry)
    include(applicationModuleRegistry)
}
```

Each module owns its entities and relations while the application-level registry provides the complete model.

---

# 20. Registry DSL Overview

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
             └── target
```

The resulting registry provides the metadata required to resolve paths such as:

```text
name
defaultContext.name
modules.defaultContext.name
modules.defaultContext.root.name
projects.members.name
```

The key principle is:

> **Every relation path is resolved one step at a time, with each relation being resolved against the entity reached by the previous step.**

This makes the registry the central description of the navigable domain graph used by the IQL compiler.
