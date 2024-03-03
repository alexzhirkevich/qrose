
kotlin {
    sourceSets.commonMain.dependencies {
        implementation(compose.ui)
        api(project(":qrose-core"))
    }
}

