
rootProject.name = "linux-properties"

include ("agent")

rootProject.children.forEach { project ->
    project.name = "${rootProject.name}-${project.name}"
}
