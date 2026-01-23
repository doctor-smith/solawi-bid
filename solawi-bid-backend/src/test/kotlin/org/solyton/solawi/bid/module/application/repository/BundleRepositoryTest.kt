package org.solyton.solawi.bid.module.application.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.data.domain.BundleDefinition
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.BundleDefinitionEntity
import org.solyton.solawi.bid.module.application.schema.BundleDefinitionsTable
import org.solyton.solawi.bid.module.application.schema.BundleEntity
import org.solyton.solawi.bid.module.application.schema.BundlesTable
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.OrganizationBundleEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationBundlesTable
import org.solyton.solawi.bid.module.application.schema.UserBundleEntity
import org.solyton.solawi.bid.module.application.schema.UserBundlesTable
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import kotlin.test.assertIs

val bundleTables = arrayOf(
    ContextsTable,
    ApplicationsTable, ModulesTable,
    BundlesTable, BundleDefinitionsTable,
    UserBundlesTable, OrganizationBundlesTable
)

data class SetupData(
    val application: ApplicationEntity,
    val module: ModuleEntity
)
private fun Transaction.setup(): SetupData {
    createRootContext("EMPTY")
    val application = createApplication(
        "Test App",
        "",
        UUID_ZERO
    )
    val module = createModule(
        "Test Module",
        "",
        application.id.value,
        UUID_ZERO
    )
    return SetupData(application, module)
}

class BundleRepositoryTest {
    @DbFunctional@Test
    fun createBundle() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()

        val bundleName = "name"
        val bundleDescription = "description"
        val bundleDefinition = BundleDefinition(
            hashMapOf(
                application.id.value to setOf(module.id.value)
            )
        )
        val bundle = createBundle(
            bundleName,
            bundleDescription,
            bundleDefinition,
            UUID_ZERO
        )

        val createdBundleDefinition = BundleDefinitionEntity.find {
            BundleDefinitionsTable.bundleId eq bundle.id
        }.toList()

