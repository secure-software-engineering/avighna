package de.fraunhofer.iem.util;

import java.util.List;

/**
 * Configuration class to for the list of url request (curl command) for manual fuzzing
 *
 * @author Ranjith Krishnamurthy
 */
public class RequestFile {
    private List<String> requests;

    public List<String> getRequests() {
        return requests;
    }

    public void setRequests(List<String> requests) {
        this.requests = requests;
    }
}
