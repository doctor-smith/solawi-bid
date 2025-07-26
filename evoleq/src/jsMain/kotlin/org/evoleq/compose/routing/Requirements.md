# Requirements

- [ ] Conditional Routes
  - [ ] Authorization, hide pages, when user is not logged in
  - [ ] App is in a certain state
  - [ ] Callback: I.e. Navigate to Login, when user is not logged in

Ideas:
1. Wrap routes: Wrap all components of childroutes with a certain component:
   1. Navbar
   2. Other layout stuff
   3. Conditions 
   4. Callbacks
   5. ...


I want to write something like this:

```kotlin

route("/") {
    login("/") { LoginPage(/* args */) }
    
    wrap { // this: RoutesConfiguration 
         
        access { 
            // i.e. if logged in
            // or if user has required access rights
        }
        layout { c ->
            // Common Layout for all
            // child components
            NavBar(/* args */)
            c()
        }
        
        route("module1") {
            component{ ... }   
        }

        route("module2") {
            component{ ... }
        }
    }
}

/**
 * Lift route configurations of children and
 * pass layout to the child components
 * pass conditional to the children
 */
fun RoutesConfiguration.wrap(configureRoutes: RoutesConfiguration.()->Unit) {
    this.configureRoutes() // good to apply it immediately??
    // -> no
}

fun RoutesConfiguration.layout(childrenOnly: Boolean, )

```


