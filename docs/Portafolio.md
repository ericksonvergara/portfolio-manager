
---

# Especificación General

## Nombre del Proyecto

**Portfolio Manager**

## Objetivo

Desarrollar una plataforma web que permita publicar y administrar proyectos de desarrollo de software mediante un panel administrativo seguro. Los visitantes podrán visualizar los proyectos publicados, mientras que el administrador tendrá acceso para crear, actualizar y eliminar el contenido del portafolio.

---

# Arquitectura Tecnológica

## Backend

* Java 21
* Spring Boot 4.1.0
* Gradle 9.5.1
* Arquitectura Hexagonal
* Spring Security
* JWT Authentication
* Spring Data JPA
* PostgreSQL
* Docker
* OpenAPI (Swagger)
* Flyway
* MapStruct
* Lombok
* JUnit 5
* Mockito

---

## Frontend

* React 19
* Vite
* React Router
* Axios
* React Hook Form
* Material UI (o Tailwind CSS)
* Context API o Redux Toolkit

---

## Base de Datos

* PostgreSQL

---

## Infraestructura

* Docker Compose

Servicios

* frontend
* backend
* postgres
* nginx (reverse proxy)

---

# Módulos

## Público

Landing Page

* Inicio
* Sobre mí
* Tecnologías
* Portafolio
* Contacto

---

## Privado

Panel Administrativo

* Login
* Dashboard
* Administración de proyectos
* Perfil del administrador
* Configuración

---

# Requerimientos Funcionales

---

## RF001 - Landing Page

El sistema deberá permitir que cualquier visitante acceda sin autenticación al sitio web.

La Landing Page mostrará:

* Presentación personal
* Imagen de perfil
* Descripción profesional
* Tecnologías utilizadas
* Lista de proyectos
* Redes sociales
* Información de contacto

---

## RF002 - Visualización de proyectos

El sistema deberá listar todos los proyectos activos.

Cada tarjeta deberá contener

* Imagen
* Nombre
* Descripción corta
* Tecnologías utilizadas
* Botón Ver Proyecto
* Botón Repositorio

---

## RF003 - Visualizar detalle del proyecto

Al seleccionar un proyecto deberá visualizarse:

* Nombre
* Descripción completa
* Galería de imágenes
* Tecnologías
* Fecha de desarrollo
* Estado
* URL del proyecto
* URL del repositorio
* Video demostrativo (opcional)

---

## RF004 - Inicio de sesión

Solo el administrador podrá autenticarse.

Credenciales

* Correo electrónico
* Contraseña

Validaciones

Correo válido.

La contraseña deberá cumplir:

* mínimo 6 caracteres
* máximo 12 caracteres
* una mayúscula
* una minúscula
* un número
* un carácter especial

---

## RF005 - Gestión de sesión

El sistema deberá:

* iniciar sesión mediante JWT
* cerrar sesión
* invalidar token expirado
* recordar la sesión mientras el token sea válido

---

## RF006 - Dashboard

El administrador visualizará indicadores como

* Total proyectos
* Total tecnologías utilizadas
* Fecha último proyecto
* Último acceso

---

## RF007 - Registrar proyecto

El administrador podrá registrar un nuevo proyecto.

Formulario

Nombre

Descripción corta

Descripción completa

Imagen principal

Galería de imágenes

URL del proyecto

Repositorio Git

Tecnologías

Estado

Fecha

Orden de aparición

Destacado (Sí/No)

---

## RF008 - Actualizar proyecto

El administrador podrá editar toda la información de un proyecto.

---

## RF009 - Eliminar proyecto

El administrador podrá eliminar proyectos.

Antes de eliminar deberá solicitar confirmación.

---

## RF010 - Activar o desactivar proyecto

El administrador podrá ocultar proyectos sin eliminarlos.

---

## RF011 - Gestión de imágenes

El sistema permitirá subir imágenes.

Formatos

* JPG
* JPEG
* PNG

Validaciones

* tamaño máximo configurable
* validación de tipo MIME
* nombre único

---

## RF012 - Vista previa

Antes de guardar un proyecto deberá visualizarse la imagen cargada.

---

## RF013 - Gestión de tecnologías

El administrador podrá administrar las tecnologías.

Ejemplo

Java

Spring Boot

Docker

PostgreSQL

Angular

React

Vue

Laravel

Python

Cada proyecto podrá asociar múltiples tecnologías.

---

## RF014 - Buscador

El visitante podrá buscar proyectos por

* nombre
* tecnología

---

## RF015 - Filtrado

