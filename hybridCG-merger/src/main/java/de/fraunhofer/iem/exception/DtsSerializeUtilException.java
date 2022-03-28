package de.fraunhofer.iem.exception;

/**
 * This exception indicates that SerializableUtility is failed to serialize/deserialize to/from DTS file
 *
 * @author Ranjith Krishnamurthy
 */
public class DtsSerializeUtilException extends HybridCGException {
    /**
     * Constructs the DtsSerializeUtilException with the given message.
     *
     * @param message Error message.
     */
    public DtsSerializeUtilException(String message) {
        super(message);
    }
}
