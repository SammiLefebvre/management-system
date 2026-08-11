import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, f1_score, hamming_loss
from sklearn.multiclass import OneVsRestClassifier
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

from data_science.src.download_data import fetch_ai4i
from data_science.src.utils import save_artifact


NUMERIC_COLS = [
    "Air temperature [K]",
    "Process temperature [K]",
    "Rotational speed [rpm]",
    "Torque [Nm]",
    "Tool wear [min]",
]
CATEGORICAL_COLS = ["Type"]
FAILURE_TYPE_COLS = ["TWF", "HDF", "PWF", "OSF", "RNF"]


def make_preprocessor():
    return ColumnTransformer(
        transformers=[
            ("num", StandardScaler(), NUMERIC_COLS),
            ("cat", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL_COLS),
        ]
    )


def train_predictive_maintenance():
    df = fetch_ai4i()
    X = df[NUMERIC_COLS + CATEGORICAL_COLS]
    y_binary = df["Machine failure"]
    y_multilabel = df[FAILURE_TYPE_COLS]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y_binary, test_size=0.2, random_state=42, stratify=y_binary
    )

    binary_clf = Pipeline(
        [
            ("prep", make_preprocessor()),
            (
                "clf",
                RandomForestClassifier(
                    n_estimators=300,
                    class_weight="balanced",
                    random_state=42,
                    n_jobs=-1,
                ),
            ),
        ]
    )
    binary_clf.fit(X_train, y_train)
    y_pred = binary_clf.predict(X_test)
    binary_report = classification_report(
        y_test, y_pred, output_dict=True, zero_division=0
    )
    print("Machine failure classification report:")
    print(classification_report(y_test, y_pred, zero_division=0))
    save_artifact(
        "pm_failure_classifier",
        binary_clf,
        {
            "model": "RandomForestClassifier",
            "dataset": "UCI AI4I 2020",
            "task": "binary: machine failure",
            "f1_macro": round(
                f1_score(y_test, y_pred, average="macro", zero_division=0), 4
            ),
            "accuracy": round(binary_report["accuracy"], 4),
            "features": NUMERIC_COLS + CATEGORICAL_COLS,
        },
    )

    X_train2, X_test2, y_train2, y_test2 = train_test_split(
        X, y_multilabel, test_size=0.2, random_state=42
    )
    type_clf = Pipeline(
        [
            ("prep", make_preprocessor()),
            (
                "clf",
                OneVsRestClassifier(
                    RandomForestClassifier(
                        n_estimators=200,
                        class_weight="balanced",
                        random_state=42,
                        n_jobs=-1,
                    )
                ),
            ),
        ]
    )
    type_clf.fit(X_train2, y_train2)
    y_pred2 = type_clf.predict(X_test2)
    f1_macro = f1_score(y_test2, y_pred2, average="macro", zero_division=0)
    print(f"Failure type hamming loss: {hamming_loss(y_test2, y_pred2):.4f}")
    print(f"Failure type macro F1: {f1_macro:.4f}")
    save_artifact(
        "pm_failure_type_classifier",
        type_clf,
        {
            "model": "OneVsRestClassifier(RandomForestClassifier)",
            "dataset": "UCI AI4I 2020",
            "task": "multilabel: failure types TWF/HDF/PWF/OSF/RNF",
            "f1_macro": round(f1_macro, 4),
            "hamming_loss": round(hamming_loss(y_test2, y_pred2), 4),
            "features": NUMERIC_COLS + CATEGORICAL_COLS,
        },
    )


if __name__ == "__main__":
    train_predictive_maintenance()
