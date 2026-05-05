import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a Student in the school system.
 * Stores personal details, grade, fee balance, and a daily attendance record.
 */
public class Student implements Person {
    private int    studentId;
    private String name;
    private int    grade;
    private double feeBalance;
    private Map<String, Boolean> attendance; // Date -> Present (true) / Absent (false)

    public static final String DELIMITER = "|";

    public Student(int studentId, String name, int grade, double feeBalance, String attendanceData) {
        this.studentId  = studentId;
        this.name       = name;
        this.grade      = grade;
        this.feeBalance = feeBalance;
        this.attendance = parseAttendance(attendanceData);
    }

    // --- Getters & Setters ---
    @Override
    public int getId()        { return studentId; }
    @Override
    public String getName()   { return name; }
    public int    getGrade()  { return grade; }
    public double getFeeBalance() { return feeBalance; }

    /** Updates the student's outstanding fee balance. */
    public void setFeeBalance(double feeBalance) { this.feeBalance = feeBalance; }

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

        return String.format("Total Days: %d | Present: %d | Absent: %d | Rate: %.2f%%",
                             attendance.size(), presentCount,
                             attendance.size() - presentCount, percent);
    }

    // --- File Persistence Methods ---

    @Override
    public String toFileString() {
        String attStr = attendance.entrySet().stream()
                .map(e -> e.getKey() + ":" + (e.getValue() ? "P" : "A"))
                .collect(Collectors.joining(","));
        return studentId + DELIMITER + name + DELIMITER + grade + DELIMITER + feeBalance + DELIMITER + attStr;
    }

    public static Student fromFileString(String line) {
        String[] parts = line.split("\\" + DELIMITER);
        if (parts.length == 5) {
            try {
                int    id      = Integer.parseInt(parts[0].trim());
                String name    = parts[1].trim();
                int    grade   = Integer.parseInt(parts[2].trim());
                double fee     = Double.parseDouble(parts[3].trim());
                String attData = parts[4].trim();
                return new Student(id, name, grade, fee, attData);
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
        System.out.println("║       STUDENT  DETAILS       ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.printf("  ID      : %d%n", studentId);
        System.out.printf("  Name    : %s%n", name);
        System.out.printf("  Grade   : %d%n", grade);
        System.out.printf("  Fee Bal : Rs. %.2f%n", feeBalance);
        System.out.println("  Attend. : " + getAttendanceRecord());
        System.out.println("================================");
    }
}