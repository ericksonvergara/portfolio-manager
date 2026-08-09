# Labels Configuration

  Campo       Valor
  ----------- -------------------
  Proyecto    Portfolio Manager
  Documento   02-labels.md
  Versión     1.0
  Fecha       2026-08-06
  Estado      Aprobado

------------------------------------------------------------------------

# Objetivo

Documentar el catálogo oficial de **Labels** utilizado para clasificar
las Issues del proyecto **Portfolio Manager**.

## 1. Tipo de trabajo

  Label             Descripción
  ----------------- --------------------------------------
  epic              Agrupa funcionalidades relacionadas.
  user-story        Historia funcional del producto.
  technical-story   Historia técnica o habilitadora.
  bug               Defecto identificado.
  task              Tarea técnica o administrativa.

## 2. Área

  Label           Descripción
  --------------- ------------------------------------------
  backend         Desarrollo Backend (Spring Boot).
  frontend        Desarrollo Frontend (React).
  database        Base de datos y migraciones.
  security        Seguridad, autenticación y autorización.
  devops          Infraestructura, Docker y CI/CD.
  qa              Calidad y pruebas.
  documentation   Documentación técnica y funcional.

## 3. Prioridad

  Label      Descripción
  ---------- --------------------------------------------------
  critical   Bloquea el proyecto o una funcionalidad crítica.
  high       Alta prioridad.
  medium     Prioridad normal.
  low        Puede esperar.

## 4. Estado especial

  -----------------------------------------------------------------------
  Label                       Descripción
  --------------------------- -------------------------------------------
  blocked                     La Issue no puede continuar por una
                              dependencia o impedimento.

  needs-review                Requiere una revisión adicional fuera del
                              flujo normal.
  -----------------------------------------------------------------------

------------------------------------------------------------------------

# Reglas de uso

-   Cada Issue debe tener una label de **Tipo** y una de **Área**.
-   La prioridad es opcional pero recomendada.
-   `blocked` y `needs-review` solo se usan cuando corresponda.
-   El estado se administra mediante el **Workflow**, no mediante
    labels.
-   El Sprint se administra mediante **Milestones**.

------------------------------------------------------------------------

# Resumen

  Categoría           Cantidad
  ----------------- ----------
  Tipo                       5
  Área                       7
  Prioridad                  4
  Estado especial            2
  **Total**             **18**

------------------------------------------------------------------------

# Historial de cambios

  Versión   Fecha        Descripción
  --------- ------------ ------------------------------------------
  1.0       2026-08-06   Creación del catálogo oficial de Labels.
