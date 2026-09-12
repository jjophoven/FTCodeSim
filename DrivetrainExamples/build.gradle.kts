plugins {
    id("dev.frozenmilk.teamcode") version "11.1.0-1.1.1"
}

repositories {
    mavenCentral()
    google()
    maven("https://www.jitpack.io")
    maven("https://repo.dairy.foundation/releases")
    maven("https://repo.dairy.foundation/snapshots")
}

ftc {
    sdk.TeamCode()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.codeblooded.ftcodesim:ftcodesim:local")
}
