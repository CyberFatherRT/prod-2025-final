from .company import Company, CompanyApi
from .admin import AdminApi

DOMAIN = None
NAME = None
CW_ADDR = "Москва, ул. Грузинский Вал, 7"

CW_W = [10, 5]
CW_H = [5, 7]
CW_A = ["I", "II"]

company = Company(DOMAIN or input("Enter company domain: "), NAME or input("Enter company name: "))
api1 = CompanyApi()
status, tok = api1.register(company)

#print(status)

assert status == 201
assert tok

#print(tok)

api = AdminApi(token=tok["jwt"])

status, place = api.new_place(CW_ADDR or input("Enter space address: "))

print(status)

assert status == 201
assert place

cws = []
for a, h, w in zip(CW_A, CW_H, CW_W):
    status, cw = api.new_coworking(place["id"], a, h, w)
    assert status == 201
    assert cw

    cws.append(cw)

print("here")

from .items_collection import Point, TOILET, TABLE_HOR, TABLE_VERT, TABLE_SMALL
import random

def randpoint(x, y):
    return Point(random.randint(0, x), random.randint(0, y))

items = []
for i in [TOILET, TABLE_HOR, TABLE_VERT, TABLE_SMALL]:
    status, item = api.new_item(None, i)

    print(status, item)
    assert status == 201
    assert item

    items.append(item)

print(place)

for cw in cws:
    print(cw)
    for cnt in range(random.randint(1, 5)):
        for item in items:
            api.place_item(place["id"], cw["id"], item["id"], randpoint(5, 5))
