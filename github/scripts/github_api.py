#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
===============================================================================
Portfolio Manager
File: github/scripts/github_api.py
Version: 1.0.0

Description:
GitHub API client used by Portfolio Manager to interact with
GitHub Issues, Labels, Milestones and Projects.

Author:
Erickson Vergara

Python:
3.12+

Dependencies:
requests
===============================================================================
"""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Any

import requests

# =============================================================================
# CONFIGURATION
# =============================================================================


@dataclass(frozen=True)
class GitHubConfig:
    """
    GitHub API configuration.
    """

    api_url: str = "https://api.github.com"

    api_version: str = "2022-11-28"

    timeout: int = 30

    user_agent: str = "PortfolioManager/1.0"


# =============================================================================
# EXCEPTIONS
# =============================================================================


class GitHubError(Exception): ...


class GitHubAuthenticationError(GitHubError): ...


class GitHubApiError(GitHubError): ...


class GitHubNotFoundError(GitHubError): ...


# =============================================================================
# ENUMERATIONS
# =============================================================================


class HttpMethod(str, Enum):
    """
    Supported HTTP methods.
    """

    GET = "GET"
    POST = "POST"
    PUT = "PUT"
    PATCH = "PATCH"
    DELETE = "DELETE"


class IssueState(str, Enum):
    """
    GitHub Issue states.
    """

    OPEN = "open"
    CLOSED = "closed"
    ALL = "all"


# =============================================================================
# GRAPHQL QUERIES
# =============================================================================

GET_PROJECTS_QUERY = """
query($owner: String!) {
    user(login: $owner) {
        projectsV2(first: 100) {
            nodes {
                id
                title
            }
        }
    }
}
"""

GET_PROJECT_FIELDS_QUERY = """
query($projectId: ID!) {
    node(id: $projectId) {
        ... on ProjectV2 {
            fields(first: 100) {
                nodes {
                    ... on ProjectV2FieldCommon {
                        id
                        name
                    }

                    ... on ProjectV2SingleSelectField {
                        id
                        name
                        options {
                            id
                            name
                        }
                    }
                }
            }
        }
    }
}
"""

UPDATE_PROJECT_FIELD_QUERY = """
mutation(
    $projectId: ID!,
    $itemId: ID!,
    $fieldId: ID!,
    $optionId: String!
) {
    updateProjectV2ItemFieldValue(
        input: {
            projectId: $projectId
            itemId: $itemId
            fieldId: $fieldId
            value: {
                singleSelectOptionId: $optionId
            }
        }
    ) {
        projectV2Item {
            id
        }
    }
}
"""

UPDATE_PROJECT_NUMBER_FIELD_QUERY = """
mutation(
    $projectId: ID!,
    $itemId: ID!,
    $fieldId: ID!,
    $numberValue: Float!
) {
    updateProjectV2ItemFieldValue(
        input: {
            projectId: $projectId
            itemId: $itemId
            fieldId: $fieldId
            value: {
                number: $numberValue
            }
        }
    ) {
        projectV2Item {
            id
        }
    }
}
"""

GET_PROJECT_ITEM_QUERY = """
query(
    $projectId: ID!,
) {
    node(id: $projectId) {
        ... on ProjectV2 {
            items(first: 100) {
                nodes {
                    id
                    content {
                        ... on Issue {
                            id
                        }
                    }
                }
            }
        }
    }
}
"""

ADD_PROJECT_ITEM_MUTATION = """
mutation(
    $projectId: ID!,
    $contentId: ID!
) {
    addProjectV2ItemById(
        input: {
            projectId: $projectId
            contentId: $contentId
        }
    ) {
        item {
            id
        }
    }
}
"""

# =============================================================================
# GITHUB CLIENT
# =============================================================================


class GitHubClient:
    """
    GitHub REST API client.
    """

    def __init__(
        self,
        token: str,
        owner: str,
        repository: str,
    ) -> None:
        """
        Initializes the GitHub client.
        """

        self.token = token

        self.owner = owner

        self.repository = repository

        self.config = GitHubConfig()

        self.session = requests.Session()

        self._repository: dict[str, Any] | None = None

        self._labels: list[dict[str, Any]] | None = None

        self._milestones: list[dict[str, Any]] | None = None

        self._projects: list[dict[str, Any]] | None = None

        self._project_fields: dict[
            str,
            list[dict[str, Any]],
        ] = {}

    # =============================================================================
    # AUTHENTICATION
    # =============================================================================

    @property
    def repository_path(self) -> str:
        """
        Returns the repository path.
        """

        return f"{self.owner}/{self.repository}"

    def _build_headers(
        self,
    ) -> dict[str, str]:
        """
        Builds the HTTP headers.
        """

        return {
            "Accept": "application/vnd.github+json",
            "Authorization": (f"Bearer {self.token}"),
            "X-GitHub-Api-Version": (self.config.api_version),
            "User-Agent": (self.config.user_agent),
        }

    def validate_token(
        self,
    ) -> bool:
        """
        Validates the GitHub token.
        """

        self._request(
            HttpMethod.GET,
            "/user",
        )

        return True

    # =============================================================================
    # HTTP CLIENT
    # =============================================================================

    def _request(
        self,
        method: HttpMethod,
        endpoint: str,
        **kwargs: Any,
    ) -> requests.Response:
        """
        Executes an HTTP request against the GitHub API.
        """

        url = f"{self.config.api_url}{endpoint}"

        response = self.session.request(
            method=method.value,
            url=url,
            headers=self._build_headers(),
            timeout=self.config.timeout,
            **kwargs,
        )

        if not response.ok:
            try:
                payload = response.json()

                if isinstance(payload, dict):
                    message = payload.get(
                        "message",
                        response.reason,
                    )

                else:
                    message = response.reason

            except ValueError:
                message = response.reason

            error_message = (
                "GitHub API request failed.\n\n"
                f"Method   : {method.value}\n"
                f"Endpoint : {endpoint}\n"
                f"Status   : {response.status_code}\n"
                f"Message  : {message}\n"
            )

            if response.status_code in (401, 403):
                raise GitHubAuthenticationError(error_message)

            if response.status_code == 404:
                raise GitHubNotFoundError(error_message)

            raise GitHubApiError(error_message)

        return response

    def _json(
        self,
        response: requests.Response,
    ) -> Any:
        """
        Returns the response JSON.
        """

        return response.json()

    def _graphql(
        self,
        query: str,
        variables: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        """
        Executes a GraphQL query.
        """

        response = self._request(
            HttpMethod.POST,
            "/graphql",
            json={
                "query": query,
                "variables": variables or {},
            },
        )

        data = self._json(response)

        errors = data.get(
            "errors",
            [],
        )

        if errors:
            messages = "; ".join(
                error.get(
                    "message",
                    "Unknown GraphQL error.",
                )
                for error in errors
            )

            raise GitHubApiError(
                f"GitHub GraphQL request failed.\n\nMessage: {messages}"
            )

        return data

    # =============================================================================
    # REPOSITORY API
    # =============================================================================

    def get_repository(
        self,
    ) -> dict[str, Any]:
        """
        Returns the repository information.
        """

        if self._repository is None:
            response = self._request(
                HttpMethod.GET,
                f"/repos/{self.repository_path}",
            )

            self._repository = self._json(response)

        return self._repository

    def repository_exists(
        self,
    ) -> bool:
        """
        Checks whether the repository exists.
        """

        try:
            self.get_repository()

            return True

        except GitHubNotFoundError:
            return False

    def get_repository_id(
        self,
    ) -> int:
        """
        Returns the repository identifier.
        """

        repository = self.get_repository()

        return repository["id"]

    def get_repository_node_id(
        self,
    ) -> str:
        """
        Returns the repository node identifier.
        """

        repository = self.get_repository()

        return repository["node_id"]

    # =============================================================================
    # ISSUE API
    # =============================================================================

    def get_issue(
        self,
        issue_number: int,
    ) -> dict[str, Any]:
        """
        Returns an Issue.
        """

        response = self._request(
            HttpMethod.GET,
            (f"/repos/{self.repository_path}/issues/{issue_number}"),
        )

        return self._json(response)

    def find_issue(
        self,
        title: str,
    ) -> dict[str, Any] | None:
        """
        Finds an Issue by title.
        """

        issues = self.list_issues()

        for issue in issues:
            if issue["title"] == title:
                return issue

        return None

    def find_issue_by_backlog_id(
        self,
        backlog_id: str,
    ) -> dict[str, Any] | None:
        """
        Finds an Issue by its Portfolio Manager backlog ID.
        """

        marker = f"- **ID:** {backlog_id}"

        for issue in self.list_issues():
            body = issue.get("body") or ""

            if marker in body:
                return issue

        return None

    def list_issues(
        self,
        state: IssueState = IssueState.ALL,
    ) -> list[dict[str, Any]]:
        """
        Returns all repository Issues across all pages.
        """

        issues: list[dict[str, Any]] = []

        page = 1

        while True:
            response = self._request(
                HttpMethod.GET,
                f"/repos/{self.repository_path}/issues",
                params={
                    "state": state.value,
                    "per_page": 100,
                    "page": page,
                },
            )

            page_issues = self._json(response)

            if not page_issues:
                break

            issues.extend(page_issues)

            if len(page_issues) < 100:
                break

            page += 1

        return issues

    def create_issue(
        self,
        title: str,
        body: str,
    ) -> dict[str, Any]:
        """
        Creates a GitHub Issue.
        """

        response = self._request(
            HttpMethod.POST,
            f"/repos/{self.repository_path}/issues",
            json={
                "title": title,
                "body": body,
            },
        )

        return self._json(response)

    def update_issue(
        self,
        issue_number: int,
        **fields: Any,
    ) -> dict[str, Any]:
        """
        Updates a GitHub Issue.
        """

        response = self._request(
            HttpMethod.PATCH,
            (f"/repos/{self.repository_path}/issues/{issue_number}"),
            json=fields,
        )

        return self._json(response)

    def close_issue(
        self,
        issue_number: int,
    ) -> dict[str, Any]:
        """
        Closes an Issue.
        """

        return self.update_issue(
            issue_number,
            state="closed",
        )

    def reopen_issue(
        self,
        issue_number: int,
    ) -> dict[str, Any]:
        """
        Reopens an Issue.
        """

        return self.update_issue(
            issue_number,
            state="open",
        )

    def assign_labels(
        self,
        issue_number: int,
        labels: list[str],
    ) -> dict[str, Any]:
        """
        Assigns labels to an Issue.
        """

        return self.update_issue(
            issue_number,
            labels=labels,
        )

    def assign_milestone(
        self,
        issue_number: int,
        milestone: int,
    ) -> dict[str, Any]:
        """
        Assigns a milestone.
        """

        return self.update_issue(
            issue_number,
            milestone=milestone,
        )

    # =============================================================================
    # LABEL API
    # =============================================================================

    def get_labels(
        self,
    ) -> list[dict[str, Any]]:
        """
        Returns all repository labels.
        """

        if self._labels is None:
            response = self._request(
                HttpMethod.GET,
                f"/repos/{self.repository_path}/labels",
            )

            self._labels = self._json(response)

        return self._labels

    def find_label(
        self,
        name: str,
    ) -> dict[str, Any] | None:
        """
        Finds a label by name.
        """

        for label in self.get_labels():
            if label["name"] == name:
                return label

        return None

    def label_exists(
        self,
        name: str,
    ) -> bool:
        """
        Checks whether a label exists.
        """

        return self.find_label(name) is not None

    def create_label(
        self,
        name: str,
        color: str,
        description: str = "",
    ) -> dict[str, Any]:
        """
        Creates a repository label.
        """

        response = self._request(
            HttpMethod.POST,
            f"/repos/{self.repository_path}/labels",
            json={
                "name": name,
                "color": color,
                "description": description,
            },
        )

        label = self._json(response)

        if self._labels is not None:
            self._labels.append(label)

        return label

    def _require_label(
        self,
        name: str,
    ) -> dict[str, Any]:
        """
        Returns a label or raises an exception.
        """

        label = self.find_label(name)

        if label is None:
            raise GitHubNotFoundError(f"Label '{name}' not found.")

        return label

    def get_label_id(
        self,
        name: str,
    ) -> int:

        return self._require_label(name)["id"]

    def get_label_name(
        self,
        name: str,
    ) -> str:

        return self._require_label(name)["name"]

    # =============================================================================
    # MILESTONE API
    # =============================================================================

    def get_milestones(
        self,
    ) -> list[dict[str, Any]]:
        """
        Returns all repository milestones.
        """

        if self._milestones is None:
            response = self._request(
                HttpMethod.GET,
                f"/repos/{self.repository_path}/milestones",
                params={
                    "state": "all",
                },
            )

            self._milestones = self._json(response)

        return self._milestones

    def find_milestone(
        self,
        title: str,
    ) -> dict[str, Any] | None:
        """
        Finds a milestone by title.
        """

        for milestone in self.get_milestones():
            if milestone["title"] == title:
                return milestone

        return None

    def milestone_exists(
        self,
        title: str,
    ) -> bool:
        """
        Checks whether a milestone exists.
        """

        return self.find_milestone(title) is not None

    def _require_milestone(
        self,
        title: str,
    ) -> dict[str, Any]:
        """
        Returns a milestone or raises an exception.
        """

        milestone = self.find_milestone(title)

        if milestone is None:
            raise GitHubNotFoundError(f"Milestone '{title}' not found.")

        return milestone

    def get_milestone_id(
        self,
        title: str,
    ) -> int:
        """
        Returns the milestone identifier.
        """

        return self._require_milestone(title)["number"]

    def create_milestone(
        self,
        title: str,
        description: str = "",
    ) -> dict[str, Any]:
        """
        Creates a repository milestone.
        """

        response = self._request(
            HttpMethod.POST,
            f"/repos/{self.repository_path}/milestones",
            json={
                "title": title,
                "description": description,
            },
        )

        milestone = self._json(response)

        if self._milestones is not None:
            self._milestones.append(milestone)

        return milestone

    # =============================================================================
    # PROJECT API
    # =============================================================================

    def get_projects(
        self,
    ) -> list[dict[str, Any]]:
        """
        Returns all Projects v2.
        """

        if self._projects is not None:
            return self._projects

        data = self._graphql(
            GET_PROJECTS_QUERY,
            {
                "owner": self.owner,
            },
        )

        self._projects = data["data"]["user"]["projectsV2"]["nodes"]

        return self._projects

    def find_project(
        self,
        title: str,
    ) -> dict[str, Any] | None:
        """
        Finds a Project by title.
        """

        for project in self.get_projects():
            if project["title"] == title:
                return project

        return None

    def _require_project(
        self,
        title: str,
    ) -> dict[str, Any]:
        """
        Returns a Project.
        """

        project = self.find_project(title)

        if project is None:
            raise GitHubNotFoundError(f"Project '{title}' not found.")

        return project

    def get_project_id(
        self,
        title: str,
    ) -> str:
        """
        Returns Project node id.
        """

        return self._require_project(title)["id"]

    def add_project_item(
        self,
        project_id: str,
        issue_id: str,
    ) -> str:
        """
        Adds an Issue to a GitHub Project.
        """

        data = self._graphql(
            ADD_PROJECT_ITEM_MUTATION,
            {
                "projectId": project_id,
                "contentId": issue_id,
            },
        )

        return data["data"]["addProjectV2ItemById"]["item"]["id"]

    def get_project_item_id(
        self,
        project_id: str,
        issue_id: str,
    ) -> str:
        """
        Returns the Project item id for an Issue.
        """

        data = self._graphql(
            GET_PROJECT_ITEM_QUERY,
            {
                "projectId": project_id,
            },
        )

        items = data["data"]["node"]["items"]["nodes"]

        for item in items:
            content = item.get("content")

            if content is None:
                continue

            if content.get("id") == issue_id:
                return item["id"]

        raise GitHubNotFoundError("Issue was not found in the GitHub Project.")

    # =============================================================================
    # CUSTOM FIELDS API
    # =============================================================================

    def get_project_fields(
        self,
        project_id: str,
    ) -> list[dict[str, Any]]:
        """
        Returns the Project fields.
        """

        if project_id not in self._project_fields:
            data = self._graphql(
                GET_PROJECT_FIELDS_QUERY,
                {
                    "projectId": project_id,
                },
            )

            self._project_fields[project_id] = data["data"]["node"]["fields"]["nodes"]

        return self._project_fields[project_id]

    def find_project_field(
        self,
        project_id: str,
        name: str,
    ) -> dict[str, Any] | None:
        """
        Finds a Project field.
        """

        for field in self.get_project_fields(project_id):
            if field["name"] == name:
                return field

        return None

    def _require_project_field(
        self,
        project_id: str,
        name: str,
    ) -> dict[str, Any]:
        """
        Returns a Project field.
        """

        field = self.find_project_field(
            project_id,
            name,
        )

        if field is None:
            raise GitHubNotFoundError(f"Field '{name}' not found.")

        return field

    def get_project_field_id(
        self,
        project_id: str,
        name: str,
    ) -> str:
        """
        Returns the Project field id.
        """

        return self._require_project_field(project_id, name)["id"]

    def get_single_select_option(
        self,
        project_id: str,
        field_name: str,
        option_name: str,
    ) -> dict[str, Any]:
        """
        Returns a single select option.

        First attempts an exact match. If no exact match is found,
        attempts a normalized match that ignores leading/trailing
        whitespace and leading emoji symbols.
        """

        field = self._require_project_field(
            project_id,
            field_name,
        )

        options = field.get(
            "options",
            [],
        )

        # Exact match.
        for option in options:
        
            if option["name"] == option_name:
            
                return option
    
        # Normalized match for display prefixes such as emojis.
        normalized_option_name = option_name.strip()
    
        for option in options:
        
            normalized_name = option["name"].strip()
    
            if normalized_name.endswith(
                normalized_option_name
            ):
    
                return option
    
        raise GitHubNotFoundError(
            f"Option '{option_name}' not found."
        )

    def update_project_field(
        self,
        project_id: str,
        item_id: str,
        field_id: str,
        option_id: str,
    ) -> dict[str, Any]:
        """
        Updates a single-select Project field.
        """

        return self._graphql(
            UPDATE_PROJECT_FIELD_QUERY,
            {
                "projectId": project_id,
                "itemId": item_id,
                "fieldId": field_id,
                "optionId": option_id,
            },
        )

    def update_project_number_field(
        self,
        project_id: str,
        item_id: str,
        field_id: str,
        value: float,
    ) -> dict[str, Any]:
        """
        Updates a numeric Project field.
        """

        return self._graphql(
            UPDATE_PROJECT_NUMBER_FIELD_QUERY,
            {
                "projectId": project_id,
                "itemId": item_id,
                "fieldId": field_id,
                "numberValue": value,
            },
        )

    def set_estimated_hours(
        self,
        project_id: str,
        item_id: str,
        hours: float,
    ) -> dict[str, Any]:
        """
        Sets the Estimated Hours field.
        """

        field = self._require_project_field(
            project_id,
            "Estimated Hours",
        )

        return self.update_project_number_field(
            project_id,
            item_id,
            field["id"],
            hours,
        )

    def _set_single_select_field(
        self,
        project_id: str,
        item_id: str,
        field_name: str,
        option_name: str,
    ) -> dict[str, Any]:
        """
        Sets a single-select Project field.
        """

        field = self._require_project_field(
            project_id,
            field_name,
        )

        option = self.get_single_select_option(
            project_id,
            field_name,
            option_name,
        )

        return self.update_project_field(
            project_id,
            item_id,
            field["id"],
            option["id"],
        )

    def set_priority(
        self,
        project_id: str,
        item_id: str,
        priority: str,
    ) -> dict[str, Any]:
        """
        Sets the Priority field.
        """

        return self._set_single_select_field(
            project_id,
            item_id,
            "Priority",
            priority,
        )

    def set_business_value(
        self,
        project_id: str,
        item_id: str,
        value: str,
    ) -> dict[str, Any]:
        """
        Sets the Business Value field.
        """

        return self._set_single_select_field(
            project_id,
            item_id,
            "Business Value",
            value,
        )

    def set_complexity(
        self,
        project_id: str,
        item_id: str,
        value: str,
    ) -> dict[str, Any]:
        """
        Sets the Complexity field.
        """

        return self._set_single_select_field(
            project_id,
            item_id,
            "Complexity",
            value,
        )

    def set_sprint(
        self,
        project_id: str,
        item_id: str,
        sprint: str,
    ) -> dict[str, Any]:
        """
        Sets the Sprint field.
        """

        return self._set_single_select_field(
            project_id,
            item_id,
            "Sprint",
            sprint,
        )

    def set_status(
        self,
        project_id: str,
        item_id: str,
        status: str,
    ) -> dict[str, Any]:
        """
        Sets the Status field.
        """

        return self._set_single_select_field(
            project_id,
            item_id,
            "Status",
            status,
        )

    def set_issue_type(
        self,
        project_id: str,
        item_id: str,
        issue_type: str,
    ) -> dict[str, Any]:
        """
        Sets the Issue Type field.
        """

        return self._set_single_select_field(
            project_id,
            item_id,
            "Issue Type",
            issue_type,
        )

    def set_module(
        self,
        project_id: str,
        item_id: str,
        module: str,
    ) -> dict[str, Any]:
        """
        Sets the Module field.
        """

        return self._set_single_select_field(
            project_id,
            item_id,
            "Module",
            module,
        )
