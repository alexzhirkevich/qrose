@file: Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.compose.internal.utils.localPropertiesFile
import java.util.Properties
import java.util.Base64

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.dokka).apply(false)
    id("maven-publish")
    id("signing")
}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

val _jvmTarget = findProperty("jvmTarget") as String

val kmpPlugin = libs.plugins.kotlin.multiplatform
val androidLibPlugin = libs.plugins.android.library
val composePlugin = libs.plugins.compose

val ver = libs.versions.qrose.get()

if (System.getenv("OSSRH_PASSWORD") != null){
    ext["OSSRH_USERNAME"] = System.getenv("OSSRH_USERNAME")
    ext["OSSRH_PASSWORD"] = System.getenv("OSSRH_PASSWORD")
    ext["GPG_KEY"] = System.getenv("GPG_KEY")
    ext["GPG_KEY_PWD"] = System.getenv("GPG_KEY_PWD")
} else {
    val properties = Properties().apply {
        load(localPropertiesFile.inputStream())
    }
    properties.forEach {
        ext[it.key.toString()] = it.value.toString()
    }
}

subprojects {
    if (!name.startsWith("qrose"))
        return@subprojects

    this.group = "io.github.alexzhirkevich"
    this.version = ver

    plugins.apply("maven-publish")
    plugins.apply("signing")
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

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

    val signingTasks = tasks.withType<Sign>()

    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(signingTasks)
    }

    publishing {

        repositories.maven {
            val releasesRepoUrl =
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl =
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri(snapshotsRepoUrl)
            } else {
                uri(releasesRepoUrl)
            }
            credentials {
                if (rootProject.ext.has("OSSRH_PASSWORD")) {
                    password = rootProject.ext["OSSRH_PASSWORD"] as String
                }
                if (rootProject.ext.has("OSSRH_USERNAME")) {
                    username = rootProject.ext["OSSRH_USERNAME"] as String
                }
            }
        }

        val publishProperties = Properties().apply {
            load(file("publish.properties").inputStream())
        }

        publications.withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set(this@subprojects.name)
                description.set(publishProperties.getProperty("description"))
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

    if (rootProject.ext.has("GPG_KEY") && rootProject.ext.has("GPG_KEY_PWD") ) {
        signing {
            useInMemoryPgpKeys(
                Base64.getDecoder().decode(rootProject.ext["GPG_KEY"] as String).decodeToString(),
                rootProject.ext["GPG_KEY_PWD"] as String,
            )
            sign(publishing.publications)
        }
    }
}
android {
    namespace = "io.github.alexzhirkevich.qrose"
    compileSdk = findProperty("android.compileSdk").toString().toInt()
}