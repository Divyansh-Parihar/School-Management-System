import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Academica Core — School Management System
 * Starts a local HTTP server on port 8080.
 * Open http://localhost:8080 in your browser to use the app.
 *
 * API Routes:
 *   GET  /api/students                  - List all students
 *   POST /api/students                  - Add student
 *   POST /api/students/delete           - Delete student
 *   POST /api/students/fee              - Update student fee
 *   POST /api/students/attendance       - Mark student attendance
 *   GET  /api/students/attendance?id=X  - View student attendance
 *   GET  /api/teachers                  - List all teachers
 *   POST /api/teachers                  - Add teacher
 *   POST /api/teachers/delete           - Delete teacher
 *   POST /api/teachers/salary           - Update teacher salary
 *   POST /api/teachers/attendance       - Mark teacher attendance
 *   GET  /api/teachers/attendance?id=X  - View teacher attendance
 *   POST /api/batch-attendance          - Teacher marks class by grade
 *   POST /api/save                      - Save all data to files
 */
public class SchoolManagementSystem {

    private final DataManager<Student> studentManager;
    private final DataManager<Teacher> teacherManager;
    private static final int PORT = 8080;

    public SchoolManagementSystem() {
        studentManager = new DataManager<>("student_data.txt", "Student", Student::fromFileString);
        teacherManager = new DataManager<>("teacher_data.txt", "Teacher", Teacher::fromFileString);
    }

