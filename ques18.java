import java.util.Scanner;

public class FibonacciSeries {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Taking number of terms as input
        System.out.print("Enter the number of terms for Fibonacci series: ");
        int n = input.nextInt();

        int first = 0, second = 1;

        System.out.println("Fibonacci Series up to " + n + " terms:");

        for (int i = 1; i <= n; i++) {
            System.out.print(first + " ");

            // Generate next term
            int next = first + second;
            first = second;
            second = next;
        }

        input.close();
    }
}
