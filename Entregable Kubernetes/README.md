# Despliegue de aplicaciones en Kubernetes
Este documento describe el proceso de creación de un cluster de Kubernetes y despliegue de una aplicación de Drupal con MySQL utilizando archivos de configuración yaml.

## Crear un cluster de kind con un mapeo de puertos personalizado
Primero, necesitamos un archivo de configuración para el cluster de kind:
```yaml
# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 8085
    hostPort: 8085
    listenAddress: "0.0.0.0"
```

Luego, ejecutamos el siguiente comando para crear el cluster:

```
kind create cluster --config kind-config.yaml
```

## Desplegar una aplicación de Drupal
Necesitamos un archivo de configuración para el despliegue de Drupal.

```yaml
# drupal.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: drupal
spec:
  replicas: 1
  selector:
    matchLabels:
      app: drupal
  template:
    metadata:
      labels:
        app: drupal
    spec:
      containers:
      - name: drupal
        image: drupal:latest
        ports:
        - containerPort: 80
```

Luego, ejecutamos el siguiente comando para desplegar Drupal:

```
kubectl apply -f drupal.yaml
```

## Conectar Drupal a una base de datos MySQL
Necesitamos un archivo de configuración para el despliegue de MySQL.

```yaml
# mysql.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:5.7
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: password
        ports:
        - containerPort: 3306
```

Luego, ejecutamos el siguiente comando para desplegar MySQL:

```
kubectl apply -f mysql.yaml
```

Para conectar Drupal a MySQL, necesitamos proporcionar la información de la base de datos a Drupal. Esto se puede hacer a través de variables de entorno o un archivo de configuración.

## Usar volúmenes persistentes
Para usar volúmenes persistentes, necesitaremos crear objetos PersistentVolume y PersistentVolumeClaim en Kubernetes.

```yaml
# persistent-volume.yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-volume
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: manual
  local:
    path: /path/to/your/local/directory
  nodeAffinity:
    required:
      nodeSelectorTerms:
      - matchExpressions:
        - key: kubernetes.io/hostname
          operator: In
          values:
          - your-node-name
---
# persistent-volume-claim.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pv-claim
spec:
  storageClassName: manual
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

Luego, montamos estos volúmenes en tus despliegues de Drupal y MySQL.

## Modificar el despliegue de Drupal para tener dos réplicas
Simplemente cambiamos el campo replicas del archivo de despliegue de Drupal a 2.

```yaml
# drupal2.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: drupal
spec:
  replicas: 2  # Cambia esta línea
  selector:
    matchLabels:
      app: drupal
  template:
    metadata:
      labels:
        app: drupal
    spec:
      containers:
      - name: drupal
        image: drupal:latest
        ports:
        - containerPort: 80
```

Luego, aplicamos este cambio con el siguiente comando:

```
kubectl apply -f drupal2.yaml
```

## Usar un Vagrantfile para crear una máquina virtual

```Vagrantfile
# Vagrantfile
Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"
  config.vm.network "forwarded_port", guest: 8085, host: 8085
  config.vm.provision "shell", inline: <<-SHELL
    sudo apt-get update
    sudo apt-get install -y docker.io
    curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.11.1/kind-linux-amd64
    chmod +x ./kind
    sudo mv ./kind /usr/local/bin/kind
    kind create cluster --config /vagrant/kind-config.yaml
    kubectl apply -f /vagrant/drupal-deployment.yaml
    kubectl apply -f /vagrant/mysql-deployment.yaml
  SHELL
end
```
Este Vagrantfile crea una máquina virtual Ubuntu, instala Docker y kind, y luego crea el cluster de kind y despliega Drupal y MySQL.