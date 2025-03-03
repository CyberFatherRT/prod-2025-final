from .util import create_admin_api
from bookit.items_collection import TABLE_HOR

api = create_admin_api()

def test_item_create():
    status, data = api.new_item(None, TABLE_HOR)
    assert status == 201
    assert data

def test_item_delete():
    status, data = api.new_item(None, TABLE_HOR)
    assert status == 201
    assert data

    status, data = api.delete_item(data["id"])
    assert status == 204

def test_item_list():
    api = create_admin_api()

    for _ in range(10):
        status, data = api.new_item(None, TABLE_HOR)
        assert status == 201
        assert data

    status, data = api.get_items()
    assert status == 200
    assert data
    assert len(data) == 10
