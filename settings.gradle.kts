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

includeBuild("Examples")
includeBuild("ftcodesim")
includeBuild("driverstation")
includeBuild("MotorModeling")

//rootProject.name = "FTCodeSim"
//
//include(
//    ":Simulator",
//    ":FakeHardware",
//    ":DriverStationWindow",
//    ":MotorModeling",
//    ":TeamCode",
//    ":FtcRobotController"
//)
includeBuild("advantagescope")