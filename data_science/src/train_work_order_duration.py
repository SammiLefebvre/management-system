import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import GradientBoostingClassifier, GradientBoostingRegressor
from sklearn.metrics import classification_report, f1_score, mean_absolute_error
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from sklearn.compose import TransformedTargetRegressor

from data_science.src.download_data import fetch_work_orders
from data_science.src.utils import save_artifact


CATEGORICAL_FEATURES = ["activity_code"]
NUMERIC_FEATURES = ["hour_added", "dow_added", "month_added", "was_completed"]
REGRESSION_FEATURES = ["activity_code", "hour_added", "dow_added", "month_added"]


def preprocess_work_orders(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["WORKORDER_ADDED"] = pd.to_datetime(df["WORKORDER_ADDED"], errors="coerce", format="ISO8601")
    df["WORKORDER_STARTED"] = pd.to_datetime(df["WORKORDER_STARTED"], errors="coerce", format="ISO8601")
    df["WORKORDER_COMPLETED"] = pd.to_datetime(df["WORKORDER_COMPLETED"], errors="coerce", format="ISO8601")

    df["duration_minutes"] = (
        (df["WORKORDER_COMPLETED"] - df["WORKORDER_STARTED"]).dt.total_seconds() / 60.0
    )
    # Remove impossible negative durations.
    df["duration_minutes"] = df["duration_minutes"].where(df["duration_minutes"] >= 0)

    df["was_completed"] = df["WORKORDER_COMPLETED"].notna().astype(int)
    df["sla_breached"] = (
        (df["duration_minutes"] > 8 * 60) | (df["was_completed"] == 0)
    ).astype(int)

    df["hour_added"] = df["WORKORDER_ADDED"].dt.hour.fillna(0).astype(int)
    df["dow_added"] = df["WORKORDER_ADDED"].dt.dayofweek.fillna(0).astype(int)
    df["month_added"] = df["WORKORDER_ADDED"].dt.month.fillna(0).astype(int)
    df["activity_code"] = df["WORKORDER_ACTIVITY_CODE"].fillna("UNKNOWN").astype(str)

    return df


def build_duration_data(df: pd.DataFrame):
    completed = df[df["duration_minutes"].notna()].copy()
    # Cap extreme outliers at the 95th percentile to keep the model stable.
    cap = completed["duration_minutes"].quantile(0.95)
    completed["duration_minutes"] = completed["duration_minutes"].clip(upper=cap)
    X = completed[REGRESSION_FEATURES]
    y = completed["duration_minutes"].astype(float)
    return X, y, cap


def build_sla_data(df: pd.DataFrame):
    X = df[NUMERIC_FEATURES + ["activity_code"]]
    y = df["sla_breached"]
    return X, y


def make_ohe_pipeline(estimator, categorical_cols):
    preprocessor = ColumnTransformer(
        transformers=[
            ("cat", OneHotEncoder(handle_unknown="ignore"), categorical_cols),
        ],
        remainder="passthrough",
    )
    return Pipeline([("prep", preprocessor), ("est", estimator)])


def train_work_order_duration():
    df = preprocess_work_orders(fetch_work_orders())

    X_dur, y_dur, cap = build_duration_data(df)
    X_train, X_test, y_train, y_test = train_test_split(
        X_dur, y_dur, test_size=0.2, random_state=42
    )
    reg = make_ohe_pipeline(
        TransformedTargetRegressor(
            regressor=GradientBoostingRegressor(random_state=42),
            func=np.log1p,
            inverse_func=np.expm1,
        ),
        ["activity_code"],
    )
    reg.fit(X_train, y_train)
    y_pred = reg.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    print(f"Duration MAE: {mae:.2f} minutes (cap={cap:.2f})")
    save_artifact(
        "wo_duration_regressor",
        reg,
        {
            "model": "GradientBoostingRegressor(log1p target)",
            "dataset": "GitHub public work-order sample",
            "task": "regression: work-order duration in minutes",
            "mae_minutes": round(mae, 2),
            "duration_cap_minutes": round(cap, 2),
            "features": REGRESSION_FEATURES,
        },
    )

    X_sla, y_sla = build_sla_data(df)
    X_train2, X_test2, y_train2, y_test2 = train_test_split(
        X_sla, y_sla, test_size=0.2, random_state=42, stratify=y_sla
    )
    clf = make_ohe_pipeline(
        GradientBoostingClassifier(random_state=42), ["activity_code"]
    )
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
            "f1_macro": round(
                f1_score(y_test2, y_pred2, average="macro", zero_division=0), 4
            ),
            "accuracy": round(report["accuracy"], 4),
            "features": NUMERIC_FEATURES + ["activity_code"],
        },
    )


if __name__ == "__main__":
    train_work_order_duration()
