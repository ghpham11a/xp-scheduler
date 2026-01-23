from fastapi import FastAPI
from contextlib import asynccontextmanager
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

from routers import users, availabilities, meetings


@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Starting up...")
    yield
    print("Shutting down...")


def create_app() -> FastAPI:

    app = FastAPI(lifespan=lifespan, title="XP Scheduler API")

    @app.get("/")
    def root():
        return {"status": "up"}

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["http://localhost:3000"],
        allow_credentials=True,
        allow_methods=["GET", "POST", "PUT", "DELETE"],
        allow_headers=["Content-Type"],
    )

    # Include routers
    app.include_router(users.router)
    app.include_router(availabilities.router)
    app.include_router(meetings.router)

    return app


load_dotenv()

# uvicorn main:app --host 0.0.0.0 --port 6969 --reload
app = create_app()
