
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("io.github.rodm.teamcity-agent")
}

base {
    archivesName.set("linux-properties-agent")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation ("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.mockito:mockito-core:4.11.0")
}

teamcity {
    agent {
        descriptor {
            pluginDeployment {
                useSeparateClassloader = true
            }
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy (jacocoTestReport)
        environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }
}
