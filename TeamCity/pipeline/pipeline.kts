import jetbrains.buildServer.configs.kotlin.*

version = "2023.11"

project {
    buildType {
        steps {
            dockerCommand {
                name = "Construir contenedor Docker"
                commandType = build {
                    source = content {
                        content = "FROM node:21-alpine3.18"
                    }
                    namesAndTags = "VS"
                }
            }

            script {
                name = "Ejecutar test.sh en contenedor"
                scriptContent = """
                    ./scripts/test.sh
                """.trimIndent()
            }

            script {
                name = "Detener contenedor"
                scriptContent = """
                    ./scripts/stop.sh
                """.trimIndent()
            }

            script {
                name = "Eliminar contenedor y datos"
                scriptContent = """
                    ./scripts/delete.sh
                """.trimIndent()
            }
        }
    }
}