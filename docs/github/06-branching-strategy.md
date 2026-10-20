# Branching Strategy

  Campo       Valor
  ----------- ---------------------------
  Proyecto    Portfolio Manager
  Documento   06-branching-strategy.md
  Versión     1.3
  Fecha       2026-10-20
  Estado      Aprobado

------------------------------------------------------------------------

# Objetivo

Definir la estrategia de ramas, la convención de nombres y el flujo de
integración del proyecto **Portfolio Manager**, en cumplimiento del
requerimiento **RNF017 - Versionamiento**.

------------------------------------------------------------------------

# Estrategia

El proyecto utiliza un **flujo de promoción por entornos** (variante
simplificada de GitFlow) con tres ramas permanentes:

  Rama      Entorno       Contenido
  --------- ------------- ----------------------------------------------
  develop   Integración   Historias terminadas y revisadas. Base de todo
                          el trabajo diario.
  qa        Pruebas       Versión candidata en pruebas funcionales y de
                          regresión.
  main      Producción    Solo código validado en qa. Siempre estable.

    feature/HU007-...  ──PR──▶  develop  ──PR──▶  qa  ──PR──▶  main
    fix/BUG003-...     ──PR──▶  develop
    hotfix/BUG005-...  ──PR──▶  main  ──PR──▶  qa / develop (sincronizar)

## Reglas del flujo

1.  Las ramas de trabajo (`feature`, `fix`, `refactor`, `chore`, `docs`)
    se crean **desde `develop`** y vuelven a `develop` mediante Pull
    Request.
2.  `develop` se promueve a `qa` mediante un Pull Request
    `develop → qa` cuando hay un conjunto de historias listo para
    probar.
3.  `qa` se promueve a `main` mediante un Pull Request `qa → main`
    (release) cuando las pruebas son exitosas.
4.  Un `hotfix` se crea **desde `main`**, vuelve a `main` y después se
    sincroniza con Pull Requests `main → qa` y `main → develop`.
5.  Nadie hace push directo a `develop`, `qa` ni `main`.

El flujo se valida automáticamente en cada Pull Request (workflow
`branch-name.yml`): por ejemplo, un Pull Request `feature/... → main` es
rechazado.

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

  Tipo       Uso                                Origen    Destino
  ---------- ---------------------------------- --------- ---------
  feature    Nueva funcionalidad (HU, HT)       develop   develop
  fix        Corrección de un defecto (BUG)     develop   develop
  hotfix     Corrección urgente en producción   main      main
  refactor   Mejora interna sin cambio          develop   develop
             funcional
  chore      Configuración, dependencias,       develop   develop
             CI/CD
  docs       Solo documentación                 develop   develop

## Reglas

1.  El `<ID>` es el identificador del backlog (`HU007`, `HT010`,
    `BUG003`, `TASK002`), no el número de la Issue en GitHub.
2.  El `<ID>` es obligatorio en `feature`, `fix` y `hotfix`; opcional en
    `refactor`, `chore` y `docs` cuando no existe una Issue.
3.  La descripción va en **minúsculas**, separada por guiones
    (kebab-case), de 2 a 5 palabras.
4.  Solo caracteres **ASCII**: sin tildes, sin `ñ`, sin espacios
    (`iniciar-sesion`, no `iniciar-sesión`).
5.  Las ramas de Dependabot (`dependabot/...`) quedan exentas y apuntan
    a `develop`.

------------------------------------------------------------------------

# Creación de la rama desde la Issue

Crear la rama desde la Issue la deja enlazada en la sección
**Development** de la tarjeta.

**Web:** Issue → *Development* → **Create a branch**:

-   **Branch source:** cambiar a `develop` (`main` solo para `hotfix`).
-   **Nombre:** GitHub propone `<número>-<título>` (por ejemplo
    `16-iniciar-sesión`): **editarlo** antes de confirmar.

**CLI:**

``` bash
gh issue develop <número> --name feature/HU007-iniciar-sesion --base develop --checkout
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

## Rama de trabajo → develop

1.  Base: `develop`.
2.  Título: `<tipo>(<ID>): <título de la Issue>`.
3.  Descripción: `Closes #<número>`. Al hacer merge la Issue se cierra
    y la tarjeta pasa a **🧪 Testing** (ver *Cierre de Issues y
    tablero*).

## develop → qa

