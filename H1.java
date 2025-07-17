import java.util.Scanner;

public class H1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int ID;
        while (true) {
            System.out.print("Enter emp ID: ");
            if (sc.hasNextInt()) {
                ID = sc.nextInt();
                sc.nextLine(); // consume newline
                break;
            } else {
                System.out.println("Invalid input! Please enter a valid integer for emp ID.");
                sc.nextLine(); // consume invalid input
            }
        }

        System.out.print("Enter emp name: ");
        String name = sc.nextLine();

        System.out.print("Enter emp phone No: ");
        String PND = sc.nextLine();

        System.out.print("Enter emp email ID: ");
        String emailID = sc.nextLine();

        int age;
        while (true) {
            System.out.print("Enter emp age: ");
            if (sc.hasNextInt()) {
                age = sc.nextInt();
                sc.nextLine(); // consume newline
                break;
            } else {
                System.out.println("Invalid input! Please enter a valid integer for age.");
                sc.nextLine();
            }
        }

        System.out.print("Enter emp gender (M/f): ");
        String gender = sc.nextLine();

        System.out.print("Enter emp marital status (M/UM): ");
        String maritalstatus = sc.nextLine();

        System.out.print("Enter Basic Salary: ");
        double basic = sc.nextDouble();
        System.out.print("Enter DA: ");
        double da = sc.nextDouble();
        System.out.print("Enter HRA: ");
        double hra = sc.nextDouble();
        System.out.print("Enter TA: ");
        double ta = sc.nextDouble();
        double gross = basic + da + hra + ta;

        System.out.println("\nEmployee Details:");
        System.out.println("ID: " + ID);
        System.out.println("Name: " + name);
        System.out.println("Phone No: " + PND);
        System.out.println("Email ID: " + emailID);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Marital Status: " + maritalstatus);
        System.out.println("Gross Salary: " + gross);

        sc.close();
    }
}