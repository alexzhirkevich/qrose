
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}


kotlin {
    jvm {

    }
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDirs("src/main/kotlin")
            dependencies {
                implementation(project(":example:shared"))

                implementation(compose.desktop.currentOs)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                api(compose.materialIconsExtended)
                implementation("com.google.zxing:core:3.5.3")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.alexzhirkevich.qrose.example.desktop.MainKt"
    }
}