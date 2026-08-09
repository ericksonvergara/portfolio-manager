#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
===============================================================================
Portfolio Manager
File: github/scripts/validate-backlog.py
Version: 2.0.0

Description:
    Validates the complete Portfolio Manager Backlog before generating
    GitHub Issues.

Author:
    Erickson Vergara

Python:
    3.12+

Dependencies:
    PyYAML
===============================================================================
"""

from __future__ import annotations

import sys
from typing import Any

from common import (
    Console,
    ValidationReport,
    EPICS_KEY,
    USER_STORIES_KEY,
    TECHNICAL_STORIES_KEY,
    LABELS_KEY,
    MILESTONES_KEY,
    SPRINTS_KEY,
    CUSTOM_FIELDS_KEY,
    load_backlog,
    get_all_work_items,
    get_all_stories,
    get_epics,
    get_user_stories,
    get_technical_stories,
    get_labels,
    get_milestones,
    get_sprints,
    get_metadata,
    backlog_statistics,
)

# =============================================================================
# PROJECT CONFIGURATION
# =============================================================================

PROJECT_NAME = "Portfolio Manager"

VALIDATOR_NAME = "Backlog Validator"

VERSION = "2.0.0"

# =============================================================================
# VALIDATION CONSTANTS
# =============================================================================

EPIC_REQUIRED_FIELDS = [
    "id",
    "name",
    "description",
    "priority",
    "businessValue",
    "status",
]

STORY_REQUIRED_FIELDS = [
    "id",
    "epic",
    "title",
    "description",
    "priority",
    "businessValue",
    "complexity",
    "estimatedHours",
    "sprint",
    "milestone",
    "labels",
    "status",
]

MANDATORY_FIELDS = [
    "priority",
    "businessValue",
    "complexity",
    "milestone",
    "labels",
    "status",
]

# =============================================================================
# REPORT
# =============================================================================

report = ValidationReport()

# =============================================================================
# REQUIRED YAML STRUCTURE
# =============================================================================

REQUIRED_SECTIONS = {
    "metadata": [],
    "epics": [EPICS_KEY],
    "user_stories": [USER_STORIES_KEY],
    "technical_stories": [TECHNICAL_STORIES_KEY],
    "labels": [LABELS_KEY],
    "milestones": [MILESTONES_KEY],
    "custom_fields": [CUSTOM_FIELDS_KEY],
    "sprints": [SPRINTS_KEY],
}


# =============================================================================
# STRUCTURE VALIDATION
# =============================================================================

def validate_required_sections(backlog: dict[str, Any]) -> None:
    """
    Validates that every YAML document contains
    the required root sections.
    """

    Console.info("Validating YAML structure...")

    for document_name, required_sections in REQUIRED_SECTIONS.items():

        document = backlog.get(document_name)

        if document is None:

            report.add_error(
                f"{document_name}.yml could not be loaded."
            )

            continue

        if not isinstance(document, dict):

            report.add_error(
                f"{document_name}.yml must contain a YAML object."
            )

            continue

        for section in required_sections:

            if section not in document:

                report.add_error(
                    f"{document_name}.yml -> Missing section '{section}'"
                )

            else:

                Console.success(
                    f"{document_name}.yml -> {section}"
                )


# =============================================================================
# BASIC DOCUMENT VALIDATION
# =============================================================================

def validate_document_types(backlog: dict[str, Any]) -> None:
    """
    Ensures that every root collection is a list.
    """

    Console.info("Validating document collections...")

    collections = [

        ("epics", EPICS_KEY),

        ("user_stories", USER_STORIES_KEY),

        ("technical_stories", TECHNICAL_STORIES_KEY),

        ("labels", LABELS_KEY),

        ("milestones", MILESTONES_KEY),

        ("sprints", SPRINTS_KEY),

    ]

    for document_name, collection_key in collections:

        document = backlog.get(document_name, {})

        collection = document.get(collection_key)

        if collection is None:

            continue

        if not isinstance(collection, list):

            report.add_error(
                f"{document_name}.yml -> '{collection_key}' must be a list."
            )

            continue

        Console.success(
            f"{document_name}.yml -> Collection '{collection_key}'"
        )


# =============================================================================
# LOAD BACKLOG
# =============================================================================

def load_and_validate_backlog() -> dict[str, Any]:
    """
    Loads the complete Backlog Master and performs
    the initial validation.
    """

    backlog = load_backlog()

    validate_required_sections(backlog)

    validate_document_types(backlog)

    return backlog


# =============================================================================
# GENERIC VALIDATOR
# =============================================================================

def validate_work_items(
    collection_name: str,
    items: list[dict[str, Any]],
    required_fields: list[str],
    valid_epics: set[str] | None = None,
) -> set[str]:
    """
    Generic validator used for Epics,
    User Stories and Technical Stories.
    """

    Console.info(f"Validating {collection_name}...")

    ids: set[str] = set()

    for item in items:

        item_id = item.get("id")

        if not item_id:

            report.add_error(
                f"{collection_name} contains an item without ID."
            )

            continue

        ids.add(item_id)

        for field in required_fields:

            if field not in item:

                report.add_error(
                    f"{item_id} -> Missing '{field}'"
                )

        if valid_epics is not None:

            epic = item.get("epic")

            if epic not in valid_epics:

                report.add_error(
                    f"{item_id} -> Invalid Epic '{epic}'"
                )

    Console.success(
        f"{collection_name}: {len(ids)} validated."
    )

    return ids


# =============================================================================
# DUPLICATE IDS
# =============================================================================

def validate_duplicate_ids(
    backlog: dict[str, Any]
) -> None:
    """
    Ensures that every ID is unique.
    """

    Console.info("Validating duplicate IDs...")

    ids: set[str] = set()

    for item in get_all_work_items(backlog):

        item_id = item.get("id")

        if not item_id:

            continue

        if item_id in ids:

            report.add_error(
                f"Duplicate ID detected: {item_id}"
            )

        else:

            ids.add(item_id)

    Console.success("Duplicate IDs validated.")


# =============================================================================
# BACKLOG IDS
# =============================================================================

def validate_backlog_ids(
    backlog: dict[str, Any]
) -> tuple[set[str], set[str], set[str]]:
    """
    Validates every work item in the backlog.
    """

    validate_duplicate_ids(backlog)

    epic_ids = validate_work_items(
        "Epics",
        get_epics(backlog),
        EPIC_REQUIRED_FIELDS,
    )

    user_story_ids = validate_work_items(
        "User Stories",
        get_user_stories(backlog),
        STORY_REQUIRED_FIELDS,
        epic_ids,
    )

    technical_story_ids = validate_work_items(
        "Technical Stories",
        get_technical_stories(backlog),
        STORY_REQUIRED_FIELDS,
        epic_ids,
    )

    return (
        epic_ids,
        user_story_ids,
        technical_story_ids,
    )
    
    
# =============================================================================
# GENERIC REFERENCE VALIDATOR
# =============================================================================

def validate_reference_field(
    items: list[dict[str, Any]],
    field_name: str,
    valid_values: set[str],
) -> None:
    """
    Validates a field whose value must exist
    in a predefined catalog.
    """

    for item in items:

        item_id = item.get("id", "<UNKNOWN>")

        value = item.get(field_name)

        if value is None:
            continue

        if value not in valid_values:

            report.add_error(
                f"{item_id} -> Invalid {field_name}: '{value}'"
            )


# =============================================================================
# DEPENDENCIES
# =============================================================================

def validate_dependencies(
    backlog: dict[str, Any],
    user_story_ids: set[str],
    technical_story_ids: set[str],
) -> None:
    """
    Validates story dependencies.
    """

    Console.info("Validating dependencies...")

    valid_dependencies = (
        user_story_ids |
        technical_story_ids
    )

    for story in get_all_stories(backlog):

        story_id = story.get("id", "<UNKNOWN>")

        for dependency in story.get("dependencies", []):

            if dependency not in valid_dependencies:

                report.add_error(
                    f"{story_id} -> Invalid dependency '{dependency}'"
                )

    Console.success("Dependencies validated.")
    
    
# =============================================================================
# LABELS
# =============================================================================

def validate_labels(
    backlog: dict[str, Any]
) -> None:
    """
    Validates labels.
    """

    Console.info("Validating labels...")

    valid_labels = {

        label["name"]

        for label in get_labels(backlog)
    }

    for item in get_all_work_items(backlog):

        item_id = item.get("id", "<UNKNOWN>")

        labels = item.get("labels", [])

        if not isinstance(labels, list):

            report.add_error(
                f"{item_id} -> labels must be a list."
            )

            continue

        for label in labels:

            if label not in valid_labels:

                report.add_error(
                    f"{item_id} -> Unknown label '{label}'"
                )

    Console.success("Labels validated.")
    
# =============================================================================
# MILESTONES
# =============================================================================

def validate_milestones(
    backlog: dict[str, Any]
) -> None:
    """
    Validates milestone references.
    """

    Console.info("Validating milestones...")

    valid_milestones = {

        milestone["title"]

        for milestone in get_milestones(backlog)
    }

    validate_reference_field(

        get_all_work_items(backlog),

        "milestone",

        valid_milestones,

    )

    Console.success("Milestones validated.")
    
# =============================================================================
# SPRINTS
# =============================================================================

def validate_sprints(
    backlog: dict[str, Any]
) -> None:
    """
    Validates Sprint references.
    """

    Console.info("Validating sprints...")

    valid_sprints = {

        sprint["name"]

        for sprint in get_sprints(backlog)
    }

    validate_reference_field(

        get_all_work_items(backlog),

        "sprint",

        valid_sprints,

    )

    Console.success("Sprints validated.")
    
# =============================================================================
# CATALOGS
# =============================================================================

def validate_catalog_field(
    item_id: str,
    field_name: str,
    value: Any,
    allowed_values: set[str],
) -> None:
    """
    Validates a catalog field.
    """

    if value is None:

        report.add_error(
            f"{item_id} -> Missing '{field_name}'"
        )

        return

    if value not in allowed_values:

        report.add_error(
            f"{item_id} -> Invalid {field_name}: '{value}'"
        )
        
# =============================================================================
# METADATA CATALOGS
# =============================================================================

def validate_catalogs(
    backlog: dict[str, Any]
) -> None:
    """
    Validates every catalog-driven field.
    """

    Console.info("Validating catalogs...")

    metadata = get_metadata(backlog)

    priorities = set(
        metadata.get("priorityLevels", [])
    )

    business_values = set(
        metadata.get("businessValueLevels", [])
    )

    complexities = set(
        metadata.get("complexityLevels", [])
    )

    workflow = set(
        metadata.get("workflow", [])
    )

    for item in get_all_work_items(backlog):

        item_id = item.get("id", "<UNKNOWN>")

        validate_catalog_field(
            item_id,
            "priority",
            item.get("priority"),
            priorities,
        )

        validate_catalog_field(
            item_id,
            "businessValue",
            item.get("businessValue"),
            business_values,
        )

        validate_catalog_field(
            item_id,
            "complexity",
            item.get("complexity"),
            complexities,
        )

        validate_catalog_field(
            item_id,
            "status",
            item.get("status"),
            workflow,
        )

    Console.success(
        "Catalogs validated."
    )
    
# =============================================================================
# ESTIMATED HOURS
# =============================================================================

def validate_estimated_hours(
    backlog: dict[str, Any]
) -> None:
    """
    Validates estimated hours.
    """

    Console.info(
        "Validating estimated hours..."
    )

    for story in get_all_stories(backlog):

        story_id = story.get("id", "<UNKNOWN>")

        hours = story.get("estimatedHours")

        if hours is None:

            report.add_error(
                f"{story_id} -> Missing estimatedHours"
            )

            continue

        if not isinstance(hours, int):

            report.add_error(
                f"{story_id} -> estimatedHours must be an integer."
            )

            continue

        if hours <= 0:

            report.add_error(
                f"{story_id} -> estimatedHours must be greater than zero."
            )

    Console.success(
        "Estimated Hours validated."
    )
    
# =============================================================================
# BUSINESS RULES
# =============================================================================

def validate_business_rules(
    backlog: dict[str, Any]
) -> None:
    """
    Validates mandatory business fields.
    """

    Console.info(
        "Validating business rules..."
    )

    for item in get_all_work_items(backlog):

        item_id = item.get("id", "<UNKNOWN>")

        for field in MANDATORY_FIELDS:

            if field not in item:

                report.add_error(
                    f"{item_id} -> Missing '{field}'"
                )

    Console.success(
        "Business rules validated."
    )
    
# =============================================================================
# STATISTICS
# =============================================================================

def print_statistics(backlog: dict[str, Any]) -> None:
    """
    Prints backlog statistics.
    """

    Console.section("Backlog Statistics")

    stats = backlog_statistics(backlog)

    print(f"Epics               : {stats['epics']}")
    print(f"User Stories        : {stats['userStories']}")
    print(f"Technical Stories   : {stats['technicalStories']}")

    Console.separator()

    print(f"Total Work Items    : {stats['total']}")

    Console.blank()
    
    
    
# =============================================================================
# SUMMARY
# =============================================================================

def print_summary(backlog: dict[str, Any]) -> None:
    """
    Prints the validation summary.
    """

    print_statistics(backlog)

    report.print()
    
# =============================================================================
# VALIDATION PIPELINE
# =============================================================================

def run_validation(
    backlog: dict[str, Any]
) -> None:
    """
    Executes the complete validation pipeline.
    """

    (
        epic_ids,
        user_story_ids,
        technical_story_ids,
    ) = validate_backlog_ids(backlog)
    
    # Cross-Reference Validation
    validate_dependencies(
        backlog,
        user_story_ids,
        technical_story_ids,
    )
    validate_labels(backlog)
    validate_milestones(backlog)
    validate_sprints(backlog)
    
    # Business Rules Validation
    validate_catalogs(backlog)
    validate_estimated_hours(backlog)
    validate_business_rules(backlog)
    
# =============================================================================
# MAIN
# =============================================================================

def main() -> None:
    """
    Application entry point.
    """

    Console.title(
        PROJECT_NAME,
        VALIDATOR_NAME,
        VERSION,
    )

    backlog = load_and_validate_backlog()

    if report.has_errors:

        print_summary(backlog)

        sys.exit(1)

    run_validation(backlog)

    print_summary(backlog)

    if report.has_errors:

        Console.error(
            "Backlog validation failed."
        )

        sys.exit(1)

    Console.success(
        "Backlog validation completed successfully."
    )

    sys.exit(0)
    
# =============================================================================
# ENTRY POINT
# =============================================================================

if __name__ == "__main__":

    try:

        main()

    except KeyboardInterrupt:

        Console.warning(
            "Validation cancelled by user."
        )

        sys.exit(130)

    except Exception as ex:

        Console.separator()

        Console.error(
            "Unexpected error."
        )

        print(ex)

        Console.separator()

        sys.exit(99)