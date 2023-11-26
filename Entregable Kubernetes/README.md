# Despliegue de aplicaciones en Kubernetes
Este documento describe el proceso de creación de un clúster de Kubernetes y el despliegue de una aplicación Drupal con MySQL en él. 

Se utilizan archivos de configuración YAML para crear los recursos necesarios en Kubernetes y un archivo Vagrantfile para crear una máquina virtual donde se crea el clúster.

## Creación del clúster de Kubernetes
Para crear el clúster de Kubernetes, se utiliza la herramienta Kind (Kubernetes in Docker). Kind permite ejecutar clústeres de Kubernetes locales utilizando Docker como “nodos”. El clúster se crea con un mapeo de puertos personalizado que apunta al puerto 8085 de la máquina local. Esto se especifica en el archivo de configuración kind-config.yaml.
```yaml
# Creación de un cluster con un mapeo de puertos personalizado.
kind: Cluster # Tipo de archivo de configuración (Cluster)
apiVersion: kind.x-k8s.io/v1alpha4 # Versión de la API de Kubernetes a utilizar
nodes: # Nodos que contiene el cluster
- role: control-plane
  extraPortMappings:
  - containerPort: 30080 # Se mapean los puertos dentro del cluster a uno de nuestro host
    hostPort: 8085 # Mapeo al host
    protocol: TCP # Protocolo usado
```

### Volúmenes
Durante el transcurso de la práctica se han empleado volúmenes persistentes para el amacenamiento de los datos. Estos son muy útiles, ya que nos permiten preservar los datos a través de las reinicializaciones de los pods. Esto significa que el almacenamiento persistente de los datos es independiente de la vida útil de los pods que lo utilizan.

#### Drupal
El archivo drupal-persistent-volumes.yaml se emplea para desplegar el volumen drupal-pvc-claim. El cual permite el acceso simultaneo de lectura y escritura, además solicita 256 MiB de almacenamiento.
```yaml
apiVersion: v1
kind: PersistentVolumeClaim # Indica que es un recurso de tipo volumen persistente
metadata:
  name: drupal-pvc-claim # Define el nombre del volumen persistente
spec:
  accessModes:
    - ReadWriteOnce # Define los modos de acceso al volumen
  resources:
    requests:
      storage: 256Mi # Define el tamaño del volumen
```

