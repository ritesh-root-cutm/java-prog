
// Create an object of Car, set its attributes, and print them.

class Car {
    // Instance variables
    String make;
    String model;
    int year;

    // Method to display car details
    void displayDetails() {
        System.out.println("Car Make  : " + make);
        System.out.println("Car Model : " + model);
        System.out.println("Car Year  : " + year);
    }
}

public class Main {
    public static void main(String[] args) {
        // Create object of Car
        Car myCar = new Car();

        // Set attributes
        myCar.make = "Toyota";
        myCar.model = "Corolla";
        myCar.year = 2022;

        // Print attributes
        myCar.displayDetails();
    }
}
