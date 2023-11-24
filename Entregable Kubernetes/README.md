# Despliegue de aplicaciones en Kubernetes
Este documento describe el proceso de creación de un clúster de Kubernetes y el despliegue de una aplicación Drupal con MySQL en él. Se utilizan archivos de configuración YAML para crear los recursos necesarios en Kubernetes y un archivo Vagrantfile para crear una máquina virtual donde se crea el clúster.

## Creación del clúster de Kubernetes
Para crear el clúster de Kubernetes, se utiliza la herramienta Kind (Kubernetes in Docker). Kind permite ejecutar clústeres de Kubernetes locales utilizando Docker como “nodos”. El clúster se crea con un mapeo de puertos personalizado que apunta al puerto 8085 de la máquina local. Esto se especifica en el archivo de configuración kind-config.yaml.

## Drupal
### Despliegue de la aplicación Drupal
La aplicación Drupal se despliega en el clúster de Kubernetes utilizando un archivo de configuración YAML (drupal-deployment.yaml). Este archivo define un recurso de Despliegue que crea y gestiona un conjunto de réplicas de Pods. Cada Pod ejecuta un contenedor con la imagen Docker de Drupal.

El Despliegue también especifica un volumen persistente para almacenar los datos de la aplicación Drupal. Esto se hace utilizando un recurso de PersistentVolumeClaim (drupal-persistent-volumes.yaml), que solicita un volumen persistente de un tamaño específico del clúster de Kubernetes.

## MySQL
### Despliegue de la base de datos MySQL
La base de datos MySQL también se despliega en el clúster de Kubernetes utilizando un archivo de configuración YAML (mysql-deployment.yaml). Al igual que con la aplicación Drupal, este archivo define un recurso de Despliegue que crea y gestiona un conjunto de réplicas de Pods. Cada Pod ejecuta un contenedor con la imagen Docker de MySQL.

El Despliegue también especifica un volumen persistente para almacenar los datos de la base de datos MySQL. Esto se hace utilizando un recurso de PersistentVolumeClaim (mysql-persistent-volumes.yaml), que solicita un volumen persistente de un tamaño específico del clúster de Kubernetes.

## Creación de la máquina virtual
Para crear la máquina virtual donde se crea el clúster de Kubernetes, se utiliza Vagrant. Vagrant es una herramienta que permite crear y gestionar máquinas virtuales de manera sencilla y consistente.

El archivo Vagrantfile define la configuración de la máquina virtual. En este caso, se utiliza una imagen de Ubuntu 18.04 (Bionic Beaver) como base para la máquina virtual. La máquina virtual se configura con una red privada y un reenvío de puertos para permitir el acceso a la aplicación Drupal desde la máquina host.

El archivo Vagrantfile también define un script de provisionamiento que se ejecuta cuando se crea la máquina virtual. Este script instala Docker, Kind y kubectl en la máquina virtual, copia los archivos de configuración de Kubernetes a la máquina virtual y luego aplica estas configuraciones para desplegar los servicios en Kubernetes.

## Conclusion
Este proyecto demuestra cómo se puede utilizar Kubernetes para desplegar una aplicación Drupal con una base de datos MySQL en una máquina virtual. Se utilizan archivos de configuración YAML para definir los recursos de Kubernetes y un archivo Vagrantfile para crear y configurar la máquina virtual. Los volúmenes persistentes se utilizan para almacenar los datos de la aplicación y de la base de datos, lo que permite que los datos persistan incluso cuando los Pods se reinician o se eliminan. El reenvío de puertos permite acceder a la aplicación Drupal desde la máquina host.