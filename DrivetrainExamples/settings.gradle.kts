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

includeBuild("../ftcodesim") {
    dependencySubstitution {
        substitute(module("org.codeblooded.ftcodesim:ftcodesim")).using(project(":"))
    }
}
