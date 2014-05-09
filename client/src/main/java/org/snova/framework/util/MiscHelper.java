/**
 * 
 */
package org.snova.framework.util;

import org.arch.config.IniProperties;
import org.arch.util.NetworkHelper;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.framework.config.SnovaConfiguration;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author yinqiwen
 * 
 */
public class MiscHelper
{
	protected static Logger	logger	= LoggerFactory.getLogger(MiscHelper.class);
	
	public static String getURLString(HttpRequest req, boolean withMethod)
	{
		String str = req.getUri();
        if(!str.contains("://"))
        {
        	if(req.getMethod().equals(HttpMethod.CONNECT))
        	{
        		str = "https://" + HttpHeaders.getHost(req);
        	}else{
        		str = "https://" + HttpHeaders.getHost(req);
        		str = str + req.getUri();
        	}
        }
        if(withMethod)
        {
        	str = req.getMethod() + " " + str;
        }
		return str;
	}
	
	public static String fetchContent(String url)
	{
		if(null == url)
		{
			return null;
		}
		byte[] content = null;
		try
		{
			content = NetworkHelper.httpGet(url, null);
		}
		catch (Exception e)
		{
			IniProperties cfg = SnovaConfiguration.getInstance()
			        .getIniProperties();
			String listen = cfg.getProperty("LocalServer", "Listen");
            InetSocketAddress address = new InetSocketAddress(listen.split(":")[0], Integer.valueOf(listen.split(":")[1]));
			try
			{
				content = NetworkHelper.httpGet(url, new Proxy(Proxy.Type.HTTP,
				        new InetSocketAddress(address.getHostString(), address.getPort())));
			}
			catch (Exception e1)
			{
				logger.error("Failed to fetch url:" + url, e1);
			}
		}
		return null != content ? new String(content) : null;
	}
	
}
