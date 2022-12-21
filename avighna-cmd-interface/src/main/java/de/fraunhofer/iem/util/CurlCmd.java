package de.fraunhofer.iem.util;

/**
 * Represents the single curl command in the list of requests
 *
 * @author Ranjith Krishnamurthy
 */
public class CurlCmd {
    private String[] curlCmd;

    public String[] getCurlCmd() {
        return curlCmd;
    }

    public void setCurlCmd(String[] curlCmd) {
        this.curlCmd = curlCmd;
    }
}
