import io
import os
import zipfile

import pandas as pd
import requests

from data_science.src.utils import RAW_DIR

AI4I_URL = (
    "https://archive.ics.uci.edu/static/public/601/"
    "ai4i+2020+predictive+maintenance+dataset.zip"
)
WORK_ORDER_URL = (
    "https://raw.githubusercontent.com/OlaOlagunju/GCP_Mage_Data_Pipeline/main/"
    "1.%20Source%20Data/work-order-management-module.csv"
)


def fetch_ai4i() -> pd.DataFrame:
    """Download/cache the UCI AI4I 2020 predictive maintenance dataset."""
    csv_path = RAW_DIR / "ai4i2020.csv"
    if not csv_path.exists():
        response = requests.get(AI4I_URL, timeout=120)
        response.raise_for_status()
        with zipfile.ZipFile(io.BytesIO(response.content)) as zf:
            with zf.open("ai4i2020.csv") as f:
                df = pd.read_csv(f)
        df.to_csv(csv_path, index=False)
    return pd.read_csv(csv_path)


def fetch_work_orders() -> pd.DataFrame:
    """Download/cache a public sample work-order CSV."""
    csv_path = RAW_DIR / "work_orders.csv"
    if not csv_path.exists():
        response = requests.get(WORK_ORDER_URL, timeout=120)
        response.raise_for_status()
        csv_path.write_bytes(response.content)
    return pd.read_csv(csv_path)


if __name__ == "__main__":
    print("AI4I rows:", len(fetch_ai4i()))
    print("Work orders rows:", len(fetch_work_orders()))
