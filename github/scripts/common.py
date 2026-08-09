#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
===============================================================================
Portfolio Manager
File: github/scripts/common.py
Version: 2.0.0

Description:
    Shared library used by all Portfolio Manager automation scripts.

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

#Backlog = dict[str, dict[str, Any]]

#WorkItem = dict[str, Any]

import yaml

# =============================================================================
# PROJECT CONFIGURATION
# =============================================================================

# -----------------------------------------------------------------------------
# Application
# -----------------------------------------------------------------------------

PROJECT_NAME = "Portfolio Manager"
PROJECT_VERSION = "2.0.0"

# -----------------------------------------------------------------------------
# Metadata
# -----------------------------------------------------------------------------

AUTHOR = "Erickson Vergara"
ENCODING = "utf-8"

# =============================================================================
# PROJECT PATHS
# =============================================================================

PROJECT_ROOT = Path(__file__).resolve().parents[2]

BACKLOG_PATH = PROJECT_ROOT / "github" / "backlog"

REPORTS_PATH = PROJECT_ROOT / "github" / "reports"

ISSUES_PATH = PROJECT_ROOT / "github" / "issues"

SCHEMAS_PATH = BACKLOG_PATH / "schema"

# =============================================================================
# BACKLOG FILES
# =============================================================================

BACKLOG_FILES = {
    
    "metadata": "metadata.yml",
    "epics": "epics.yml",
    "user_stories": "user-stories.yml",
    "technical_stories": "technical-stories.yml",
    "labels": "labels.yml",
    "milestones": "milestones.yml",
    "custom_fields": "custom-fields.yml",
    "sprints": "sprints.yml",
}

FILES = {
    
    key: BACKLOG_PATH / value
    
    for key, value in BACKLOG_FILES.items()

}

# =============================================================================
# YAML ROOT KEYS
# =============================================================================

METADATA_KEY = "metadata"

EPICS_KEY = "epics"

USER_STORIES_KEY = "userStories"

TECHNICAL_STORIES_KEY = "technicalStories"

LABELS_KEY = "labels"

MILESTONES_KEY = "milestones"

SPRINTS_KEY = "sprints"

CUSTOM_FIELDS_KEY = "customFields"

# =============================================================================
# EXIT CODES
# =============================================================================

EXIT_SUCCESS = 0

EXIT_VALIDATION_ERROR = 1

EXIT_INTERRUPTED = 130

EXIT_UNEXPECTED_ERROR = 99

# =============================================================================
# CONSOLE
# =============================================================================

class Console:
    """
    Console output helper.
    """

    SEPARATOR = "=" * 79

    SUB_SEPARATOR = "-" * 79

    INFO = "[INFO]"

    SUCCESS = "[ OK ]"

    WARNING = "[WARN]"

    ERROR = "[FAIL]"

    @staticmethod
    def separator() -> None:
        """
        Prints a separator line.
        """

        print(Console.SEPARATOR)

    @staticmethod
    def sub_separator() -> None:
        """
        Prints a secondary separator.
        """

        print(Console.SUB_SEPARATOR)

    @staticmethod
    def blank() -> None:
        """
        Prints a blank line.
        """

        print()

    @staticmethod
    def title(
        project: str,
        application: str,
        version: str,
    ) -> None:
        """
        Prints application title.
        """

        Console.separator()

        print(project)

        print(application)

        print(f"Version: {version}")

        Console.separator()
        
        Console.blank()        
        

    @staticmethod
    def info(message: str) -> None:
        """
        Prints information message.
        """

        print(f"{Console.INFO} {message}")

    @staticmethod
    def success(message: str) -> None:
        """
        Prints success message.
        """

        print(f"{Console.SUCCESS} {message}")

    @staticmethod
    def warning(message: str) -> None:
        """
        Prints warning message.
        """

        print(f"{Console.WARNING} {message}")

    @staticmethod
    def error(message: str) -> None:
        """
        Prints error message.
        """

        print(f"{Console.ERROR} {message}")

    @staticmethod
    def section(title: str) -> None:
        """
        Prints a section title.
        """

        Console.blank()

        Console.separator()

        print(title)

        Console.separator()
        
        Console.blank()

    @staticmethod
    def subsection(title: str) -> None:
        """
        Prints a subsection title.
        """

        Console.blank()

        Console.sub_separator()

        print(title)

        Console.sub_separator()
        
        Console.blank()
        
# =============================================================================
# VALIDATION REPORT
# =============================================================================

