plugins {
    id("org.jetbrains.compose")
    kotlin("multiplatform")
}


val copyJsResources = tasks.register<Copy>("copyJsResourcesWorkaround") {
    from(project(":example:shared").file("src/commonMain/composeResources"))
    into("build/processedResources/js/main")
}

val copyWasmJsResources = tasks.register<Copy>("copyWasmJsResourcesWorkaround") {
    from(project(":example:shared").file("src/commonMain/composeResources"))
    into("build/processedResources/wasmJs/main")
}

afterEvaluate {
    tasks.named("jsProcessResources") {
        finalizedBy(copyJsResources)
    }
    tasks.named("wasmJsProcessResources") {
        finalizedBy(copyWasmJsResources)
    }
}

kotlin {
    js(IR){
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(project(":example:shared"))
        }
    }
}

compose.experimental.web.application{}
