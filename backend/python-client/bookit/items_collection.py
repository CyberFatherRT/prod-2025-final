class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y


class Item:
    def __init__(self, bookable, name, description, offsets, color):
        self.bookable = bookable
        self.name = name
        self.description = description
        self.offsets = offsets
        self.color = color

TABLE_SMALL = Item(True, "Small table", "Small table for work and stuff", [Point(0, 0)], "134d23")
TABLE_HOR = Item(True, "Medium table", "Medium table for meetings", [Point(0, 0), Point(1, 0)], "134d23")
TABLE_VERT = Item(True, "Medium table", "Medium table for meetings", [Point(0, 0), Point(0, 1)], "134d23")
TOILET = Item(False, "WC", "Water closet", [Point(x, y) for x, y in  __import__("itertools").product(range(3), range(2))], "134d23")
