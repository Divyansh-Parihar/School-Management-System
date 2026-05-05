import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Generic class to manage a list of any type T that implements Person.
 * Handles all CRUD operations and file persistence.
 */
public class DataManager<T extends Person> {
    private List<T> dataList;
    private final String fileName;
    private final Function<String, T> parser;
    private final String dataType;

    public DataManager(String fileName, String dataType, Function<String, T> parser) {
        this.fileName = fileName;
        this.dataType = dataType;
        this.parser = parser;
        this.dataList = new ArrayList<>();
        loadData();
    }

    /** Returns all records. */
    public List<T> getAll() {
        return dataList;
    }

    /** Adds a new record; throws DuplicateIdException if ID already exists. */
    public void add(T item) throws DuplicateIdException {
        if (dataList.stream().anyMatch(d -> d.getId() == item.getId())) {
            throw new DuplicateIdException(item.getId());
        }
        dataList.add(item);
    }

    /** Finds a record by its unique ID. Returns null if not found. */
    public T findById(int id) {
        return dataList.stream()
            .filter(d -> d.getId() == id)
            .findFirst()
            .orElse(null);
    }

    /**
     * Deletes a record by ID.
     * @return true if deleted successfully, false if ID not found.
     */
    public boolean deleteById(int id) {
        return dataList.removeIf(d -> d.getId() == id);
    }

    /** Loads data from the flat file into memory on startup. */
    private void loadData() {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("--- INFO: " + dataType + " file not found. Starting with empty records.");
            return;
        }

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    T item = parser.apply(line);
                    if (item != null) {
                        dataList.add(item);
                    }
                }
            }
            System.out.println("--- SUCCESS: " + dataType + " data loaded. (" + dataList.size() + " records)");
        } catch (FileNotFoundException e) {
            System.err.println("X Critical I/O Error: File not found during load: " + e.getMessage());
        }
    }

    /** Persists all in-memory records back to the flat file. */
    public void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (T item : dataList) {
                writer.println(item.toFileString());
            }
            System.out.println("--- SUCCESS: " + dataType + " data saved to " + fileName);
        } catch (IOException e) {
            System.err.println("X Critical I/O Error: Could not write data to file: " + e.getMessage());
        }
    }
}