class ValidationReport:
    """
    Stores validation errors and warnings generated during script execution.
    """

    def __init__(self) -> None:

        self.errors: list[str] = []

        self.warnings: list[str] = []

    # -------------------------------------------------------------------------
    # Properties
    # -------------------------------------------------------------------------

    @property
    def has_errors(self) -> bool:
        """
        Returns True when at least one validation error exists.
        """

        return len(self.errors) > 0

    @property
    def has_warnings(self) -> bool:
        """
        Returns True when at least one warning exists.
        """

        return len(self.warnings) > 0

    @property
    def error_count(self) -> int:
        """
        Returns the total number of errors.
        """

        return len(self.errors)

    @property
    def warning_count(self) -> int:
        """
        Returns the total number of warnings.
        """

        return len(self.warnings)

    # -------------------------------------------------------------------------
    # Add messages
    # -------------------------------------------------------------------------

    def add_error(self, message: str) -> None:
        """
        Adds a validation error.
        """

        self.errors.append(message)

    def add_warning(self, message: str) -> None:
        """
        Adds a validation warning.
        """

        self.warnings.append(message)

    # -------------------------------------------------------------------------
    # Output
    # -------------------------------------------------------------------------

    def print(self) -> None:
        """
        Prints the validation report.
        """

        if self.has_errors:

            Console.section("Validation Errors")

            for error in self.errors:

                Console.error(error)

        if self.has_warnings:

            Console.section("Validation Warnings")

            for warning in self.warnings:

                Console.warning(warning)
        
        Console.subsection("Summary")

        print(f"Errors   : {self.error_count}")
        print(f"Warnings : {self.warning_count}")

        Console.blank()

    # -------------------------------------------------------------------------
    # Reset
    # -------------------------------------------------------------------------

    def clear(self) -> None:
        """
        Clears the validation report.
        """

        self.errors.clear()

        self.warnings.clear()
        
# =============================================================================
# FILE UTILITIES
# =============================================================================

def file_exists(path: Path) -> bool:
    """
    Returns True if the specified file exists.
    """

    return path.exists() and path.is_file()


def load_yaml(path: Path) -> dict[str, Any]:
    """
    Loads a YAML file.

    Raises:
        FileNotFoundError:
            If the file does not exist.

        yaml.YAMLError:
            If the YAML content is invalid.
    """

    if not file_exists(path):

        raise FileNotFoundError(path)

    with path.open(
        "r",
        encoding=ENCODING,
    ) as stream:

        data = yaml.safe_load(stream)

    if data is None:

        return {}

    if not isinstance(data, dict):

        raise TypeError(
            f"{path.name} must contain a YAML object."
        )

    return data


def load_backlog() -> dict[str, dict[str, Any]]:
    """
    Loads every backlog file.

    Returns
    -------
    dict
        Dictionary containing all backlog documents.
    """

    Console.section("Loading Backlog")

    backlog: dict[str, dict[str, Any]] = {}

    for name, path in FILES.items():

        Console.info(f"Loading '{path.name}'...")

        backlog[name] = load_yaml(path)

    Console.success(f"{len(backlog)} backlog files loaded.")

    Console.blank()

    return backlog

# =============================================================================
# BACKLOG UTILITIES
# =============================================================================

