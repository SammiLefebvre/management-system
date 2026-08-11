from data_science.src.download_data import fetch_ai4i, fetch_work_orders


def test_ai4i_download():
    df = fetch_ai4i()
    assert len(df) == 10000
    assert "Machine failure" in df.columns


def test_work_orders_download():
    df = fetch_work_orders()
    assert len(df) > 100
    assert "WORKORDER_ACTIVITY_CODE" in df.columns
