package org.kevoree.library.javase.authentication;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/12
 * Time: 21:09
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface Authentication {

	boolean authenticate(String login, String credentials);
}
