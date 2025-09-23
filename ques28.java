// that prints all the car's details.

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
    void displayInfo() {
        System.out.println("Car Make  : " + make);
        System.out.println("Car Model : " + model);
        System.out.println("Car Year  : " + year);
        System.out.println("---------------------------");
    }
}

public class Main {
    public static void main(String[] args) {
        // Create object using default constructor
        Car car1 = new Car();

        // Create object using parameterized constructor
        Car car2 = new Car("Hyundai", "i20", 2024);

        // Call displayInfo() method for both cars
        System.out.println("Details of Car1:");
        car1.displayInfo();

        System.out.println("Details of Car2:");
        car2.displayInfo();
    }
}
