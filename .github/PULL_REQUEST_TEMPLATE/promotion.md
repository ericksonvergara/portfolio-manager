<!--
============================================================================
Portfolio Manager
File: .github/PULL_REQUEST_TEMPLATE/promotion.md
Version: 1.0.0
Description: Pull Request Template for the develop -> qa promotion
Author: Erickson Vergara
Repository: https://github.com/ericksonvergara/portfolio-manager
License: MIT
============================================================================

Title: release: promote develop to qa
Base: qa  <-  Compare: develop
-->

## 📋 Summary

Promotes `develop` to `qa` with the work of **Sprint N – <sprint goal>**. Every change was already reviewed and merged into `develop` through its own PR:

<!-- List the merged PRs: git log --merges --format=%s origin/qa..origin/develop -->

| PR | Change |
|----|--------|
| #  | **HU###** – |
| #  | Dependencies (Dependabot) |

---

## 🎯 Objective

Validate in QA <what the sprint delivers> before releasing it to `main` as **vX.Y.Z**.

---

## 🔗 Related Issues

<!-- Refs, not Closes: issues are closed by the qa -> main release. -->

Refs #, #

---

## 📦 Type of Change

- [ ] ✨ New Feature
- [ ] 🐞 Bug Fix
- [ ] ♻️ Refactoring
- [ ] 📚 Documentation
- [ ] ⚡ Performance Improvement
- [ ] 🔒 Security
- [ ] 🛠️ Build / DevOps
- [ ] 🧪 Tests

---

## 🧪 QA Plan

What will be tested on `qa` before the release:

- [ ] Smoke test of the full stack (`docker compose up --build`).
- [ ] Acceptance criteria of each story:
  - [ ] HU### –
- [ ] Postman collection run against the QA environment.
- [ ] Regression of the previous release.

Test Evidence:

- CI green on every merged PR (Validate branch name, Backend, Frontend, Quality Gate).

<!-- Add results, screenshots or logs once QA is done -->

---

## ⚠️ Breaking Changes

- [ ] This Pull Request introduces breaking changes.

Deployment notes (new environment variables, migrations, runtime versions):

---

## 📝 Additional Notes

Next step once QA passes: `qa` → `main` as **vX.Y.Z** (template `release.md`).

---

# ✅ Promotion Checklist

- [ ] Every included PR is merged into `develop` with CI green.
- [ ] `develop` contains no unfinished work.
- [ ] Related Issue(s) linked with `Refs`.
- [ ] Cards of the included stories are in **🧪 Testing**.
