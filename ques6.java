import java.util.Scanner;

public class CelsiusToFahrenheit {
    public static void main(String[] args) {
        // Create Scanner object for input
        Scanner scanner = new Scanner(System.in);

        // Ask user for Celsius temperature
        System.out.print("Enter temperature in Celsius: ");
        double celsius = scanner.nextDouble();  // Read temperature as double

        // Convert to Fahrenheit
        double fahrenheit = celsius * (9.0 / 5.0) + 32;

        // Display result
        System.out.println(celsius + "°C is equal to " + fahrenheit + "°F");

        // Close the scanner
        scanner.close();
    }
}
