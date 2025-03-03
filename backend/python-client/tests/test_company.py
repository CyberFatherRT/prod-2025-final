from .util import create_company, rand_n_str



# Covers usage
def test_create_company():
    domain = rand_n_str(10)
    name = rand_n_str(10)

    status_code, data = create_company(domain, name)
    assert status_code == 201
    assert data is not None

    status_code, data = create_company(domain, name)
    assert status_code == 409