-   Título: `release: promote develop to qa`.
-   Descripción: plantilla `promotion.md`, con la lista de Pull
    Requests e historias incluidas (`Refs #…`) y el plan de pruebas en
    QA.
-   Abrir con la plantilla:
    <https://github.com/ericksonvergara/portfolio-manager/compare/qa...develop?quick_pull=1&template=promotion.md&title=release:+promote+develop+to+qa>

## qa → main (release)

-   Título: `release: vX.Y.Z`.
-   Descripción: plantilla `release.md`, con `Refs #<número>` por cada
    historia incluida y la evidencia de QA. Las Issues ya están
    cerradas desde su merge en `develop`.
-   Tras el merge: crear el tag `vX.Y.Z` y mover las tarjetas incluidas
    de **🧪 Testing** a **✅ Done**.
-   Abrir con la plantilla (cambiar `vX.Y.Z` en el título):
    <https://github.com/ericksonvergara/portfolio-manager/compare/main...qa?quick_pull=1&template=release.md&title=release:+vX.Y.Z>

## Cierre de Issues y tablero

`develop` es la rama por defecto del repositorio, y GitHub cierra una
Issue cuando un Pull Request enlazado se mergea en la rama por defecto.
El enlace se crea con `Closes #<número>` en la descripción o al crear
la rama desde la Issue (*Development*). Por eso:

  Merge                   Issue     Tarjeta
  ----------------------- --------- -------------------------
  trabajo → develop       Cerrada   🧪 Testing (automático)
  develop → qa            Cerrada   🧪 Testing
  qa → main (release)     Cerrada   ✅ Done (manual)

Para que una Issue cerrada no pase directamente a Done, el workflow
integrado del GitHub Project **Item closed** (*Project → ⋯ →
Workflows*) fija el estado **🧪 Testing** en lugar de **✅ Done**. Las
promociones y el release usan `Refs`, nunca `Closes`.

Un `hotfix` se mergea en `main`, que no es la rama por defecto: su Issue
no se cierra sola y se cierra manualmente tras el merge.

## Plantillas de Pull Request

  Plantilla                                      Uso
  ---------------------------------------------- ------------------------
  `.github/pull_request_template.md`             Por defecto: ramas de
                                                 trabajo → develop.
  `.github/PULL_REQUEST_TEMPLATE/promotion.md`   develop → qa.
  `.github/PULL_REQUEST_TEMPLATE/release.md`     qa → main.

GitHub solo aplica una plantilla de la carpeta `PULL_REQUEST_TEMPLATE/`
cuando la URL del Pull Request incluye `template=<archivo>`; por eso
las promociones se abren con los enlaces anteriores.

## Reglas comunes

1.  El CI y la validación de ramas deben estar en verde antes del merge.
2.  Merge mediante **Create a merge commit** (no *Squash*), para
    conservar los commits y que `develop`, `qa` y `main` compartan
    historia.
3.  Las ramas de trabajo se eliminan automáticamente tras el merge
    (*Settings → Automatically delete head branches*). `develop` y `qa`
    no se eliminan porque están protegidas con *Restrict deletions*.

## Ramas apiladas

Si una historia depende de otra que aún no está en `develop`, su rama se
crea desde la rama de la dependencia y su Pull Request apunta a ella.
Al hacer merge de la dependencia, GitHub cambia la base a `develop`
automáticamente.

------------------------------------------------------------------------

# Protección de ramas

Ruleset **Protect long-lived branches** (*Settings → Rules →
Rulesets*), con objetivo **solo** `main`, `qa` y `develop` (nunca *All
branches*):

-   Restrict deletions.
-   Block force pushes.
-   Require a pull request before merging (0 aprobaciones; método
    *Merge*).
-   Require status checks to pass: `Validate branch name` y
    `Quality Gate`.

------------------------------------------------------------------------

# Historial de Cambios

  Versión   Fecha        Descripción
  --------- ------------ ----------------------------------------------
  1.0       2026-10-16   Definición de la estrategia de ramas, la
                         convención de nombres y el flujo de Pull
                         Requests.
  1.1       2026-10-16   Ramas permanentes develop y qa, flujo de
                         promoción develop → qa → main, hotfix y
                         protección de ramas.
  1.2       2026-10-19   Plantillas de Pull Request para las
                         promociones develop → qa y qa → main.
  1.3       2026-10-20   Cierre de Issues al mergear en develop (rama
                         por defecto); las tarjetas pasan a Done en el
                         release.
