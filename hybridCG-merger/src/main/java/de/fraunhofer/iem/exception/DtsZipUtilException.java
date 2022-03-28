package de.fraunhofer.iem.exception;

/**
 * This exception indicates that ZipUtility is failed to unzip DTS file
 *
 * @author Ranjith Krishnamurthy
 */
public class DtsZipUtilException extends HybridCGException {
    /**
     * Constructs the ZipUtilException with the given message.
     *
     * @param message Error message.
     */
    public DtsZipUtilException(String message) {
        super(message);
    }
}
