#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
===============================================================================
Portfolio Manager
File: github/scripts/create-github-issues.py
Version: 1.1.0

Description:
Creates GitHub Issues from the generated backlog and assigns
labels, milestones and Project fields.

The milestone of a User or Technical Story is its sprint
(docs/github/03-milestones.md); Epics span several sprints and keep
the release milestone. The release of every Issue is also stored in
the Project "Release" field.

Author:
Erickson Vergara

Python:
3.12+
===============================================================================
"""

from __future__ import annotations

import os
from typing import Any

from common import (
    Console,
    load_backlog,
    get_metadata,
    get_epics,
    get_user_stories,
    get_technical_stories,
    get_all_work_items,
    get_work_item_ids,
    backlog_statistics,
)

from github_api import (
    GitHubClient,
    GitHubNotFoundError,
)


# =============================================================================
# CONFIGURATION
# =============================================================================


PROJECT_NAME = "Portfolio Manager"

APPLICATION_NAME = "GitHub Issue Creator"

VERSION = "1.0.0"


# =============================================================================
# GITHUB CONFIGURATION
# =============================================================================


GITHUB_TOKEN = os.getenv(
    "GITHUB_TOKEN",
    ""
)

GITHUB_OWNER = os.getenv(
    "GITHUB_OWNER",
    ""
)

GITHUB_REPOSITORY = os.getenv(
    "GITHUB_REPOSITORY",
    ""
)

GITHUB_PROJECT = os.getenv(
    "GITHUB_PROJECT",
    ""
)


# =============================================================================
# PROJECT FIELD MAPPING
# =============================================================================


EPIC_MODULE_MAP = {
    "EP001": "Landing Page",
    "EP002": "Authentication",
    "EP003": "Dashboard",
    "EP004": "Projects",
    "EP005": "Technologies",
    "EP006": "Profile",
    "EP007": "Audit",
    "EP008": "Analytics",
    "EP009": "Business Rules",
}

DEFAULT_EPIC_COMPLEXITY = "Medium"


TECHNICAL_STORY_MODULE_MAP = {
    "HT001": "Authentication",
    "HT002": "Authentication",
    "HT003": "Infrastructure",
    "HT004": "DevOps",
    "HT005": "Infrastructure",
    "HT006": "Infrastructure",
    "HT007": "Infrastructure",
    "HT008": "Authentication",
    "HT009": "Authentication",
    "HT010": "Authentication",
    "HT011": "Business Rules",
    "HT012": "Business Rules",
    "HT013": "Business Rules",
    "HT014": "Analytics",
    "HT015": "Analytics",
    "HT016": "DevOps",
    "HT017": "DevOps",
    "HT018": "DevOps",
}


def get_epic_module(
    epic_id: str,
) -> str:
    """
    Returns the Project Module associated with an Epic.
    """

    try:

        return EPIC_MODULE_MAP[epic_id]

    except KeyError as error:

        raise ValueError(
            f"No module mapping found for Epic '{epic_id}'."
        ) from error


def get_user_story_module(
    story: dict[str, Any],
) -> str:
    """
    Returns the Project Module associated with a User Story.
    """

    epic_id = story["epic"]

    return get_epic_module(
        epic_id,
    )

def get_technical_story_module(
    story: dict[str, Any],
) -> str:
    """
    Returns the Project Module associated with a Technical Story.
    """

    technical_story_id = story["id"]

    try:

        return TECHNICAL_STORY_MODULE_MAP[
            technical_story_id
        ]

    except KeyError as error:

        raise ValueError(
            "No module mapping found for "
            f"Technical Story '{technical_story_id}'."
        ) from error

def get_epic_estimated_hours(
    epic: dict[str, Any],
    user_stories: list[dict[str, Any]],
) -> float:
    """
    Returns the total Estimated Hours for an Epic.
    """

    epic_id = epic["id"]

    total_hours = sum(
        float(story["estimatedHours"])
        for story in user_stories
        if story["epic"] == epic_id
    )

    return total_hours


def get_issue_milestone(
    work_item: dict[str, Any],
    work_item_type: str,
) -> str | None:
    """
    Returns the GitHub milestone of an Issue: the sprint for stories,
    the release for Epics and for stories still in the Backlog.
    """

    if work_item_type != "Epic":

        sprint = work_item.get(
            "sprint",
        )

        if sprint and sprint.startswith("Sprint "):
            return sprint

    return work_item.get(
        "milestone",
    )


# =============================================================================
# CONFIGURATION VALIDATION
# =============================================================================


def validate_configuration() -> None:
    """
    Validates the required GitHub configuration.
    """

    configuration = {
        "GITHUB_TOKEN": GITHUB_TOKEN,
        "GITHUB_OWNER": GITHUB_OWNER,
        "GITHUB_REPOSITORY": GITHUB_REPOSITORY,
        "GITHUB_PROJECT": GITHUB_PROJECT,
    }

    missing = [
        name
        for name, value in configuration.items()
        if not value
    ]

    if missing:

        missing_values = ", ".join(
            missing
        )

        raise RuntimeError(
            "Missing required GitHub configuration: "
            f"{missing_values}"
        )


# =============================================================================
# CLIENT
# =============================================================================


def create_github_client() -> GitHubClient:
    """
    Creates and returns the GitHub client.
    """

    return GitHubClient(
        token=GITHUB_TOKEN,
        owner=GITHUB_OWNER,
        repository=GITHUB_REPOSITORY,
    )


# =============================================================================
# VALIDATION
# =============================================================================


def validate_repository(
    client: GitHubClient,
) -> None:
    """
    Validates the configured GitHub repository.
    """

    Console.section("Validating Repository")

    if not client.repository_exists():

        raise RuntimeError(
            "GitHub repository does not exist."
        )

    Console.success(
        "GitHub repository validated."
    )


def validate_project(
    client: GitHubClient,
) -> str:
    """
    Validates the configured GitHub Project
    and returns its ID.
    """

    Console.section("Validating Project")

    project_id = client.get_project_id(
        GITHUB_PROJECT,
    )

    Console.success(
        "GitHub Project validated."
    )

    return project_id


def ensure_milestones(
    client: GitHubClient,
    backlog: dict[str, dict[str, Any]],
) -> None:
    """
    Ensures all backlog milestones required by the project
    exist in the GitHub repository.
    """

    Console.section(
        "Ensuring GitHub Milestones"
    )

    milestone_catalog = backlog.get(
        "milestones",
        {},
    )

    milestones = milestone_catalog.get(
        "milestones",
        [],
    )

    if not milestones:

        Console.success(
            "No milestones declared in backlog."
        )

        Console.blank()

        return

    existing_milestones = {
        milestone["title"]
        for milestone in client.get_milestones()
    }

    created = 0
    reused = 0

    for milestone in milestones:

        title = milestone["title"]

        if title in existing_milestones:

            reused += 1

            continue

        client.create_milestone(
            title=title,
            description=milestone.get(
                "description",
                "",
            ),
        )

        created += 1

        existing_milestones.add(
            title
        )

        Console.success(
            f"Created milestone: {title}"
        )

    Console.success(
        f"Milestones ready. "
        f"Created: {created}. "
        f"Reused: {reused}."
    )

    Console.blank()

def validate_token(
    client: GitHubClient,
) -> None:
    """
    Validates the configured GitHub token.
    """

    Console.section("Validating GitHub Token")

    client.validate_token()

    Console.success(
        "GitHub token validated."
    )


# =============================================================================
# EXISTING ISSUE INDEX
# =============================================================================


def build_existing_issue_index(
    client: GitHubClient,
) -> dict[str, dict[str, Any]]:
    """
    Builds an index of existing GitHub Issues by backlog ID.
    """

    Console.section(
        "Indexing Existing Issues"
    )

    existing_issues = client.list_issues()

    issue_index: dict[str, dict[str, Any]] = {}

    for issue in existing_issues:

        if "pull_request" in issue:

            continue

        body = issue.get(
            "body"
        ) or ""

        backlog_id = None

        for line in body.splitlines():

            line = line.strip()

            if line.startswith(
                "- **ID:**"
            ):

                backlog_id = line.removeprefix(
                    "- **ID:**"
                ).strip()

                break

        if not backlog_id:

            continue

        if backlog_id in issue_index:

            first_issue = issue_index[
                backlog_id
            ]

            raise RuntimeError(
                "Duplicate backlog ID detected "
                f"'{backlog_id}' in GitHub Issues "
                f"#{first_issue['number']} and "
                f"#{issue['number']}."
            )

        issue_index[
            backlog_id
        ] = issue

    Console.success(
        f"Indexed {len(issue_index)} existing "
        "backlog Issues."
    )

    Console.blank()

    return issue_index


# =============================================================================
# ISSUE CREATION
# =============================================================================


def create_epic_issue(
    client: GitHubClient,
    epic: dict[str, Any],
) -> dict[str, Any]:
    """
    Creates a GitHub Issue for an Epic.
    """

    title = epic["name"]

    body = (
        f"# {epic['name']}\n\n"
        f"{epic['description']}\n\n"
        "## Metadata\n\n"
        f"- **ID:** {epic['id']}\n"
        f"- **Priority:** {epic['priority']}\n"
        f"- **Business Value:** "
        f"{epic['businessValue']}\n"
        f"- **Status:** {epic['status']}\n"
    )

    return client.create_issue(
        title=title,
        body=body,
    )


def create_user_story_issue(
    client: GitHubClient,
    story: dict[str, Any],
) -> dict[str, Any]:
    """
    Creates a GitHub Issue for a User Story.
    """

    title = story["title"]

    acceptance_criteria = "\n".join(
        f"- {criterion}"
        for criterion in story["acceptanceCriteria"]
    )

    dependencies = "\n".join(
        f"- {dependency}"
        for dependency in story["dependencies"]
    )

    body = (
        f"# {story['title']}\n\n"
        f"{story['description']}\n\n"
        "## Metadata\n\n"
        f"- **ID:** {story['id']}\n"
        f"- **Epic:** {story['epic']}\n"
        f"- **Business Value:** "
        f"{story['businessValue']}\n"
        f"- **Priority:** {story['priority']}\n"
        f"- **Complexity:** {story['complexity']}\n"
        f"- **Estimated Hours:** "
        f"{story['estimatedHours']}\n"
        f"- **Sprint:** {story['sprint']}\n"
        f"- **Milestone:** {story['milestone']}\n"
        f"- **Status:** {story['status']}\n\n"
        "## Acceptance Criteria\n\n"
        f"{acceptance_criteria}\n\n"
        "## Dependencies\n\n"
        f"{dependencies}\n"
    )

    return client.create_issue(
        title=title,
        body=body,
    )


def create_technical_story_issue(
    client: GitHubClient,
    story: dict[str, Any],
) -> dict[str, Any]:
    """
    Creates a GitHub Issue for a Technical Story.
    """

    title = story["title"]

    acceptance_criteria = "\n".join(
        f"- {criterion}"
        for criterion in story["acceptanceCriteria"]
    )

    dependencies = "\n".join(
        f"- {dependency}"
        for dependency in story["dependencies"]
    )

    body = (
        f"# {story['title']}\n\n"
        f"{story['description']}\n\n"
        "## Metadata\n\n"
        f"- **ID:** {story['id']}\n"
        f"- **Epic:** {story['epic']}\n"
        f"- **Business Value:** "
        f"{story['businessValue']}\n"
        f"- **Priority:** {story['priority']}\n"
        f"- **Complexity:** {story['complexity']}\n"
        f"- **Estimated Hours:** "
        f"{story['estimatedHours']}\n"
        f"- **Sprint:** {story['sprint']}\n"
        f"- **Milestone:** {story['milestone']}\n"
        f"- **Status:** {story['status']}\n\n"
        "## Acceptance Criteria\n\n"
        f"{acceptance_criteria}\n\n"
        "## Dependencies\n\n"
        f"{dependencies}\n"
    )

    return client.create_issue(
        title=title,
        body=body,
    )


# =============================================================================
# ISSUE CONFIGURATION
# =============================================================================


def configure_issue(
    client: GitHubClient,
    issue: dict[str, Any],
    work_item: dict[str, Any],
    work_item_type: str,
    project_id: str,
    user_stories: list[dict[str, Any]] | None = None,
) -> None:
    """
    Configures a GitHub Issue using backlog metadata.
    """

    if work_item_type not in {
        "Epic",
        "User Story",
        "Technical Story",
    }:
        raise ValueError(
            f"Unsupported work item type: "
            f"{work_item_type}"
        )

    issue_number = issue["number"]

    labels = work_item.get(
        "labels",
        [],
    )

    if labels:

        client.assign_labels(
            issue_number,
            labels,
        )

    milestone = get_issue_milestone(
        work_item,
        work_item_type,
    )

    if milestone:

        milestone_id = client.get_milestone_id(
            milestone,
        )

        client.assign_milestone(
            issue_number,
            milestone_id,
        )

    try:
        project_item_id = client.get_project_item_id(
            project_id,
            issue["node_id"],
        )
    except GitHubNotFoundError:
        project_item_id = client.add_project_item(
            project_id,
            issue["node_id"],
        )

    if work_item_type == "Epic":

        issue_type = "Epic"

        module = get_epic_module(
            work_item["id"],
        )

        complexity = DEFAULT_EPIC_COMPLEXITY

        if user_stories is None:
            raise ValueError(
                "User stories are required "
                "to calculate Epic estimated hours."
            )

        estimated_hours = get_epic_estimated_hours(
            work_item,
            user_stories,
        )

    elif work_item_type == "User Story":

        issue_type = "User Story"

        module = get_user_story_module(
            work_item,
        )

        complexity = work_item["complexity"]

        estimated_hours = float(
            work_item["estimatedHours"]
        )

    else:

        issue_type = "Technical Story"

        module = get_technical_story_module(
            work_item,
        )

        complexity = work_item["complexity"]

        estimated_hours = float(
            work_item["estimatedHours"]
        )

    priority = work_item.get(
        "priority",
    )

    if priority:

        client.set_priority(
            project_id,
            project_item_id,
            priority,
        )

    business_value = work_item.get(
        "businessValue",
    )

    if business_value:

        client.set_business_value(
            project_id,
            project_item_id,
            business_value,
        )

    client.set_complexity(
        project_id,
        project_item_id,
        complexity,
    )

    sprint = work_item.get(
        "sprint",
    )

    if sprint:

        client.set_sprint(
            project_id,
            project_item_id,
            sprint,
        )

    release = work_item.get(
        "milestone",
    )

    if release:

        try:
            client.set_release(
                project_id,
                project_item_id,
                release,
            )
        except GitHubNotFoundError:
            Console.warning(
                f"Project field 'Release' or option '{release}' "
                f"not found: create it (custom-fields.yml, CF014)."
            )

    status = work_item.get(
        "status",
    )

    if status:

        client.set_status(
            project_id,
            project_item_id,
            status,
        )

    client.set_issue_type(
        project_id,
        project_item_id,
        issue_type,
    )

    client.set_estimated_hours(
        project_id,
        project_item_id,
        estimated_hours,
    )

    if module:

        client.set_module(
            project_id,
            project_item_id,
            module,
        )


# =============================================================================
# PROCESSING
# =============================================================================


def process_backlog(
    client: GitHubClient,
    backlog: dict[str, dict[str, Any]],
    project_id: str,
    existing_issue_index: dict[str, dict[str, Any]],
) -> dict[str, int]:
    """
    Creates and configures GitHub Issues for the complete backlog.
    """

    epics = get_epics(
        backlog,
    )

    user_stories = get_user_stories(
        backlog,
    )

    technical_stories = get_technical_stories(
        backlog,
    )

    statistics = {
        "epics": 0,
        "userStories": 0,
        "technicalStories": 0,
        "total": 0,
    }

    # =========================================================================
    # EPICS
    # =========================================================================

    Console.section("Creating or Reusing Epics")

    for epic in epics:

        issue = existing_issue_index.get(
            epic["id"],
        )

        if issue is None:
            issue = create_epic_issue(
                client,
                epic,
            )

        configure_issue(
            client,
            issue,
            epic,
            "Epic",
            project_id,
            user_stories,
        )

        statistics["epics"] += 1
        statistics["total"] += 1

    # =========================================================================
    # USER STORIES
    # =========================================================================

    Console.section("Creating or Reusing User Stories")

    for story in user_stories:

        issue = existing_issue_index.get(
            story["id"],
        )

        if issue is None:
            issue = create_user_story_issue(
                client,
                story,
            )

        configure_issue(
            client,
            issue,
            story,
            "User Story",
            project_id,
        )

        statistics["userStories"] += 1
        statistics["total"] += 1

    # =========================================================================
    # TECHNICAL STORIES
    # =========================================================================

    Console.section("Creating or Reusing Technical Stories")

    for story in technical_stories:

        issue = existing_issue_index.get(
            story["id"],
        )

        if issue is None:
            issue = create_technical_story_issue(
                client,
                story,
            )

        configure_issue(
            client,
            issue,
            story,
            "Technical Story",
            project_id,
        )

        statistics["technicalStories"] += 1
        statistics["total"] += 1

    return statistics


# =============================================================================
# BACKLOG VALIDATION
# =============================================================================


def validate_backlog(
    backlog: dict[str, dict[str, Any]],
) -> None:
    """
    Validates the backlog before creating GitHub Issues.
    """

    Console.section("Validating Backlog")

    metadata = get_metadata(
        backlog,
    )

    backlog_metadata = metadata.get(
        "backlog",
        {},
    )

    expected_counts = {
        "epics": backlog_metadata.get(
            "totalEpics"
        ),
        "userStories": backlog_metadata.get(
            "totalUserStories"
        ),
        "technicalStories": backlog_metadata.get(
            "totalTechnicalStories"
        ),
        "total": backlog_metadata.get(
            "totalIssues"
        ),
    }

    actual_counts = backlog_statistics(
        backlog,
    )

    if actual_counts != {
        "epics": expected_counts["epics"],
        "userStories": expected_counts["userStories"],
        "technicalStories": expected_counts["technicalStories"],
        "total": expected_counts["total"],
    }:

        raise RuntimeError(
            "Backlog statistics do not match metadata.\n"
            f"Expected: {expected_counts}\n"
            f"Actual:   {actual_counts}"
        )

    work_items = get_all_work_items(
        backlog,
    )

    work_item_ids = get_work_item_ids(
        work_items,
    )

    if len(work_items) != len(work_item_ids):

        raise RuntimeError(
            "Duplicate work item IDs detected "
            "in the backlog."
        )

    epic_ids = {
        epic["id"]
        for epic in get_epics(
            backlog
        )
    }

    for story in (
        get_user_stories(backlog)
        + get_technical_stories(backlog)
    ):

        epic_id = story.get(
            "epic"
        )

        if epic_id not in epic_ids:

            raise RuntimeError(
                f"Work item '{story.get('id')}' "
                f"references unknown Epic "
                f"'{epic_id}'."
            )

    Console.success(
        "Backlog validation passed."
    )

    Console.blank()


def load_project_backlog() -> dict[str, dict[str, Any]]:
    """
    Loads the complete Portfolio Manager backlog.
    """

    return load_backlog()


# =============================================================================
# STATISTICS
# =============================================================================


def print_statistics(
    statistics: dict[str, int],
) -> None:
    """
    Prints backlog statistics.
    """

    Console.section(
        "Backlog Statistics"
    )

    print(
        f"Epics               : "
        f"{statistics['epics']}"
    )

    print(
        f"User Stories        : "
        f"{statistics['userStories']}"
    )

    print(
        f"Technical Stories   : "
        f"{statistics['technicalStories']}"
    )

    Console.separator()

    print(
        f"Total Work Items    : "
        f"{statistics['total']}"
    )

    Console.blank()


# =============================================================================
# MAIN
# =============================================================================


def main() -> None:
    """
    Main entry point.
    """

    Console.title(
        PROJECT_NAME,
        APPLICATION_NAME,
        VERSION,
    )

    validate_configuration()

    client = create_github_client()

    validate_token(
        client,
    )

    validate_repository(
        client,
    )

    project_id = validate_project(
        client,
    )

    backlog = load_project_backlog()

    validate_backlog(
        backlog,
    )
    
    ensure_milestones(
        client,
        backlog,
    )

    existing_issue_index = build_existing_issue_index(
        client,
    )

    statistics = process_backlog(
        client,
        backlog,
        project_id,
        existing_issue_index,
    )

    print_statistics(
        statistics,
    )


# =============================================================================
# ENTRY POINT
# =============================================================================


if __name__ == "__main__":

    main()