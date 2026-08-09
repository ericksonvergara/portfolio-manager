# Portfolio Manager - Backlog Master

## Overview

The **Backlog Master** is the single source of truth for the Portfolio Manager project.

It centralizes all planning artifacts required to generate and maintain the GitHub Project, GitHub Issues, Sprint Planning, and project reports.

No GitHub Issue should be created manually.

All Issues must be generated from the files contained in this directory.

---

# Objectives

The Backlog Master provides:

- Centralized project planning
- Traceability between requirements and implementation
- Automatic GitHub Issue generation
- Sprint planning support
- Validation of project consistency
- Reproducible project management

---

# Directory Structure

```text
github/backlog/

README.md

CHANGELOG.md

epics.yml

user-stories.yml

technical-stories.yml

metadata.yml

labels.yml

milestones.yml

custom-fields.yml

sprints.yml

schema/
```

---

# File Description

| File | Description |
|------|-------------|
| epics.yml | Defines all project Epics |
| user-stories.yml | Defines all User Stories |
| technical-stories.yml | Defines all Technical Stories |
| metadata.yml | Global project metadata |
| labels.yml | GitHub Labels catalog |
| milestones.yml | GitHub Milestones catalog |
| custom-fields.yml | GitHub Project Custom Fields |
| sprints.yml | Sprint planning |
| CHANGELOG.md | Backlog evolution history |

---

# Workflow

```
Requirements

↓

Backlog

↓

Validation

↓

Issue Generation

↓

GitHub Issues

↓

Sprint Planning

↓

Development

↓

Pull Request

↓

Code Review

↓

Testing

↓

Done
```

---

# Available Scripts

The following scripts operate on the Backlog Master.

| Script | Purpose |
|---------|---------|
| validate-backlog.py | Validates the integrity of the backlog |
| generate-issues.py | Generates Markdown Issues |
| create-github-issues.ps1 | Creates Issues using GitHub CLI (PowerShell) |
| create-github-issues.sh | Creates Issues using GitHub CLI (Linux/macOS) |

---

# Naming Conventions

## Epics

EP001

EP002

...

---

## User Stories

HU001

HU002

...

---

## Technical Stories

HT001

HT002

...

---

# Rules

1. Never create GitHub Issues manually.
2. Every change must originate in the Backlog Master.
3. Every User Story must belong to one Epic.
4. Every Technical Story must belong to one Epic.
5. Every Issue must belong to one Sprint.
6. Every Issue must have an associated Milestone.
7. Every Issue must have a valid Priority.
8. Every Issue must have a Business Value.
9. Every Issue must define its Complexity.
10. Every Issue must include an Estimated Hours value.

---

# Validation

Before generating GitHub Issues execute:

```bash
python github/scripts/validate-backlog.py
```

If the validation succeeds:

```bash
python github/scripts/generate-issues.py
```

Finally:

PowerShell

```powershell
./github/scripts/create-github-issues.ps1
```

Linux / macOS

```bash
./github/scripts/create-github-issues.sh
```

---

# Version

Current Version:

**1.0.0**