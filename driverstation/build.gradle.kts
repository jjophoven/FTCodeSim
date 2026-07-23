plugins {
    id("dev.frozenmilk.jvm-library") version "11.1.0-1.1.1"
    id("dev.frozenmilk.publish") version "0.0.5"
    id("dev.frozenmilk.doc") version "0.0.5"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

repositories {
    maven("https://www.jitpack.io")
}

dependencies {
    implementation("com.github.WilliamAHartman:Jamepad:1.3.2")
    implementation("com.github.kwhat:jnativehook:2.2.2")
}

//
//tasks.build {
//    dependsOn(tasks.shadowJar)
//}

tasks.shadowJar {
    archiveBaseName.set("DriverStationWindow1")
//    archiveVersion.set("")
//    archiveClassifier.set("")

    manifest {
        attributes["Main-Class"] = "org.codeblooded.ftcodesim.driverstation.DriverStationWindow"
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "org.codeblooded.ftcodesim"
            artifactId = "DriverStationWindow"

            artifact(dairyDoc.dokkaJavadocJar)
            artifact(dairyDoc.dokkaHtmlJar)

            afterEvaluate {
                from(components["java"])
            }
        }
    }
}

application {
    mainClass = "org.codeblooded.ftcodesim.driverstation.DriverStationWindow"
}

java {
    manifest {
        attributes["Main-Class"] = "org.codeblooded.ftcodesim.driverstation.DriverStationWindow"
    }
}
