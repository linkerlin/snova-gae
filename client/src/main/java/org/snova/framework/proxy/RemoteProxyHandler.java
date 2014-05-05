/**
 * 
 */
package org.snova.framework.proxy;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;


/**
 * @author yinqiwen
 * 
 */
public interface RemoteProxyHandler
{
	public void handleRequest(LocalProxyHandler local, HttpRequest req);
	
	public void handleChunk(LocalProxyHandler local, HttpChunk chunk);
	
	public void handleRawData(LocalProxyHandler local, ChannelBuffer raw);
	
	public void close();
	
	public String getName();
}
