import java.util.Scanner;

public class SumOfNaturalNumbers {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Take user input for N
        System.out.print("Enter a positive integer (N): ");
        int n = scanner.nextInt();

        int sum = 0;
        int i = 1;

        // While loop to calculate sum from 1 to N
        while (i <= n) {
            sum += i;
            i++;
        }

        // Output the result
        System.out.println("Sum of natural numbers from 1 to " + n + " is: " + sum);
    }
}
