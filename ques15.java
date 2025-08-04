import java.util.Scanner;

public class ReverseInteger {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Input: Read the number from user
        System.out.print("Enter an integer to reverse: ");
        int number = scanner.nextInt();

        int reversed = 0;
        int originalNumber = number;

        // Logic to reverse the number
        while (number != 0) {
            int digit = number % 10;        // Get the last digit
            reversed = reversed * 10 + digit; // Add digit to reversed number
            number = number / 10;           // Remove the last digit
        }

        // Output: Display the reversed number
        System.out.println("Reversed number of " + originalNumber + " is: " + reversed);
        
        scanner.close();
    }
}
