import java.util.Scanner;

public class LogicalOperatorsDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Binary-style input: 1 for true, 0 for false
        System.out.print("Enter first boolean value (1 for true, 0 for false): ");
        int input1 = scanner.nextInt();

        System.out.print("Enter second boolean value (1 for true, 0 for false): ");
        int input2 = scanner.nextInt();

        // Convert to boolean
        boolean bool1 = (input1 == 1);
        boolean bool2 = (input2 == 1);

        // Logical operations
        boolean andResult = bool1 && bool2;
        boolean orResult = bool1 || bool2;
        boolean notBool1 = !bool1;
        boolean notBool2 = !bool2;

        // Output results
        System.out.println("\n--- Logical Operations ---");
        System.out.println("bool1 AND bool2 (&&): " + andResult);
        System.out.println("bool1 OR  bool2 (||): " + orResult);
        System.out.println("NOT bool1 (!): " + notBool1);
        System.out.println("NOT bool2 (!): " + notBool2);

        scanner.close();
    }
}
