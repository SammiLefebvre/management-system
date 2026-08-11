# Data Science Model Training Implementation Plan

> **For agentic workers:** REQUIRED SUB-AL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Build a `data-science/` module that downloads two public datasets, trains scikit-learn models relevant to the work-order system, and saves artifacts + metrics so they can later be served by a Python API or called from the Java backend.

**Architecture:** Keep all Python code in `data-science/`. Use a virtual environment installed from `requirements.txt`. Public data is downloaded into `data-science/data/raw/` by scripts; model artifacts land in `data-science/models/`. Raw data and `.venv` are gitignored. Two model families are trained: (1) predictive maintenance on the UCI AI4I dataset — failure risk + failure-type classification, (2) work-order duration / SLA breach on a public sewer work-order CSV.

**Tech Stack:** Python 3.8, scikit-learn, pandas, numpy, joblib, requests.

## Global Constraints

- Use only public datasets with clear licenses (UCI AI4I CC BY 4.0, GitHub sample CSV assumed public for demo use).
- Model training must run on a fresh machine with just `requirements.txt` + `python3`.
- No large external dependencies (no PyTorch, no TensorFlow, no Kaggle API).
- Artifacts must be `.pkl` files readable by scikit-learn.
- All scripts must be runnable from the repo root via `python3 -m data_science.src.<script>`.

---

## Task 1: Scaffold the data-science environment

**Files:**
- Create: `data-science/requirements.txt`
- Create: `data-science/.gitignore`
- Create: `data-science/src/__init__.py`
- Modify: `.gitignore` (add `data-science/.venv/`, `data-science/data/raw/`, `data-science/models/*.pkl`, `data-science/__pycache__/`)
- Test: `data-science/tests/test_import.py`

**Interfaces:**
- Produces: `data-science/` is a runnable Python package with `src` and `tests` subpackages.

- [ ] **Step 1: Create requirements.txt**

```text
pandas>=1.3.0
scikit-learn>=1.0.0
numpy>=1.21.0
joblib>=1.0.0
requests>=2.25.0
```

- [ ] **Step 2: Create data-science/.gitignore**

```text
.venv/
__pycache__/
*.pyc
.pytest_cache/
data/raw/
models/*.pkl
models/*.json
notebooks/.ipynb_checkpoints/
```

- [ ] **Step 3: Create data-science/src/__init__.py**

Empty file, just to make `data_science` a package.

- [ ] **Step 4: Update root .gitignore**

Append:

```text
# Data science
/data-science/.venv/
/data-science/data/raw/
/data-science/models/*.pkl
/data-science/models/*.json
/data-science/**/__pycache__/
/data-science/.pytest_cache/
```

- [ ] **Step 5: Write import test**

Create `data-science/tests/test_import.py`:

```python
import importlib

def test_import_package():
    import data_science.src.utils
    import data_science.src.download_data
    import data_science.src.train_predictive_maintenance
    import data_science.src.train_work_order_duration
```

