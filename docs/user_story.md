
# Epic 1 - Landing Page

## HU001 - Visualizar Landing Page

**Como** visitante
**Quiero** acceder a la Landing Page sin autenticación
**Para** conocer el perfil profesional y los proyectos publicados.

### Criterios de aceptación

* El sitio es accesible sin iniciar sesión.
* Se muestra presentación personal.
* Se muestra fotografía.
* Se muestran tecnologías.
* Se muestran redes sociales.
* Se muestra información de contacto.

---

## HU002 - Visualizar proyectos

**Como** visitante
**Quiero** visualizar los proyectos publicados
**Para** conocer el portafolio disponible.

### Criterios

* Solo aparecen proyectos activos.
* Cada tarjeta muestra:

  * imagen
  * nombre
  * descripción corta
  * tecnologías
  * botón Ver Proyecto
  * botón Repositorio

---

## HU003 - Consultar detalle del proyecto

**Como** visitante
**Quiero** visualizar el detalle de un proyecto
**Para** conocer toda su información.

### Criterios

* Mostrar:

  * nombre
  * descripción completa
  * galería
  * tecnologías
  * fecha
  * estado
  * URL
  * repositorio
  * video (opcional)

---

## HU004 - Buscar proyectos

**Como** visitante
**Quiero** buscar proyectos por nombre o tecnología
**Para** encontrarlos rápidamente.

---

## HU005 - Filtrar proyectos

**Como** visitante
**Quiero** filtrar proyectos por categoría tecnológica
**Para** visualizar únicamente los que me interesan.

Filtros:

* Backend
* Frontend
* Full Stack
* Mobile
* DevOps
* IA

---

## HU006 - Enviar mensaje de contacto

**Como** visitante
**Quiero** enviar un mensaje desde el formulario de contacto
**Para** comunicarme con el administrador.

---

# Epic 2 - Autenticación

## HU007 - Iniciar sesión

**Como** administrador
**Quiero** iniciar sesión mediante correo y contraseña
**Para** acceder al panel administrativo.

### Validaciones

* correo válido
* contraseña segura
* autenticación JWT

---

## HU008 - Gestionar sesión

**Como** administrador
**Quiero** mantener mi sesión activa mientras el token sea válido
**Para** no autenticarme constantemente.

---

## HU009 - Cerrar sesión

**Como** administrador
**Quiero** cerrar mi sesión
**Para** proteger mi cuenta.

---

## HU010 - Recuperar contraseña

**Como** administrador
**Quiero** recuperar mi contraseña mediante correo electrónico
**Para** volver a acceder al sistema.

---

## HU011 - Cambiar contraseña

**Como** administrador
**Quiero** cambiar mi contraseña
**Para** mantener la seguridad de mi cuenta.

---

# Epic 3 - Dashboard

## HU012 - Visualizar Dashboard

**Como** administrador
**Quiero** visualizar indicadores generales
**Para** conocer el estado del portafolio.

Indicadores:

* Total proyectos
* Tecnologías
* Último proyecto
* Último acceso

---

## HU013 - Visualizar estadísticas de visitas

**Como** administrador
**Quiero** consultar estadísticas de visitas
**Para** medir el uso del sitio.

Indicadores

* visitantes
* visitas del día
* visitas del mes
* proyecto más visto
* tecnologías
* último proyecto

---

## HU014 - Visualizar ranking de proyectos

**Como** administrador
**Quiero** visualizar los proyectos más visitados
**Para** conocer cuáles generan mayor interés.

---

# Epic 4 - Gestión de Proyectos

## HU015 - Registrar proyecto

**Como** administrador
**Quiero** crear un nuevo proyecto
**Para** publicarlo en el portafolio.

Incluye

* nombre
* descripción corta
* descripción completa
* imágenes
* tecnologías
* URLs
* estado
* fecha
* orden
* destacado

---

## HU016 - Editar proyecto

**Como** administrador
**Quiero** modificar la información de un proyecto
**Para** mantenerla actualizada.

---

## HU017 - Eliminar proyecto

**Como** administrador
**Quiero** eliminar un proyecto
**Para** retirar información obsoleta.

Debe solicitar confirmación.

---

## HU018 - Activar o desactivar proyecto

**Como** administrador
**Quiero** cambiar el estado de un proyecto
**Para** ocultarlo sin eliminarlo.

---

## HU019 - Gestionar imágenes

**Como** administrador
**Quiero** cargar imágenes para un proyecto
**Para** mostrar contenido visual.

Validaciones

* JPG
* JPEG
* PNG
* tamaño máximo
* MIME
* nombre único

---

## HU020 - Visualizar vista previa

**Como** administrador
**Quiero** ver la imagen antes de guardar
**Para** verificar que sea la correcta.

---

# Epic 5 - Tecnologías

## HU021 - Administrar tecnologías

**Como** administrador
**Quiero** crear, editar y eliminar tecnologías
**Para** mantener actualizado el catálogo.

---

## HU022 - Asociar tecnologías

**Como** administrador
**Quiero** asociar múltiples tecnologías a un proyecto
**Para** describir correctamente el stack utilizado.

---

# Epic 6 - Perfil

## HU023 - Actualizar perfil

**Como** administrador
**Quiero** modificar mi información personal
**Para** mantenerla actualizada.

Incluye

* nombre
* fotografía
* correo

---

# Epic 7 - Auditoría

## HU024 - Registrar auditoría

**Como** sistema
**Quiero** registrar las operaciones críticas
**Para** mantener trazabilidad.

Registrar

* login
* creación
* edición
* eliminación
* cambio de contraseña

---

# Epic 8 - Analítica de Visitas

## HU025 - Registrar visitas

**Como** sistema
**Quiero** registrar automáticamente cada visita
**Para** generar estadísticas.

Registrar

* fecha
* IP anonimizada
* navegador
* dispositivo
* sistema operativo
* país
* proyecto

---

## HU026 - Contabilizar visitas al proyecto

**Como** sistema
**Quiero** incrementar el contador al abrir el detalle del proyecto
**Para** medir su popularidad.

---

## HU027 - Evitar duplicidad de visitas

**Como** sistema
**Quiero** evitar múltiples registros por recargas consecutivas
**Para** mantener estadísticas confiables.

---

# Epic 9 - Reglas de Negocio

## HU028 - Validar unicidad del proyecto

**Como** sistema
**Quiero** impedir proyectos con el mismo nombre
**Para** mantener la integridad de la información.

---

## HU029 - Validar URLs

**Como** sistema
**Quiero** validar las URLs del proyecto y del repositorio
**Para** evitar enlaces inválidos.

---

## HU030 - Validar publicación

**Como** sistema
**Quiero** impedir publicar proyectos sin imagen principal o sin tecnologías asociadas
**Para** garantizar la calidad del contenido.

---

# Resumen del Backlog

| Epic                 |                   Historias |
| -------------------- | --------------------------: |
| Landing Page         |                           6 |
| Autenticación        |                           5 |
| Dashboard            |                           3 |
| Gestión de Proyectos |                           6 |
| Tecnologías          |                           2 |
| Perfil               |                           1 |
| Auditoría            |                           1 |
| Analítica de Visitas |                           3 |
| Reglas de Negocio    |                           3 |
| **Total**            | **30 Historias de Usuario** |

Este backlog cubre los requerimientos funcionales definidos en la especificación y considera las principales reglas de negocio del proyecto, dejando una base adecuada para planificar sprints e implementar la solución de forma incremental. 
