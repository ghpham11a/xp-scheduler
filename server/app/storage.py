import json
import os
from pathlib import Path
from typing import List, Dict, Any

from models import User, Availability, Meeting

# Data directory
DATA_DIR = Path(__file__).parent.parent / "data"

# Default users (seeded if file doesn't exist)
DEFAULT_USERS = [
    {
        "id": "user-1",
        "name": "Alice Johnson",
        "email": "alice@example.com",
        "avatarColor": "#3B82F6",
    },
    {
        "id": "user-2",
        "name": "Bob Smith",
        "email": "bob@example.com",
        "avatarColor": "#10B981",
    },
    {
        "id": "user-3",
        "name": "Carol Williams",
        "email": "carol@example.com",
        "avatarColor": "#F59E0B",
    },
    {
        "id": "user-4",
        "name": "David Brown",
        "email": "david@example.com",
        "avatarColor": "#EF4444",
    },
]


def ensure_data_dir():
    """Ensure data directory exists."""
    DATA_DIR.mkdir(parents=True, exist_ok=True)


def get_file_path(filename: str) -> Path:
    """Get full path for a data file."""
    return DATA_DIR / filename


def read_json(filename: str, default: Any = None) -> Any:
    """Read JSON from file, return default if doesn't exist."""
    ensure_data_dir()
    filepath = get_file_path(filename)
    if not filepath.exists():
        return default if default is not None else []
    with open(filepath, "r") as f:
        return json.load(f)


def write_json(filename: str, data: Any) -> None:
    """Write data to JSON file."""
    ensure_data_dir()
    filepath = get_file_path(filename)
    with open(filepath, "w") as f:
        json.dump(data, f, indent=2)


# Users
def get_users() -> List[Dict]:
    """Get all users, seeding defaults if empty."""
    users = read_json("users.json")
    if not users:
        write_json("users.json", DEFAULT_USERS)
        return DEFAULT_USERS
    return users


def get_user(user_id: str) -> Dict | None:
    """Get a single user by ID."""
    users = get_users()
    return next((u for u in users if u["id"] == user_id), None)


def save_users(users: List[Dict]) -> None:
    """Save users list."""
    write_json("users.json", users)


# Availabilities
def get_availabilities() -> List[Dict]:
    """Get all availabilities."""
    return read_json("availabilities.json", [])


def get_availability(user_id: str) -> Dict | None:
    """Get availability for a user."""
    availabilities = get_availabilities()
    return next((a for a in availabilities if a["userId"] == user_id), None)


def save_availability(user_id: str, slots: List[Dict]) -> Dict:
    """Save or update availability for a user."""
    availabilities = get_availabilities()

    # Find existing or create new
    existing = next((a for a in availabilities if a["userId"] == user_id), None)
    if existing:
        existing["slots"] = slots
    else:
        availabilities.append({"userId": user_id, "slots": slots})

    write_json("availabilities.json", availabilities)
    return {"userId": user_id, "slots": slots}


def save_all_availabilities(availabilities: List[Dict]) -> None:
    """Save all availabilities."""
    write_json("availabilities.json", availabilities)


# Meetings
def get_meetings() -> List[Dict]:
    """Get all meetings."""
    return read_json("meetings.json", [])


def get_meeting(meeting_id: str) -> Dict | None:
    """Get a single meeting by ID."""
    meetings = get_meetings()
    return next((m for m in meetings if m["id"] == meeting_id), None)


def save_meeting(meeting: Dict) -> Dict:
    """Add a new meeting."""
    meetings = get_meetings()
    meetings.append(meeting)
    write_json("meetings.json", meetings)
    return meeting


def delete_meeting(meeting_id: str) -> bool:
    """Delete a meeting by ID."""
    meetings = get_meetings()
    original_len = len(meetings)
    meetings = [m for m in meetings if m["id"] != meeting_id]
    if len(meetings) < original_len:
        write_json("meetings.json", meetings)
        return True
    return False


def save_all_meetings(meetings: List[Dict]) -> None:
    """Save all meetings."""
    write_json("meetings.json", meetings)
