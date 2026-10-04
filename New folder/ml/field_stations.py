"""Official Irrigation Department / DMC stages for FloodSense rivers.

Alert / minor / major levels are from DMC Water Level & Rainfall bulletins.
These are independent of the on-device tree (4.97 m watch / 8.0 m high).
"""

from __future__ import annotations

# Same rules as the ESP32 and FloodRiskEngine.
# HIGH water 8.0 m = 200 ml in the 250 ml demo beaker (full = 10 m).
TREE_HIGH_RAIN_MM = 200.0
TREE_HIGH_WATER_M = 8.0
TREE_MEDIUM_RAIN_MM = 59.97
TREE_MEDIUM_WATER_M = 4.97

# Map app station ids to the nearest official gauging station.
FIELD_STATIONS = [
    {
        "station_id": "kelani-seethawaka",
        "app_name": "Kelani River - Seethawaka",
        "gauge": "Deraniyagala",
        "river": "Seethawaka Ganga",
        "path": "kelani-ganga-basin/seethawaka-ganga-river/deraniyagala-station",
        "lat": 6.93,
        "lon": 80.34,
        "alert_m": 4.80,
        "minor_m": 5.80,
        "major_m": 6.40,
    },
    {
        "station_id": "kalu-millaniya",
        "app_name": "Kalu Ganga - Millaniya",
        "gauge": "Putupaula",
        "river": "Kalu Ganga",
        "path": "kalu-ganga-basin/kalu-ganga-river/putupaula-station",
        "lat": 6.6116,
        "lon": 80.0597,
        "alert_m": 3.00,
        "minor_m": 4.00,
        "major_m": 5.00,
    },
    {
        "station_id": "gin-gampaha",
        "app_name": "Gin Ganga - Gampaha",
        "gauge": "Baddegama",
        "river": "Gin Ganga",
        "path": "gin-ganga-basin/gin-ganga-river/baddegama-station",
        "lat": 6.1775,
        "lon": 80.1805,
        "alert_m": 3.50,
        "minor_m": 4.00,
        "major_m": 5.00,
    },
    {
        "station_id": "attanagalu-jaela",
        "app_name": "Attanagalu Oya - Ja-Ela",
        "gauge": "Dunamale",
        "river": "Attanagalu Oya",
        "path": "aththanagalu-oya-basin/aththanagalu-oya-river/dunamale-station",
        "lat": 7.1130,
        "lon": 80.0790,
        "alert_m": 3.30,
        "minor_m": 4.40,
        "major_m": 5.50,
    },
]


def official_risk(water_m: float, alert_m: float, minor_m: float, major_m: float) -> int:
    """0 = Normal, 1 = Alert, 2 = Minor or Major flood."""
    if water_m >= minor_m:
        return 2
    if water_m >= alert_m:
        return 1
    return 0


def tree_risk(water_m: float, rainfall_mm: float) -> int:
    if rainfall_mm > TREE_HIGH_RAIN_MM or water_m > TREE_HIGH_WATER_M:
        return 2
    if rainfall_mm > TREE_MEDIUM_RAIN_MM or water_m > TREE_MEDIUM_WATER_M:
        return 1
    return 0
