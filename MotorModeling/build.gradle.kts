plugins {
    id("dev.frozenmilk.jvm-library") version "11.1.0-1.1.1"
    id("dev.frozenmilk.publish") version "0.0.5"
    id("dev.frozenmilk.doc") version "0.0.5"
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "org.codeblooded.ftcodesim"
            artifactId = "MotorModeling"

            artifact(dairyDoc.dokkaJavadocJar)
            artifact(dairyDoc.dokkaHtmlJar)

            afterEvaluate {
                from(components["java"])
            }
        }
    }
}
