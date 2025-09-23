// and a parameterized constructor that initializes all instance variables.

class Car {
    String make;
    String model;
    int year;

    // Default constructor
    Car() {
        make = "Unknown";
        model = "Unknown";
        year = 0;
    }

    // Parameterized constructor
    Car(String make, String model, int year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }

    // Method to display car details
    void displayDetails() {
        System.out.println("Car Make  : " + make);
        System.out.println("Car Model : " + model);
        System.out.println("Car Year  : " + year);
        System.out.println("---------------------------");
    }
}

public class Main {
    public static void main(String[] args) {
        // Using default constructor
        Car car1 = new Car();
        
        // Using parameterized constructor
        Car car2 = new Car("Honda", "Civic", 2023);

        // Display details
        System.out.println("Details of Car1 (Default Constructor):");
        car1.displayDetails();

        System.out.println("Details of Car2 (Parameterized Constructor):");
        car2.displayDetails();
    }
}