El visitante podrá filtrar por

* Backend
* Frontend
* Full Stack
* Mobile
* DevOps
* IA

---

## RF016 - Contacto

Formulario

Nombre

Correo

Mensaje

Validaciones correspondientes.

---

## RF017 - Perfil del administrador

El administrador podrá modificar

* Nombre
* Fotografía
* Correo
* Contraseña

---

## RF018 - Cambio de contraseña

Se solicitará

Contraseña actual

Nueva contraseña

Confirmación

---

## RF019 - Recuperación de contraseña

Mediante correo electrónico.

---

## RF020 - Auditoría

Registrar

Inicio de sesión

Creación

Edición

Eliminación

Fecha

Usuario

IP

## RF021 - Registro de visitas

El sistema deberá registrar automáticamente cada visita realizada a la Landing Page.

Para cada visita se almacenará como mínimo:

* Fecha y hora.
* Dirección IP (anonimizada o enmascarada por seguridad).
* Navegador.
* Sistema operativo.
* Dispositivo (Desktop, Tablet, Mobile).
* País (opcional mediante geolocalización por IP).
* Proyecto visitado (cuando aplique).

---

### RF022 - Contador General

El Dashboard mostrará el número total de visitantes registrados desde la creación del sitio.

**Indicador**

```
Visitantes Totales

12.583
```

---

### RF023 - Visitantes del día

El Dashboard mostrará el total de visitas realizadas durante el día actual.

Ejemplo

```
Hoy

54 visitas
```

---

### RF024 - Visitantes del mes

El Dashboard mostrará el total de visitas del mes actual.

---

### RF025 - Proyectos más visitados

El Dashboard mostrará un ranking de los proyectos más consultados.

Ejemplo

| Posición | Proyecto    | Visitas |
| -------- | ----------- | ------- |
| 1        | Sistema POS | 320     |
| 2        | ETL Aliados | 210     |
| 3        | CRM Revent  | 165     |

---

### RF026 - Contador por proyecto

Cada proyecto incrementará su contador cuando un usuario ingrese al detalle del mismo.

---

### RF027 - Dashboard Estadístico

El Dashboard mostrará indicadores como:

* Total de proyectos.
* Total de visitantes.
* Visitantes del día.
* Visitantes del mes.
* Proyecto más visitado.
* Último proyecto publicado.
* Total de tecnologías registradas.

---

# Requerimientos No Funcionales

---

## RNF001 Arquitectura

El backend deberá implementar Arquitectura Hexagonal.

Separación mínima

```
application

domain

infrastructure

shared
```

---

## RNF002 Principios SOLID

Todo el código deberá cumplir

* SRP
* OCP
* LSP
* ISP
* DIP

---

## RNF003 Clean Code

Aplicar

* nombres descriptivos
* métodos pequeños
* clases cohesivas
* bajo acoplamiento
* alta cohesión

---

## RNF004 Seguridad

Autenticación JWT.

Contraseñas cifradas mediante BCrypt.

---

## RNF005 API REST

Todas las operaciones deberán exponerse mediante API REST.

---

## RNF006 Documentación

La API deberá documentarse con Swagger/OpenAPI.

---

## RNF007 Migraciones

La base de datos deberá administrarse mediante Flyway.

---

## RNF008 Persistencia

PostgreSQL 17 (o la versión estable que decidan adoptar).

---

## RNF009 Contenedores

Toda la aplicación deberá ejecutarse mediante Docker Compose.

Servicios mínimos

* frontend
* backend
* postgres
* nginx

---

## RNF010 Variables de entorno

No deberán existir credenciales dentro del código fuente.

Uso de `.env`.

---

## RNF011 Logs

Registro de

* errores
* advertencias
* accesos

---

## RNF012 Rendimiento

Las consultas deberán responder idealmente en menos de 2 segundos bajo carga normal.

---

## RNF013 Responsive

Compatible con

* Desktop
* Tablet
* Mobile

---

## RNF014 Compatibilidad

Compatible con

* Chrome
* Firefox
* Edge
* Safari

---

## RNF015 Escalabilidad

La arquitectura deberá permitir incorporar nuevos módulos sin modificar la lógica existente.

---

## RNF016 Mantenibilidad

Cobertura mínima recomendada del **80%** en pruebas unitarias del backend para casos de negocio críticos.

---

## RNF017 Versionamiento

Uso obligatorio de Git con estrategia de ramas (por ejemplo, GitFlow o Trunk-Based).

---

## RNF018 Calidad del código

Integración con herramientas como SonarQube para validar métricas de calidad, duplicación y vulnerabilidades.

