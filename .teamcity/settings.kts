
import jetbrains.buildServer.configs.kotlin.v2018_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.version
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

version = "2019.1"

project {

    val vcsRoot = GitVcsRoot {
        id("LinuxProperties")
        name = "linux-properties"
        url = "https://github.com/rodm/teamcity-linux-properties-plugin.git"
        useMirrors = false
    }
    vcsRoot(vcsRoot)

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val buildTemplate = template {
        id("Build")
        name = "build"

        params {
            param("gradle.opts", "")
            param("gradle.tasks", "clean build")
        }

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_30"
                tasks = "%gradle.tasks%"
                buildFile = ""
                gradleParams = "%gradle.opts%"
                enableStacktrace = true
                jdkHome = "%java8.home%"
            }
        }

        triggers {
            vcs {
                id = "vcsTrigger"
                branchFilter = ""
            }
        }

        features {
            perfmon {
                id = "perfmon"
            }
        }
    }

    val build1 = buildType {
        id("BuildTeamCity100")
        templates(buildTemplate)
        name = "Build - TeamCity 2018.1"
    }

    val build2 = buildType {
        id("Build2")
        templates(buildTemplate)
        name = "Build - TeamCity 2018.2"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2018.2")
        }
    }

    val build3 = buildType {
        id("Build3")
        templates(buildTemplate)
        name = "Build - TeamCity 2019.1"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2019.1")
        }
    }

    val reportCodeQuality = buildType {
        id("ReportCodeQuality")
        templates(buildTemplate)
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts%")
            param("gradle.tasks", "clean build sonarqube")
        }
    }

    buildTypesOrder = arrayListOf(build1, build2, build3, reportCodeQuality)
}
