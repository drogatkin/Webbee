/* **********************************************************************
 * WebBee Copyright 2009-2015 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.aldan3.data.util.SimpleField;
import org.aldan3.model.Log;

import com.beegman.webbee.model.Auth;
/** This class gives an example of Auth implementation
 * against LDAP/AD
 * @author Dmitriy
 *
 */
public class LDAPAuth extends Auth {
	static String [] LDAD_ATTRS = { "user",  "sn", "givenName", "mail", "cn" }; //"password",
	private Properties ldapProps;
	public LDAPAuth(Properties ldapProps) {
		this.ldapProps = ldapProps;
		for (String name : LDAD_ATTRS) {
			defineField(new SimpleField(name));
		}
		definePassword();
	}
	
	protected void definePassword() {
		defineField(new SimpleField("password"));
	}

	@Override
	public String getFullUserNameFieldName() {
		return "cn";
	}

	@Override
	public String getPrincipalFieldName() {
		return "user";
	}

	@Override
	public String getUserKeyName() {
		return "user";
	}

	@Override
	public void authenticate() {
		String searchFilter = "(&(objectCategory=person)(objectClass=user)(sAMAccountName=" + get("user") + "))";

		// Create the search controls
		SearchControls searchCtls = new SearchControls();
		searchCtls.setReturningAttributes(LDAD_ATTRS);

		// Specify the search scope
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapProps.getProperty("ldapHost"));
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, get("user") + "@" + ldapProps.getProperty("domain"));
		env.put(Context.SECURITY_CREDENTIALS, (String) get("password"));

		LdapContext ctxGC = null;

		try {
			ctxGC = new InitialLdapContext(env, null);
			// Search objects in GC using filters
			NamingEnumeration answer = ctxGC.search(ldapProps.getProperty("searchBase"), searchFilter,
					searchCtls);
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				Attributes attrs = sr.getAttributes();
				if (attrs != null) {
					NamingEnumeration ne = attrs.getAll();
					while (ne.hasMore()) {
						Attribute attr = (Attribute) ne.next();
						modifyField(attr.getID(), attr.get());
					}
					ne.close();
					principle = (String) get(getPrincipalFieldName());
				}
			}
		} catch (NamingException ex) {
			Log.l.error("", ex);
		}
	}

}
