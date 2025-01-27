
pluginManagement {
    plugins {
        id ("io.github.rodm.teamcity-server") version "1.5.5"
        id ("io.github.rodm.teamcity-environments") version "1.5.5"
        id ("org.sonarqube") version "4.0.0.2929"
    }
}

rootProject.name = "linux-properties"

include ("agent")

rootProject.children.forEach { project ->
    project.name = "${rootProject.name}-${project.name}"
}
