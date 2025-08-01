import java.util.Scanner;

public class SumCalculator {
    public static void main(String[] args) {
        // Create Scanner object for input
        Scanner scanner = new Scanner(System.in);

        // Ask user for first number
        System.out.print("Enter the first number: ");
        int num1 = scanner.nextInt();  // Read first integer

        // Ask user for second number
        System.out.print("Enter the second number: ");
        int num2 = scanner.nextInt();  // Read second integer

        // Calculate sum
        int sum = num1 + num2;

        // Display the result
        System.out.println("The sum of " + num1 + " and " + num2 + " is: " + sum);

        // Close the scanner
        scanner.close();
    }
}
