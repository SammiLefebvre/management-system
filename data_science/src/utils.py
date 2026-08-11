import json
from pathlib import Path
from typing import Any

import joblib

MODEL_DIR = Path(__file__).resolve().parents[1] / "models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

RAW_DIR = Path(__file__).resolve().parents[1] / "data" / "raw"
RAW_DIR.mkdir(parents=True, exist_ok=True)


def save_artifact(name: str, obj: Any, metadata: dict) -> None:
    """Persist a fitted model/pipeline and a JSON sidecar with metrics."""
    joblib.dump(obj, MODEL_DIR / f"{name}.pkl")
    (MODEL_DIR / f"{name}.json").write_text(
        json.dumps(metadata, indent=2, ensure_ascii=False), encoding="utf-8"
    )


def load_artifact(name: str) -> Any:
    return joblib.load(MODEL_DIR / f"{name}.pkl")
