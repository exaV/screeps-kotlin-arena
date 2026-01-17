import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec

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


tasks.register("setup-screeps-arenas") {
    group = "screeps"
    dependsOn("build")

    val execOps = project.serviceOf<ExecOperations>()
    val nodespec = project.the<NodeJsEnvSpec>() // or rootProject.the<NodeJsEnvSpec>() if that's where it lives

    doLast {
        val arenasDir = project.parent!!.file("arenas")
        logger.lifecycle("setup-screeps-arenas: scanning {}", arenasDir.absolutePath)

        val arenaFolders = arenasDir.listFiles()?.filter { it.isDirectory }.orEmpty()
        if (arenaFolders.isEmpty()) {
            logger.warn("No arena subfolders found under {}", arenasDir.absolutePath)
            return@doLast
        }

        val nodeDir = File(nodespec.executable.get()).parentFile
        logger.lifecycle("using node at {}", nodeDir)
        var npm = File(nodeDir.parentFile, "lib/node_modules/npm/bin/npm-cli.js")
        if (!npm.exists()){
            logger.debug("{} does not exist", npm.absolutePath)
            npm = File(nodeDir, "node_modules/npm/bin/npm-cli.js")
        }
        if (!npm.exists()){
            logger.debug("{} does not exist", npm.absolutePath)
            throw GradleException("Could not locate npm-cli.js near bundled node at: ${nodeDir.absolutePath}")
        }

        arenaFolders.forEach { arenaFolder ->
            logger.lifecycle("Setting up arena: {}", arenaFolder.name)

            execOps.exec {
                workingDir = arenaFolder
                executable = nodespec.executable.get()
                args(npm.absolutePath, "install",  "--no-audit")

            }
        }
    }
}
