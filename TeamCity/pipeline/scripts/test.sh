echo "Verificando instalación de npm..."

node_version=$(node --version)

if [ $? -eq 0 ]; then
    echo "node ha sido instalado correctamente. Versión: $node_version"
else
    echo "Error: node no está instalado o no se pudo ejecutar."
    exit 1
fi