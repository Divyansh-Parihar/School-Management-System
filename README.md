# 🏫 Academica Core — School Management System

> A full-stack Java web application for managing students, teachers, and attendance — runs on **localhost** like Streamlit, built entirely with pure Java and HTML/CSS/JS.

---

## 📌 Overview

**Academica Core** is a Java-powered web application that starts a local HTTP server and serves a beautiful browser-based dashboard. No frameworks, no Node.js, no external libraries — just the **Java Standard Library** for the backend and **plain HTML/CSS/JS** for the frontend.

This project demonstrates practical Java skills: **OOP**, **Generics**, **Custom Exceptions**, **File I/O**, **Java Streams**, and **HTTP Server** programming.

---

## ✨ Features

| Category              | Feature                                                  |
|-----------------------|----------------------------------------------------------|
| 👨‍🎓 Student Management | Add, Search, List, Delete, Update Fee Balance            |
| 👨‍🏫 Teacher Management | Add, Search, List, Delete, Update Salary                 |
| 📋 Attendance          | Mark & View attendance for students and teachers         |
| 🔐 Role-Based Access   | Teacher-authorized batch attendance marking by grade     |
| 💾 Data Persistence    | Auto-save/load via flat `.txt` files (no database)       |
| 🌐 Web Interface       | Browser-based dashboard running at `localhost:8080`      |
| 🛡️ Input Validation    | Server-side error handling with JSON error responses     |

---

## 🧠 OOP & Java Concepts Demonstrated

| Concept               | Implementation                                                  |
|-----------------------|-----------------------------------------------------------------|
| **Interface**         | `Person.java` — common contract for Student & Teacher           |
| **Polymorphism**      | Student & Teacher implement `Person` differently                |
| **Generics**          | `DataManager<T extends Person>` — works for any entity          |
| **Encapsulation**     | Private fields, controlled getters/setters                      |
| **Custom Exception**  | `DuplicateIdException` for duplicate ID error handling          |
| **Java Streams**      | Filtering, searching, and parsing with lambda expressions       |
| **File I/O**          | `Scanner`, `PrintWriter`, `FileWriter` for flat-file persistence|
| **HTTP Server**       | `com.sun.net.httpserver` — built-in JDK HTTP server             |

---

## 🗂️ Project Structure

```
Academica-Core/
├── Person.java                  # Interface — base contract
├── Student.java                 # Student entity (grade, fee, attendance)
├── Teacher.java                 # Teacher entity (subject, salary, attendance)
├── DataManager.java             # Generic CRUD + file persistence manager
├── DuplicateIdException.java    # Custom checked exception
├── SchoolManagementSystem.java  # HTTP Server + REST API + entry point
├── index.html                   # Web frontend (HTML/CSS/JS SPA)
├── student_data.txt             # Flat-file student database (auto-managed)
├── teacher_data.txt             # Flat-file teacher database (auto-managed)
├── requirements.txt             # Prerequisites (Java JDK 11+)
└── .gitignore                   # Ignores .class files and IDE configs
```

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 11 or higher** (JDK 17 LTS recommended)

```bash
java -version   # Must show 11 or higher
```

### Step 1 — Clone

```bash
git clone https://github.com/your-username/academica-core.git
cd academica-core
```

### Step 2 — Compile

```bash
javac *.java
```

### Step 3 — Run

```bash
java SchoolManagementSystem
```

### Step 4 — Open in Browser

```
http://localhost:8080
```

That's it! The app runs in your browser just like a Streamlit app.

---

## 🖥️ How It Works

```
Browser (index.html)
      │
      │  fetch("/api/students")   ← HTTP GET/POST
      ▼
Java HTTP Server (localhost:8080)
      │
      │  DataManager<Student>
      │  DataManager<Teacher>
      ▼
  student_data.txt / teacher_data.txt  (flat-file storage)
```

- **Java starts an HTTP server** using `com.sun.net.httpserver.HttpServer` (built into JDK — no dependencies)
- **`index.html` is served** at `http://localhost:8080`
- The frontend uses `fetch()` to call the **REST-style API** on the same server
- **Data is auto-saved** to `.txt` files on exit (`Ctrl+C`)

---

## 🔌 API Endpoints

| Method | Endpoint                        | Description                      |
|--------|---------------------------------|----------------------------------|
| GET    | `/api/students`                 | List all students                |
| POST   | `/api/students`                 | Add a new student                |
| POST   | `/api/students/delete`          | Delete student by ID             |
| POST   | `/api/students/fee`             | Update student fee balance       |
| POST   | `/api/students/attendance`      | Mark student attendance          |
| GET    | `/api/students/attendance?id=X` | View student attendance record   |
| GET    | `/api/teachers`                 | List all teachers                |
| POST   | `/api/teachers`                 | Add a new teacher                |
| POST   | `/api/teachers/delete`          | Delete teacher by ID             |
| POST   | `/api/teachers/salary`          | Update teacher salary            |
| POST   | `/api/teachers/attendance`      | Mark teacher attendance          |
| GET    | `/api/teachers/attendance?id=X` | View teacher attendance record   |
| POST   | `/api/batch-attendance`         | Teacher marks class by grade     |
| POST   | `/api/save`                     | Persist all data to files        |

---

## 🛠️ Tech Stack

| Layer      | Technology                                    |
|------------|-----------------------------------------------|
| Backend    | Java SE (JDK 11+) — pure standard library     |
| HTTP Server| `com.sun.net.httpserver.HttpServer` (JDK built-in) |
| Frontend   | HTML5, Vanilla CSS, Vanilla JavaScript        |
| Storage    | Flat-file (`.txt`) — no database needed       |
| Build Tool | None — compile directly with `javac`          |
| Dependencies | **Zero** — no Maven, Gradle, or npm        |

---

## 📄 Data Format

Data is stored as pipe-delimited (`|`) plain text:

```
# student_data.txt
101|Alice Johnson|10|2500.0|2026-05-01:P,2026-05-02:P

# teacher_data.txt
201|Mr. James Harris|Mathematics|55000.0|2026-05-01:P
```

---

## 📜 License

Open source under the [MIT License](LICENSE).
