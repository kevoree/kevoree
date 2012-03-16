package org.kevoree.library.javase.authentication;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/12
 * Time: 20:51
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "host", optional = false),
		@DictionaryAttribute(name = "context", optional = false),
		@DictionaryAttribute(name = "base", optional = false),
		@DictionaryAttribute(name = "filter", optional = false),
		@DictionaryAttribute(name = "loginKey", optional = false)
})
@Provides({
		@ProvidedPort(name = "authenticate", type = PortType.SERVICE, className = Authentication.class)
})
public class LDAPAuthentication extends AbstractComponentType implements Authentication {

	@Start
	@Stop
	public void dummy(){}


	private String getUUID (String login) throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, getDictionary().get("context").toString());
		env.put(Context.PROVIDER_URL, getDictionary().get("host").toString());
		DirContext ctx = new InitialDirContext(env);
		SearchControls sc = new SearchControls();
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<SearchResult> items = ctx
				.search(getDictionary().get("base").toString(), "(&" + getDictionary().get("filter").toString() + "(" + getDictionary().get("loginKey").toString() + "=" + login + "))", sc);
		String result = null;
		while (items.hasMore()) {
			SearchResult item = items.next();
			if (item.getAttributes().get("uid") != null) {
				result = item.getAttributes().get("uid").get().toString();
			}
		}
		return result;
	}

	@Port(name = "authenticate", method = "authenticate")
	public boolean authenticate (String login, String password) {
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, getDictionary().get("context").toString());
			env.put(Context.PROVIDER_URL, getDictionary().get("host").toString());
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, "uid=" + getUUID(login) + "," + getDictionary().get("base").toString());
			env.put(Context.SECURITY_CREDENTIALS, password);
			DirContext ctx = new InitialDirContext(env);

			SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);


			ctx.search(getDictionary().get("base").toString(), getDictionary().get("filter").toString(), sc);
			return true;
		} catch (NamingException e) {
			return false;
		}
	}
}
