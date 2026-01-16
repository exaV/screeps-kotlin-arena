plugins {
    kotlin("multiplatform") version "2.2.21"
    kotlin("plugin.js-plain-objects") version "2.2.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
}

kotlin {

    sourceSets {
        jsMain {
            dependencies {
            }

        }
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    js {
        compilerOptions {
            target = "es2015"
        }
        nodejs {
            binaries.executable()
        }


    }
}

tasks.register("build-screeps-arena") {
    group = "screeps"
    dependsOn("build")

    val jsOutputDirectory = layout.buildDirectory.dir("/Users/patrick/ScreepsArena/screeps-arena/build/compileSync/js/main/productionExecutable/kotlin")
    doLast {
        val outputDir = jsOutputDirectory.get().asFile
        val sourceFile = outputDir.resolve("${project.name}.mjs")
        val targetFile = outputDir.resolve("main.mjs")
        if (!sourceFile.exists()) {
            throw GradleException("Expected JS output not found: ${sourceFile.absolutePath}")
        }
        sourceFile.copyTo(targetFile, overwrite = true)
    }
}
