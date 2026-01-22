from fastapi import APIRouter, HTTPException
from typing import List

from models import User
from storage import get_users, get_user

router = APIRouter(prefix="/users", tags=["users"])


@router.get("", response_model=List[User])
def list_users():
    """Get all users."""
    return get_users()


@router.get("/{user_id}", response_model=User)
def read_user(user_id: str):
    """Get a single user by ID."""
    user = get_user(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user
