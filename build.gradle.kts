
import com.github.rodm.teamcity.DockerTeamCityEnvironment

plugins {
    id ("io.github.rodm.teamcity-server") version "1.5.3"
    id ("io.github.rodm.teamcity-environments") version "1.5.3"
    id ("org.sonarqube") version "4.0.0.2929"
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
        }
    }

    environments {
        baseDataDir = "${rootDir}/data"

        register("teamcity2018.1", DockerTeamCityEnvironment::class.java) {
            version = "2018.1.5"
            port = "7111"
        }

        register("teamcity2024.03", DockerTeamCityEnvironment::class.java) {
            version = "2024.03"
            port = "7111"
        }
    }
}
