#include <bits/stdc++.h>
using namespace std;

class Addition {
public:
    int add(int a, int b) {
        int res = 0;
        res = a + b;
        return res;
    }
};

int main() {
    int a, b;
    cin >> a >> b;
    int result = Addition().add(a, b);
    cout << result << endl;
}
