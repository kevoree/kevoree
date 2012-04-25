package org.kevoree.library.javase.authentication;

import java.util.HashMap;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/03/12
 * Time: 18:29
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class TriskellINRIALDAPTester {
	
	public static void main (String[] args) {

		HashMap<String,Object> dictionary = new HashMap<String, Object>();
		dictionary.put("host", "ldap://ildap1-ren.irisa.fr:389");
		dictionary.put("context", "com.sun.jndi.ldap.LdapCtxFactory");
		dictionary.put("base", "ou=people,dc=inria,dc=fr");
		dictionary.put("filter", "(&(objectclass=inriaperson)(inriaentrystatus=valid)(ou=UR-Rennes))"); //(inriaGroupMemberOf=cn=TRISKELL-ren,ou=groups,dc=inria,dc=fr)
		dictionary.put("loginKey", "inriaLogin");

		LDAPAuthentication authenticationService = new LDAPAuthentication();
		authenticationService.setDictionary(dictionary);

		System.out.println(authenticationService.authenticate(args[0], args[1]));

	}
}
