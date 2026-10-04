"""Serve the saved FloodSense models.

    python3 -m pip install -r ml/requirements.txt
    python3 ml/serve_api.py

POST /predict
{
  "water_level": 7.4,
  "rainfall": 128.0,
  "temperature": 29.0,
  "humidity": 86.0,
  "online": true
}
"""

from __future__ import annotations

import sys
from pathlib import Path

import joblib
import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

sys.path.insert(0, str(Path(__file__).resolve().parent))
from flood_decision import decide  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = ROOT / "ml" / "models"

forest = joblib.load(MODEL_DIR / "flood_risk_rf.joblib")
anomaly = joblib.load(MODEL_DIR / "anomaly_iforest.joblib")

app = FastAPI(title="FloodSense ML", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class Reading(BaseModel):
    water_level: float
    rainfall: float
    temperature: float = 29.0
    humidity: float = 80.0
    ultrasonic_distance: float | None = None
    online: bool = True


@app.get("/")
def root() -> dict:
    return {
        "service": "FloodSense ML",
        "status": "ok",
        "model": "flood_risk_rf + anomaly_iforest",
        "try": {
            "docs": "http://127.0.0.1:8000/docs",
            "health": "http://127.0.0.1:8000/health",
            "predict": "POST http://127.0.0.1:8000/predict",
        },
        "example_body": {
            "water_level": 7.4,
            "rainfall": 128.0,
            "temperature": 29.0,
            "humidity": 86.0,
            "online": True,
        },
    }


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "model": "flood_risk_rf + anomaly_iforest"}


@app.post("/predict")
def predict(reading: Reading) -> dict:
    risk = int(forest.predict([[reading.water_level, reading.rainfall]])[0])
    probability = float(forest.predict_proba([[reading.water_level, reading.rainfall]])[0][risk])
    iso = int(anomaly.predict([[reading.water_level, reading.rainfall, reading.temperature, reading.humidity]])[0])
    return decide(reading, risk, probability, iso_outlier=iso == -1)


if __name__ == "__main__":
    uvicorn.run("serve_api:app", host="0.0.0.0", port=8000, reload=False)
