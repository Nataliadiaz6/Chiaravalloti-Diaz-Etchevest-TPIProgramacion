# Chiaravalloti-Diaz-Etchevest-TPIProgramacion
Trabajo Final Integrador: Gestión de Pedidos y Envíos (Java + MySQL)
Este proyecto implementa una aplicación de consola en Java, desarrollada con arquitectura de tres capas (Models, DAO y Service) y una relación 1 a 1 unidireccional entre las entidades Pedido y Envio.
La persistencia se maneja mediante JDBC, utilizando transacciones con commit y rollback para asegurar la integridad de los datos.

Dominio Elegido y Arquitectura
El dominio está formado por dos entidades principales: Pedido y Envio.
Un Pedido representa una solicitud generada por un cliente, mientras que un Envio contiene la información del despacho asociado.
La relación entre ambas es uno a uno y unidireccional:
el Pedido conoce a su Envio, pero el Envio no conoce al Pedido.

Las claves de negocio utilizadas son:
Pedido.numero
Envio.tracking
En el diseño de persistencia, la tabla pedido contiene una columna llamada envioId.
Esta columna es una clave foránea y además es única, lo cual garantiza que cada Envío solo pueda asociarse a un único Pedido.
Esto asegura correctamente la relación 1:1.

Requisitos y Pasos para Crear la Base de Datos
Requisitos del sistema
El proyecto requiere:
Java 8 o superior (se recomienda Java 21).
Servidor MySQL, ya sea directamente o mediante XAMPP/WAMP.
El driver MySQL Connector/J agregado al classpath.
Un entorno de desarrollo como NetBeans.

Pasos para crear la base con los scripts provistos
Iniciar MySQL.
Asegurarse de que el servidor esté corriendo (por ejemplo, activando MySQL desde XAMPP).
Abrir Workbench o la terminal de MySQL.
Ejecutar el script de creación de tablas.
El archivo create_tables_pedido_envio.sql crea la base de datos (como tpi) y genera las tablas correspondientes.
También define la clave foránea envioId con la restricción UNIQUE para garantizar la relación uno a uno.

Ejecutar el script de datos de prueba.
El archivo test_data.sql carga registros iniciales, incluyendo pedidos con envío asociado y pedidos sin envío.
Esto permite usar la aplicación sin necesidad de ingresar datos manualmente.

Ejecución del Proyecto y Credenciales de Prueba
Credenciales por defecto
La configuración de conexión se encuentra en:
tpi/Config/DatabaseConnection.java

El proyecto usa por defecto:
Servidor y puerto: localhost:3306
Base de datos: tpi
Usuario: root
Contraseña: (vacía)
Si tu entorno es diferente, solo tenés que modificar ese archivo.

Cómo compilar y ejecutar
Abrir el proyecto TPIProgramacion en Apache NetBeans.
Comprobar que el conector MySQL/J esté agregado a las librerías del proyecto.

Ejecutar la clase principal:
tpi.main.MainApp.java

Flujo de uso recomendado para pruebas
El menú de la aplicación de consola guía todas las operaciones.
Algunas pruebas sugeridas:

Crear un nuevo Pedido.
Verifica la transacción completa 1:1.
El sistema debe mostrar un mensaje de éxito, asignando automáticamente un ID y un código de tracking.

Crear un Envío.
Prueba la validación de la regla uno a uno.
Si se intenta asignar un envío a un pedido que ya tiene uno, aparecerá el mensaje de error correspondiente.

Buscar un Pedido por ID.
Permite comprobar las lecturas con LEFT JOIN mostrando los datos del pedido y, si corresponde, el envío asociado.

Eliminar un Pedido.
Realiza una baja lógica (soft delete).
Al eliminarlo, el pedido ya no debería aparecer en las opciones de listado.

Enlace al video explicativo:
https://drive.google.com/file/d/12oDpfyZZuNd3sJBUUctEzZQURck030-l/view?usp=sharing