---

## RNF019 Disponibilidad

La aplicación deberá iniciar correctamente mediante un único comando (`docker compose up -d`) y permitir el despliegue consistente en cualquier servidor con Docker.

---

## RNF020 Accesibilidad

Cumplimiento básico de criterios WCAG:

* Navegación mediante teclado.
* Contraste adecuado.
* Textos alternativos para imágenes.
* Etiquetas accesibles en formularios.

## RNF021

El registro de visitas no deberá afectar el tiempo de respuesta de la aplicación.

---

## RNF022

El sistema deberá evitar incrementar múltiples veces el contador cuando un mismo usuario refresque la página continuamente.

**Sugerencia de implementación**

* Cookie.
* LocalStorage.
* SessionStorage.
* O una combinación de IP + User Agent + ventana de tiempo (por ejemplo, no contar una nueva visita desde el mismo origen durante los siguientes 30 minutos).

---

## RNF023

Las consultas del Dashboard deberán estar optimizadas mediante índices en PostgreSQL para garantizar tiempos de respuesta inferiores a 2 segundos.

---

## Entidades adicionales

En lugar de guardar únicamente un campo `visitas` en la tabla de proyectos, te recomiendo crear una entidad dedicada:

## Visit

```text
id
visitor_token
ip
user_agent
browser
device
country
visited_at
project_id (nullable)
```

Con esta entidad podrás obtener fácilmente:

* Total de visitas.
* Visitas diarias.
* Visitas mensuales.
* Proyectos más consultados.
* Navegadores más usados.
* Dispositivos más utilizados.
* Países de origen.
* Gráficas históricas.
* Tendencias de crecimiento.

## Dashboard propuesto

El panel administrativo podría mostrar tarjetas como las siguientes:

```
+---------------------------------------------------------------+

📁 Proyectos              15

👀 Visitantes Totales     8.452

📈 Visitas Hoy            124

📅 Visitas Mes            2.415

🛠 Tecnologías            18

⭐ Proyecto más visto      Sistema ERP

+---------------------------------------------------------------+

📊 Visitas últimos 30 días
        (Gráfico de líneas)

---------------------------------------------------------------

🏆 Top 5 proyectos más visitados
(Gráfico de barras)

---------------------------------------------------------------

💻 Dispositivos

Desktop      68%
Mobile       28%
Tablet        4%

---------------------------------------------------------------

🌎 Países

🇨🇴 Colombia
🇩🇪 Alemania
🇺🇸 Estados Unidos
🇪🇸 España
```

# Reglas de Negocio

## RN001

No podrán existir dos proyectos con el mismo nombre.

---

## RN002

La URL del proyecto deberá ser válida.

---

## RN003

La URL del repositorio deberá ser válida.

---

## RN004

Solo podrán publicarse proyectos con una imagen principal.

---

## RN005

Un proyecto deberá tener al menos una tecnología asociada.

---

## RN006

Solo el administrador autenticado podrá crear, modificar o eliminar proyectos.

---

## RN007

Las imágenes deberán almacenarse con un nombre único para evitar colisiones.

---

## RN008

Si un proyecto está marcado como inactivo, no deberá aparecer en la Landing Page pública.

---

## RN009

Las operaciones críticas (crear, editar, eliminar y cambiar contraseña) deberán quedar registradas en la auditoría del sistema.

---

# Modelo de Entidades Inicial

Pensando en una arquitectura hexagonal y en futuras ampliaciones, el modelo mínimo podría incluir las siguientes entidades:

* **User**: administrador del sistema.
* **Role**: roles de acceso (inicialmente `ADMIN`, con posibilidad de agregar más en el futuro).
* **Project**: información principal de cada proyecto.
* **ProjectImage**: una o varias imágenes asociadas a un proyecto.
* **Technology**: catálogo de tecnologías.
* **ProjectTechnology**: relación muchos a muchos entre proyectos y tecnologías.
* **ContactMessage**: mensajes enviados desde el formulario de contacto.
* **AuditLog**: registro de acciones relevantes realizadas por el administrador.

Este diseño deja preparada la base para futuras funcionalidades como múltiples administradores, categorías, etiquetas, estadísticas de visitas o internacionalización, sin requerir cambios importantes en la arquitectura. En conjunto, estos requerimientos ofrecen una especificación suficientemente completa para iniciar el análisis, el diseño de la base de datos, la definición de la arquitectura hexagonal, la creación del backlog y la implementación del proyecto siguiendo buenas prácticas de ingeniería de software.
