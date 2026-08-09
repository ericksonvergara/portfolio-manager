# Custom Fields Configuration

  Campo       Valor
  ----------- ---------------------
  Proyecto    Portfolio Manager
  Documento   04-custom-fields.md
  Versión     1.0
  Fecha       2026-08-06
  Estado      Aprobado

------------------------------------------------------------------------

# Objetivo

Documentar la configuración oficial de los **Custom Fields** utilizados
en GitHub Projects para la planificación, seguimiento y control del
proyecto **Portfolio Manager**.

------------------------------------------------------------------------

# Principios de diseño

Los campos personalizados fueron definidos con los siguientes objetivos:

-   Evitar información duplicada.
-   Registrar únicamente información útil para la gestión del proyecto.
-   Facilitar filtros, vistas y reportes.
-   Mantener una planificación sencilla y consistente.

------------------------------------------------------------------------

# Campos configurados

## 1. Priority

**Tipo:** Single Select

  -----------------------------------------------------------------------
  Valor                       Descripción
  --------------------------- -------------------------------------------
  Critical                    Impide continuar el proyecto o requiere
                              atención inmediata.

  High                        Funcionalidad prioritaria para el Sprint
                              actual.

  Medium                      Prioridad normal.

  Low                         Puede implementarse posteriormente.
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 2. Sprint

**Tipo:** Single Select

Valores:

-   Sprint 1
-   Sprint 2
-   Sprint 3
-   Sprint 4
-   Sprint 5
-   Sprint 6
-   Sprint 7
-   Sprint 8

------------------------------------------------------------------------

## 3. Business Value

**Tipo:** Single Select

  Valor    Significado
  -------- ------------------------------------------
  High     Alto valor para el usuario o el negocio.
  Medium   Valor funcional importante.
  Low      Mejora o funcionalidad de bajo impacto.

------------------------------------------------------------------------

## 4. Complexity

**Tipo:** Single Select

  Valor    Descripción
  -------- --------------------------------------------------------
  Low      Implementación sencilla y bajo riesgo técnico.
  Medium   Complejidad moderada con integraciones o validaciones.
  High     Alta complejidad técnica o arquitectónica.

------------------------------------------------------------------------

## 5. Estimated Hours

**Tipo:** Number

Representa la estimación oficial en horas para cada Issue. La estimación
detallada por perfil (Junior, Semi Senior y Senior) se mantiene en la
planificación del proyecto y no en GitHub.

------------------------------------------------------------------------

# Relación entre los campos

  Campo             Pregunta que responde
  ----------------- ------------------------------------
  Priority          ¿Qué debemos hacer primero?
  Business Value    ¿Qué aporta más valor al producto?
  Complexity        ¿Qué tan difícil es implementarlo?
  Estimated Hours   ¿Cuánto tiempo estimamos invertir?
  Sprint            ¿En qué Sprint se desarrollará?

------------------------------------------------------------------------

# Reglas de uso

1.  Toda Issue deberá tener asignados estos cinco campos antes de
    iniciar su desarrollo.
2.  Priority y Business Value representan conceptos diferentes y no
    deben utilizarse como sinónimos.
3.  Complexity evalúa el reto técnico, no el esfuerzo.
4.  Estimated Hours será la referencia oficial para el seguimiento del
    proyecto.
5.  Sprint deberá coincidir con el Milestone asignado a la Issue.

------------------------------------------------------------------------

# Estado de la Configuración

  Configuración     Estado
  ----------------- --------
  Priority          ✅
  Sprint            ✅
  Business Value    ✅
  Complexity        ✅
  Estimated Hours   ✅

------------------------------------------------------------------------

# Historial de Cambios

  ------------------------------------------------------------------------
  Versión                Fecha             Descripción
  ---------------------- ----------------- -------------------------------
  1.0                    2026-08-06        Creación del documento de
                                           configuración de Custom Fields.

  ------------------------------------------------------------------------
