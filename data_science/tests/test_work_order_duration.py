from data_science.src.train_work_order_duration import train_work_order_duration
from data_science.src.utils import MODEL_DIR


def test_train_work_order_duration():
    train_work_order_duration()
    assert (MODEL_DIR / "wo_duration_regressor.pkl").exists()
    assert (MODEL_DIR / "wo_duration_regressor.json").exists()
    assert (MODEL_DIR / "wo_sla_classifier.pkl").exists()
    assert (MODEL_DIR / "wo_sla_classifier.json").exists()
