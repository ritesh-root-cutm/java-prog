import java.util.Scanner;

public class SwapWithoutTemp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Input two integers
        System.out.print("Enter first number (a): ");
        int a = sc.nextInt();
        System.out.print("Enter second number (b): ");
        int b = sc.nextInt();

        System.out.println("\nBefore swapping:");
        System.out.println("a = " + a + ", b = " + b);

        // Swapping without third variable
        a = a + b;  // step 1
        b = a - b;  // step 2
        a = a - b;  // step 3

        System.out.println("\nAfter swapping:");
        System.out.println("a = " + a + ", b = " + b);

        sc.close();
    }
}
