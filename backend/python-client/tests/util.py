import random
import string

def rand_n_str(n):
    return "".join(random.choices(string.ascii_lowercase, k=n))
