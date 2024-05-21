
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("io.github.rodm.teamcity-agent")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    testImplementation ("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation ("org.testcontainers:junit-jupiter:1.19.7")
    testImplementation ("org.mockito:mockito-core:4.11.0")

    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
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
            xml.required = true
        }
    }
}
