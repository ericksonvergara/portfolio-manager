
---

# Epic Técnico 1 - Arquitectura

## HT001 - Configurar la arquitectura del proyecto

**Como** equipo de desarrollo
**Quiero** estructurar el backend utilizando Arquitectura Hexagonal
**Para** garantizar una separación adecuada de responsabilidades y facilitar el mantenimiento.

### Criterios de aceptación

* Estructura mínima:

  * application
  * domain
  * infrastructure
  * shared
* Dependencias orientadas al dominio.
* Casos de uso desacoplados de la infraestructura.

---

## HT002 - Aplicar principios SOLID y Clean Code

**Como** equipo de desarrollo
**Quiero** desarrollar el código siguiendo SOLID y Clean Code
**Para** mejorar la mantenibilidad y escalabilidad del sistema.

---

# Epic Técnico 2 - Infraestructura

## HT003 - Configurar Docker Compose

**Como** equipo de desarrollo
**Quiero** ejecutar toda la solución mediante Docker Compose
**Para** facilitar el despliegue y la portabilidad.

### Servicios

* frontend
* backend
* postgres
* nginx

---

## HT004 - Configurar variables de entorno

**Como** equipo de desarrollo
**Quiero** administrar la configuración mediante archivos `.env`
**Para** evitar credenciales en el código fuente.

---

## HT005 - Configurar Nginx

**Como** equipo de desarrollo
**Quiero** utilizar Nginx como Reverse Proxy
**Para** centralizar el acceso a los servicios.

---

# Epic Técnico 3 - Persistencia

## HT006 - Configurar PostgreSQL

**Como** equipo de desarrollo
**Quiero** integrar PostgreSQL como motor de base de datos
**Para** almacenar la información del sistema.

---

## HT007 - Configurar Flyway

**Como** equipo de desarrollo
**Quiero** administrar las migraciones mediante Flyway
**Para** controlar la evolución de la base de datos.

---

# Epic Técnico 4 - Seguridad

## HT008 - Configurar Spring Security

**Como** equipo de desarrollo
**Quiero** proteger la API mediante Spring Security
**Para** restringir el acceso a los recursos protegidos.

---

## HT009 - Implementar autenticación JWT

**Como** equipo de desarrollo
**Quiero** implementar autenticación basada en JWT
**Para** garantizar sesiones seguras.

---

# Epic Técnico 5 - API

## HT010 - Documentar la API

**Como** equipo de desarrollo
**Quiero** documentar la API utilizando OpenAPI/Swagger
**Para** facilitar su consumo y mantenimiento.

---

# Epic Técnico 6 - Calidad

## HT011 - Implementar pruebas unitarias

**Como** equipo de desarrollo
**Quiero** desarrollar pruebas unitarias para la lógica de negocio
**Para** garantizar el correcto funcionamiento de los casos críticos.

### Criterios

* Uso de JUnit 5.
* Uso de Mockito.
* Cobertura mínima recomendada del 80% para casos de negocio críticos.

---

## HT012 - Configurar análisis de calidad

**Como** equipo de desarrollo
**Quiero** integrar SonarQube
**Para** detectar vulnerabilidades, duplicidad y problemas de calidad.

---

## HT013 - Configurar registro de logs

**Como** equipo de desarrollo
**Quiero** registrar errores, advertencias y accesos
**Para** facilitar el monitoreo y diagnóstico de la aplicación.

---

# Epic Técnico 7 - Rendimiento

## HT014 - Optimizar consultas del Dashboard

**Como** equipo de desarrollo
**Quiero** optimizar las consultas e índices de PostgreSQL
**Para** garantizar tiempos de respuesta inferiores a dos segundos.

---

## HT015 - Optimizar el registro de visitas

**Como** equipo de desarrollo
**Quiero** registrar visitas sin afectar el rendimiento de la aplicación
**Para** mantener una buena experiencia de usuario.

---

# Epic Técnico 8 - CI/CD y Despliegue

> **Nota:** Estas historias no aparecen explícitamente como requerimientos funcionales en la especificación, pero son una descomposición natural de los RNF relacionados con despliegue y calidad. 

## HT016 - Automatizar la construcción del proyecto

**Como** equipo de desarrollo
**Quiero** ejecutar automáticamente la compilación del backend y frontend
**Para** detectar errores de integración de forma temprana.

---

## HT017 - Automatizar la ejecución de pruebas

**Como** equipo de desarrollo
**Quiero** ejecutar automáticamente las pruebas unitarias en cada integración
**Para** asegurar la estabilidad del sistema.

---

## HT018 - Automatizar el despliegue

**Como** equipo de desarrollo
**Quiero** desplegar la aplicación mediante un proceso automatizado utilizando Docker Compose
**Para** reducir errores manuales y facilitar la puesta en producción.

---

# Resumen del Backlog Técnico

| Epic Técnico       |                 Historias |
| ------------------ | ------------------------: |
| Arquitectura       |                         2 |
| Infraestructura    |                         3 |
| Persistencia       |                         2 |
| Seguridad          |                         2 |
| API                |                         1 |
| Calidad            |                         3 |
| Rendimiento        |                         2 |
| CI/CD y Despliegue |                         3 |
| **Total**          | **18 Historias Técnicas** |


* **Backlog Técnico:** 18 Historias Técnicas (Enablers) orientadas a arquitectura, infraestructura, calidad y despliegue.

