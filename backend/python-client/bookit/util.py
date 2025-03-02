def extract_data(r):
    try:
        data = r.json()
    except:
        data = None

    return data
