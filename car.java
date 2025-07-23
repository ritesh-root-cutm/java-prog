public class car
{String mark;
    String model;
    int year;
    
    public car()
    {this.car = "fortuner";}

    void setAttributes()
    {scanner setAttributes = new scanner(System.in);
        System.out.println("Enter car mark:");
        mark = setAttributes.nextLine();
        System.out.println("Enter car model:");
        model = setAttributes.nextLine();
        System.out.println("Enter car year:");
        year = setAttributes.nextInt();
    }

    void display()
    {System.out.println("Car Mark: " + mark);
        System.out.println("Car Model: " + model);
        System.out.println("Car Year: " + year);
        System.out.println("car: " + car);
        
    }
}