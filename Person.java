/**
 * Defines the contract for any person (Student or Teacher) in the system.
 * Demonstrates the use of Java Interfaces and Polymorphism (OOP principles).
 */
public interface Person {
    int    getId();
    String getName();
    String toFileString();
    void   displayDetails();
    void   markAttendance(String date, boolean present);
    String getAttendanceRecord();
}