def get_epics(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns all Epics.
    """

    return backlog["epics"].get(
        EPICS_KEY,
        []
    )


def get_user_stories(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns all User Stories.
    """

    return backlog["user_stories"].get(
        USER_STORIES_KEY,
        []
    )


def get_technical_stories(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns all Technical Stories.
    """

    return backlog["technical_stories"].get(
        TECHNICAL_STORIES_KEY,
        []
    )
    
    
def get_labels(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns all Labels.
    """

    return backlog["labels"].get(
        LABELS_KEY,
        []
    )


def get_milestones(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns all Milestones.
    """

    return backlog["milestones"].get(
        MILESTONES_KEY,
        []
    )


def get_sprints(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns all Sprints.
    """

    return backlog["sprints"].get(
        SPRINTS_KEY,
        []
    )


def get_custom_fields(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns all Custom Fields.
    """

    return backlog["custom_fields"].get(
        CUSTOM_FIELDS_KEY,
        []
    )


def get_metadata(
    backlog: dict[str, dict[str, Any]]
) -> dict[str, Any]:
    """
    Returns project metadata.
    """

    return backlog.get(
        METADATA_KEY,
        {}
    )
    
def get_all_stories(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns every User Story and Technical Story.
    """

    return (

        get_user_stories(backlog)

        +

        get_technical_stories(backlog)

    )


def get_all_work_items(
    backlog: dict[str, dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns every work item contained
    in the backlog.
    """

    return (

        get_epics(backlog)

        +

        get_user_stories(backlog)

        +

        get_technical_stories(backlog)

    )
    
def get_work_items_by_epic(
    backlog: dict[str, dict[str, Any]],
    epic_id: str,
) -> list[dict[str, Any]]:
    """
    Returns every work item that belongs
    to an Epic.
    """

    return [

        item

        for item in get_all_stories(backlog)

        if item.get("epic") == epic_id

    ]
    
def get_work_item_ids(
    items: list[dict[str, Any]]
) -> set[str]:
    """
    Returns all IDs from a collection.
    """

    return {

        item["id"]

        for item in items

        if "id" in item

    }
    
# =============================================================================
# SEARCH UTILITIES
# =============================================================================

def find_epic(
    backlog: dict[str, dict[str, Any]],
    epic_id: str,
) -> dict[str, Any] | None:
    """
    Returns an Epic by its ID.
    """

    for epic in get_epics(backlog):

        if epic.get("id") == epic_id:

            return epic

    return None


def find_user_story(
    backlog: dict[str, dict[str,Any]],
    story_id: str,
) -> dict[str, Any] | None:
    """
    Returns a User Story by its ID.
    """

    for story in get_user_stories(backlog):

        if story.get("id") == story_id:

            return story

    return None


def find_technical_story(
    backlog: dict[str, dict[str, Any]],
    story_id: str,
) -> dict[str, Any] | None:
    """
    Returns a Technical Story by its ID.
    """

    for story in get_technical_stories(backlog):

        if story.get("id") == story_id:

            return story

    return None


def find_work_item(
    backlog: dict[str, dict[str, Any]],
    work_item_id: str,
) -> dict[str, Any] | None:
    """
    Returns any work item by its ID.
    """

    for item in get_all_work_items(backlog):

        if item.get("id") == work_item_id:

            return item

    return None

def work_item_exists(
    backlog: dict[str, dict[str, Any]],
    work_item_id: str,
) -> bool:
    """
    Returns True if a work item exists.
    """

    return (
        find_work_item(
            backlog,
            work_item_id,
        )
        is not None
    )
    
def epic_exists(
    backlog: dict[str, dict[str, Any]],
    epic_id: str,
) -> bool:
    """
    Returns True if an Epic exists.
    """

    return (
        find_epic(
            backlog,
            epic_id,
        )
        is not None
    )
    
def sort_work_items(
    items: list[dict[str, Any]]
) -> list[dict[str, Any]]:
    """
    Returns work items ordered by ID.
    """

    return sorted(
        items,
        key=lambda item: item.get("id", ""),
    )
    
# =============================================================================
# GENERIC HELPERS
# =============================================================================

def unique_ids(
    items: list[dict[str, Any]]
) -> set[str]:
    """
    Returns every unique ID contained
    in a collection.
    """

    return {

        item["id"]

        for item in items

        if "id" in item

    }


def group_by_epic(
    backlog: dict[str, dict[str, Any]]
) -> dict[str, list[dict[str, Any]]]:
    """
    Groups all stories by Epic.
    """

    grouped: dict[str, list[dict[str, Any]]] = {}

    for story in get_all_stories(backlog):

        epic = story.get("epic")

        grouped.setdefault(
            epic,
            []
        ).append(story)

    return grouped


def backlog_statistics(
    backlog: dict[str, dict[str, Any]]
) -> dict[str, int]:
    """
    Returns backlog statistics.
    """

    epics = len(get_epics(backlog))

    user_stories = len(
        get_user_stories(backlog)
    )

    technical_stories = len(
        get_technical_stories(backlog)
    )

    return {

        "epics": epics,

        "userStories": user_stories,

        "technicalStories": technical_stories,

        "total":

            epics

            + user_stories

            + technical_stories,

    }
    
# =============================================================================
# MODULE EXPORTS
# =============================================================================

__all__ = [

    # Configuration

    "PROJECT_NAME",

    "PROJECT_VERSION",

    "AUTHOR",

    "ENCODING",

    # Paths

    "PROJECT_ROOT",

    "BACKLOG_PATH",

    "REPORTS_PATH",

    "ISSUES_PATH",

    "SCHEMAS_PATH",

    # Files

    "FILES",

    # YAML Keys
    
    "METADATA_KEY",

    "EPICS_KEY",

    "USER_STORIES_KEY",

    "TECHNICAL_STORIES_KEY",

    "LABELS_KEY",

    "MILESTONES_KEY",

    "SPRINTS_KEY",

    "CUSTOM_FIELDS_KEY",

    # Exit Codes

    "EXIT_SUCCESS",

    "EXIT_VALIDATION_ERROR",

    "EXIT_INTERRUPTED",

    "EXIT_UNEXPECTED_ERROR",

    # Console

    "Console",

    # Report

    "ValidationReport",

    # File Utilities

    "file_exists",

    "load_yaml",

    "load_backlog",

    # Backlog Utilities

    "get_metadata",

    "get_epics",

    "get_user_stories",

    "get_technical_stories",

    "get_labels",

    "get_milestones",

    "get_sprints",

    "get_custom_fields",

    "get_all_stories",

    "get_all_work_items",

    "get_work_items_by_epic",

    "get_work_item_ids",

    # Search

    "find_epic",

    "find_user_story",

    "find_technical_story",

    "find_work_item",

    "work_item_exists",

    "epic_exists",

    "sort_work_items",

    # Helpers

    "unique_ids",

    "group_by_epic",

    "backlog_statistics",

]