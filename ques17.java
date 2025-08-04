import java.util.Scanner;

public class MultiplicationTable {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Taking number input from the user
        System.out.print("Enter a number to print its multiplication table: ");
        int number = input.nextInt();

        // Printing multiplication table
        System.out.println("Multiplication table of " + number + ":");
        for (int i = 1; i <= 10; i++) {
            System.out.println(number + " x " + i + " = " + (number * i));
        }

        input.close();
    }
}
