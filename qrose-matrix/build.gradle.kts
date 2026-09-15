plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            api(project(":qrose-core"))
            api(project(":qrose-encoder-matrix"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
