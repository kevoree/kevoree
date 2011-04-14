package com.espertech.esper.client.deploy;

import java.util.List;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Exception list populated in a deployment operation.
 */
public class DeploymentActionException extends DeploymentException {

    private static final long serialVersionUID = -2738808350555092087L;
    
    private static String newline = System.getProperty("line.separator");

    private List<DeploymentItemException> exceptions;

    /**
     * Ctor.
     * @param message deployment error message
     * @param exceptions that occured deploying
     */
    public DeploymentActionException(String message, List<DeploymentItemException> exceptions) {
        super(message);
        this.exceptions = exceptions;
    }

    /**
     * Returns the exception list.
     * @return exceptions
     */
    public List<DeploymentItemException> getExceptions() {
        return exceptions;
    }

    /**
     * Returns a detail print of all exceptions and messages line-separated.
     * @return exception list
     */
    public String getDetail() {
        StringWriter detail = new StringWriter();
        int count = 0;
        String delimiter = "";
        for (DeploymentItemException item : exceptions) {
            detail.write(delimiter);
            detail.write("Exception #");
            detail.write(Integer.toString(count));
            detail.write(" : ");
            detail.write(item.getInner().getMessage());
            delimiter = newline + newline;
            count++;
        }
        return detail.toString();
    }
}
