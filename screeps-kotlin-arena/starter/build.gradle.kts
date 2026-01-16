plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        compilerOptions {
            target = "es2015"
        }
        nodejs {
            binaries.executable()
        }
    }

    sourceSets {
        jsMain {
            dependencies {
                implementation(project(":types"))
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.register("build-screeps-arena") {
    group = "screeps"
    dependsOn("build")
//    screeps-arena/screeps-kotlin-arena-starter/build/compileSync/js/main/productionExecutable/kotlin/screeps-kotlin-arena-starter.mjs

    val jsOutputDirectory = layout.buildDirectory.dir("compileSync/js/main/productionExecutable/kotlin")
    doLast {
        val outputDir = jsOutputDirectory.get().asFile
        val sourceFile = outputDir.resolve("screeps-kotlin-arena-${project.name}.mjs")
        val targetFile = outputDir.resolve("main.mjs")
        if (!sourceFile.exists()) {
            throw GradleException("Expected JS output not found: ${sourceFile.absolutePath}")
        }
        sourceFile.copyTo(targetFile, overwrite = true)
    }
}
