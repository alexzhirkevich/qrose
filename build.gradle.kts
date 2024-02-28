@file: Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.dokka).apply(false)
}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

val _jvmTarget = findProperty("jvmTarget") as String

val kmpPlugin = libs.plugins.kotlin.multiplatform
val androidLibPlugin = libs.plugins.android.library
val composePlugin = libs.plugins.compose

val ver = libs.versions.qrose.get()

subprojects {
    if (!name.startsWith("qrose"))
        return@subprojects

    this.group = "io.github.alexzhirkevich"
    this.version = ver

    plugins.apply(kmpPlugin.get().pluginId)
    plugins.apply(androidLibPlugin.get().pluginId)
    plugins.apply(composePlugin.get().pluginId)

    kotlin {
        applyDefaultHierarchyTemplate()

        androidTarget {
            publishLibraryVariants("release")
            compilations.all {
                kotlinOptions {
                    jvmTarget = _jvmTarget
                }
            }
        }
        iosArm64()
        iosX64()
        iosSimulatorArm64()
        js(IR) {
            browser()
        }
        wasmJs(){
            browser()
        }
        jvm("desktop") {
            compilations.all {
                kotlinOptions {
                    jvmTarget = _jvmTarget
                }
            }
        }

        macosArm64()
        macosX64()

        sourceSets {

            val desktopMain by getting

            val darwinMain by creating {
                dependsOn(commonMain.get())
                macosMain.get().dependsOn(this)
                iosMain.get().dependsOn(this)
            }

            val wasmJsMain by getting

            val skikoMain by creating {
                dependsOn(commonMain.get())
                jsMain.get().dependsOn(this)
                wasmJsMain.dependsOn(this)
                desktopMain.dependsOn(this)
                darwinMain.dependsOn(this)
            }
        }
    }

    android {
        namespace = "io.github.alexzhirkevich.${name.replace('-','.')}"
        compileSdk = 34
        defaultConfig {
            minSdk = 21
        }

        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(_jvmTarget)
            targetCompatibility = JavaVersion.toVersion(_jvmTarget)
        }
    }
}
android {
    namespace = "io.github.alexzhirkevich.qrose"
    compileSdk = findProperty("android.compileSdk").toString().toInt()
}