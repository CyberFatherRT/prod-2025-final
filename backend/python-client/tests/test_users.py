import requests
from bookit.user import UserApi
from .util import create_random_user_api, create_random_company
import jwt

_, domain = create_random_company()

def test_user_register():
    api = create_random_user_api(domain)
    s, d = api.register()
    assert s == 201

    s, d = api.register()
    assert s == 409

def test_user_lifecycle():
    api = create_random_user_api(domain)
    s, d = api.register()
    assert s == 201

    s, d = api.login()
    assert s == 200

    s, d = api.get_profile()
    assert s == 200
    assert d
    assert d["role"] == "GUEST"
    assert d["email"] == api.user.email

    pfp = requests.get("https://cdn-icons-png.flaticon.com/512/9908/9908191.png").content
    s, d = api.patch_profile(patch_dict={"name": "NewName"}, avatar=pfp)
    assert s == 200

    s, d = api.get_profile()
    assert s == 200
    assert d
    assert d["name"] == "NewName"


    sample = requests.get("https://pdfobject.com/pdf/sample.pdf").content
    s, d = api.upload_document(sample)
    #print(d)
    assert s == 200

    j = jwt.decode(api.token, algorithms=["RS256", "HS256"], options={"verify_signature": False})
    uid = j["user_id"]
    print(uid)
    s, d = api.get_avatar(uid)
    assert s == 200
    assert d == pfp

    s, d = api.delete()
    assert s == 204

    s, d = api.get_profile()
    assert s == 404
