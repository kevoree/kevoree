package org.kevoree.library.sky.provider

import javax.naming.Context
import javax.naming.directory.{SearchControls, InitialDirContext}
import java.util.Hashtable
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 17/01/12
 * Time: 09:13
 * To change this template use File | Settings | File Templates.
 */

object InriaLdap extends App {

  def getUUID(login: String): String = {
    val CONTEXT = "com.sun.jndi.ldap.LdapCtxFactory"
    val HOST = "ldap://ildap1-ren.irisa.fr:389"
    val BASE = "ou=people,dc=inria,dc=fr"
    val filter = "(&(objectclass=inriaperson)(inriaentrystatus=valid)(inriaLogin=" + login + "))";
    try {
      val env = new Hashtable[String, String]()
      env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT)
      env.put(Context.PROVIDER_URL, HOST)
      val ctx = new InitialDirContext(env)
      val sc = new SearchControls()
      sc.setSearchScope(SearchControls.SUBTREE_SCOPE)
      val items = ctx.search(BASE, filter, sc)
      import scala.collection.JavaConversions._
      var result = "notfound"
      items.foreach {
        im =>
          im.getAttributes.getAll.find(att => att.getID == "uid") match {
            case Some(att) => {
              result = att.getAll.mkString; true
            }
            case None => false
          }
      }
      result
    } catch {
      case _@e => e.printStackTrace(); "not found"
    }
  }

  def testLogin(login: String, password: String): Boolean = {
    val CONTEXT = "com.sun.jndi.ldap.LdapCtxFactory"
    val HOST = "ldap://ildap1-ren.irisa.fr:389"
    val BASE = "ou=people,dc=inria,dc=fr"
    val filter = "(&(objectclass=inriaperson)(inriaentrystatus=valid)(ou=UR-Rennes) (inriaGroupMemberOf=cn=TRISKELL-ren,ou=groups,dc=inria,dc=fr))";
    try {
      val env = new Hashtable[String, String]()
      env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT)
      env.put(Context.PROVIDER_URL, HOST)
      env.put(Context.SECURITY_AUTHENTICATION, "simple")
      env.put(Context.SECURITY_PRINCIPAL, "uid=" + getUUID(login) + "," + BASE)
      env.put(Context.SECURITY_CREDENTIALS, password)
      val ctx = new InitialDirContext(env);

      val sc = new SearchControls();
      sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
      val items = ctx.search(BASE, filter, sc);
      true
    }
    catch {
      case _@e => false
    }
  }


}