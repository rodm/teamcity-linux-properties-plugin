
import com.github.rodm.teamcity.DockerTeamCityEnvironment

plugins {
    alias (libs.plugins.teamcity.server)
    alias (libs.plugins.teamcity.environments)
    alias (libs.plugins.sonarqube)
}

group = "com.github.rodm"
version = "1.0-SNAPSHOT"

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

dependencies {
    agent (project(path = ":linux-properties-agent", configuration = "plugin"))
}

teamcity {
    version = extra["teamcityVersion"] as String

    server {
        descriptor {
            name = "linux-properties"
            displayName = "Linux Properties"
            version = rootProject.version as String
            description = "Provides additional properties to identify a Linux distribution."
            vendorName = "Rod MacKenzie"
            vendorUrl = "https://github.com/rodm"
            email = "rod.n.mackenzie@gmail.com"
            useSeparateClassloader = true
            minimumBuild = "58245"
        }
    }

    environments {
        val type = DockerTeamCityEnvironment::class.java
        register("teamcity2018.1", type) {
            version = "2018.1.5"
            port = "7111"
        }

        register("teamcity2025.03", type) {
            version = "2025.03"
            port = "7111"
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-linux-properties")
        property("sonar.projectName", "teamcity-linux-properties")
    }
}
