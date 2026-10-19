plugins {
    id("dev.frozenmilk.jvm-library") version "12.0.0-1.2.3"
    id("dev.frozenmilk.publish") version "0.1.0"
    id("dev.frozenmilk.doc") version "0.1.0"
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "org.codeblooded.ftcodesim"
            artifactId = "advantagescope"

            artifact(dairyDoc.dokkaJavadocJar)
            artifact(dairyDoc.dokkaHtmlJar)

            afterEvaluate {
                from(components["java"])
            }
        }
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.3")
    implementation("org.psilynx.psikit:core:0.2.0")
}