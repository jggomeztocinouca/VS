# Crear el clúster con Kind
Write-Host "Creando el clúster con Kind..."
kind create cluster --config kind-config.yaml

# Despliegue de los volumenes persistentes
Write-Host "`nDesplegando los volúmenes persistentes para Drupal..."
kubectl apply -f drupal-persistent-volumes.yaml

Write-Host "`nDesplegando los volúmenes persistentes para MySQL..."
kubectl apply -f mysql-persistent-volumes.yaml

# Despligue de los servicios y aplicaciones de MySQL
Write-Host "`nDesplegando los servicios y aplicaciones de MySQL..."
kubectl apply -f mysql-deployment.yaml
kubectl apply -f mysql-sv.yaml

# Despligue de los servicios y aplicaciones de Drupal
Write-Host "`nDesplegando los servicios y aplicaciones de Drupal..."
kubectl apply -f drupal-deployment.yaml
kubectl apply -f drupal-sv.yaml
