import java.util.Scanner;

class Addition {
    public int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        scanner.close();

        int result = new Addition().add(a,b);
        System.out.println(result);
    }
}
