
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":qrose-encoder-core"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