#### MySQL
El archivo mysql-persistent-volumes.yaml se emplea para desplegar el volumen msql-pvc-claim. El cual permite el acceso simultaneo de lectura y escritura, además solicita 256 MiB de almacenamiento.
```yaml
apiVersion: v1
kind: PersistentVolumeClaim # Indica que es un recurso de tipo volumen persistente
metadata:
  name: mysql-pvc-claim # Define el nombre del volumen persistente
spec:
  accessModes:
    - ReadWriteOnce # Define los modos de acceso al volumen
  resources:
    requests:
      storage: 256Mi # Define el tamaño del volumen
```
### Drupal
#### Despliegue de la aplicación Drupal
La aplicación Drupal se despliega en el clúster de Kubernetes utilizando un archivo de configuración YAML (drupal-deployment.yaml). Este archivo define un recurso de Despliegue que crea y gestiona un conjunto de réplicas de Pods. Cada Pod ejecuta un contenedor con la imagen Docker de Drupal.
```yaml
apiVersion: apps/v1 # Versión de este tipo de recurso (apps/v1)
kind: Deployment # Tipo de recurso (Deployment)
metadata:
 name: drupal # Nombre de este recurso específico
spec:
  replicas: 1
  selector:
    matchLabels: # selecciona aquellos Pods con una etiqueta (app:drupal) para que pertenezcan a este Deployment
      app: drupal
  template:
    metadata:
      labels: # Define una etiqueta (drupal) para los Pods que envuelven tu contenedor
        app: drupal
    spec:
      containers:
      - name: drupal # Nombre para el contenedor (drupal)
        image: drupal:latest # Nombre de la imagen Docker a usar (drupal:latest)
        ports:
          - containerPort: 80 # Puerto en el que exponemos el contenedor
        volumeMounts:
          - name: drupal-persistent-storage # Define el nombre del volumen
            mountPath: /drupal # Especifica directorio de montaje
      volumes: 
        - name: drupal-persistent-storage # Define el nombre del volumen
          persistentVolumeClaim:
            claimName: drupal-pvc-claim # Define el nombre que se utilizará para solicitar un volumen persistente del cluster de Kubernetes
```
El Despliegue también especifica un volumen persistente para almacenar los datos de la aplicación Drupal. Esto se hace utilizando un recurso de PersistentVolumeClaim (drupal-persistent-volumes.yaml), que solicita un volumen persistente de un tamaño específico del clúster de Kubernetes.
#### Servicio de la aplicación Drupal
Tras el despliegue en el cluster, se define un recurso de Kubernetes, un Service, para la aplicacion de Drupal. El cual actuará como un punto de entrada para las solicitudes de red y las redirige al Pod. Este servicio se encuentra en drupal-sv.yaml.
```yaml
apiVersion: v1 # Versión de la API de Kubernetes a utilizar
kind: Service # Tipo de archivo de configuración (Service)
metadata: # Metadatos del recurso
  name: service-drupal # Nombre del servicio
spec: 
  selector: # El Servicio (Service) selecciona los Pods que se expondrán en función de sus etiquetas.
    app: drupal # Deben coincidir con las especificadas para los Pods en el recurso de Despliegue (en nuestro caso drupal).
  type: NodePort # Puerto del Nodo
  ports:
    - name: http # Método de solicitud
      port: 80 # Escucha solicitudes en el puerto 80
      nodePort: 30080 # Las reenvía al puerto 30080 del Pod de destino
```
### MySQL
#### Despliegue de la base de datos MySQL
La base de datos MySQL también se despliega en el clúster de Kubernetes utilizando un archivo de configuración YAML (mysql-deployment.yaml). Al igual que con la aplicación Drupal, este archivo define un recurso de Despliegue que crea y gestiona un conjunto de réplicas de Pods. Cada Pod ejecuta un contenedor con la imagen Docker de MySQL.
```yaml
apiVersion: apps/v1 # Versión de este tipo de recurso (apps/v1)
kind: Deployment # Tipo de recurso (Deployment)
metadata:
 name: mysql # Nombre de este recurso específico
spec:
 selector:
    matchLabels: # selecciona aquellos Pods con una etiqueta (app:mysql) para que pertenezcan a este Deployment
      app: mysql
 template:
    metadata:
      labels: # Define una etiqueta (app:mysql) para los Pods que envuelven tu contenedor
        app: mysql
    spec:
      containers:
      - name: mysql # Nombre para el contenedor (mysql)
        image: mysql:latest # Nombre de la imagen Docker a usar (mysql:latest)
        ports:
        - containerPort: 3306 # Puerto en el que exponemos el contenedor
        env: # Variables de entorno
        - name: MYSQL_ROOT_PASSWORD
          value: root
        - name: MYSQL_DATABASE
          value: drupal
        - name: MYSQL_USER
          value: drupal
        - name: MYSQL_PASSWORD
          value: drupal
        volumeMounts:
          - name: mysql-persistent-storage # Define el nombre del volumen
            mountPath: /mysql # Especifica directorio de montaje
      volumes: 
      - name: mysql-persistent-storage # Define el nombre del volumen
        persistentVolumeClaim:
          claimName: mysql-pvc-claim # Define el nombre que se utilizará para solicitar un volumen persistente del cluster de Kubernetes
```
El Despliegue también especifica un volumen persistente para almacenar los datos de la base de datos MySQL. Esto se hace utilizando un recurso de PersistentVolumeClaim (mysql-persistent-volumes.yaml), que solicita un volumen persistente de un tamaño específico del clúster de Kubernetes.
#### Servicio de la base de datos MySQL
Al igualque ocurre con Drupal, se define un Service para MySQL, el cual cumple la misma función. Este se encuentra en mysql-sv.yaml.
```yaml
apiVersion: v1 # Versión de la API de Kubernetes a utilizar
kind: Service # Tipo de archivo de configuración (Service)
metadata: # Metadatos del recurso
  name: service-mysql # Nombre del servicio
spec: 
  selector: # El Servicio (Service) selecciona los Pods que se expondrán en función de sus etiquetas.
    app: mysql # Deben coincidir con las especificadas para los Pods en el recurso de Despliegue (en nuestro caso drupal).
  type: ClusterIP # Puerto del Nodo
  ports:
    - name: http # Método de solicitud
      port: 3306
      targetPort: 3306
```
## Creación de la máquina virtual
Para crear la máquina virtual donde se crea el clúster de Kubernetes, se utiliza Vagrant. Vagrant es una herramienta que permite crear y gestionar máquinas virtuales de manera sencilla y consistente.

El archivo Vagrantfile define la configuración de la máquina virtual. En este caso, se utiliza una imagen de Ubuntu 18.04 (Bionic Beaver) como base para la máquina virtual. La máquina virtual se configura con una red privada y un reenvío de puertos para permitir el acceso a la aplicación Drupal desde la máquina host.

El archivo Vagrantfile también define un script de provisionamiento que se ejecuta cuando se crea la máquina virtual. Este script instala Docker, Kind y kubectl en la máquina virtual, copia los archivos de configuración de Kubernetes a la máquina virtual y luego aplica estas configuraciones para desplegar los servicios en Kubernetes.

## Conclusion
Este proyecto demuestra cómo se puede utilizar Kubernetes para desplegar una aplicación Drupal con una base de datos MySQL en una máquina virtual. Se utilizan archivos de configuración YAML para definir los recursos de Kubernetes y un archivo Vagrantfile para crear y configurar la máquina virtual. Los volúmenes persistentes se utilizan para almacenar los datos de la aplicación y de la base de datos, lo que permite que los datos persistan incluso cuando los Pods se reinician o se eliminan. El reenvío de puertos permite acceder a la aplicación Drupal desde la máquina host.