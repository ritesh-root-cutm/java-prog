import java.util.Scanner;

public class SumOfArray {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int[] arr = new int[5]; // array of size 5
        int sum = 0;

        System.out.println("Enter 5 integers:");

        for (int i = 0; i < arr.length; i++) {
            arr[i] = sc.nextInt();  // take input
            sum += arr[i];          // add to sum
        }

        System.out.println("Sum of all elements: " + sum);
        
        sc.close();
    }
}
