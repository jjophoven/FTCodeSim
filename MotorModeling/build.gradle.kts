plugins {
    id("dev.frozenmilk.jvm-library") version "12.0.0-1.2.1"
    id("dev.frozenmilk.publish") version "0.1.0"
    id("dev.frozenmilk.doc") version "0.1.0"
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
