from pydantic import BaseModel
from typing import List, Optional


class User(BaseModel):
    id: str
    name: str
    email: str
    avatarColor: str


class TimeSlot(BaseModel):
    date: str  # ISO date string (YYYY-MM-DD)
    startHour: float  # 0-24, supports 0.5 increments
    endHour: float


class Availability(BaseModel):
    userId: str
    slots: List[TimeSlot]


class Meeting(BaseModel):
    id: str
    organizerId: str
    participantId: str
    date: str
    startHour: float
    endHour: float
    title: str


class MeetingCreate(BaseModel):
    organizerId: str
    participantId: str
    date: str
    startHour: float
    endHour: float
    title: str
