public class TypeCastingDemo {
    public static void main(String[] args) {
        // Implicit type casting (int -> double)
        int intNum = 25;
        double doubleNum = intNum; // implicit conversion
        System.out.println("Implicit Casting:");
        System.out.println("Integer value: " + intNum);
        System.out.println("Converted to double: " + doubleNum);

        // Explicit type casting (double -> int)
        double originalDouble = 45.67;
        int convertedInt = (int) originalDouble; // explicit conversion
        System.out.println("\nExplicit Casting:");
        System.out.println("Double value: " + originalDouble);
        System.out.println("Converted to int: " + convertedInt);
    }
}
