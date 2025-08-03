import java.util.Scanner;

public class LargestOfThree {
    public static void main(String[] args) {
        // Create Scanner for dynamic input
        Scanner scanner = new Scanner(System.in);

        // Input three numbers
        System.out.print("Enter first number: ");
        int num1 = scanner.nextInt();

        System.out.print("Enter second number: ");
        int num2 = scanner.nextInt();

        System.out.print("Enter third number: ");
        int num3 = scanner.nextInt();

        int largest;

        // Binary comparisons using if-else if-else
        if (num1 >= num2 && num1 >= num3) {
            largest = num1;
        } else if (num2 >= num1 && num2 >= num3) {
            largest = num2;
        } else {
            largest = num3;
        }

        // Display result
        System.out.println("The largest number is: " + largest);

        // Close scanner
        scanner.close();
    }
}
