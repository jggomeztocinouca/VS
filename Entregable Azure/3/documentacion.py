# Importa el módulo 'os' para interactuar con el sistema operativo.
import os

# Define una función llamada 'generar_markdown' que toma una lista de archivos como argumento.
def generar_markdown(archivos):
    # Inicializa una cadena vacía para almacenar el contenido Markdown.
    contenido_markdown = ""

    # Itera sobre la lista de archivos.
    for archivo in archivos:
        # Abre cada archivo en modo lectura ('r').
        with open(archivo, 'r') as f:
            # Lee el contenido del archivo y lo agrega a la cadena 'contenido_markdown'.
            contenido = f.read()
            contenido_markdown += contenido + "\n\n"

    # Abre el archivo 'README.md' en modo escritura ('w') y escribe el contenido generado.
    with open('README.md', 'w') as f:
        f.write(contenido_markdown)

# Define una función principal llamada 'main'.
def main():
    # Lista de archivos que se utilizarán para generar el contenido Markdown.
    archivos = ['pipeline.txt', 'graficos.txt', 'web.txt']

    # Llama a la función 'generar_markdown' con la lista de archivos como argumento.
    generar_markdown(archivos)

# Verifica si el script se está ejecutando directamente (no importado como un módulo).
if __name__ == "__main__":
    # Llama a la función 'main' solo si el script se ejecuta directamente.
    main()