    // ── Server Startup ──────────────────────────────────────────────────────

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/students",       this::handleStudents);
        server.createContext("/api/teachers",       this::handleTeachers);
        server.createContext("/api/batch-attendance", this::handleBatchAttendance);
        server.createContext("/api/save",           this::handleSave);
        server.createContext("/",                   this::handleStatic);
        server.setExecutor(null);
        server.start();

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    Academica Core — Server Running       ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║    Open: http://localhost:8080           ║");
        System.out.println("║    Press Ctrl+C to stop & save data.     ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // Auto-save on shutdown (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            studentManager.saveData();
            teacherManager.saveData();
            System.out.println("\n[✓] Data saved. Goodbye!");
        }));
    }

    // ── Static File Handler (serves index.html) ─────────────────────────────

    private void handleStatic(HttpExchange ex) throws IOException {
        File htmlFile = new File("index.html");
        if (!htmlFile.exists()) {
            sendResponse(ex, 404, "text/plain", "index.html not found.");
            return;
        }
        byte[] bytes;
        try (FileInputStream fis = new FileInputStream(htmlFile)) {
            bytes = fis.readAllBytes();
        }
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    // ── Student Handler ─────────────────────────────────────────────────────

    private void handleStudents(HttpExchange ex) throws IOException {
        String path   = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        if ("OPTIONS".equals(method)) { sendJson(ex, 200, "{}"); return; }

        try {
            switch (path) {
                case "/api/students":
                    if ("GET".equals(method))  { sendJson(ex, 200, studentsToJson(studentManager.getAll())); }
                    else if ("POST".equals(method)) {
                        Map<String,String> b = parseBody(ex.getRequestBody());
                        Student s = new Student(
                            parseInt(b,"id"), b.get("name"),
                            parseInt(b,"grade"), parseDouble(b,"feeBalance"), "");
                        studentManager.add(s);
                        sendJson(ex, 201, ok("Student \"" + s.getName() + "\" added."));
                    } break;

                case "/api/students/delete":
                    int delId = parseInt(parseBody(ex.getRequestBody()), "id");
                    boolean wasDeleted = studentManager.deleteById(delId);
                    sendJson(ex, wasDeleted ? 200 : 404, wasDeleted ? ok("Student deleted.") : err("Student not found."));
                    break;

                case "/api/students/fee":
                    Map<String,String> fb = parseBody(ex.getRequestBody());
                    Student fs = studentManager.findById(parseInt(fb,"id"));
                    if (fs == null) { sendJson(ex, 404, err("Student not found.")); break; }
                    fs.setFeeBalance(parseDouble(fb,"feeBalance"));
                    sendJson(ex, 200, ok("Fee updated for " + fs.getName() + "."));
                    break;

                case "/api/students/attendance":
                    if ("POST".equals(method)) {
                        Map<String,String> ab = parseBody(ex.getRequestBody());
                        Student as = studentManager.findById(parseInt(ab,"id"));
                        if (as == null) { sendJson(ex, 404, err("Student not found.")); break; }
                        String st = ab.getOrDefault("status","").toUpperCase();
                        if (!st.equals("P") && !st.equals("A")) { sendJson(ex, 400, err("Status must be P or A.")); break; }
                        as.markAttendance(LocalDate.now().toString(), st.equals("P"));
                        sendJson(ex, 200, ok("Attendance marked for " + as.getName() + "."));
                    } else {
                        Map<String,String> q = parseQuery(ex.getRequestURI().getQuery());
                        Student vs = studentManager.findById(parseInt(q,"id"));
                        if (vs == null) { sendJson(ex, 404, err("Student not found.")); break; }
                        sendJson(ex, 200, "{\"attendance\":\"" + esc(vs.getAttendanceRecord()) + "\"}");
                    } break;

                default: sendJson(ex, 404, err("Unknown route."));
            }
        } catch (DuplicateIdException e) { sendJson(ex, 409, err(e.getMessage()));
        } catch (Exception e)            { sendJson(ex, 400, err("Bad request: " + e.getMessage())); }
    }

    // ── Teacher Handler ─────────────────────────────────────────────────────

    private void handleTeachers(HttpExchange ex) throws IOException {
        String path   = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        if ("OPTIONS".equals(method)) { sendJson(ex, 200, "{}"); return; }

        try {
            switch (path) {
                case "/api/teachers":
                    if ("GET".equals(method))  { sendJson(ex, 200, teachersToJson(teacherManager.getAll())); }
                    else if ("POST".equals(method)) {
                        Map<String,String> b = parseBody(ex.getRequestBody());
                        Teacher t = new Teacher(
                            parseInt(b,"id"), b.get("name"),
                            b.get("subject"), parseDouble(b,"salary"), "");
                        teacherManager.add(t);
                        sendJson(ex, 201, ok("Teacher \"" + t.getName() + "\" added."));
                    } break;

                case "/api/teachers/delete":
                    int tid = parseInt(parseBody(ex.getRequestBody()), "id");
                    boolean del = teacherManager.deleteById(tid);
                    sendJson(ex, del ? 200 : 404, del ? ok("Teacher deleted.") : err("Teacher not found."));
                    break;

                case "/api/teachers/salary":
                    Map<String,String> sb = parseBody(ex.getRequestBody());
                    Teacher ts = teacherManager.findById(parseInt(sb,"id"));
                    if (ts == null) { sendJson(ex, 404, err("Teacher not found.")); break; }
                    ts.setSalary(parseDouble(sb,"salary"));
                    sendJson(ex, 200, ok("Salary updated for " + ts.getName() + "."));
                    break;

                case "/api/teachers/attendance":
                    if ("POST".equals(method)) {
                        Map<String,String> ab = parseBody(ex.getRequestBody());
                        Teacher at = teacherManager.findById(parseInt(ab,"id"));
                        if (at == null) { sendJson(ex, 404, err("Teacher not found.")); break; }
                        String st = ab.getOrDefault("status","").toUpperCase();
                        if (!st.equals("P") && !st.equals("A")) { sendJson(ex, 400, err("Status must be P or A.")); break; }
                        at.markAttendance(LocalDate.now().toString(), st.equals("P"));
                        sendJson(ex, 200, ok("Attendance marked for " + at.getName() + "."));
                    } else {
                        Map<String,String> q = parseQuery(ex.getRequestURI().getQuery());
                        Teacher vt = teacherManager.findById(parseInt(q,"id"));
                        if (vt == null) { sendJson(ex, 404, err("Teacher not found.")); break; }
                        sendJson(ex, 200, "{\"attendance\":\"" + esc(vt.getAttendanceRecord()) + "\"}");
                    } break;

                default: sendJson(ex, 404, err("Unknown route."));
            }
        } catch (DuplicateIdException e) { sendJson(ex, 409, err(e.getMessage()));
        } catch (Exception e)            { sendJson(ex, 400, err("Bad request: " + e.getMessage())); }
    }

    // ── Batch Attendance ────────────────────────────────────────────────────

    private void handleBatchAttendance(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) { sendJson(ex, 405, err("POST only.")); return; }
        try {
            Map<String,String> b = parseBody(ex.getRequestBody());
            int teacherId = parseInt(b, "teacherId");
            int grade     = parseInt(b, "grade");
            Teacher teacher = teacherManager.findById(teacherId);
            if (teacher == null) { sendJson(ex, 403, err("Authorization failed: Teacher ID not found.")); return; }

            List<Student> classStudents = studentManager.getAll().stream()
                .filter(s -> s.getGrade() == grade).collect(Collectors.toList());
            if (classStudents.isEmpty()) { sendJson(ex, 404, err("No students in Grade " + grade + ".")); return; }

            // attendanceData format: "101:P,102:A,103:P"
            String today = LocalDate.now().toString();
            Map<Integer,String> attMap = new HashMap<>();
            for (String entry : b.getOrDefault("attendanceData","").split(",")) {
                String[] kv = entry.split(":");
                if (kv.length == 2) attMap.put(Integer.parseInt(kv[0].trim()), kv[1].trim());
            }
            int marked = 0;
            for (Student s : classStudents) {
                String status = attMap.get(s.getId());
                if ("P".equals(status) || "A".equals(status)) {
                    s.markAttendance(today, "P".equals(status));
                    marked++;
                }
            }
            sendJson(ex, 200, "{\"message\":\"Marked " + marked + "/" + classStudents.size()
                + " students by " + esc(teacher.getName()) + ".\"}");
        } catch (Exception e) { sendJson(ex, 400, err("Bad request: " + e.getMessage())); }
    }

    // ── Save Handler ────────────────────────────────────────────────────────

    private void handleSave(HttpExchange ex) throws IOException {
        studentManager.saveData();
        teacherManager.saveData();
        sendJson(ex, 200, ok("All data saved successfully."));
    }

    // ── Utilities ───────────────────────────────────────────────────────────

    private void sendJson(HttpExchange ex, int code, String json) throws IOException {
        sendResponse(ex, code, "application/json", json);
    }

    private void sendResponse(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type",                  type + "; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",   "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods",  "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers",  "Content-Type");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private Map<String,String> parseQuery(String query) {
        Map<String,String> m = new HashMap<>();
        if (query == null || query.isBlank()) return m;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            try { if (kv.length == 2) m.put(URLDecoder.decode(kv[0],"UTF-8"), URLDecoder.decode(kv[1],"UTF-8")); }
            catch (Exception ignored) {}
        }
        return m;
    }

    private Map<String,String> parseBody(InputStream is) throws IOException {
        return parseQuery(new String(is.readAllBytes(), StandardCharsets.UTF_8));
    }

    private int    parseInt   (Map<String,String> m, String k) { return Integer.parseInt(m.getOrDefault(k,"0").trim()); }
    private double parseDouble(Map<String,String> m, String k) { return Double.parseDouble(m.getOrDefault(k,"0").trim()); }
    private String ok (String msg) { return "{\"message\":\"" + esc(msg) + "\"}"; }
    private String err(String msg) { return "{\"error\":\""   + esc(msg) + "\"}"; }
    private String esc(String s)   { return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }

    private String studentsToJson(List<Student> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Student s = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"id\":%d,\"name\":\"%s\",\"grade\":%d,\"feeBalance\":%.2f,\"attendance\":\"%s\"}",
                s.getId(), esc(s.getName()), s.getGrade(), s.getFeeBalance(), esc(s.getAttendanceRecord())));
        }
        return sb.append("]").toString();
    }

    private String teachersToJson(List<Teacher> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Teacher t = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"id\":%d,\"name\":\"%s\",\"subject\":\"%s\",\"salary\":%.2f,\"attendance\":\"%s\"}",
                t.getId(), esc(t.getName()), esc(t.getSubject()), t.getSalary(), esc(t.getAttendanceRecord())));
        }
        return sb.append("]").toString();
    }

    // ── Entry Point ─────────────────────────────────────────────────────────

    public static void main(String[] args) throws IOException {
        new SchoolManagementSystem().start();
    }
}