from requests.sessions import Session
from .user import UserApi
from .config import BASE_URL
from .util import extract_data


class AdminApi(UserApi):
    def __init__(self, token=None):
        self.s = Session()
        if token:
            self.token = token
            self.s.headers["authorization"] = f"Bearer {self.token}"

    def list_users(self):
        r = self.s.get(BASE_URL + "/admin/user/list")

        data = extract_data(r)

        return r.status_code, data

    def get_user(self, user_id):
        r = self.s.get(BASE_URL + f"/admin/user/{user_id}")

        data = extract_data(r)

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

        data = extract_data(r)

        return r.status_code, data

    def verify_guest(self, user_id):
        r = self.s.post(BASE_URL + f"/admin/user/{user_id}/verify")

        return r.status_code, None
