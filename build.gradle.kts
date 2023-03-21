
import com.github.rodm.teamcity.DockerTeamCityEnvironment

plugins {
    id ("io.github.rodm.teamcity-server") version "1.5"
    id ("io.github.rodm.teamcity-environments") version "1.5"
    id ("org.sonarqube") version "4.0.0.2929"
}

group = "com.github.rodm"
version = "1.0.1"

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"
extra["downloadsDir"] = project.findProperty("downloads.dir") as String? ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") as String? ?: "$rootDir/servers"
extra["java8Home"] = project.findProperty("java8.home") ?: "/opt/jdk1.8.0_152"

dependencies {
    agent (project(path = ":agent", configuration = "plugin"))
}

teamcity {
    version = extra["teamcityVersion"] as String

    server {
        archiveName = "linux-properties-${rootProject.version}.zip"

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
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String
        baseDataDir = "${rootDir}/data"

        register("teamcity2018.1") {
            version = "2018.1.4"
            javaHome = extra["java8Home"] as String
        }

        register("teamcity2022.10", DockerTeamCityEnvironment::class.java) {
            version = "2022.10.2"
            port = "7111"
        }
    }
}
