import importlib


def test_import_package():
    importlib.import_module("data_science.src.utils")
    importlib.import_module("data_science.src.download_data")
    importlib.import_module("data_science.src.train_predictive_maintenance")
    importlib.import_module("data_science.src.train_work_order_duration")
