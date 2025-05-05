package org.solyton.solawi.bid.module.user.action

import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.application.ui.page.user.action.createUser
import org.solyton.solawi.bid.application.ui.page.user.action.getUsers
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test

class UserActionTests {
    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun getUsersTest() = runTest {
        val name = "name"
        installSerializers()

        val action = getUsers()

        composition {
            val storage = TestStorage()
            /*
                        (storage * action.writer).write(apiAuctions) on Unit

                        assertEquals(1, (storage * auctions).read().size)

                        assertEquals(auction.auctionId, (storage * action.reader).emit().auctionIds.first())

                        (storage * action.writer).write(ApiAuctions()) on Unit

                        assertEquals(0, (storage * auctions).read().size)

             */
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun createUserTest() = runTest {
        val name = "name"
        installSerializers()
        val user = CreateUser("username", "password")
        val action = createUser(user)

        composition {
            val storage = TestStorage()
            /*
                        (storage * action.writer).write(apiAuctions) on Unit

                        assertEquals(1, (storage * auctions).read().size)

                        assertEquals(auction.auctionId, (storage * action.reader).emit().auctionIds.first())

                        (storage * action.writer).write(ApiAuctions()) on Unit

                        assertEquals(0, (storage * auctions).read().size)

             */
        }
    }
}