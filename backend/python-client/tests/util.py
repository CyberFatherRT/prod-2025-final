import random
import string

from bookit.company import Company, CompanyApi
from bookit.admin import AdminApi
from bookit.user import User, UserApi

def rand_n_str(n):
    return "".join(random.choices(string.ascii_lowercase, k=n))

def create_company(domain, name):
    company = Company(domain, name)
    api = CompanyApi()
    status_code, data = api.register(company)
    print(status_code)

    return status_code, data

def create_random_company():
    domain = rand_n_str(10)
    name = rand_n_str(10)

    status_code, data = create_company(domain, name)
    assert status_code == 201
    assert data is not None

    return data["jwt"], domain

def create_random_user_api(domain):
    user = User(domain, rand_n_str(10) + "@179.ru", "Lol", "Loll", "sup3rSecret!")
    return UserApi(user)

def create_admin_api():
    tok, _ = create_random_company()
    return AdminApi(token=tok)
