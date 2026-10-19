plugins {
    id("dev.frozenmilk.android-library") version "12.0.0-1.2.1"
    id("dev.frozenmilk.publish") version "0.1.0"
    id("dev.frozenmilk.doc") version "0.1.0"
}

repositories {
    maven("https://www.jitpack.io")
}

android.namespace = "org.codeblooded.ftcodesim"

dairyPublishing {
    gitDir = file("..")
}

ftc {
    sdk {
        compileOnly(RobotCore)
        compileOnly(FtcCommon)
        compileOnly(Hardware)

        testImplementation(RobotCore)

        dairy {
            implementation(Sloth)
            implementation(slothboard)
        }
    }

    psiLynx{
        version = "0.2.0"
        api(ftc)
        api(core)
    }
}

dependencies {
    compileOnly("androidx.annotation:annotation-jvm:1.10.0")

    api("org.codeblooded.ftcodesim:driverstation:${dairyPublishing.version}")
    api("org.codeblooded.ftcodesim:advantagescope:${dairyPublishing.version}")
    api("org.codeblooded.ftcodesim:MotorModeling:${dairyPublishing.version}")

    implementation("com.pedropathing:revhub:3.0.1")

    testImplementation("junit:junit:4.13.2")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "org.codeblooded.ftcodesim"
            artifactId = "ftcodesim"

            artifact(dairyDoc.dokkaHtmlJar)
            artifact(dairyDoc.dokkaJavadocJar)

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
