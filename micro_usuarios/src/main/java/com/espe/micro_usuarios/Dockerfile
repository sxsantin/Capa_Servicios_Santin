# Usar una imagen base de Java
FROM openjdk:17-jdk-slim

# Establecer el directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo JAR del proyecto al contenedor
COPY micro_usuarios.jar app.jar

# Exponer el puerto que usa la aplicación
EXPOSE 8004

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
