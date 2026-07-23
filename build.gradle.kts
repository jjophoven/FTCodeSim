import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

run {
    val runDriverStationWindowReference = gradle.includedBuild("driverstation").task(":run")

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
        .includedBuild("driverstation")//
        .task(":publishToMavenLocal")
    val publishSimulator = gradle//
        .includedBuild("ftcodesim")//
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
        .includedBuild("driverstation")//
        .task(":publishReleasePublicationToDairyRepository")
    val publishSimulator = gradle//
        .includedBuild("ftcodesim")//
        .task(":publishReleasePublicationToDairyRepository")

    tasks.register("publishDairy") {
        description = "Publish all libraries in the monorepo to repo.dairy.foundation"
        dependsOn(publishMotorModeling)
        dependsOn(publishDriverStationWindow)
        dependsOn(publishSimulator)
    }
}