import jetbrains.buildServer.configs.kotlin.*

version = "2023.11"

project {
    buildType {
        steps {
            dockerCompose {
                name = "Node contenerizado"
                file = "docker-compose.yml"
            }
        }
        script {
            name "Test de dependencias"
            // El contenedor se llama VS
            scriptContent = "docker exec VS npm test"
        }
        script {
            name "Detener contenedor"
            scriptContent = "docker stop VS"
        }
        script {
            name "Eliminar contenedor"
            scriptContent = "docker rm VS"
        }
    }
}