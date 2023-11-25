# Crear el clúster con Kind
kind create cluster --config kind-config.yaml

# Despliegue de los volumenes persistentes
kubectl apply -f drupal-persistent-volumes.yaml
kubectl apply -f mysql-persistent-volumes.yaml

# Despligue de los servicios y aplicaciones de MySQL
kubectl apply -f mysql-deployment.yaml
kubectl apply -f mysql-sv.yaml

# Despligue de los servicios y aplicaciones de MySQL
kubectl apply -f drupal-deployment.yaml
kubectl apply -f drupal-sv.yaml
