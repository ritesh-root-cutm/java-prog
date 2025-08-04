import java.util.Scanner;

public class PrintNumbersDynamic {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask the user to enter the ending number
        System.out.print("Enter the ending number (e.g., 10): ");
        int end = scanner.nextInt();

        System.out.println("Numbers from 1 to " + end + ":");

        // Using a for loop to print numbers from 1 to end
        for (int i = 1; i <= end; i++) {
            System.out.println(i);
        }

        scanner.close();
    }
}
