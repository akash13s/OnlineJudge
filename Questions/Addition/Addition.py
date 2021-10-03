
def add(a, b):
    res = 0
    res = a + b

    return res

if __name__ == '__main__':

    inp = input().split()

    a = int(inp[0])

    b = int(inp[1])

    print(add(a,b))
