# Data Science Module — Predictive Models

This module turns the existing work-order system into a data-driven platform by training reusable machine-learning models on public datasets. The Java backend can call these models through a Python service (FastAPI wrapper coming next) or use them offline.

## What is inside

| File | Purpose |
|---|---|
| `src/download_data.py` | Download and cache public datasets |
| `src/utils.py` | Shared artifact helpers |
| `src/train_predictive_maintenance.py` | Train failure-risk models on UCI AI4I 2020 |
| `src/train_work_order_duration.py` | Train duration/SLA models on a public work-order CSV |
| `models/*.pkl` + `*.json` | Trained models + metrics sidecars |
| `tests/*.py` | pytest test suite |

## Quick start

```bash
cd data_science
python3 -m venv .venv
source .venv/bin/activate
# If python3-venv is missing, use: virtualenv .venv
pip install -r requirements.txt

# Download datasets once
python3 -m data_science.src.download_data

# Train all models
python3 -m data_science.src.train_predictive_maintenance
python3 -m data_science.src.train_work_order_duration

# Or run the full test suite from the repo root
python3 -m pytest data_science/tests -v
```

## Datasets

### 1. UCI AI4I 2020 Predictive Maintenance Dataset

- **URL:** https://archive.ics.uci.edu/ml/datasets/ai4i+2020+predictive+maintenance+dataset
- **License:** CC BY 4.0
- **Rows:** 10,000 simulated industrial process records
- **Features:** air temperature, process temperature, rotational speed, torque, tool wear, product type
- **Targets:** machine failure (binary) + five failure modes (TWF, HDF, PWF, OSF, RNF)

### 2. Public Work-Order Sample (sewer maintenance)

- **URL:** https://raw.githubusercontent.com/OlaOlagunju/GCP_Mage_Data_Pipeline/main/1.%20Source%20Data/work-order-management-module.csv
- **License:** assumed public/demo use (GitHub public sample)
- **Rows:** ~300 maintenance work orders
- **Features:** activity code, request/creation time
- **Targets:** completion duration in minutes, SLA breach (binary)

> These public datasets are **not the exact domain** of the Java system, but they prove the modeling pipeline and produce reusable models that can be retrained on real work-order data later.

## Trained models

| Model | Dataset | Task | Key Metric | Value |
|---|---|---|---|---|
| `pm_failure_classifier` | AI4I 2020 | Binary: will the machine fail? | Accuracy / Macro F1 | 0.98 / 0.7988 |
| `pm_failure_type_classifier` | AI4I 2020 | Multilabel: which failure mode(s)? | Macro F1 / Hamming loss | 0.3893 / 0.0048 |
| `wo_duration_regressor` | Work-order sample | Regression: how long will the job take? | MAE (minutes) | 6714.08 |
| `wo_sla_classifier` | Work-order sample | Binary: will the SLA be breached? | Accuracy / Macro F1 | 0.9333 / 0.9250 |

### Notes on the duration model

The public work-order sample contains very long-tail jobs (some lasting days). The regressor caps the target at the 95th percentile during training to avoid extreme outliers dominating the loss. As a result the MAE is high in absolute minutes. When the Java system has real, cleaner work-order history, retraining on that data will drop the MAE significantly.

## How to use a model from Python

```python
from data_science.src.utils import load_artifact
import pandas as pd

clf = load_artifact("pm_failure_classifier")
sample = pd.DataFrame([{
    "Air temperature [K]": 300,
    "Process temperature [K]": 310,
    "Rotational speed [rpm]": 1500,
    "Torque [Nm]": 45,
    "Tool wear [min]": 150,
    "Type": "M",
}])
print(clf.predict(sample))        # [0] or [1]
print(clf.predict_proba(sample)) # failure probability
```

## Integration with the Java backend

The next step is to wrap these models in a small FastAPI service (`data_science/api/`). The Spring Boot backend will send feature JSON to the Python service and receive predictions:

```http
POST /predict/failure-risk
{
  "airTemperature": 300,
  "processTemperature": 310,
  "rotationalSpeed": 1500,
  "torque": 45,
  "toolWear": 150,
  "productType": "M"
}
→ { "failureProbability": 0.82, "riskLevel": "high" }
```

Until the API is added, the Java side can still run the Python scripts as a subprocess and read the JSON sidecars, or load the `.pkl` files through a JEP / PyTorch Java bridge (not recommended for simple demos).

## Next steps

1. Add `data_science/api/main.py` with FastAPI endpoints for each model.
2. Extend the Java backend with a `PredictionClient` that calls the Python service.
3. Add a "Predictive Maintenance" card to the Vue Dashboard that shows failure risk per device.
4. Retrain models on real MySQL data from the running system.
5. Add model monitoring (drift detection, performance decay).
