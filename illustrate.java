public class illustrate 
{
    public static void main(String[] args)
    {
        // decimal-form literal (float type suffix 'f' or 'F' is required)
        float a = 101.230f;

        // It is a decimal literal despite the leading zero
        float b = 0123.222f;

        // Hexadecimal floating-point literals are NOT supported in Java and will cause an error
        // float c = 0x123.222; // This line causes compilation error

        System.out.println(a);
        System.out.println(b);
        // System.out.println(c); // Commented out due to error
    }
}
