

## Sprint 1 - Preparación del proyecto y arquitectura

**Objetivo:** dejar lista la infraestructura, arquitectura y entorno de desarrollo.

### Historias técnicas

* HT001 - Configurar arquitectura hexagonal.
* HT002 - Aplicar SOLID y Clean Code.
* HT003 - Configurar Docker Compose.
* HT004 - Configurar variables de entorno.
* HT006 - Configurar PostgreSQL.
* HT007 - Configurar Flyway.
* HT013 - Configurar sistema de logs.

### Actividades

#### Backend

* Crear proyecto Spring Boot.
* Configurar Gradle.
* Implementar estructura Hexagonal.
* Configurar PostgreSQL.
* Crear migración inicial.
* Configurar perfiles.
* Configurar Lombok y MapStruct.

#### Frontend

* Crear proyecto React + Vite.
* Configurar React Router.
* Definir estructura base.
* Configurar Axios.
* Configurar Material UI.

#### DevOps

* Docker Compose.
* Variables .env.
* Repositorio Git.
* Estrategia de ramas.

**Entregable**

* Proyecto ejecutándose con `docker compose up`.

---

# Sprint 2 - Seguridad y autenticación

**Objetivo:** habilitar el acceso seguro al panel administrativo.

### Historias funcionales

* HU007
* HU008
* HU009

### Historias técnicas

* HT008
* HT009
* HT010

### Actividades

#### Backend

* Entidad User.
* Entidad Role.
* Login JWT.
* Refresh de sesión.
* Logout.
* BCrypt.
* Swagger.

#### Frontend

* Pantalla Login.
* Contexto de autenticación.
* Protección de rutas.
* Manejo de sesión.

**Entregable**

* Administrador autenticado.

---

# Sprint 3 - Landing Page

**Objetivo:** publicar el sitio público.

### Historias

* HU001
* HU002
* HU003

### Actividades

#### Backend

* API pública de proyectos.
* Consulta detalle.
* DTOs.

#### Frontend

* Landing.
* Sobre mí.
* Tecnologías.
* Portafolio.
* Contacto visual.
* Detalle del proyecto.

**Entregable**

* Landing completamente navegable.

---

# Sprint 4 - Administración de proyectos

**Objetivo:** administrar el contenido del portafolio.

### Historias

* HU015
* HU016
* HU017
* HU018
* HU019
* HU020

### Actividades

#### Backend

* CRUD Proyecto.
* CRUD imágenes.
* Validaciones.
* Upload.

#### Frontend

* Dashboard.
* Tabla.
* Formulario.
* Vista previa.
* Activar/Inactivar.

**Entregable**

* CRUD completo de proyectos.

---

# Sprint 5 - Tecnologías y perfil

### Historias

* HU021
* HU022
* HU023
* HU010
* HU011

### Actividades

#### Backend

* CRUD Tecnologías.
* Asociación Proyecto-Tecnología.
* Perfil.
* Cambio contraseña.

#### Frontend

* Gestión Tecnologías.
* Perfil.
* Cambio contraseña.

**Entregable**

* Administración completa del catálogo.

---

# Sprint 6 - Contacto, búsqueda y reglas

### Historias

* HU004
* HU005
* HU006
* HU028
* HU029
* HU030

### Actividades

#### Backend

* Buscador.
* Filtros.
* Contacto.
* Validaciones.

#### Frontend

* Barra búsqueda.
* Filtros.
* Formulario contacto.

**Entregable**

* Navegación completa del portafolio.

---

# Sprint 7 - Dashboard y analítica

### Historias

* HU012
* HU013
* HU014
* HU024
* HU025
* HU026
* HU027

### Historias técnicas

* HT014
* HT015

### Actividades

#### Backend

* Registro de visitas.
* Auditoría.
* Estadísticas.
* Ranking.
* Optimización consultas.

#### Frontend

* Dashboard.
* Tarjetas KPI.
* Gráficas.
* Ranking.

**Entregable**

* Dashboard analítico funcionando.

---

# Sprint 8 - Calidad, pruebas y despliegue

### Historias técnicas

* HT011
* HT012
* HT016
* HT017
* HT018

### Actividades

#### Calidad

* Pruebas unitarias.
* Mockito.
* Cobertura.

#### DevOps

* SonarQube.
* Pipeline CI.
* Pipeline CD.
* Docker Compose producción.
* Validaciones finales.

#### QA

* Pruebas funcionales.
* Pruebas de regresión.
* Corrección de incidencias.

**Entregable**

* Aplicación lista para producción.

---

# Resumen de planificación

| Sprint | Objetivo                       | HU | HT |
| ------ | ------------------------------ | -: | -: |
| 1      | Arquitectura e infraestructura |  0 |  7 |
| 2      | Autenticación                  |  3 |  3 |
| 3      | Landing Page                   |  3 |  0 |
| 4      | Gestión de proyectos           |  6 |  0 |
| 5      | Tecnologías y perfil           |  4 |  0 |
| 6      | Contacto, búsqueda y reglas    |  6 |  0 |
| 7      | Dashboard y analítica          |  7 |  2 |
| 8      | Calidad, QA y despliegue       |  0 |  5 |

## Observaciones

* La **QA** y el **despliegue** se incluyen principalmente en el **Sprint 8**, pero es recomendable realizar pruebas funcionales y de integración al cierre de cada sprint para detectar problemas de forma temprana.
* Las historias técnicas de arquitectura, infraestructura y seguridad se implementan al inicio porque habilitan el desarrollo de las funcionalidades posteriores.
* La planificación mantiene una progresión lógica: primero se construye la plataforma, luego las funcionalidades públicas, después la administración y finalmente la analítica, optimización y salida a producción.
