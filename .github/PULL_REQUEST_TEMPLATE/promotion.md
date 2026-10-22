<!--
============================================================================
Portfolio Manager
File: .github/PULL_REQUEST_TEMPLATE/promotion.md
Version: 1.2.0
Description: Pull Request Template for the develop -> qa promotion
Author: Erickson Vergara
Repository: https://github.com/ericksonvergara/portfolio-manager
License: MIT
============================================================================

Title: release: promote develop to qa
Base: qa  <-  Compare: develop

Blocks between the "auto:<name>" marks, and the checklist items that can be
verified through the API, are filled by the PR Autofill workflow
(.github/workflows/pr-autofill.yml) when the PR is opened or updated and
when the CI finishes. Boxes ticked by hand stay ticked.
-->

## 📋 Summary

<!-- auto:sprint -->
**Sprint N – <sprint name>**: <sprint goal>
<!-- /auto:sprint -->

Promotes `develop` to `qa`. Every change was already reviewed and merged into `develop` through its own PR:

<!-- auto:prs -->
| PR | Change |
|----|--------|
| #  | **HU###** – |
<!-- /auto:prs -->

---

## 🎯 Objective

<!-- auto:objective -->
Validate in QA <what the sprint delivers> before releasing it to `main` as **vX.Y.Z**.
<!-- /auto:objective -->

---

## 🔗 Related Issues

<!-- Refs, not Closes: the Issues were closed by their PRs to develop (the default branch). -->

<!-- auto:issues -->
Refs #, #
<!-- /auto:issues -->

---

## 📦 Type of Change

<!-- auto:types -->
- [ ] ✨ New Feature
- [ ] 🐞 Bug Fix
- [ ] ♻️ Refactoring
- [ ] 📚 Documentation
- [ ] ⚡ Performance Improvement
- [ ] 🔒 Security
- [ ] 🛠️ Build / DevOps
- [ ] 🧪 Tests
<!-- /auto:types -->

---

## 🧪 QA Plan

What will be tested on `qa` before the release:

<!-- auto:ci -->
- [ ] Smoke test of the full stack (`docker compose up --build`): waiting for the CI run.
- [ ] Postman collection with Newman: waiting for the CI run.
- [ ] API regression of the previous release (Postman): waiting for the CI run.
<!-- /auto:ci -->
- [ ] Acceptance criteria of each story:
  <!-- auto:acceptance -->
  - [ ] HU### –
  <!-- /auto:acceptance -->
- [ ] UI regression of the previous release (login, session renewal, logout in the browser).
- [ ] Responsive check (mobile, tablet, desktop).

Test Evidence:

- CI green on every merged PR (Validate branch name, Backend, Frontend, Smoke Test, Quality Gate).

<!-- Add results, screenshots or logs once QA is done -->

---

## ⚠️ Breaking Changes

<!-- auto:breaking -->
- [ ] This Pull Request introduces breaking changes.
<!-- /auto:breaking -->

Deployment notes (new environment variables, migrations, runtime versions):

<!-- auto:deploy -->
-
<!-- /auto:deploy -->

---

## 📝 Additional Notes

Next step once QA passes: `qa` → `main` as **vX.Y.Z** (template `release.md`).

---

# ✅ Promotion Checklist

- [ ] Every included PR is merged into `develop` with CI green.
- [ ] `develop` contains no unfinished work.
- [ ] Related Issue(s) linked with `Refs`.
- [ ] Cards of the included stories are in **🧪 Testing**.
