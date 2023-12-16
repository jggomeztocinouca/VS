echo "Eliminando el contenedor..."

if docker rm -f VS; then
    echo "Contenedor eliminado con éxito."
else
    echo "Error al intentar eliminar el contenedor."
    exit 1
fi
