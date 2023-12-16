import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

version = "2023.11"

project {
    buildType {
        name = "Ejemplo CI/CD Pipeline con Docker"

        steps {
            dockerCommand {
                name = "Crear contenedor"
                commandType = customScript {
                    scriptContent = "docker run -d --name VS node:21-alpine3.18"
                }
            }
            script {
                name = "Test de dependencias"
                scriptContent = "./scripts/test.sh"
                executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            }
            script {
                name = "Detener servicio"
                scriptContent = "./scripts/stop.sh"
                executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            }
            script {
                name = "Eliminar contenedor"
                scriptContent = "./scripts/delete.sh"
                executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            }
        }
    }
}
