import uuid
from fastapi import APIRouter, HTTPException
from typing import List

from models import Meeting, MeetingCreate
from storage import get_meetings, get_meeting, save_meeting, delete_meeting

router = APIRouter(prefix="/meetings", tags=["meetings"])


@router.get("", response_model=List[Meeting])
def list_meetings():
    """Get all meetings."""
    return get_meetings()


@router.get("/{meeting_id}", response_model=Meeting)
def read_meeting(meeting_id: str):
    """Get a single meeting by ID."""
    meeting = get_meeting(meeting_id)
    if not meeting:
        raise HTTPException(status_code=404, detail="Meeting not found")
    return meeting


@router.post("", response_model=Meeting)
def create_meeting(meeting: MeetingCreate):
    """Create a new meeting."""
    meeting_data = meeting.model_dump()
    meeting_data["id"] = f"{uuid.uuid4()}"
    return save_meeting(meeting_data)


@router.delete("/{meeting_id}")
def remove_meeting(meeting_id: str):
    """Delete a meeting."""
    if not delete_meeting(meeting_id):
        raise HTTPException(status_code=404, detail="Meeting not found")
    return {"status": "deleted", "id": meeting_id}
