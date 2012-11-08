package org.kevoree.library.sky.provider.api;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/10/12
 * Time: 18:22
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SubmissionException extends Exception {
	public SubmissionException (String message) {
		super(message);
	}

	public SubmissionException (String message, Throwable cause) {
		super(message, cause);
	}
}
