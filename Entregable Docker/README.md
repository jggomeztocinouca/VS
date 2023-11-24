# Entregable 1: Docker.

## Parte 1: Docker Compose para Drupal y mySQL

### Volúmenes

Se ha creado un volumen compartido, llamado `volumenCompartido`.

```yml
volumes:
  volumenCompartido:
```

### Contenedor mySQL

1. Se establece la imagen _mysql_, en su versión predeterminada latest, para construir el contenedor.
2. Se establece la contraseña del usuario _root_ a `root`.
3. Se nombra a la BD sobre la que trabajar como `drupal`.
4. Se establece un nuevo directorio dentro del contendor, con el nombre `compartido`. Este directorio está almacenado en el volumen compartido `volumenCompartido`.

```yml
mysql:
  image: mysql
  environment:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: drupal
  volumes:
    - volumenCompartido:/compartido
```

### Contenedor Drupal

1. Se establece la imagen _drupal_, en su versión predeterminada latest, para construir el contenedor.
2. Se liga el puerto 80 del contenedor al puerto 81 del S.O. anfitrión.
3. Se establece un nuevo directorio dentro del contendor, con el nombre `compartido`. Este directorio está almacenado en el volumen compartido `volumenCompartido`.

```yml
drupal:
  image: drupal
  ports:
    - "81:80"
  volumes:
    - volumenCompartido:/compartido
```

## Parte 2: Docker Compose para WordPress y MariaDB

### Red

Se ha creado una red virtual con el nombre `redDocker`.

```yml
networks:
  redDocker:
```

### Contenedor MariaDB

1. Se establece la imagen `mariadb`, en su versión predeterminada latest, para construir el contenedor.
2. Se establece la contraseña del usuario _root_ a `root`.
3. Se nombra a la BD sobre la que trabajar como `wordpress`.
4. Se indica el uso de la red `redDocker`, común a ambos contenedores.

```yml
mariadb:
  image: mariadb
  environment:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: wordpress
  networks:
    - redDocker
```

### Contenedor Wordpress

1. Se establece la imagen `wordpress`, en su versión predeterminada latest, para construir el contenedor.
2. Se liga el puerto 80 del contenedor al puerto 82 del S.O. anfitrión.
3. Se indica el uso de la red `redDocker`, común a ambos contenedores.
4. Se establecen variables de entorno varias para la configuración de WordPress (host, usuario, contraseña, nombre BD).

```yml
drupal:
  image: drupal
  ports:
    - "81:80"
  volumes:
    - volumenCompartido:/compartido
```

## Construcción y ejecución de los contenedores

### Ejecución

Dentro de la carpeta donde se encuentra `docker-compose.yml`, se ejecuta el comando `docker-compose up -d` para ejecutar el contenedor.
