import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a Teacher in the school system.
 * Stores personal details, salary, and a daily attendance record.
 */
public class Teacher implements Person {
    private int teacherId;
    private String name;
    private String subject;
    private double salary;
    private Map<String, Boolean> attendance; // Date -> Present (true) / Absent (false)

    public static final String DELIMITER = Student.DELIMITER;

    public Teacher(int teacherId, String name, String subject, double salary, String attendanceData) {
        this.teacherId = teacherId;
        this.name = name;
        this.subject = subject;
        this.salary = salary;
        this.attendance = parseAttendance(attendanceData);
    }

    // --- Getters & Setters ---
    @Override
    public int getId()       { return teacherId; }
    @Override
    public String getName()  { return name; }
    public String getSubject() { return subject; }
    public double getSalary()  { return salary; }

    /** Updates the teacher's monthly salary. */
    public void setSalary(double salary) { this.salary = salary; }

    // --- Attendance Logic ---
    private Map<String, Boolean> parseAttendance(String data) {
        Map<String, Boolean> map = new HashMap<>();
        if (data != null && !data.isEmpty()) {
            Arrays.stream(data.split(","))
                  .filter(s -> s.contains(":"))
                  .forEach(entry -> {
                      String[] parts = entry.split(":");
                      map.put(parts[0], parts[1].equals("P"));
                  });
        }
        return map;
    }

    @Override
    public void markAttendance(String date, boolean present) {
        this.attendance.put(date, present);
    }

    @Override
    public String getAttendanceRecord() {
        if (attendance.isEmpty()) return "No attendance records found.";

        long presentCount = attendance.values().stream().filter(p -> p).count();
        double percent    = (double) presentCount / attendance.size() * 100;

        return String.format("Total Shifts: %d | Present: %d | Absent: %d | Rate: %.2f%%",
                             attendance.size(), presentCount,
                             attendance.size() - presentCount, percent);
    }

    // --- File Persistence Methods ---

    @Override
    public String toFileString() {
        String attStr = attendance.entrySet().stream()
                .map(e -> e.getKey() + ":" + (e.getValue() ? "P" : "A"))
                .collect(Collectors.joining(","));
        return teacherId + DELIMITER + name + DELIMITER + subject + DELIMITER + salary + DELIMITER + attStr;
    }

    public static Teacher fromFileString(String line) {
        String[] parts = line.split("\\" + DELIMITER);
        if (parts.length == 5) {
            try {
                int    id      = Integer.parseInt(parts[0].trim());
                String name    = parts[1].trim();
                String subject = parts[2].trim();
                double salary  = Double.parseDouble(parts[3].trim());
                String attData = parts[4].trim();
                return new Teacher(id, name, subject, salary, attData);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Corrupt data line skipped: " + line);
                return null;
            }
        }
        return null;
    }

    @Override
    public void displayDetails() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║       TEACHER  DETAILS       ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.printf("  ID      : %d%n", teacherId);
        System.out.printf("  Name    : %s%n", name);
        System.out.printf("  Subject : %s%n", subject);
        System.out.printf("  Salary  : Rs. %.2f / month%n", salary);
        System.out.println("  Attend. : " + getAttendanceRecord());
        System.out.println("================================");
    }
}