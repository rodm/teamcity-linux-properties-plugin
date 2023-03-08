
plugins {
    id ("org.gradle.java")
    id ("io.github.rodm.teamcity-agent")
}

base {
    archivesName.set("linux-properties-agent")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
