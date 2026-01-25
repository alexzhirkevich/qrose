
import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.mavenPublish)

}

subprojects {

    group = findProperty("group") as String
    version = findProperty("version") as String


    if (!name.startsWith("qrose")) {
        return@subprojects
    }

    plugins.apply("org.jetbrains.kotlin.multiplatform")
    plugins.apply("com.vanniktech.maven.publish")
    plugins.apply("android-library")

    androidLibrarySetup()
    multiplatformSetup()
    publicationSetup()
}

kotlin {
    jvm()
}

fun Project.publicationSetup() {
    mavenPublishing {
        publishToMavenCentral()
        signAllPublications()

        pom {
            name.set("QRose")
            description.set("Compose Multiplatform QR code generator")
            url.set("https://github.com/alexzhirkevich/qrose")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("alexzhirkevich")
                    name.set("Alexander Zhirkevich")
                    email.set("sasha.zhirkevich@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/alexzhirkevich/qrose")
                connection.set("scm:git:git://github.com/alexzhirkevich/qrose.git")
                developerConnection.set("scm:git:git://github.com/alexzhirkevich/qrose.git")
            }
        }
    }
}


fun Project.multiplatformSetup() {
    project.kotlin {

        applyDefaultHierarchyTemplate {
            common {
                group("jvmNative") {
                    withAndroidTarget()
                    withJvm()
                    withIos()
                    withMacos()
                }
                group("java"){
                    withJvm()
                    withAndroidTarget()
                }
                group("web") {
                    withJs()
                    withWasmJs()
                }
                group("skiko") {
                    withJvm()
                    withIos()
                    withMacos()
                    withJs()
                    withWasmJs()
                }
            }
        }

        jvm("desktop") {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(findProperty("jvmTarget") as String))
            }
        }


        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(findProperty("jvmTarget") as String))
            }
            publishLibraryVariants("release")
        }

        iosArm64()
        iosX64()
        iosSimulatorArm64()
        macosX64()
        macosArm64()

        js(IR) {
            browser()
        }

        wasmJs() {
            browser()
        }
    }
}




fun Project.androidLibrarySetup() {
    extensions.configure<LibraryExtension> {
        namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
        compileSdk = (findProperty("android.compileSdk") as String).toInt()

        defaultConfig {
            minSdk = (findProperty("android.minSdk") as String).toInt()
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
}
