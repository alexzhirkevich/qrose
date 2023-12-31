@file:Suppress("DSL_SCOPE_VIOLATION")

import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

group = "io.github.alexzhirkevich"
version = libs.versions.qrose.get()

val _jvmTarget = findProperty("jvmTarget") as String

kotlin {
    androidTarget{
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = _jvmTarget
            }
        }
    }
    ios()
    iosSimulatorArm64()
    js(IR){
        browser()
    }
    jvm("desktop"){
        compilations.all {
            kotlinOptions {
                jvmTarget = _jvmTarget
            }
        }
    }

    macosArm64()
    macosX64()


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
            }
        }

        val jsMain by getting
        val iosMain by getting
        val iosSimulatorArm64Main by getting
        val desktopMain by getting
        val macosX64Main by getting
        val macosArm64Main by getting

        val darwinMain by creating {
            dependsOn(commonMain)
            macosX64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
            iosMain.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            desktopMain.dependsOn(this)
            darwinMain.dependsOn(this)
        }
    }
}

android {
    namespace = "io.github.alexzhirkevich.qrose"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(_jvmTarget)
        targetCompatibility = JavaVersion.toVersion(_jvmTarget)
    }

}

//ext["signing.keyId"] = null
//ext["signing.password"] = null
//ext["signing.secretKeyRingFile"] = null
//ext["ossrhUsername"] = null
//ext["ossrhPassword"] = null

extra.apply {
    val publishPropFile = rootProject.file("local.properties")
    if (publishPropFile.exists()) {
        Properties().apply {
            load(publishPropFile.inputStream())
        }.forEach { name, value ->
            if (name == "signing.secretKeyRingFile") {
                set(name.toString(), rootProject.file(value.toString()).absolutePath)
            } else {
                set(name.toString(), value)
            }
        }
    } else {
        ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
        ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
        ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
        ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
        ext["ossrhPassword"]= System.getenv("OSSRH_PASSWORD")
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}
// https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

publishing {
    if (rootProject.file("local.properties").exists()) {

        repositories {
            maven {
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
                    username = project.ext.get("ossrhUsername").toString()
                    password = project.ext.get("ossrhPassword").toString()
                }
            }
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            name.set("QRose")
            description.set("Styled QR code generation library for Compose Multiplatform")
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

signing {
    sign(publishing.publications)
}