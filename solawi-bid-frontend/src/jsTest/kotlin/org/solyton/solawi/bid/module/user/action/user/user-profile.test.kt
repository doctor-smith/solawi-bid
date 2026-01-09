package org.solyton.solawi.bid.module.user.action.user

import org.evoleq.math.emit
import org.evoleq.math.on
import org.evoleq.math.write
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.managedUsers
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.api.userprofile.*
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class UserProfileTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun readUserProfilesTest() = runTest {
        val users = listOf<ManagedUser>(
            ManagedUser("user-id-1", "", "", Permissions(), null)
        )
        val apiUserProfiles = ApiUserProfiles(listOf(
            ApiUserProfile("1", "user-id-1" , "", "", null, "123", listOf())
        ))

        val action = readUserProfiles(listOf("1","2","3"))

        val expected = listOf<ManagedUser>(
            ManagedUser("user-id-1", "", "", Permissions(),
                UserProfile(
                    "1", "", "", null, "123", listOf()
                ))
        )
        composition {
            val storage = TestStorage()

            val readUserIds = (storage * userIso * action.reader).emit()
            assertEquals(ReadUserProfiles(listOf("1","2","3")), readUserIds)

            (storage * managedUsers.set) write users on Unit

            (storage * userIso * action.writer) write apiUserProfiles on Unit
            val storedManagedUsers = (storage * managedUsers.get).emit()
            assertEquals(expected, storedManagedUsers )
        }
    }


    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun importUserProfilesTest() = runTest {
        val users = listOf<ManagedUser>(
            ManagedUser("user-id-1", "", "", Permissions(), null)
        )
        val apiUserProfiles = ApiUserProfiles(listOf(
            ApiUserProfile("1", "user-id-1" , "", "", null, "123", listOf())
        ))
        val userProfilesToImport = ImportUserProfiles(listOf(
            UserProfileToImport(
                "uname",
                "fname",
                "lname",
                "dr",
                "123",
                CreateAddress(
                    "rname",
                    "oname",
                    "line1",
                    "line2",
                    "city",
                    "state",
                    "123",
                    "code"
                ),
            )
        ))
        val action = importUserProfiles(userProfilesToImport)

        val expected = listOf<ManagedUser>(
            ManagedUser("user-id-1", "", "", Permissions(),
                UserProfile(
                    "1", "", "", null, "123", listOf()
                ))
        )
        composition {
            val storage = TestStorage()

            val readUserProfilesToImport: ImportUserProfiles = (storage * userIso * action.reader).emit()
            assertEquals( userProfilesToImport, readUserProfilesToImport)

            (storage * managedUsers.set) write users on Unit
            (storage * userIso * action.writer) write apiUserProfiles on Unit
            val storedManagedUsers = (storage * managedUsers.get).emit()
            assertEquals(expected, storedManagedUsers )
        }
    }
}
