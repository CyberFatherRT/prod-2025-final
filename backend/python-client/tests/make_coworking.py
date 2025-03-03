from .make_company import create_random_company
from .util import rand_n_str
from ..bookit.admin import AdminApi

def create_place(address):
    domain = rand_n_str(10)
    name = rand_n_str(10)

    tok, _ = create_random_company()
    api = AdminApi(token=tok)
