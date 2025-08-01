package org.solyton.solawi.bid.module.user.component.table

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.permissions
import org.solyton.solawi.bid.module.user.data.managed.permissions as managedPermissions
import org.solyton.solawi.bid.module.user.data.user

@Markup
@Composable
@Suppress("FunctionName")
fun ContextRoleTableForUser(application: Storage<Application>, texts: Source<Lang.Block>) {
    val columns = texts * subComp("columns")
    Table{
        Thead {
            Td{
                Text((columns * subComp("context") * title).emit())
            }
            Td {
                Text((columns * subComp("roles") * title).emit())
            }
        }
        Tbody {
            (application * user * permissions * contexts.get).emit().forEach {
                Tr {
                    Td{
                        Text(it.contextName)
                    }
                    Td{
                        Text(it.roles.joinToString(", ") { it.roleName })
                    }
                }
            }
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun ContextRoleTableManagedUser(application: Storage<Application>, userId: String) = Table{
    val managedUser = managedUsers * FirstBy { it.id == userId }

    Thead {
        Td{
            Text("Context")
        }
        Td {
            Text("Roles")
        }
    }
    Tbody {
        (application * managedUser * managedPermissions * contexts.get).emit().forEach {
            Tr {
                Td{
                    Text(it.contextName)
                }
                Td{
                    Text(it.roles.joinToString(", ") { it.roleName })
                }
            }
        }
    }
}
