"""Validate FloodSense risk rules on real field logs.

The 100% hold-out score on Data_Set/flood_data.csv is not field accuracy.
Those labels are a deterministic function of the same water/rain thresholds
the tree recovered. This script:

1. Measures that label leakage on the synthetic CSV.
2. Scores the deployed tree against official DMC stages on field_logs.csv.
3. Writes ml/models/field_validation.json for the app and report.

Run from the FloodSense folder:

    python3 ml/fetch_field_logs.py
    python3 ml/validate_field_logs.py
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

import pandas as pd
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix

sys.path.insert(0, str(Path(__file__).resolve().parent))
from field_stations import (  # noqa: E402
    TREE_HIGH_RAIN_MM,
    TREE_HIGH_WATER_M,
    TREE_MEDIUM_RAIN_MM,
    TREE_MEDIUM_WATER_M,
    official_risk,
    tree_risk,
)

ROOT = Path(__file__).resolve().parents[1]
SYNTHETIC = ROOT / "Data_Set" / "flood_data.csv"
FIELD = ROOT / "Data_Set" / "field_logs.csv"
OUT = ROOT / "ml" / "models" / "field_validation.json"
ASSET = (
    ROOT
    / "FloodSense-Kotlin-Android-Starter"
    / "FloodSense-Kotlin-Android"
    / "app"
    / "src"
    / "main"
    / "assets"
    / "field_validation.json"
)


def leakage_report(frame: pd.DataFrame) -> dict:
    rebuilt = [
        tree_risk(float(row.water_level), float(row.rainfall))
        for row in frame.itertuples(index=False)
    ]
    labels = frame["flood_risk"].astype(int).tolist()
    acc = float(accuracy_score(labels, rebuilt))
    return {
        "rows": int(len(frame)),
        "rule_reconstructs_labels": acc,
        "note": (
            "The documented 4.97/8.0 m and 59.97/200 mm tree reconstructs "
            "only part of flood_data.csv. Random Forest still scores 100% on a "
            "random split of that same synthetic file, which is overfitting, "
            "not field skill."
        ),
        "tree_rules": {
            "high_rainfall_mm": TREE_HIGH_RAIN_MM,
            "high_water_m": TREE_HIGH_WATER_M,
            "medium_rainfall_mm": TREE_MEDIUM_RAIN_MM,
            "medium_water_m": TREE_MEDIUM_WATER_M,
        },
    }


def field_report(frame: pd.DataFrame) -> dict:
    predicted = [
        tree_risk(float(row.water_level), float(row.rainfall))
        for row in frame.itertuples(index=False)
    ]
    observed = frame["observed_risk"].astype(int).tolist()
    acc = float(accuracy_score(observed, predicted))
    matrix = confusion_matrix(observed, predicted, labels=[0, 1, 2]).tolist()
    report = classification_report(
        observed,
        predicted,
        labels=[0, 1, 2],
        target_names=["LOW/Normal", "MEDIUM/Alert", "HIGH/Minor+Major"],
        digits=3,
        output_dict=True,
        zero_division=0,
    )
    by_station = {}
    for station_id, group in frame.groupby("station_id"):
        pred = [tree_risk(float(r.water_level), float(r.rainfall)) for r in group.itertuples(index=False)]
        by_station[str(station_id)] = {
            "rows": int(len(group)),
            "accuracy": round(float(accuracy_score(group["observed_risk"], pred)), 4),
            "official_high": int((group["observed_risk"] == 2).sum()),
            "model_high": int(sum(value == 2 for value in pred)),
            "water_min": round(float(group["water_level"].min()), 3),
            "water_max": round(float(group["water_level"].max()), 3),
            "rain_max": round(float(group["rainfall"].max()), 2),
        }

    missed_high = int(((frame["observed_risk"] == 2) & (pd.Series(predicted) != 2)).sum())
    false_high = int(((frame["observed_risk"] != 2) & (pd.Series(predicted) == 2)).sum())
    return {
        "rows": int(len(frame)),
        "stations": int(frame["station_id"].nunique()),
        "window": sorted(frame["window"].dropna().unique().tolist()),
        "source": frame["source"].iloc[0] if "source" in frame else "",
        "accuracy_vs_official_stages": round(acc, 4),
        "missed_official_high": missed_high,
        "false_model_high": false_high,
        "confusion_matrix": matrix,
        "classification_report": report,
        "by_station": by_station,
        "label": {
            "0": "DMC Normal (below alert)",
            "1": "DMC Alert",
            "2": "DMC Minor or Major flood",
        },
        "note": (
            "Ground truth is the official Irrigation Department / DMC stage "
            "at the nearest gauge. Rain is Open-Meteo daily precipitation. "
            "This is not the synthetic CSV label."
        ),
    }


def main() -> dict:
    synthetic = pd.read_csv(SYNTHETIC)
    leak = leakage_report(synthetic)

    if not FIELD.exists():
        raise SystemExit(f"Missing {FIELD}. Run: python3 ml/fetch_field_logs.py")

    field = pd.read_csv(FIELD)
    if field.empty:
        raise SystemExit(f"{FIELD} is empty.")

    # Recompute official labels in case thresholds were edited.
    field["observed_risk"] = [
        official_risk(float(row.water_level), float(row.alert_m), float(row.minor_m), float(row.major_m))
        for row in field.itertuples(index=False)
    ]
    field_metrics = field_report(field)

    payload = {
        "synthetic": leak,
        "field": field_metrics,
        "headline": (
            f"Synthetic rule leakage {leak['rule_reconstructs_labels']:.0%}; "
            f"field accuracy vs DMC stages {field_metrics['accuracy_vs_official_stages']:.1%}"
        ),
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(payload, indent=2), encoding="utf-8")
    ASSET.parent.mkdir(parents=True, exist_ok=True)
    ASSET.write_text(json.dumps(payload, indent=2), encoding="utf-8")

    print(payload["headline"])
    print("Synthetic leakage:", leak["rule_reconstructs_labels"])
    print("Field accuracy:", field_metrics["accuracy_vs_official_stages"])
    print("Confusion matrix [true rows x pred cols 0/1/2]:", field_metrics["confusion_matrix"])
    print("Missed official HIGH:", field_metrics["missed_official_high"])
    print("Saved", OUT)
    return payload


if __name__ == "__main__":
    main()
