# Branching Strategy

  Campo       Valor
  ----------- ---------------------------
  Proyecto    Portfolio Manager
  Documento   06-branching-strategy.md
  Versión     1.0
  Fecha       2026-10-16
  Estado      Aprobado

------------------------------------------------------------------------

# Objetivo

Definir la estrategia de ramas, la convención de nombres y el flujo de
integración del proyecto **Portfolio Manager**, en cumplimiento del
requerimiento **RNF017 - Versionamiento**.

------------------------------------------------------------------------

# Estrategia

El proyecto utiliza **GitHub Flow** (variante ligera de Trunk-Based
Development):

-   `main` es la única rama permanente y siempre debe estar estable.
-   Todo cambio se desarrolla en una **rama de corta duración** creada
    desde `main`.
-   Los cambios se integran mediante **Pull Request** con el CI en verde.
-   La rama se elimina automáticamente después del merge; la historia
    queda en `main` y en el Pull Request.

------------------------------------------------------------------------

# Convención de nombres

    <tipo>/<ID>-<descripcion-corta>

Ejemplos:

    feature/HU007-iniciar-sesion
    feature/HT010-documentar-api
    fix/BUG003-token-no-expira
    chore/TASK002-actualizar-dependencias
    docs/branch-naming-convention

## Tipos

  Tipo       Uso                                      Issue asociada
  ---------- ---------------------------------------- -----------------
  feature    Nueva funcionalidad o habilitador         User Story (HU),
                                                       Technical Story
                                                       (HT)
  fix        Corrección de un defecto                  Bug (BUG)
  hotfix     Corrección urgente sobre producción       Bug (BUG)
  refactor   Mejora interna sin cambio funcional       Task (TASK)
  chore      Configuración, dependencias, CI/CD        Task (TASK)
  docs       Solo documentación                        Task (TASK) u
                                                       opcional

## Reglas

1.  El `<ID>` es el identificador del backlog (`HU007`, `HT010`,
    `BUG003`, `TASK002`), no el número de la Issue en GitHub.
2.  El `<ID>` es obligatorio en `feature`, `fix` y `hotfix`; opcional en
    `refactor`, `chore` y `docs` cuando no existe una Issue.
3.  La descripción va en **minúsculas**, separada por guiones
    (kebab-case), de 2 a 5 palabras.
4.  Solo caracteres **ASCII**: sin tildes, sin `ñ`, sin espacios
    (`iniciar-sesion`, no `iniciar-sesión`).
5.  Las ramas de Dependabot (`dependabot/...`) quedan exentas.

La convención se valida automáticamente en cada Pull Request
(workflow `branch-name.yml`).

------------------------------------------------------------------------

# Creación de la rama desde la Issue

Crear la rama desde la Issue la deja enlazada en la sección
**Development** de la tarjeta.

**Web:** Issue → *Development* → **Create a branch**. GitHub propone
`<número>-<título>` (por ejemplo `16-iniciar-sesión`): **editar el
nombre** en ese diálogo antes de confirmar.

**CLI:**

``` bash
gh issue develop <número> --name feature/HU007-iniciar-sesion --base main --checkout
```

------------------------------------------------------------------------

# Commits

Se utiliza **Conventional Commits** con el ID del backlog como scope:

    <tipo>(<ID>): <descripción en inglés, en imperativo>

  Tipo de commit   Uso
  ---------------- ------------------------------------
  feat             Nueva funcionalidad
  fix              Corrección de defecto
  refactor         Cambio interno sin cambio funcional
  test             Pruebas
  docs             Documentación
  chore            Configuración, dependencias
  ci               Pipelines de CI/CD

Ejemplos:

    feat(HU007): add login page with form validation
    fix(BUG003): reject expired refresh tokens

------------------------------------------------------------------------

# Pull Requests

1.  Base: `main` (salvo ramas apiladas, ver abajo).
2.  Título: `<tipo>(<ID>): <título de la Issue>`.
3.  Descripción: incluir `Closes #<número>` para enlazar y cerrar la
    Issue al hacer merge.
4.  El CI debe estar en verde antes del merge.
5.  Merge mediante **Create a merge commit** (no *Squash*), para
    conservar los commits de la rama.
6.  La rama se elimina automáticamente tras el merge
    (*Settings → Automatically delete head branches*).

## Ramas apiladas

Si una historia depende de otra que aún no está en `main`, su rama se
crea desde la rama de la dependencia y su Pull Request apunta a ella.
Al hacer merge de la dependencia, GitHub cambia la base a `main`
automáticamente.

------------------------------------------------------------------------

# Historial de Cambios

  Versión   Fecha        Descripción
  --------- ------------ ----------------------------------------------
  1.0       2026-10-16   Definición de la estrategia de ramas, la
                         convención de nombres y el flujo de Pull
                         Requests.