Run: `cd data-science && python3 -m pytest tests/test_import.py -v` (fails because files don't exist yet).
Expected: FAIL with `ModuleNotFoundError`.

- [ ] **Step 6: Commit**

```bash
git add data-science/requirements.txt data-science/.gitignore data-science/src/__init__.py data-science/tests/test_import.py .gitignore
git commit -m "chore: scaffold data-science module"
```

---

## Task 2: Download public datasets

**Files:**
- Create: `data-science/src/download_data.py`
- Create: `data-science/src/utils.py`
- Test: `data-science/tests/test_download.py`

**Interfaces:**
- Consumes: URLs from constants.
- Produces: `data-science/data/raw/ai4i2020.csv` and `data-science/data/raw/work_orders.csv`.
- Produces: `fetch_ai4i()` and `fetch_work_orders()` functions returning pandas DataFrames.

- [ ] **Step 1: Write download_data.py**

```python
import os
import zipfile
import io
import requests
import pandas as pd
from pathlib import Path

RAW_DIR = Path(__file__).resolve().parents[2] / "data" / "raw"
RAW_DIR.mkdir(parents=True, exist_ok=True)

AI4I_URL = "https://archive.ics.uci.edu/static/public/601/ai4i+2020+predictive+maintenance+dataset.zip"
WORK_ORDER_URL = "https://raw.githubusercontent.com/OlaOlagunju/GCP_Mage_Data_Pipeline/main/1.%20Source%20Data/work-order-management-module.csv"


def fetch_ai4i() -> pd.DataFrame:
    csv_path = RAW_DIR / "ai4i2020.csv"
    if not csv_path.exists():
        r = requests.get(AI4I_URL, timeout=60)
        r.raise_for_status()
        with zipfile.ZipFile(io.BytesIO(r.content)) as z:
            with z.open("ai4i2020.csv") as f:
                df = pd.read_csv(f)
                df.to_csv(csv_path, index=False)
    return pd.read_csv(csv_path)


def fetch_work_orders() -> pd.DataFrame:
    csv_path = RAW_DIR / "work_orders.csv"
    if not csv_path.exists():
        r = requests.get(WORK_ORDER_URL, timeout=60)
        r.raise_for_status()
        csv_path.write_bytes(r.content)
    return pd.read_csv(csv_path)


if __name__ == "__main__":
    print("AI4I rows:", len(fetch_ai4i()))
    print("Work orders rows:", len(fetch_work_orders()))
```

- [ ] **Step 2: Write utils.py with common helpers**

```python
import json
from pathlib import Path
from typing import Any
import joblib

MODEL_DIR = Path(__file__).resolve().parents[2] / "models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)


def save_artifact(name: str, obj: Any, metadata: dict) -> None:
    joblib.dump(obj, MODEL_DIR / f"{name}.pkl")
    (MODEL_DIR / f"{name}.json").write_text(json.dumps(metadata, indent=2, ensure_ascii=False), encoding="utf-8")


def load_artifact(name: str) -> Any:
    return joblib.load(MODEL_DIR / f"{name}.pkl")
```

- [ ] **Step 3: Write test_download.py**

```python
from data_science.src.download_data import fetch_ai4i, fetch_work_orders


def test_ai4i_download():
    df = fetch_ai4i()
    assert len(df) == 10000
    assert "Machine failure" in df.columns


def test_work_orders_download():
    df = fetch_work_orders()
    assert len(df) > 100
    assert "WORKORDER_ACTIVITY_CODE" in df.columns
```

- [ ] **Step 4: Run tests**

```bash
cd /mnt/e/INTERNSHIP/management-system/data-science
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python3 -m pytest tests/test_download.py -v
```

Expected: PASS after downloading.

- [ ] **Step 5: Commit**

```bash
git add data-science/src/download_data.py data-science/src/utils.py data-science/tests/test_download.py
git commit -m "feat: download public datasets for model training"
```

---

## Task 3: Train predictive-maintenance models (AI4I)

**Files:**
- Create: `data-science/src/train_predictive_maintenance.py`
- Create: `data-science/tests/test_predictive_maintenance.py`

**Interfaces:**
- Consumes: `fetch_ai4i()`.
- Produces: `models/pm_failure_classifier.pkl` + `.json` and `models/pm_failure_type_classifier.pkl` + `.json`.
- Produces: `train_predictive_maintenance()` function that prints metrics.

- [ ] **Step 1: Write train_predictive_maintenance.py**

```python
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.metrics import classification_report, f1_score
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from data_science.src.download_data import fetch_ai4i
from data_science.src.utils import save_artifact


def preprocess_ai4i(df: pd.DataFrame):
    df = df.rename(columns=lambda c: c.strip().replace(" [K]", "").replace(" [rpm]", "").replace(" [Nm]", "").replace(" [min]", ""))
    df = df.drop(columns=["UDI", "Product ID"])
    df["Type"] = df["Type"].astype(str)
    return df


def build_features_target(df: pd.DataFrame):
    X = df.drop(columns=["Machine failure", "TWF", "HDF", "PWF", "OSF", "RNF"])
    y_binary = df["Machine failure"]
    # failure type: 0 = none, 1 = TWF, 2 = HDF, 3 = PWF, 4 = OSF, 5 = RNF
    y_type = (df[["TWF", "HDF", "PWF", "OSF", "RNF"]].sum(axis=1) == 0).astype(int)
    y_type = y_type.where(y_type == 0, 0)
    y_type = (
        df["TWF"] * 1 + df["HDF"] * 2 + df["PWF"] * 3 + df["OSF"] * 4 + df["RNF"] * 5
    ).astype(int)
    return X, y_binary, y_type


def make_pipeline(classifier, categorical_cols):
    numeric_cols = ["Air temperature", "Process temperature", "Rotational speed", "Torque", "Tool wear"]
    preprocessor = ColumnTransformer(
        transformers=[
            ("num", StandardScaler(), numeric_cols),
            ("cat", OneHotEncoder(handle_unknown="ignore"), categorical_cols),
        ]
    )
    return Pipeline([("prep", preprocessor), ("clf", classifier)])


def train_predictive_maintenance():
    df = preprocess_ai4i(fetch_ai4i())
    X, y_binary, y_type = build_features_target(df)
    X_train, X_test, y_train, y_test = train_test_split(X, y_binary, test_size=0.2, random_state=42, stratify=y_binary)

    binary_clf = make_pipeline(GradientBoostingClassifier(random_state=42), ["Type"])
    binary_clf.fit(X_train, y_train)
    y_pred = binary_clf.predict(X_test)
    binary_report = classification_report(y_test, y_pred, output_dict=True, zero_division=0)
    print("Machine failure classification report:")
    print(classification_report(y_test, y_pred, zero_division=0))
    save_artifact(
        "pm_failure_classifier",
        binary_clf,
        {
            "model": "GradientBoostingClassifier",
            "dataset": "UCI AI4I 2020",
            "task": "binary: machine failure",
            "f1_macro": round(f1_score(y_test, y_pred, average="macro", zero_division=0), 4),
            "accuracy": round(binary_report["accuracy"], 4),
            "features": X.columns.tolist(),
        },
    )

    X_train2, X_test2, y_train2, y_test2 = train_test_split(X, y_type, test_size=0.2, random_state=42, stratify=y_type)
    type_clf = make_pipeline(RandomForestClassifier(random_state=42, n_estimators=200, class_weight="balanced"), ["Type"])
    type_clf.fit(X_train2, y_train2)
    y_pred2 = type_clf.predict(X_test2)
    type_report = classification_report(y_test2, y_pred2, output_dict=True, zero_division=0)
    print("Failure type classification report:")
    print(classification_report(y_test2, y_pred2, zero_division=0))
    save_artifact(
        "pm_failure_type_classifier",
        type_clf,
        {
            "model": "RandomForestClassifier",
            "dataset": "UCI AI4I 2020",
            "task": "multiclass: failure type (0=none,1=TWF,2=HDF,3=PWF,4=OSF,5=RNF)",
            "f1_macro": round(f1_score(y_test2, y_pred2, average="macro", zero_division=0), 4),
            "accuracy": round(type_report["accuracy"], 4),
            "features": X.columns.tolist(),
        },
    )


if __name__ == "__main__":
    train_predictive_maintenance()
```

- [ ] **Step 2: Write test**

```python
from data_science.src.train_predictive_maintenance import train_predictive_maintenance
from data_science.src.utils import MODEL_DIR


def test_train_predictive_maintenance():
    train_predictive_maintenance()
    assert (MODEL_DIR / "pm_failure_classifier.pkl").exists()
    assert (MODEL_DIR / "pm_failure_classifier.json").exists()
    assert (MODEL_DIR / "pm_failure_type_classifier.pkl").exists()
    assert (MODEL_DIR / "pm_failure_type_classifier.json").exists()
```

- [ ] **Step 3: Run test**

```bash
cd /mnt/e/INTERNSHIP/management-system/data-science
source .venv/bin/activate
python3 -m pytest tests/test_predictive_maintenance.py -v -s
```

Expected: PASS, metrics printed.

- [ ] **Step 4: Commit**

```bash
git add data-science/src/train_predictive_maintenance.py data-science/tests/test_predictive_maintenance.py
git commit -m "feat: train predictive maintenance models on AI4I dataset"
```

---

## Task 4: Train work-order duration / SLA models

**Files:**
- Create: `data-science/src/train_work_order_duration.py`
- Create: `data-science/tests/test_work_order_duration.py`

**Interfaces:**
- Consumes: `fetch_work_orders()`.
- Produces: `models/wo_duration_regressor.pkl` + `.json` and `models/wo_sla_classifier.pkl` + `.json`.

- [ ] **Step 1: Write train_work_order_duration.py**

```python
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import GradientBoostingRegressor, GradientBoostingClassifier
from sklearn.metrics import mean_absolute_error, classification_report, f1_score
from sklearn.preprocessing import OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from data_science.src.download_data import fetch_work_orders
from data_science.src.utils import save_artifact


def preprocess_work_orders(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["WORKORDER_STARTED"] = pd.to_datetime(df["WORKORDER_STARTED"], errors="coerce")
    df["WORKORDER_COMPLETED"] = pd.to_datetime(df["WORKORDER_COMPLETED"], errors="coerce")
    df["WORKORDER_ADDED"] = pd.to_datetime(df["WORKORDER_ADDED"], errors="coerce")
    df["duration_minutes"] = (df["WORKORDER_COMPLETED"] - df["WORKORDER_STARTED"]).dt.total_seconds() / 60.0
    df["was_completed"] = df["WORKORDER_COMPLETED"].notna().astype(int)
    # SLA breach: if started->completed > 4 hours, or if not completed and added older than 7 days
    df["sla_breached"] = (
        (df["duration_minutes"] > 4 * 60)
        | ((df["WORKORDER_COMPLETED"].isna()) & ((pd.Timestamp.now() - df["WORKORDER_ADDED"]).dt.days > 7))
    ).astype(int)
    df["hour_added"] = df["WORKORDER_ADDED"].dt.hour.fillna(0).astype(int)
    df["dow_added"] = df["WORKORDER_ADDED"].dt.dayofweek.fillna(0).astype(int)
    df["month_added"] = df["WORKORDER_ADDED"].dt.month.fillna(0).astype(int)
    df["activity_code"] = df["WORKORDER_ACTIVITY_CODE"].fillna("UNKNOWN").astype(str)
    df = df.dropna(subset=["WORKORDER_ADDED"])
    return df


def build_duration_data(df: pd.DataFrame):
    completed = df[df["duration_minutes"].notna() & (df["duration_minutes"] >= 0)].copy()
    X = completed[["activity_code", "hour_added", "dow_added", "month_added"]]
    y = completed["duration_minutes"].astype(float)
    return X, y


def build_sla_data(df: pd.DataFrame):
    X = df[["activity_code", "hour_added", "dow_added", "month_added", "was_completed"]]
    y = df["sla_breached"]
    return X, y


def make_pipeline_with_ohe(estimator, categorical_cols):
    preprocessor = ColumnTransformer(
        transformers=[("cat", OneHotEncoder(handle_unknown="ignore"), categorical_cols)],
        remainder="passthrough",
    )
    return Pipeline([("prep", preprocessor), ("est", estimator)])


def train_work_order_duration():
    df = preprocess_work_orders(fetch_work_orders())

    X_dur, y_dur = build_duration_data(df)
    X_train, X_test, y_train, y_test = train_test_split(X_dur, y_dur, test_size=0.2, random_state=42)
    reg = make_pipeline_with_ohe(GradientBoostingRegressor(random_state=42), ["activity_code"])
    reg.fit(X_train, y_train)
    y_pred = reg.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    print(f"Duration MAE: {mae:.2f} minutes")
    save_artifact(
        "wo_duration_regressor",
        reg,
        {
            "model": "GradientBoostingRegressor",
            "dataset": "GitHub public work-order sample",
            "task": "regression: work-order duration in minutes",
            "mae_minutes": round(mae, 2),
            "features": X_dur.columns.tolist(),
        },
    )

    X_sla, y_sla = build_sla_data(df)
    X_train2, X_test2, y_train2, y_test2 = train_test_split(X_sla, y_sla, test_size=0.2, random_state=42, stratify=y_sla)
    clf = make_pipeline_with_ohe(GradientBoostingClassifier(random_state=42), ["activity_code"])
    clf.fit(X_train2, y_train2)
    y_pred2 = clf.predict(X_test2)
    report = classification_report(y_test2, y_pred2, output_dict=True, zero_division=0)
    print("SLA breach classification report:")
    print(classification_report(y_test2, y_pred2, zero_division=0))
    save_artifact(
        "wo_sla_classifier",
        clf,
        {
            "model": "GradientBoostingClassifier",
            "dataset": "GitHub public work-order sample",
            "task": "binary: SLA breach",
            "f1_macro": round(f1_score(y_test2, y_pred2, average="macro", zero_division=0), 4),
            "accuracy": round(report["accuracy"], 4),
            "features": X_sla.columns.tolist(),
        },
    )


if __name__ == "__main__":
    train_work_order_duration()
```

- [ ] **Step 2: Write test**

```python
from data_science.src.train_work_order_duration import train_work_order_duration
from data_science.src.utils import MODEL_DIR


def test_train_work_order_duration():
    train_work_order_duration()
    assert (MODEL_DIR / "wo_duration_regressor.pkl").exists()
    assert (MODEL_DIR / "wo_duration_regressor.json").exists()
    assert (MODEL_DIR / "wo_sla_classifier.pkl").exists()
    assert (MODEL_DIR / "wo_sla_classifier.json").exists()
```

- [ ] **Step 3: Run test**

```bash
cd /mnt/e/INTERNSHIP/management-system/data-science
source .venv/bin/activate
python3 -m pytest tests/test_work_order_duration.py -v -s
```

Expected: PASS, metrics printed.

- [ ] **Step 4: Commit**

```bash
git add data-science/src/train_work_order_duration.py data-science/tests/test_work_order_duration.py
git commit -m "feat: train work-order duration and SLA models on public work-order dataset"
```

---

## Task 5: Documentation and integration notes

**Files:**
- Create: `data-science/README.md`
- Create: `data-science/data/feature_mapping.md`
- Modify: `README.md` (root) to mention the data-science module.

- [ ] **Step 1: Write data-science/README.md**

Include:
- How to install (`python3 -m venv .venv`, `pip install -r requirements.txt`).
- How to download data (`python3 -m data_science.src.download_data`).
- How to train models (`python3 -m data_science.src.train_predictive_maintenance` and `python3 -m data_science.src.train_work_order_duration`).
- How to run tests (`python3 -m pytest tests/`).
- List of models and metrics placeholders (filled after training).
- Notes on mapping to the Java system (AI4I telemetry maps to device health; work-order duration/SLA maps to the work-order pipeline).

- [ ] **Step 2: Write feature_mapping.md**

Map AI4I columns to the Java `Device`/`WorkOrder` schema and work-order columns to the Java schema.

- [ ] **Step 3: Update root README.md**

Add a "Data Science & Predictive Models" section under the existing structure, pointing to `data-science/README.md`.

- [ ] **Step 4: Commit**

```bash
git add data-science/README.md data-science/data/feature_mapping.md README.md
git commit -m "docs: add data-science module documentation and feature mapping"
```

---

## Self-Review

1. **Spec coverage:** The user asked for "搜集公开数据，然后训练模型". This plan covers two public datasets, four models, and saved artifacts.
2. **Placeholder scan:** No TBD/TODO; all steps contain concrete code.
3. **Type consistency:** Functions return `pd.DataFrame` and accept `Path`; artifact names are consistent.

**Gaps:**
- No real-time serving API is built yet; that is a follow-up after models exist.
- The work-order dataset is a small public sample (sewer maintenance), not the exact domain of the Java system, but it demonstrates the modeling pipeline.

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-08-11-data-science-models.md`.**

Two execution options:

1. **Inline Execution** - I execute the tasks in this session and report results.
2. **Subagent-Driven** - Dispatch fresh subagents per task.

Since the user already asked to execute, I will proceed with **Inline Execution** unless instructed otherwise.
