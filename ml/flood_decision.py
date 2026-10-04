"""Shared automated-decision rules used by the API and documented for the app."""

from __future__ import annotations

from typing import Any


RISK_LABEL = {0: "LOW", 1: "MEDIUM", 2: "HIGH"}


def decide(reading: Any, risk: int, probability: float, iso_outlier: bool) -> dict:
    reasons: list[str] = []
    if not getattr(reading, "online", True):
        reasons.append("Station offline — no fresh reading")
    if reading.water_level < 0 or reading.water_level > 10:
        reasons.append("Water level outside trained range 0–10 m")
    if reading.rainfall < 0 or reading.rainfall > 300:
        reasons.append("Rainfall outside trained range 0–300 mm")
    ultrasonic = getattr(reading, "ultrasonic_distance", None)
    if ultrasonic is not None and (ultrasonic < 0 or ultrasonic > 400):
        reasons.append("Ultrasonic distance outside range 0–400 cm")
    if iso_outlier:
        reasons.append("Isolation Forest flagged an unusual sensor pattern")

    anomaly = bool(reasons)
    if any("outside trained range" in reason or "offline" in reason.lower() for reason in reasons) and risk < 2:
        decision = "FLAG_SENSOR"
        action = "Check the ESP32, power, and calibration."
    elif risk == 2:
        decision = "ISSUE_ALERT"
        action = "Notify citizens immediately and keep the station on watch."
    elif risk == 1 or anomaly:
        decision = "WATCH"
        action = "Continue monitoring. Prepare alerts if water or rain keeps rising."
    else:
        decision = "NORMAL"
        action = "No automated action required."

    return {
        "risk_level": RISK_LABEL[risk],
        "risk_class": risk,
        "probability": round(probability, 4),
        "confidence": "High" if probability >= 0.75 else "Moderate",
        "anomaly": anomaly,
        "anomaly_reasons": reasons,
        "decision": decision,
        "action": action,
        "source": "Random Forest",
    }
