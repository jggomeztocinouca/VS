import jetbrains.buildServer.configs.kotlin.v2023_11.*
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script

version = "2023.11"

project {
    buildType {
        id("DockerPipeline")
        name = "Docker Pipeline"

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
                    docker run --rm -v ${'$'}PWD/scripts:/scripts VS /scripts/test.sh
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