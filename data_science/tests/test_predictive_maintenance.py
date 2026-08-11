from data_science.src.train_predictive_maintenance import train_predictive_maintenance
from data_science.src.utils import MODEL_DIR


def test_train_predictive_maintenance():
    train_predictive_maintenance()
    assert (MODEL_DIR / "pm_failure_classifier.pkl").exists()
    assert (MODEL_DIR / "pm_failure_classifier.json").exists()
    assert (MODEL_DIR / "pm_failure_type_classifier.pkl").exists()
    assert (MODEL_DIR / "pm_failure_type_classifier.json").exists()
