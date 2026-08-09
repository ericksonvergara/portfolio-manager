# Issue Management Standard

  Campo       Valor
  ----------- ---------------------------------
  Proyecto    Portfolio Manager
  Documento   05-issue-management-standard.md
  Versión     1.0
  Fecha       2026-08-06
  Estado      Aprobado

------------------------------------------------------------------------

# Objetivo

Definir el estándar oficial para la creación, clasificación,
administración y cierre de todas las Issues del proyecto **Portfolio
Manager**.

Este documento establece las reglas que garantizan consistencia,
trazabilidad y calidad durante todo el ciclo de desarrollo.

------------------------------------------------------------------------

# Alcance

Este estándar aplica a los siguientes tipos de Issues:

-   Epic
-   User Story
-   Technical Story
-   Task
-   Bug

Toda nueva Issue creada en el proyecto deberá cumplir este estándar.

------------------------------------------------------------------------

# Tipos de Issues

  Tipo              Descripción
  ----------------- -----------------------------------------------------------
  Epic              Agrupa funcionalidades relacionadas.
  User Story        Requerimiento funcional del producto.
  Technical Story   Trabajo técnico habilitador.
  Task              Actividad técnica o administrativa.
  Bug               Defecto identificado durante el desarrollo o las pruebas.

------------------------------------------------------------------------

# Convención de nombrado

  Tipo              Formato
  ----------------- ---------------------------------------
  Epic              EP001 - Nombre de la Epic
  User Story        HU001 - Nombre de la Historia
  Technical Story   HT001 - Nombre de la Historia Técnica
  Task              TASK001 - Nombre de la Tarea
  Bug               BUG001 - Descripción del Error

------------------------------------------------------------------------

# Campos obligatorios

Toda Issue deberá tener configurados los siguientes elementos:

  Campo                 Obligatorio
  ----------------- -------------------
  Status                    Sí
  Labels                    Sí
  Milestone                 Sí
  Priority                  Sí
  Sprint                    Sí
  Business Value            Sí
  Complexity                Sí
  Estimated Hours           Sí
  Assignee           Según corresponda

------------------------------------------------------------------------

# Relación entre Labels y Tipos

  Label             Representa
  ----------------- -----------------
  epic              Epic
  user-story        User Story
  technical-story   Technical Story
  task              Task
  bug               Bug

------------------------------------------------------------------------

# Workflow Oficial

1.  📥 Backlog
2.  📋 Ready
3.  🚀 In Progress
4.  👀 Code Review
5.  🧪 Testing
6.  ✅ Done

Toda Issue deberá seguir este flujo de trabajo.

------------------------------------------------------------------------

# Estructura obligatoria

## User Story

-   Descripción
-   Objetivo
-   Criterios de aceptación
-   Dependencias
-   Notas técnicas
-   Definition of Done

## Technical Story

-   Objetivo técnico
-   Criterios de aceptación
-   Dependencias
-   Consideraciones técnicas
-   Definition of Done

## Task

-   Descripción
-   Objetivo
-   Entregables
-   Definition of Done

## Bug

-   Descripción
-   Pasos para reproducir
-   Resultado esperado
-   Resultado actual
-   Evidencias
-   Definition of Done

## Epic

-   Objetivo
-   Alcance
-   Historias asociadas
-   Criterios de finalización

------------------------------------------------------------------------

# Definition of Done

Una Issue únicamente podrá pasar al estado **Done** cuando:

-   Desarrollo completado.
-   Código revisado.
-   Pruebas aprobadas.
-   Documentación actualizada.
-   Merge realizado.
-   Criterios de aceptación cumplidos.

------------------------------------------------------------------------

# Plantillas oficiales

El proyecto utilizará los siguientes Issue Forms:

``` text
.github/
└── ISSUE_TEMPLATE/
    ├── epic.yml
    ├── user-story.yml
    ├── technical-story.yml
    ├── task.yml
    └── bug-report.yml
```

------------------------------------------------------------------------

# Reglas generales

1.  Toda Issue deberá pertenecer a un único Milestone.
2.  Toda Issue deberá tener un Sprint asignado.
3.  Toda Issue deberá tener al menos un Label de tipo.
4.  Toda Issue deberá registrar una estimación de horas.
5.  Ninguna Issue podrá pasar a Done sin cumplir la Definition of Done.

------------------------------------------------------------------------

# Historial de Cambios

  Versión   Fecha        Descripción
  --------- ------------ ---------------------------------------------
  1.0       2026-08-06   Creación del estándar de gestión de Issues.
