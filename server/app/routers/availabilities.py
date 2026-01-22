from fastapi import APIRouter, HTTPException
from typing import List

from models import Availability, TimeSlot
from storage import get_availabilities, get_availability, save_availability

router = APIRouter(prefix="/availabilities", tags=["availabilities"])


@router.get("", response_model=List[Availability])
def list_availabilities():
    """Get all availabilities."""
    return get_availabilities()


@router.get("/{user_id}", response_model=Availability)
def read_availability(user_id: str):
    """Get availability for a specific user."""
    availability = get_availability(user_id)
    if not availability:
        # Return empty slots if no availability set
        return {"userId": user_id, "slots": []}
    return availability


@router.put("/{user_id}", response_model=Availability)
def update_availability(user_id: str, slots: List[TimeSlot]):
    """Update availability for a user."""
    slots_data = [slot.model_dump() for slot in slots]
    return save_availability(user_id, slots_data)
