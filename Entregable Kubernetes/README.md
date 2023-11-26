# Despliegue de aplicaciones en Kubernetes
Este documento describe el proceso de creación de un clúster de _Kubernetes_ y el despliegue de una aplicación _Drupal_ enlazado a una base de datos _MySQL_. 

Se utilizan archivos de configuración _YAML_ para crear los recursos necesarios en _Kubernetes_ y un archivo _Vagrantfile_ para crear una máquina virtual donde se crea el clúster.

## Creación del clúster de Kubernetes
Para crear el clúster de _Kubernetes_, se utiliza la herramienta _Kind_ (_Kubernetes in Docker_). _Kind_ permite ejecutar clústeres de _Kubernetes_ locales utilizando _Docker_ como “nodos”. El clúster se crea con un mapeo de puertos personalizado que apunta al puerto 8085 de la máquina local. Esto se especifica en el archivo de configuración ```kind-config.yaml```.
```yaml
# Creación de un cluster con un mapeo de puertos personalizado.
kind: Cluster # Tipo de archivo de configuración (Cluster)
apiVersion: kind.x-k8s.io/v1alpha4 # Versión de la API de Kubernetes a utilizar
nodes: # Nodos que contiene el cluster
- role: control-plane
  extraPortMappings:
  - containerPort: 30080 
  # Se mapean los puertos dentro del cluster (30080) a uno de nuestro host (8085)
    hostPort: 8085 # Mapeo al host
    protocol: TCP # Protocolo usado
```

## Volúmenes persistentes
Durante el transcurso de la práctica se han empleado volúmenes persistentes para el amacenamiento de los datos. Estos son muy útiles, ya que nos permiten preservar los datos a través de las reinicializaciones de los pods. Esto significa que el almacenamiento persistente de los datos es independiente de la vida útil de los pods que lo utilizan.

### Volumen persistente: MySQL
El archivo ```mysql-persistent-volumes.yaml``` se emplea para desplegar el volumen _msql-pvc-claim_. El cual permite el acceso simultaneo de lectura y escritura, además solicita 256 MiB de almacenamiento.

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

### Volumen persistente: Drupal
El archivo ```drupal-persistent-volumes.yaml``` se emplea para desplegar el volumen _drupal-pvc-claim_. El cual permite el acceso simultaneo de lectura y escritura, además solicita 256 MiB de almacenamiento.

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

## Despliegue de Drupal
### Deployment
La aplicación _Drupal_ se despliega en el clúster de _Kubernetes_ utilizando un archivo de configuración _YAML_ (```drupal-deployment.yaml```). Este archivo define un recurso de Despliegue que crea y gestiona un conjunto de réplicas de Pods. Cada Pod ejecuta un contenedor con la imagen _Docker_ de _Drupal_.

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
El Despliegue también especifica un volumen persistente para almacenar los datos de la aplicación _Drupal_. Esto se hace utilizando un recurso de _PersistentVolumeClaim_ (```drupal-persistent-volumes.yaml```), que solicita un volumen persistente de un tamaño específico del clúster de Kubernetes.

#### Escalado del despliegue
Si quisieramos tener redundancia para conseguir tolerancia a fallos, podríamos establecer un mayor número de replicas. Esto se conseguiría modificando la siguiente línea:
```yaml
replicas: 1 --> replicas: 2
```

