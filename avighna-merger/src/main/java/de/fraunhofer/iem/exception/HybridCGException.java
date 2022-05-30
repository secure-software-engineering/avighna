package de.fraunhofer.iem.exception;

/**
 * Top level exception for HybridCG-merger library that catches all the related this library exception
 *
 * @author Ranjith Krishnamurthy
 */
public class HybridCGException extends Exception {
    /**
     * Constructs the HybridCGException with the given message.
     *
     * @param message Error message.
     */
    public HybridCGException(String message) {
        super(message);
    }
}
