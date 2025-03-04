from requests import get, post, Session
from .config import BASE_URL
from .util import extract_data, extract_json
from json import dumps

class User:
    def __init__(self, company_domain, email, name, surname, password):
        self.company_domain = company_domain
        self.email = email
        self.name = name
        self.surname = surname
        self.password = password


class UserApi:
    def __init__(self, user: User):
        self.user = user
        self.token = None
        self.s = Session()

    def register(self):
        r = self.s.post(BASE_URL + "/user/register", json=self.user.__dict__)
        try:
            self.token = r.json()["jwt"]
            self.s.headers["authorization"] = f"Bearer {self.token}"
        except:
            ...
        return r.status_code, None

    def login(self):
        r = self.s.post(
            BASE_URL + "/user/login",
            json={
                "domain": self.user.company_domain,
                "email": self.user.email,
                "password": self.user.password,
            },
        )
        try:
            self.token = r.json()["jwt"]
            self.s.headers["authorization"] = f"Bearer {self.token}"
        except:
            ...
        return r.status_code, None

    def get_profile(self):
        r = self.s.get(BASE_URL + "/user/profile")
        data = extract_json(r)

        return r.status_code, data

    def upload_document(self, document: bytes):
        r = self.s.post(
            BASE_URL + "/user/upload_document", files={"document": ("lol", document, "application/pdf")}
        )
        return r.status_code, r.text

    def patch_profile(self, patch_dict=None, avatar=None):
        files = {}
        if patch_dict:
            files["json"] = dumps(patch_dict).encode()
        if avatar:
            files["avatar"] = ("lol", avatar, "image/png")

        r = self.s.patch(BASE_URL + "/user/profile", files=files)

        data = extract_json(r)

        return r.status_code, data

    def get_avatar(self, user_id):
        r = self.s.get(BASE_URL + f"/user/{user_id}/avatar")
        return r.status_code, r.content

    def get_items(self):
        r = self.s.get(BASE_URL + f"/items")

        data = extract_json(r)
        return r.status_code, data

    def delete(self):
        r = self.s.delete(BASE_URL + "/user")
        return r.status_code, r.content
    #def create
