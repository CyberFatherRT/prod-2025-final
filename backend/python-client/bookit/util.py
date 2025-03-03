def extract_json(r):
    try:
        data = r.json()
    except:
        data = None

    return data

def extract_data(r):
    try:
        data = r.text
    except:
        data = None

    return data
