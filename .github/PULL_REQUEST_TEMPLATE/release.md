<!--
============================================================================
Portfolio Manager
File: .github/PULL_REQUEST_TEMPLATE/release.md
Version: 1.1.0
Description: Pull Request Template for the qa -> main release
Author: Erickson Vergara
Repository: https://github.com/ericksonvergara/portfolio-manager
License: MIT
============================================================================

Title: release: vX.Y.Z
Base: main  <-  Compare: qa
-->

## 📋 Summary

Release **vX.Y.Z** – Sprint N: <sprint goal>.

<!-- What users get in this version, one line per story. -->

- **<Feature> (HU###, #PR):**

Also included:

-

Validated in `qa` through #<!-- PR number of develop -> qa -->.

---

## 🎯 Objective

Release <the sprint goal> to `main`. The work was validated in QA and is ready for production.

---

## 🔗 Related Issues

<!-- Refs, not Closes: the Issues were closed by their PRs to develop (the default branch). -->

Refs #, #

Promotion to QA: #

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

## 🧪 QA Validation

- [ ] Smoke test of the full stack on `qa`.
- [ ] Acceptance criteria of every story verified.
- [ ] Postman collection: requests, assertions, 0 failures.
- [ ] Regression of the previous release.

Test Evidence:

<!-- Results, screenshots or logs from QA -->

---

## ⚠️ Breaking Changes

- [ ] This Pull Request introduces breaking changes.

Deployment notes (new environment variables, migrations, runtime versions):

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
