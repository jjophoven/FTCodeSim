import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

run {
    val runDriverStationWindowReference = gradle.includedBuild("DriverStationWindow").task(":run")

    tasks.register("runDriverStationWindow") {
        description = "Start the driver station window"
        dependsOn(runDriverStationWindowReference)
    }
}

run {
    val publishMotorModeling = gradle//
        .includedBuild("MotorModeling")//
        .task(":publishToMavenLocal")
    val publishDriverStationWindow = gradle//
        .includedBuild("DriverStationWindow")//
        .task(":publishToMavenLocal")
    val publishSimulator = gradle//
        .includedBuild("Simulator")//
        .task(":publishToMavenLocal")

    tasks.register("publishLocal") {
        description = "Publish all libraries in the monorepo to maven local"
        dependsOn(publishMotorModeling)
        dependsOn(publishDriverStationWindow)
        dependsOn(publishSimulator)
    }
}

run {
    val publishMotorModeling = gradle//
        .includedBuild("MotorModeling")//
        .task(":publishReleasePublicationToDairyRepository")
    val publishDriverStationWindow = gradle//
        .includedBuild("DriverStationWindow")//
        .task(":publishReleasePublicationToDairyRepository")
    val publishSimulator = gradle//
        .includedBuild("Simulator")//
        .task(":publishReleasePublicationToDairyRepository")

    tasks.register("publishDairy") {
        description = "Publish all libraries in the monorepo to repo.dairy.foundation"
        dependsOn(publishMotorModeling)
        dependsOn(publishDriverStationWindow)
        dependsOn(publishSimulator)
    }
}

subprojects {
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(8))
            }
        }
    }
}