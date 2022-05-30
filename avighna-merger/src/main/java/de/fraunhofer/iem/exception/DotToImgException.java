package de.fraunhofer.iem.exception;

/**
 * This exception indicates that the library failed to convert DOT to Image file
 *
 * @author Ranjith Krishnamurthy
 */
public class DotToImgException extends HybridCGException {
    /**
     * Constructs the DotToImgException with the given message.
     *
     * @param message Error message.
     */
    public DotToImgException(String message) {
        super(message);
    }
}
