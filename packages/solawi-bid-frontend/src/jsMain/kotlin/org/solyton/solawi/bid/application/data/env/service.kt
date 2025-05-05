package org.solyton.solawi.bid.application.data.env


val env:dynamic by lazy { val x = js("PROCESS_ENV")
     console.log(x)
    x
}


fun getEnv(): Environment {
    val e =Environment(
        type = env["ENVIRONMENT"].unsafeCast<String>(),
        frontendUrl = env["FRONTEND_URL"].unsafeCast<String>(),
        frontendPort = env["FRONTEND_PORT"].unsafeCast<String>().toInt(),
        backendUrl = env["BACKEND_URL"].unsafeCast<String>(),
        backendPort = env["BACKEND_PORT"].unsafeCast<String>().toInt(),
    )
    console.log(e)
    return e
}