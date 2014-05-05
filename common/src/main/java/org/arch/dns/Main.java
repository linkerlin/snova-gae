/**
 * This file is part of the Test project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Main.java 
 *
 * @author yinqiwen
 *
 */
package org.arch.dns;

import org.arch.dns.exception.NamingException;

import java.io.IOException;
import java.util.Arrays;


/**
 *
 */
public class Main
{
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws NamingException
	 */
	public static void main(String[] args) throws IOException, NamingException
	{
		ResolveOptions options = new ResolveOptions();
		options.useTcp = true;
		long start = System.currentTimeMillis();
		String[] ips = Resolver.resolveIPv4(new String[] { "8.8.8.8" },
		        "facebook.com", options);
		long end = System.currentTimeMillis();
		System.out.println(Arrays.toString(ips));
		System.out.println(end - start);
	}
	
}