Obteniendo el siguiente archivo modificado:
```yaml
apiVersion: apps/v1 # Versión de este tipo de recurso (apps/v1)
kind: Deployment # Tipo de recurso (Deployment)
metadata:
 name: drupal # Nombre de este recurso específico
spec:
  replicas: 2
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

### Service
Tras el despliegue en el cluster, se define un recurso de _Kubernetes_, un _Service_, para la aplicacion de _Drupal_. El cual actuará como un punto de entrada para las solicitudes de red y las redirige al Pod. Este servicio se encuentra en ```drupal-sv.yaml```.

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
## MySQL
### Deployment
La base de datos _MySQL_ también se despliega en el clúster de _Kubernetes_ utilizando un archivo de configuración _YAML_ (```mysql-deployment.yaml```). Al igual que con la aplicación _Drupal_, este archivo define un recurso de Despliegue que crea y gestiona un conjunto de réplicas de Pods. Cada Pod ejecuta un contenedor con la imagen _Docker_ de _MySQL_.

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
El Despliegue también especifica un volumen persistente para almacenar los datos de la base de datos _MySQL_. Esto se hace utilizando un recurso de _PersistentVolumeClaim_ (```mysql-persistent-volumes.yaml```), que solicita un volumen persistente de un tamaño específico del clúster de _Kubernetes_.

### Service
Al igual que ocurre con _Drupal_, se define un _Service_ para _MySQL_, el cual cumple la misma función. Este se encuentra en ```mysql-sv.yaml```.

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
Para crear la máquina virtual donde se crea el clúster de _Kubernetes_, se utiliza _Vagrant_. 

_Vagrant_ es una herramienta que permite crear y gestionar máquinas virtuales de manera sencilla y consistente.

El archivo ```Vagrantfile``` define la configuración de la máquina virtual. En este caso, se utiliza una imagen de _Ubuntu 18.04_ como base para la máquina virtual, la cual está configurada para el reenvío de puertos y permitir el acceso a la aplicación _Drupal_ desde la máquina host.

El archivo ```Vagrantfile``` también define un script de provisionamiento que se ejecuta cuando se crea la máquina virtual. Este script sigue el siguiente proceso:
1. Copia los archivos de configuración (_YAML_) desde el host al directorio raíz de la máquina virtual.
2. Instala _Docker_.
3. Instala _Kind_.
4. Instala _kubectl_.
5. Ejecuta los archivos de despliegue y configuración de _Kubernetes_ (_YAML_).

```Ruby
Vagrant.configure("2") do |config|
    # Configuración de la máquina virtual
    config.vm.box = "generic/ubuntu1804" # Box base para la VM
    # Redirección de puertos VM --> Host
    config.vm.network "forwarded_port", guest: 8085, host: 8085

     # Copiar archivos de configuración de Kubernetes a la VM
    config.vm.provision "file", source: "../Cluster/kind-config.yaml", 
      destination: "kind-config.yaml"
    
    config.vm.provision "file", source: "../Cluster/mysql-persistent-volumes.yaml", 
      destination: "mysql-persistent-volumes.yaml"
    
    config.vm.provision "file", source: "../Cluster/drupal-persistent-volumes.yaml", 
      destination: "drupal-persistent-volumes.yaml"
    
    config.vm.provision "file", source: "../Cluster/mysql-deployment.yaml", 
      destination: "mysql-deployment.yaml"
    
    config.vm.provision "file", source: "../Cluster/mysql-sv.yaml", 
      destination: "mysql-sv.yaml"
    
    config.vm.provision "file", source: "../Cluster/drupal-deployment.yaml", 
      destination: "drupal-deployment.yaml"
    
    config.vm.provision "file", source: "../Cluster/drupal-sv.yaml", 
      destination: "drupal-sv.yaml"

    # Configuración de provisionamiento (instrucciones de terminal)
    config.vm.provision "shell", inline: <<-SHELL
        # Actualizar repositorios
        sudo apt-get update

        # Instalar Docker
        sudo apt-get install -y 
        apt-transport-https ca-certificates curl software-properties-common
        
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | 
        
        sudo apt-key add -
        
        sudo add-apt-repository "deb [arch=amd64] 
          https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
        
        sudo apt-get update
        
        sudo apt-get install -y docker-ce

        # Instalar Kind
        # For AMD64 / x86_64
        [ $(uname -m) = x86_64 ] && 
          curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-$(uname)-amd64
        
        chmod +x ./kind
        
        sudo mv ./kind /usr/local/bin/kind

        # Instalar Kubectl
        curl -LO 
        "https://dl.k8s.io/release/$(
          curl -L -s https://dl.k8s.io/release/stable.txt
        )/bin/linux/amd64/kubectl"
        
        sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

        # Crear cluster de Kubernetes
        kind create cluster --config kind-config.yaml

        # Aplicar configuraciones de Kubernetes
        kubectl apply -f drupal-persistent-volumes.yaml
        kubectl apply -f mysql-persistent-volumes.yaml
        kubectl apply -f mysql-deployment.yaml
        kubectl apply -f mysql-sv.yaml
        kubectl apply -f drupal-deployment.yaml
        kubectl apply -f drupal-sv.yaml
    SHELL
end
  
```

## Conclusion
Este proyecto demuestra cómo se puede utilizar _Kubernetes_ para desplegar una aplicación _Drupal_ con una base de datos _MySQL_ tanto en local como en una máquina virtual. Se utilizan archivos de configuración _YAML_ para definir los recursos de _Kubernetes_ y un archivo de tipo _Vagrantfile_ para crear y configurar la máquina virtual. Los volúmenes persistentes se utilizan para almacenar los datos de la aplicación y de la base de datos, lo que permite que los datos persistan incluso cuando los Pods se reinician o se eliminan. El reenvío de puertos permite acceder a la aplicación _Drupal_ desde la máquina que los aloja (ya sea host o la VM).