
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
    testImplementation (platform(libs.junit.bom))
    testImplementation (libs.junit.jupiter)
    testImplementation (libs.testcontainers)
    testImplementation (libs.mockito)

    testRuntimeOnly (libs.junit.launcher)
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
