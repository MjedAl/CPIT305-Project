/**
 *
 * @author Mjed
 */

public class dbNotSettedUpException extends Exception{
    public dbNotSettedUpException() {
        super("Error while reterving DB. DB is not ready yet.");
    }
}
