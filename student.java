public class student
{
    int rollno;
    String name;
    float marks;
    String university;
    public student()
    {this.university="CUTM";
    }
    void setdata(int r, String n, float m)
    {
        this.rollno = r;
        this.name = n;
        this.marks = m;
    }
    void display(){
        System.out.println("Roll No: " + rollno);
        System.out.println("Name: " + name);
        System.out.println("Marks: " + marks);
        System.out.println("University: " + university);
        System.out.println("-----");
    }
    public static void main(String[] args)
    {student s1 = new student();
     student s2 = new student();
     s1.setdata(152,"harr",99.6f);
     s2.setdata(153,"ragg",98.6f);

     s1.display();
     s2.display();
    }
}
