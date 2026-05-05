/**
 * Custom checked exception for when a user attempts to add a record 
 * that already exists in the system.
 */
public class DuplicateIdException extends Exception {
    public DuplicateIdException(int id) {
        super("Error: A record with ID " + id + " already exists in the system.");
    }
}