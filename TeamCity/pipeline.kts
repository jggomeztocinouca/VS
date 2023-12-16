import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

version = "2021.1"

project {
    buildType {
        name = "Ejemplo CI/CD Pipeline con Docker"

        steps {
            dockerCommand {
                name = "Crear y ejecutar contenedor 'VS'"
                commandType = run {
                    image = "node:21-alpine3.18"
                    args = "--name VS"
                }
            }
            script {
                name = "Test de dependencias"
                scriptContent = "./scripts/test.sh"
            }
            script {
                name = "Detener servicio"
                scriptContent = "./scripts/stop.sh"
            }
            script {
                name = "Eliminar contenedor"
                scriptContent = "./scripts/delete.sh"
            }
        }
    }
}
