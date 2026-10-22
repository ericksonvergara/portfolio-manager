<!--
============================================================================
Portfolio Manager
File: .github/PULL_REQUEST_TEMPLATE/release.md
Version: 1.3.0
Description: Pull Request Template for the qa -> main release
Author: Erickson Vergara
Repository: https://github.com/ericksonvergara/portfolio-manager
License: MIT
============================================================================

Title: release: vX.Y.Z
Base: main  <-  Compare: qa

Blocks between the "auto:<name>" marks, and the checklist items that can be
verified through the API, are filled by the PR Autofill workflow
(.github/workflows/pr-autofill.yml) when the PR is opened or updated and
when the CI finishes. Boxes ticked by hand stay ticked.
-->

## 📋 Summary

Release **vX.Y.Z**:

<!-- auto:sprint -->
**Sprint N – <sprint name>**: <sprint goal>
<!-- /auto:sprint -->

<!-- What users get in this version, one line per story: review the wording. -->

<!-- auto:stories -->
- **<Feature> (HU###, #PR):**
<!-- /auto:stories -->

<!-- auto:promotion -->
Validated in `qa` through #
<!-- /auto:promotion -->

---

## 🎯 Objective

<!-- auto:objective -->
Release <the sprint goal> to `main`. The work was validated in QA and is ready for production.
<!-- /auto:objective -->

---

## 🔗 Related Issues

<!-- Refs, not Closes: the Issues were closed by their PRs to develop (the default branch). -->

<!-- auto:issues -->
Refs #, #

Promotion to QA: #
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

## 🧪 QA Validation

<!-- auto:ci -->
- [ ] Smoke test of the full stack (`docker compose up --build`): waiting for the CI run.
- [ ] Postman collection with Newman: waiting for the CI run.
- [ ] API regression of the previous release (Postman): waiting for the CI run.
<!-- /auto:ci -->
- [ ] Acceptance criteria of every story verified:
  <!-- auto:acceptance -->
  - [ ] HU### –
  <!-- /auto:acceptance -->
- [ ] UI regression of the previous release (login, session renewal, logout in the browser).

Test Evidence:

<!-- Results, screenshots or logs from QA -->

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

After merging:

1. Create the annotated tag **vX.Y.Z** on `main` and push it; `release.yml` creates the GitHub release.
2. Move the cards of the included stories from **🧪 Testing** to **✅ Done**.
3. Close the sprint in the GitHub Project.

---

# ✅ Release Checklist

- [ ] QA passed and its evidence is attached above.
- [ ] `qa` is identical to what was tested (no merges after QA).
- [ ] No known critical defects remain.
- [ ] Every included story is linked with `Refs #`.
- [ ] Version number decided (vX.Y.Z).
