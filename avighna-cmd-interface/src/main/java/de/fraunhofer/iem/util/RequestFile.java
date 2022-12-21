package de.fraunhofer.iem.util;

import java.util.List;

/**
 * Configuration class to for the list of url request (curl command) for manual fuzzing
 *
 * @author Ranjith Krishnamurthy
 */
public class RequestFile {
    private List<CurlCmd> requests;

    public List<CurlCmd> getRequests() {
        return requests;
    }

    public void setRequests(List<CurlCmd> requests) {
        this.requests = requests;
    }
}
