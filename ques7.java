import java.util.Scanner;

public class ArithmeticOperatorsDouble {
    public static void main(String[] args) {
        // Create Scanner object for dynamic input
        Scanner scanner = new Scanner(System.in);

        // Get two double values from the user
        System.out.print("Enter the first number (decimal allowed): ");
        double num1 = scanner.nextDouble();

        System.out.print("Enter the second number (decimal allowed): ");
        double num2 = scanner.nextDouble();

        // Perform arithmetic operations
        double sum = num1 + num2;
        double difference = num1 - num2;
        double product = num1 * num2;

        // Check to avoid division by zero
        if (num2 != 0) {
            double quotient = num1 / num2;
            double remainder = num1 % num2;

            // Display results
            System.out.println("Addition (+): " + sum);
            System.out.println("Subtraction (-): " + difference);
            System.out.println("Multiplication (*): " + product);
            System.out.println("Division (/): " + quotient);
            System.out.println("Modulus (%): " + remainder);
        } else {
            System.out.println("Division and Modulus by zero are not allowed.");
        }

        // Close the scanner
        scanner.close();
    }
}
