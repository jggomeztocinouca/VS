project {
    buildType {
        name = "Ejemplo CI/CD Pipeline con Docker"
        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            dockerCommand {
                name = "Crear contenedor"
                commandType = customScript {
                    scriptContent = "docker run -d --name VS node:21-alpine3.18"
                }
            }
            dockerWrapper {
                name = "Test de dependencias"
                command = "./jenkins/scripts/test.sh"
                containerName = "VS"
            }
            dockerWrapper {
                name = "Detener servicio"
                command = "./jenkins/scripts/stop.sh"
                containerName = "VS"
            }
            dockerWrapper {
                name = "Eliminar contenedor"
                command = "./jenkins/scripts/delete.sh"
                containerName = "VS"
            }
        }
    }
}
