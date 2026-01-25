pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

rootProject.name = "QRose"
include(":qrose")
include(":qrose-core")
include(":qrose-oned")
//include(":qrose-scanner")

include(":example:desktopApp")
include(":example:webApp")
include(":example:androidapp")
include(":example:shared")
