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
        substitute(module("org.codeblooded.ftcodesim:MotorModeling")).using(project(":"))
    }
}
includeBuild("../driverstation") {
    dependencySubstitution {
        substitute(module("org.codeblooded.ftcodesim:driverstation")).using(project(":"))
    }
}
includeBuild("../advantagescope") {
    dependencySubstitution {
        substitute(module("org.codeblooded.ftcodesim:advantagescope")).using(project(":"))
    }
}