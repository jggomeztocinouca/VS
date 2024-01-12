# Importa las bibliotecas necesarias para la manipulación de datos y la visualización.
import matplotlib.pyplot as plt
import pandas as pd
import os


# Define una función para trazar la temperatura frente al tiempo.
def plot_temperature_vs_time(data):
    # Crea un DataFrame de Pandas para facilitar el manejo de los datos.
    df = pd.DataFrame(data, columns=[
        "_id._bsontype", "_id.id.0", "_id.id.1", "_id.id.2", "_id.id.3", "_id.id.4", "_id.id.5", "_id.id.6", "_id.id.7",
        "_id.id.8",
        "_id.id.9", "_id.id.10", "_id.id.11", "_id.id.offset", "_id.generationTime", "timestamp", "panID", "sensorNode",
        "temperatureSHT31",
        "humiditySHT31", "co2eqIAQ", "tvocIAQ", "co2SCD41", "temperatureSCD41", "humiditySCD41", "pm1ugSPS30",
        "pm25ugSPS30", "pm4ugSPS30",
        "pm10ugSPS30", "pm05cmSPS30", "pm1cmSPS30", "pm25cmSPS30", "pm4cmSPS30", "pm10cmSPS30", "particleSizeSPS30"
    ])

    # Calcula el promedio móvil de la temperatura con una ventana de 100 puntos de datos.
    df['temperatureSHT31_MA'] = df['temperatureSHT31'].rolling(window=100).mean()

    # Genera un gráfico del promedio móvil de la temperatura en función del tiempo.
    plt.figure(figsize=(10, 6))
    plt.plot(df['timestamp'], df['temperatureSHT31_MA'], marker='o', linestyle='-', color='b')
    plt.title('Promedio móvil de la temperatura en función del tiempo')
    plt.xlabel('Tiempo')
    plt.ylabel('Temperatura (°C)')
    plt.grid(True)
    plt.savefig('grafica.png')

# Comprueba si el script se está ejecutando directamente.
if __name__ == "__main__":
    # Define el nombre del archivo.
    file_name = "SensorData.csv"

    try:
        # Intenta leer el archivo CSV y generar el gráfico.
        data = pd.read_csv(file_name)
        plot_temperature_vs_time(data)
    except FileNotFoundError:
        # Imprime un mensaje de error si el archivo no se encuentra.
        print("Error: Archivo no encontrado.")
    except Exception as e:
        # Imprime un mensaje de error si ocurre cualquier otro error.
        print(f"Error: {e}")
