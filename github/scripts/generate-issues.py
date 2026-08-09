#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
===============================================================================
Portfolio Manager
File: github/scripts/generate-issues.py
Version: 1.0.0

Description:
    Generates GitHub Issue Markdown files from the validated
    Portfolio Manager Backlog.

Author:
    Erickson Vergara

Python:
    3.12+

Dependencies:
    PyYAML
===============================================================================
"""

from __future__ import annotations

from pathlib import Path
from typing import Any
from collections.abc import Callable

from common import (
    Console,
    ISSUES_PATH,
    load_backlog,
    backlog_statistics,
    get_epics,
    get_user_stories,
    get_technical_stories,
)

# =============================================================================
# PROJECT CONFIGURATION
# =============================================================================

PROJECT_NAME = "Portfolio Manager"

APPLICATION_NAME = "Issue Generator"

VERSION = "1.0.0"

GENERATED_BY = "Portfolio Manager"

# =============================================================================
# OUTPUT DIRECTORIES
# =============================================================================

OUTPUT_ROOT = ISSUES_PATH

EPICS_OUTPUT = (
    OUTPUT_ROOT / "epics"
)

USER_STORIES_OUTPUT = (
    OUTPUT_ROOT / "user-stories"
)

TECHNICAL_STORIES_OUTPUT = (
    OUTPUT_ROOT / "technical-stories"
)

OUTPUT_DIRECTORIES = {
    "epics": EPICS_OUTPUT,
    "user-stories": USER_STORIES_OUTPUT,
    "technical-stories": TECHNICAL_STORIES_OUTPUT,
}

# =============================================================================
# MARKDOWN BUILDERS
# =============================================================================

def build_header(
    work_item_id: str,
    title: str,
) -> str:
    """
    Builds the Markdown header.
    """

    return (
        "<!--\n"
        f"{GENERATED_BY}\n"
        "Generated automatically.\n"
        "Do not edit manually.\n"
        "-->\n\n"
        f"# {work_item_id} - {title}\n"
    )
    
def build_description(
    description: str,
) -> str:
    """
    Builds the description section.
    """

    return (
        "## Description\n\n"
        f"{description}\n"
    )
    
def build_acceptance_criteria(
    criteria: list[str],
) -> str:
    """
    Builds the Acceptance Criteria section.
    """

    lines = [
        "",
        "## Acceptance Criteria",
        "",
    ]

    lines.extend(
        f"- [ ] {item}"
        for item in criteria
    )

    return "\n".join(lines)

def build_dependencies(
    dependencies: list[str],
) -> str:
    """
    Builds the Dependencies section.
    """

    if not dependencies:

        return ""

    lines = [
        "",
        "## Dependencies",
        "",
    ]

    lines.extend(
        f"- {dependency}"
        for dependency in dependencies
    )

    return "\n".join(lines)

def build_footer() -> str:
    """
    Builds the footer.
    """

    return (
        "\n---\n\n"
        "_Generated automatically by Portfolio Manager._\n"
    )
    
# =============================================================================
# MARKDOWN TEMPLATES
# =============================================================================

def build_epic_markdown(
    epic: dict[str, Any],
) -> str:
    """
    Builds the Markdown document for an Epic.
    """

    return (

        build_header(
            epic["id"],
            epic["name"],
        )

        + build_description(
            epic["description"],
        )

        + build_footer()

    )
    
def build_story_markdown(
    story: dict[str, Any],
) -> str:
    """
    Builds the Markdown document for
    any Story.
    """

    return (

        build_header(
            story["id"],
            story["title"],
        )

        + build_description(
            story["description"],
        )

        + build_acceptance_criteria(
            story.get(
                "acceptanceCriteria",
                [],
            ),
        )

        + build_dependencies(
            story.get(
                "dependencies",
                [],
            ),
        )

        + build_footer()

    )
    
def build_user_story_markdown(
    story: dict[str, Any],
) -> str:
    """
    Builds the Markdown document for a User Story.
    """

    return build_story_markdown(story)
    
def build_technical_story_markdown(
    story: dict[str, Any],
) -> str:
    """
    Builds the Markdown document for a Technical Story.
    """

    return build_story_markdown(story)

# =============================================================================
# FILE GENERATOR
# =============================================================================

def ensure_output_directories() -> None:
    """
    Creates the output directories if they do not exist.
    """   
    
    for directory in OUTPUT_DIRECTORIES.values():

        directory.mkdir(
            parents=True,
            exist_ok=True,
        )
    
    
def save_issue(
    output_directory: Path,
    work_item_id: str,
    markdown: str,
) -> Path:
    """
    Saves a Markdown Issue file.
    """

    output_file = (
        output_directory
        / f"{work_item_id}.md"
    )

    output_file.write_text(
        markdown,
        encoding="utf-8",
    )

    return output_file

def clean_output_directory(
    directory: Path,
) -> None:
    """
    Removes all generated Markdown files.
    """

    for file in directory.glob("*.md"):

        file.unlink()
        
def clean_output_directories() -> None:
    """
    Cleans all output directories.
    """

    for directory in OUTPUT_DIRECTORIES.values():

        clean_output_directory(directory)
        
# =============================================================================
# ISSUE GENERATORS
# =============================================================================

def generate_issues(
    work_items: list[dict[str, Any]],
    output_directory: Path,
    markdown_builder: Callable[
        [dict[str, Any]],
        str,
    ],
) -> int:
    """
    Generates Markdown Issues.
    """

    count = 0

    for item in work_items:

        markdown = markdown_builder(item)

        save_issue(
            output_directory,
            item["id"],
            markdown,
        )

        Console.info(
            f"Generating {item['id']}.md"
        )

        count += 1

    return count

def generate_epic_issues(
    backlog: dict[str, Any],
) -> int:
    """
    Generates all Epic Issues.
    """

    return generate_issues(
        get_epics(backlog),
        EPICS_OUTPUT,
        build_epic_markdown,
    )

def generate_user_story_issues(
    backlog: dict[str, Any],
) -> int:
    """
    Generates all User Story Issues.
    """

    return generate_issues(
        get_user_stories(backlog),
        USER_STORIES_OUTPUT,
        build_user_story_markdown,
    )

def generate_technical_story_issues(
    backlog: dict[str, Any],
) -> int:
    """
    Generates all Technical Story Issues.
    """

    return generate_issues(
        get_technical_stories(backlog),
        TECHNICAL_STORIES_OUTPUT,
        build_technical_story_markdown,
    )

# =============================================================================
# GENERATION PIPELINE
# =============================================================================

def generate_all_issues(
    backlog: dict[str, Any],
) -> dict[str, int]:
    """
    Generates all GitHub Issue Markdown files.
    """

    Console.section(
        "Generating GitHub Issues"
    )

    ensure_output_directories()

    clean_output_directories()

    epics = generate_epic_issues(
        backlog,
    )

    user_stories = generate_user_story_issues(
        backlog,
    )

    technical_stories = (
        generate_technical_story_issues(
            backlog,
        )
    )

    return {
        "epics": epics,
        "userStories": user_stories,
        "technicalStories": technical_stories,
        "total": (
            epics
            + user_stories
            + technical_stories
        ),
    }
    
# =============================================================================
# STATISTICS
# =============================================================================

def print_statistics(
    statistics: dict[str, int],
) -> None:
    """
    Prints generation statistics.
    """

    Console.section(
        "Generation Summary"
    )

    print(
        f"Epics               : {statistics['epics']}"
    )

    print(
        f"User Stories        : {statistics['userStories']}"
    )

    print(
        f"Technical Stories   : "
        f"{statistics['technicalStories']}"
    )

    Console.separator()

    print(
        f"Total Issues        : "
        f"{statistics['total']}"
    )

    Console.blank()
    
def validate_generation(
    backlog: dict[str, Any],
    statistics: dict[str, int],
) -> None:
    """
    Validates the generated Issues count.
    """

    expected = backlog_statistics(
        backlog,
    )

    if (
        expected["total"]
        != statistics["total"]
    ):

        raise RuntimeError(
            "Generated Issues count does not "
            "match Backlog statistics."
        )
        
    
# =============================================================================
# MAIN
# =============================================================================

def main() -> None:
    """
    Main entry point.
    """

    try:

        Console.title(
            PROJECT_NAME,
            APPLICATION_NAME,
            VERSION,
        )

        backlog = load_backlog()

        statistics = generate_all_issues(
            backlog,
        )

        validate_generation(
            backlog,
            statistics,
        )

        print_statistics(
            statistics,
        )

        Console.success(
            "GitHub Issues generated successfully."
        )

    except Exception as error:

        Console.error(
            str(error)
        )

        raise
    
# =============================================================================
# ENTRY POINT
# =============================================================================

if __name__ == "__main__":

    main()