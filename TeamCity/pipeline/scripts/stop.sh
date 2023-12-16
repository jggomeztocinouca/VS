echo "Deteniendo el contenedor..."

if docker stop VS; then
    echo "Contenedor detenido con éxito."
else
    echo "Error al intentar detener el contenedor."
    exit 1
fi
