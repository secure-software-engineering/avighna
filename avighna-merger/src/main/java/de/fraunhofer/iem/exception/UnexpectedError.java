package de.fraunhofer.iem.exception;

/**
 * This exception represents that there is some exception or error during runtime, which is not expected
 *
 * @author Ranjith Krishnamurthy
 */
public class UnexpectedError extends HybridCGException {
    /**
     * Constructs the UnexpectedError with the given message.
     *
     * @param message Error message.
     */
    public UnexpectedError(String message) {
        super(message);
    }
}
