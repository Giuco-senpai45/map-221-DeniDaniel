package main.service.serviceExceptions;

/**
 * This custom exception extends the RuntimeException class
 */
public class RemoveException extends RuntimeException{
    /**
     * default constructor
     */
    public RemoveException() {
    }

    /**
     * Overloaded constructor
     * @param message String representing the message
     */
    public RemoveException(String message) {
        super(message);
    }

    /**
     * Overloaded constructor
     * @param message String
     * @param cause Throwable
     */
    public RemoveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Overloaded constructor
     * @param cause throwable
     */
    public RemoveException(Throwable cause) {
        super(cause);
    }

    /**
     * Overloaded constructor
     * @param message the message of the error
     * @param cause throwable cause
     * @param enableSuppression boolean
     * @param writableStackTrace boolean
     */
    public RemoveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
