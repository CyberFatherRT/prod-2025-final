from bookit.company import Company, CompanyApi
from bookit.admin import AdminApi
from .util import rand_n_str


def create_company(domain, name):
    company = Company(domain, name)
    api = CompanyApi()
    status_code, data = api.register(company)
    print(status_code)

    return status_code, data


def test_create_company():
    domain = rand_n_str(10)
    name = rand_n_str(10)

    status_code, data = create_company(domain, name)
    assert status_code == 200
    api = AdminApi(token=data["jwt"])

    status_code, data = create_company(domain, name)
    assert status_code == 409
