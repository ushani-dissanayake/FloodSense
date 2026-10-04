"""Download Irrigation Department gauge logs and join Open-Meteo daily rain.

Writes Data_Set/field_logs.csv. Source:
  https://github.com/nuuuwan/lk_irrigation  (Irrigation Dept. water levels)
  https://open-meteo.com                    (daily precipitation)

Run from the FloodSense folder:

    python3 ml/fetch_field_logs.py
"""

from __future__ import annotations

import json
import urllib.request
from datetime import date, datetime, timedelta, timezone
from pathlib import Path
import sys

import pandas as pd

sys.path.insert(0, str(Path(__file__).resolve().parent))
from field_stations import FIELD_STATIONS, official_risk  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "Data_Set" / "field_logs.csv"
RAW_DIR = ROOT / "Data_Set" / "field_raw"
GITHUB = "https://api.github.com/repos/nuuuwan/lk_irrigation/contents/data/rwlds"
RAW = "https://raw.githubusercontent.com/nuuuwan/lk_irrigation/main/data/rwlds"

# November 2025 southwest-coast floods + a recent dry week.
FLOOD_START = date(2025, 11, 23)
FLOOD_END = date(2025, 11, 30)
DRY_START = date(2026, 8, 8)
DRY_END = date(2026, 8, 15)
KEEP_HOURS = {0, 6, 12, 18}


def daterange(start: date, end: date):
    day = start
    while day <= end:
        yield day
        day += timedelta(days=1)


def get_json(url: str):
    request = urllib.request.Request(url, headers={"User-Agent": "FloodSense-field-validation"})
    with urllib.request.urlopen(request, timeout=45) as response:
        return json.loads(response.read().decode("utf-8"))


def list_day_files(path: str, day: date) -> list[str]:
    url = f"{GITHUB}/{path}/{day.year}/{day:%Y-%m}/{day:%Y-%m-%d}"
    try:
        listing = get_json(url)
    except Exception:
        return []
    names = []
    for item in listing:
        name = item.get("name", "")
        if not name.endswith(".json"):
            continue
        try:
            hour = int(name.split("-")[3])
        except (IndexError, ValueError):
            continue
        if hour in KEEP_HOURS:
            names.append(name)
    return names


def load_reading(path: str, day: date, name: str) -> dict | None:
    url = f"{RAW}/{path}/{day.year}/{day:%Y-%m}/{day:%Y-%m-%d}/{name}"
    try:
        payload = get_json(url)
    except Exception:
        return None
    water = payload.get("water_level_m")
    time_ut = payload.get("time_ut")
    if water is None or time_ut is None:
        return None
    return {
        "timestamp": datetime.fromtimestamp(int(time_ut), tz=timezone.utc).isoformat(),
        "water_level": float(water),
        "gauge": payload.get("station_name", ""),
    }


def fetch_rain_window(lat: float, lon: float, start: date, end: date) -> dict[str, float]:
    url = (
        "https://archive-api.open-meteo.com/v1/archive"
        f"?latitude={lat}&longitude={lon}&start_date={start.isoformat()}&end_date={end.isoformat()}"
        "&daily=precipitation_sum&timezone=Asia%2FColombo"
    )
    payload = get_json(url)
    days = payload.get("daily", {}).get("time", [])
    rain = payload.get("daily", {}).get("precipitation_sum", [])
    return {day: float(value or 0.0) for day, value in zip(days, rain)}


def fetch_rain(lat: float, lon: float) -> dict[str, float]:
    merged: dict[str, float] = {}
    merged.update(fetch_rain_window(lat, lon, FLOOD_START, FLOOD_END))
    merged.update(fetch_rain_window(lat, lon, DRY_START, DRY_END))
    return merged


def main() -> None:
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    rows: list[dict] = []
    days = list(daterange(FLOOD_START, FLOOD_END)) + list(daterange(DRY_START, DRY_END))

    for station in FIELD_STATIONS:
        print(f"Rain {station['gauge']}...")
        rain_by_day = fetch_rain(station["lat"], station["lon"])
        for day in days:
            window = "flood" if FLOOD_START <= day <= FLOOD_END else "dry"
            names = list_day_files(station["path"], day)
            for name in names:
                reading = load_reading(station["path"], day, name)
                if reading is None:
                    continue
                rainfall = rain_by_day.get(day.isoformat(), 0.0)
                observed = official_risk(
                    reading["water_level"],
                    station["alert_m"],
                    station["minor_m"],
                    station["major_m"],
                )
                rows.append(
                    {
                        "timestamp": reading["timestamp"],
                        "station_id": station["station_id"],
                        "app_name": station["app_name"],
                        "gauge": station["gauge"],
                        "river": station["river"],
                        "window": window,
                        "water_level": round(reading["water_level"], 3),
                        "rainfall": round(rainfall, 2),
                        "observed_risk": observed,
                        "alert_m": station["alert_m"],
                        "minor_m": station["minor_m"],
                        "major_m": station["major_m"],
                        "source": "Irrigation Department via nuuuwan/lk_irrigation + Open-Meteo rain",
                    }
                )
            print(f"  {station['gauge']} {day} ({len(names)} files)")

    frame = pd.DataFrame(rows).drop_duplicates(subset=["timestamp", "station_id"])
    frame = frame.sort_values(["station_id", "timestamp"])
    OUT.parent.mkdir(parents=True, exist_ok=True)
    frame.to_csv(OUT, index=False)
    print(f"Wrote {len(frame)} rows to {OUT}")
    print(frame.groupby(["station_id", "observed_risk"]).size())


if __name__ == "__main__":
    main()
