plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)

}

val _jvmTarget = findProperty("jvmTarget") as String

kotlin {

    applyDefaultHierarchyTemplate()
    jvm("desktop"){}
    androidTarget(){
    }
    js(IR){
        browser()
    }

    wasmJs(){
        browser()
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":qrose"))
            implementation(project(":qrose-oned"))
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")

        }

        val mobileMain by creating {
            dependsOn(commonMain.get())
            androidMain.get().dependsOn(this)
            iosMain.get().dependsOn(this)

            dependencies {
                implementation("dev.icerock.moko:permissions-compose:0.16.0")
//                implementation(project(":qrose-scanner"))
            }
        }

        val desktopMain by getting
        val wasmJsMain by getting

        val notMobileMain by creating {
            dependsOn(commonMain.get())

            desktopMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
            jsMain.get().dependsOn(this)
        }
    }
}

android {
    namespace = "qrose.example.shared"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(_jvmTarget)
        targetCompatibility = JavaVersion.toVersion(_jvmTarget)
    }

    defaultConfig {
        minSdk = 24
    }
}