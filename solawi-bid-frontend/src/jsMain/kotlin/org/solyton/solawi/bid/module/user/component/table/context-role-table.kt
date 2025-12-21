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
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.permissions.service.readableName
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.availablePermissions
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.permissions
import org.solyton.solawi.bid.module.user.data.managed.permissions as managedPermissions


@Markup
@Composable
@Suppress("FunctionName")
fun ListAvailablePermissions (application: Storage<Application>, texts: Source<Lang.Block>) {
    val columns = texts * subComp("columns")
    val listStyles = ListStyles()
        .modifyListItemWrapper { width(100.percent) }
        .modifyDataWrapper { }
    ListWrapper {
        TitleWrapper { Title { H2{Text("Available Permissions")} } }
        HeaderWrapper { Header {
            HeaderCell((columns * subComp("context") * title).emit()){
                width(50.percent)
            }
            HeaderCell((columns * subComp("roles") * title).emit()){
                width(50.percent)
            }
        }}
        (application * availablePermissions * contexts.get).emit()
            //.filter{context -> context.contextName.contains(".") }
            .forEach{ context -> Items(listStyles, context) }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun Items(listStyles: ListStyles, context: Context, deepth: Int = 0) {
    ListItemWrapper(listStyles.listItemWrapper) {
        DataWrapper {
            val offset = (deepth * 2.5)
            Div({style { width(offset.percent); color(Color.transparent) }}){ "-" }
            TextCell( context.contextName.readableName() ){
                width((50 - offset).percent)
            }
            TextCell(context.roles.joinToString(", ") { it.roleName }) {
                width(50.percent)
            }
        }
    }
    context.children.forEach { context -> Items(listStyles, context, deepth + 1) }
}

@Markup
@Composable
@Suppress("FunctionName")
fun ListUserPermissions (application: Storage<Application>, texts: Source<Lang.Block>, allRoles: Boolean = false) {
    val columns = texts * subComp("columns")
    val listStyles = ListStyles()
        .modifyListItemWrapper { width(100.percent) }
        // .modifyDataWrapper { }
    ListWrapper {
        TitleWrapper { Title { H2{Text((texts * title).emit())} } }
        HeaderWrapper { Header {
            HeaderCell((columns * subComp("context") * title).emit()){
                width(50.percent)
            }
            HeaderCell((columns * subComp("roles") * title).emit()){
                width(25.percent)
            }
            HeaderCell((columns * subComp("rights") * title).emit()){
                width(25.percent)
            }
        }}
        /**/
        val userContexts = (application * user * permissions * contexts.get).emit()
        (application * availablePermissions * contexts.get).emit()
            .filter(userContexts)
            .forEach{ context -> UserItems(listStyles, context, userContexts, allRoles) }
        /**/
        //val userContexts = (application * user * permissions * contexts.get).emit()
        //userContexts.forEach{ context -> UserItems(listStyles, context, userContexts, allRoles) }
    }
}

fun List<Context>.filter(userContexts: List<Context>): List<Context>  {
    return filter{ context ->
        userContexts.any { it.contextId == context.contextId } ||
        context.children.filter(userContexts).isNotEmpty()
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun UserItems(listStyles: ListStyles, context: Context, userContexts: List<Context>, allRoles: Boolean = false, deepth: Int = 0) {
    ListItemWrapper(listStyles.listItemWrapper) {
        DataWrapper {
            val offset = (deepth * 2.5)
            Div({style { width(offset.percent); color(Color.transparent) }}){ "-" }
            TextCell( context.contextName ){ //.readableName()
                width((50 - offset).percent)
            }
            val userContext = userContexts.first{ c -> c.contextId == context.contextId }
            val userRoles = when(allRoles) {
                true -> context.roles
                false -> userContext.roles // context.roles.filter { it.roleId in userContext.roles.map { r -> r.roleId } }
            }
            TextCell(userRoles.joinToString(", ") { it.roleName }) {
                width(25.percent)
            }
            TextCell(userRoles.map { it.rights }.flatten()
                .map { it.rightName }.distinct()
                .joinToString(", ") { it }) {
                width(25.percent)
            }
        }
    }
    context.children.filter(userContexts).forEach {
        context -> UserItems(listStyles, context, userContexts,allRoles, deepth + 1)
    }
}

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
                        Text(it.readableName())
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
