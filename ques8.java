import java.util.Scanner;

public class RelationalOperatorsString {
    public static void main(String[] args) {
        // Scanner for dynamic input
        Scanner scanner = new Scanner(System.in);

        // Input two strings
        System.out.print("Enter the first string: ");
        String str1 = scanner.nextLine();

        System.out.print("Enter the second string: ");
        String str2 = scanner.nextLine();

        // Compare using equals() and compareTo()
        System.out.println("str1 equals str2 (str1.equals(str2)): " + str1.equals(str2));
        System.out.println("str1 not equals str2 (!str1.equals(str2)): " + !str1.equals(str2));
        System.out.println("str1 < str2 (str1.compareTo(str2) < 0): " + (str1.compareTo(str2) < 0));
        System.out.println("str1 > str2 (str1.compareTo(str2) > 0): " + (str1.compareTo(str2) > 0));
        System.out.println("str1 <= str2 (str1.compareTo(str2) <= 0): " + (str1.compareTo(str2) <= 0));
        System.out.println("str1 >= str2 (str1.compareTo(str2) >= 0): " + (str1.compareTo(str2) >= 0));

        // Close scanner
        scanner.close();
    }
}
