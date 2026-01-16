group = "org.example"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("multiplatform") version "2.3.0" apply false
    kotlin("plugin.js-plain-objects") version "2.3.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}