        assertEquals(bundleName, bundle.name)
        assertEquals(bundleDescription, bundle.description)
        assertEquals(1, createdBundleDefinition.size)
        assertEquals(application.id.value, createdBundleDefinition.first().application.value)
        assertEquals(module.id.value, createdBundleDefinition.first().module.value)
    }

    @DbFunctional@Test
    fun createBundleFailsWithDuplicateName() = runSimpleH2Test(*bundleTables) {
        val (application, module) = setup()

        val bundleName = "name"
        val bundleDescription = "description"
        val bundleDefinition = BundleDefinition(
            hashMapOf(
                application.id.value to setOf(module.id.value)
            )
        )
        createBundle(
            bundleName,
            bundleDescription,
            bundleDefinition,
            UUID_ZERO
        )

        try{
            createBundle(
                bundleName,
                bundleDescription,
                bundleDefinition,
                UUID_ZERO
            )
        } catch (exception: Exception) {
            assertIs<ApplicationException.DuplicateBundleName>(exception)
        }
    }


    @DbFunctional@Test
    fun readBundleById() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(
                application.id.value to setOf(module.id.value)
            )
        )
        val bundleName = "Test Bundle"
        val bundleDescription = "description"
        val bundle = createBundle(
            bundleName,
            bundleDescription,
            bundleDefinition,
            UUID_ZERO
        )

        assertEquals(bundle, readBundle(bundle.id.value))
    }

    @DbFunctional@Test
    fun readBundleByNonExistingId() = runSimpleH2Test(*bundleTables){
        assertEquals(null, readBundle(UUID_ZERO))
    }

    @DbFunctional@Test
    fun readBundleByName() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(
                application.id.value to setOf(module.id.value)
            )
        )
        val bundleName = "Test Bundle"
        val bundleDescription = "description"
        val bundle = createBundle(
            bundleName,
            bundleDescription,
            bundleDefinition,
            UUID_ZERO
        )

        assertEquals(bundle, readBundleByName(bundle.name))
    }

    @DbFunctional@Test
    fun readBundleByNonExistingName() = runSimpleH2Test(*bundleTables){
        assertEquals(null, readBundleByName("NotThere"))
    }

    @DbFunctional@Test
    fun updateBundleChangeApplication() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()
        val application2 = createApplication(
            "Test App 2", "", UUID_ZERO
        )
        val bundle = createBundle(
            "Bundle",
            "description",
            BundleDefinition(hashMapOf(application.id.value to setOf(module.id.value))),
            UUID_ZERO
        )

        val newBundleDefinition = BundleDefinition(hashMapOf(
            application2.id.value to setOf(module.id.value)
        ))

        val updatedBundle = updateBundle(
            bundle.id.value,
            bundle.name,
            bundle.description,
            newBundleDefinition,
            UUID_ZERO
        )

        val updatedBundleDefinition = BundleDefinitionEntity.find {
            BundleDefinitionsTable.bundleId eq bundle.id
        }.toList()

        assertEquals(bundle.name, updatedBundle.name)
        assertEquals(bundle.description, updatedBundle.description)
        assertEquals(bundle.id.value, updatedBundle.id.value)
        assertEquals(1, updatedBundleDefinition.size)
        assertEquals(application2.id.value, updatedBundleDefinition.first().application.value)
        assertEquals(module.id.value, updatedBundleDefinition.first().module.value)
    }
    @DbFunctional@Test
    fun updateBundleChangeModule() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()
        val module2 = createModule(
            "Test App 2", "", application.id.value, UUID_ZERO
        )
        val bundle = createBundle(
            "Bundle",
            "description",
            BundleDefinition(hashMapOf(application.id.value to setOf(module.id.value))),
            UUID_ZERO
        )

        val newBundleDefinition = BundleDefinition(hashMapOf(
            application.id.value to setOf(module2.id.value)
        ))

        val updatedBundle = updateBundle(
            bundle.id.value,
            bundle.name,
            bundle.description,
            newBundleDefinition,
            UUID_ZERO
        )

        val updatedBundleDefinition = BundleDefinitionEntity.find {
            BundleDefinitionsTable.bundleId eq bundle.id
        }.toList()

        assertEquals(bundle.name, updatedBundle.name)
        assertEquals(bundle.description, updatedBundle.description)
        assertEquals(bundle.id.value, updatedBundle.id.value)
        assertEquals(1, updatedBundleDefinition.size)
        assertEquals(application.id.value, updatedBundleDefinition.first().application.value)
        assertEquals(module2.id.value, updatedBundleDefinition.first().module.value)
    }


    @DbFunctional@Test
    fun deleteBundle() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(
                application.id.value to setOf(module.id.value)
            )
        )
        val bundleName = "Test Bundle"
        val bundleDescription = "description"
        val bundle = createBundle(
            bundleName,
            bundleDescription,
            bundleDefinition,
            UUID_ZERO
        )

        deleteBundle(bundle.id.value)

        val hasDefinitions = !BundleDefinitionEntity.all().empty()
        val hasBundles = !BundleEntity.all().empty()

        assertFalse(hasBundles, "Has bundles")
        assertFalse(hasDefinitions, "Has Definitions")
    }

    @DbFunctional@Test
    fun deleteBundleWithUserSubscriptionFails() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(
                application.id.value to setOf(module.id.value)
            )
        )
        val bundleName = "Test Bundle"
        val bundleDescription = "description"
        val bundle = createBundle(
            bundleName,
            bundleDescription,
            bundleDefinition,
            UUID_ZERO
        )
        UserBundleEntity.new {
            user = UUID_ZERO
            createdBy = UUID_ZERO
            this.bundle = bundle.id
        }
        try {
            deleteBundle(bundle.id.value)
        } catch(exception: Exception) {
            assertIs<ApplicationException.CannotDeleteBundle>(exception)
        }

        val hasBundles = !BundleEntity.all().empty()

        assertTrue(hasBundles, "Has No bundles")
    }
    @DbFunctional@Test
    fun deleteBundleWithOrganizationSubscriptionFails() = runSimpleH2Test(*bundleTables){
        val (application, module) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(
                application.id.value to setOf(module.id.value)
            )
        )
        val bundleName = "Test Bundle"
        val bundleDescription = "description"
        val bundle = createBundle(
            bundleName,
            bundleDescription,
            bundleDefinition,
            UUID_ZERO
        )
        OrganizationBundleEntity.new {
            organization = UUID_ZERO
            createdBy = UUID_ZERO
            this.bundle = bundle.id
        }
        try {
            deleteBundle(bundle.id.value)
        } catch(exception: Exception) {
            assertIs<ApplicationException.CannotDeleteBundle>(exception)
        }

        val hasBundles = !BundleEntity.all().empty()

        assertTrue(hasBundles, "Has No bundles")
    }

    @DbFunctional@Test
    fun validateBundleDefinition() = runSimpleH2Test(*bundleTables) {
        val (app, mod) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(app.id.value to setOf(mod.id.value))
        )

        validateBundleDefinition(bundleDefinition)
    }

    @DbFunctional@Test
    fun validateBundleDefinitionNoApp() = runSimpleH2Test(*bundleTables) {
        val (_, mod) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(UUID_ZERO to setOf(mod.id.value))
        )
        try {
            validateBundleDefinition(bundleDefinition)
        } catch (exception: Exception) {
            assertIs<ApplicationException.NoSuchAppsOrModules>(exception)
        }
    }

    @DbFunctional@Test
    fun validateBundleDefinitionNoMod() = runSimpleH2Test(*bundleTables) {
        val (app, _) = setup()
        val bundleDefinition = BundleDefinition(
            hashMapOf(app.id.value to setOf(UUID_ZERO))
        )
        try {
            validateBundleDefinition(bundleDefinition)
        } catch (exception: Exception) {
            assertIs<ApplicationException.NoSuchAppsOrModules>(exception)
        }
    }
}




/*  All implicitly tested
@DbFunctional@Test
fun addBundleDefinition() {
    TODO("Not implemented")
}

@DbFunctional@Test
fun removeBundleDefinitionOfBundle() {
    TODO("Not implemented")
}

@DbFunctional@Test
fun testRemoveBundleDefinitionOfBundle() {
    TODO("Not implemented")
}

@DbFunctional@Test
fun updateBundleDefinition() {
    TODO("Not implemented")
}
*/
