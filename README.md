# TP Protocolos de Comunicación

### Instrucciones de Instalación

###### Nota: Se supone Maven 3 y JDK 1.6 instalados en un entorno Linux/Unix compatible con bash.

### Paso 1: 
Ejecute **./dependency_installer.sh** para cargar los .jars necesarios a su repositorio maven local. (UserAgentUtils 1.6)

### Paso 2:
Ejecute **mvn clean compile assembly:single**. Esto generará un .jar ejecutable que recibe distintos parámetros para la ejecución del trabajo práctico.

### Paso 3:
Ejecute **java -jar target/pdc-2012-1.0-SNAPSHOT-jar-with-dependencies.jar --help** para ver los posibles parámetros de arranque del proxy

### Paso 4
Ejecute el mismo comando con sus configuraciones deseadas
