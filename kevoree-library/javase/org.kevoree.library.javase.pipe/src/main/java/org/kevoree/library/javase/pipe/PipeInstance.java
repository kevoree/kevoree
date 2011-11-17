package org.kevoree.library.javase.pipe;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/11/11
 * Time: 20:01
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface PipeInstance {

	public String getName ();

	public String getNodeName ();

	public void localForward (Object data);
}
