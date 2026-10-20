files:
- @Composable Storage component: solawi-bid-frontend/src/jsMain/kotlin/org/solyton/solawi/bid/application/storage/storage.kt
- Application State: solawi-bid-frontend/src/jsMain/kotlin/org/solyton/solawi/bid/application/data/application.kt
folders:
- events: solawi-bid-frontend/src/jsMain/kotlin/org/solyton/solawi/bid/application/storage/event
- middleware: solawi-bid-frontend/src/jsMain/kotlin/org/solyton/solawi/bid/application/storage/middleware

Access data via lenses: 
- @Lensify, @ReadOnly, @ReadWrite -> (teilweise autogeneriert mit ksp plugin)
- source fundamentals in evoleq/src/commonMain/kotlin/org/evoleq/optics