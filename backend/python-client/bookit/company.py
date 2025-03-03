from requests import Session
from .config import BASE_URL
from .util import extract_json

class Company:
    def __init__(self, domain, name):
        self.domain = domain
        self.name = name

class CompanyApi:
    def __init__(self):
        self.s = Session()

    def register(self, company: Company):
        r = self.s.post(BASE_URL + "/company/register", json=company.__dict__)
        data = extract_json(r)
        return r.status_code, data
