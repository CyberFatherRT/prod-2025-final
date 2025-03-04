from requests.sessions import Session
from .user import UserApi
from .config import BASE_URL
from .util import extract_data, extract_json
from json import dumps
from copy import deepcopy


class AdminApi(UserApi):
    def __init__(self, token=None):
        self.s = Session()
        if token:
            self.token = token
            self.s.headers["authorization"] = f"Bearer {self.token}"

    # /admin paths

    def get_document(self, user_id):
        r = self.s.get(BASE_URL + f"/admin/documents/{user_id}")
        data = extract_data(r)
        return r.status_code, data

    def list_requests(self):
        r = self.s.get(BASE_URL + "/admin/list_requests")
        data = extract_json(r)
        return r.status_code, data

    def list_users(self):
        r = self.s.get(BASE_URL + "/admin/user/list")

        data = extract_json(r)

        return r.status_code, data

    def get_user(self, user_id):
        r = self.s.get(BASE_URL + f"/admin/user/{user_id}")

        data = extract_json(r)

        return r.status_code, data

    def delete_user(self, user_id):
        r = self.s.delete(BASE_URL + f"/admin/user/{user_id}")

        return r.status_code, None

    def patch_user(self, user_id, patch_dict=None, avatar=None):
        files = {}
        if patch_dict:
            files["json"] = patch_dict
        if avatar:
            files["avatar"] = avatar

        r = self.s.patch(BASE_URL + f"/admin/user/{user_id}", files=files)

        data = extract_json(r)

        return r.status_code, data

    def verify_guest(self, user_id):
        r = self.s.post(BASE_URL + f"/admin/user/{user_id}/verify")

        return r.status_code, None

    # /place admin paths

    def new_place(self, address):
        r = self.s.post(BASE_URL + "/place/new", json={
            "address": address
        })

        data = extract_json(r)

        return r.status_code, data

    def new_coworking(self, building_id, address, height, width):
        r = self.s.post(BASE_URL + f"/place/{building_id}/coworking/new",
            json={
                "address": address,
                "height": height,
                "width": width
            }
        )

        data = extract_json(r)

        return r.status_code, data

    def new_item(self, icon, item):

        item = deepcopy(item)
        item.offsets = [{"x": p.x, "y": p.y} for p in item.offsets]

        r = self.s.post(BASE_URL + "/items/new", files={
            "json": dumps(item.__dict__).encode()
        })
        data = extract_json(r)
        return r.status_code, data

    def delete_item(self, item_id):
        r = self.s.delete(BASE_URL + f"/items/{item_id}")

        return r.status_code, None

    def place_item(self, building_id, coworking_id, item_id, basepoint):
        r = self.s.post(BASE_URL + f"/place/{building_id}/coworking/{coworking_id}/items/new", json={
            "base_point": {
                "x": basepoint.x,
                "y": basepoint.y
            },
            "description": "lol",
            "item_id": item_id,
            "name": "Lol item"
        })
