pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://repo.dairy.foundation/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("../MotorModeling") {
    dependencySubstitution {
        substitute(module("org.codeblooded:MotorModeling")).using(project(":"))
    }
}
includeBuild("../DriverStationWindow") {
    dependencySubstitution {
        substitute(module("org.codeblooded:DriverStationWindow")).using(project(":"))
    }
}
