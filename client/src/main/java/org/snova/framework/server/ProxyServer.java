/**
 * 
 */
package org.snova.framework.server;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author yinqiwen
 * 
 */
public class ProxyServer
{
	protected static Logger	logger	  = LoggerFactory
	                                          .getLogger(ProxyServer.class);
	
	private ServerBootstrap	bootstrap	= new ServerBootstrap();
	private Channel	        server	  = null;
	
	public ProxyServer(InetSocketAddress listenAddress,
	        final ProxyServerType type)
	{
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
		        Executors.newCachedThreadPool(),
		        Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{
			@Override
			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", new ProxyHandler(type));
				return pipeline;
			}
		});
		
		// Bind and start to accept incoming connections.
		bootstrap.bind(listenAddress);
	}
	
	public void close()
	{
		if (null != server)
		{
			server.close();
			server = null;
		}
		bootstrap.shutdown();
	}
}
