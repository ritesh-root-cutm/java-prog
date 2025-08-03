import java.util.Scanner;

public class LeapYearBinary {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Input year from the user
        System.out.print("Enter a year: ");
        int year = scanner.nextInt();

        // Check using binary method
        boolean divisibleBy4 = (year & 3) == 0;           // year % 4 == 0
        boolean divisibleBy100 = (year % 100 == 0);
        boolean divisibleBy400 = (year % 400 == 0);

        boolean isLeap = divisibleBy4 && (!divisibleBy100 || divisibleBy400);

        // Output result
        if (isLeap) {
            System.out.println(year + " is a leap year.");
        } else {
            System.out.println(year + " is NOT a leap year.");
        }

        scanner.close();
    }
}
