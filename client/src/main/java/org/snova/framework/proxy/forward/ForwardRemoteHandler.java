/**
 * 
 */
package org.snova.framework.proxy.forward;

import com.ning.http.client.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.framework.proxy.LocalProxyHandler;
import org.snova.framework.proxy.RemoteProxyHandler;
import org.snova.framework.proxy.hosts.HostsService;
import org.snova.framework.server.ProxyHandler;
import org.snova.framework.util.ObjectConverter;
import org.snova.framework.util.SharedObjectHelper;
import org.snova.http.client.HttpClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;

/**
 * @author yinqiwen
 * 
 */
public class ForwardRemoteHandler implements RemoteProxyHandler
{
	protected static Logger	  logger	= LoggerFactory
	                                         .getLogger(ForwardRemoteHandler.class);
//	private static HttpClient	directHttpClient;
    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	
	private LocalProxyHandler	localHandler;
	private HttpClientHandler	proxyClientHandler;
	private ChannelFuture	  proxyTunnel;
	private URL	              targetAddress;
	
	private static void initHttpClient() throws Exception
	{
//		if (null != directHttpClient)
//		{
//			return;
//		}
//		directHttpClient = new HttpClient(null,
//		        SharedObjectHelper.getClientBootstrap());
	}
	
	public ForwardRemoteHandler(Map<String, String> attrs)
	{
		try
		{
			initHttpClient();
			for (String attr : attrs.keySet())
			{
                // TODO: https support???
				if (attr.startsWith("http://") || attr.startsWith("socks://"))
				{
					targetAddress = new URL(attr);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
            logger.error("ForwardRemoteHandler init failed."+e.getMessage());
		}
		
	}
	
	@Override
	public void handleRequest(final LocalProxyHandler local,
	        final HttpRequest req)
	{
		localHandler = local;
		if (req.getMethod().equals(HttpMethod.CONNECT))
		{
			ProxyHandler p = (ProxyHandler) localHandler;
			p.switchRawHandler();
			String address = req.getUri();
			String host = address;
			final String x = host;
			int port = 443;
			if (address.contains(":"))
			{
				host = address.split(":")[0];
				port = Integer.parseInt(address.split(":")[1]);
			}
			host = HostsService.getRealHost(host, port);
			logger.info("Find " + host + " for " + x);
			final InetSocketAddress remote = new InetSocketAddress(host, port);
			proxyTunnel = SharedObjectHelper.getClientBootstrap().connect(
			        remote);
			proxyTunnel.getChannel().getConfig().setConnectTimeoutMillis(5000);
			proxyTunnel.addListener(new ChannelFutureListener()
			{
				public void operationComplete(ChannelFuture future)
				        throws Exception
				{
					if (future.isSuccess())
					{
						byte[] established = "HTTP/1.1 200 Connection established\r\n\r\n"
						        .getBytes();
						local.handleRawData(ForwardRemoteHandler.this,
						        ChannelBuffers.wrappedBuffer(established));
					}
					else
					{
						close();
						local.onProxyFailed(ForwardRemoteHandler.this, req);
						logger.warn("Failed to connect " + remote);
					}
				}
			});
			proxyTunnel.getChannel().getPipeline()
			        .addLast("Forward", new SimpleChannelUpstreamHandler()
			        {
                        public void exceptionCaught(
                                ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
                            if(e instanceof java.nio.channels.ClosedChannelException){
                                // do nothing
                            }else{
                                super.exceptionCaught(ctx, e);
                            }
                        }
				        public void channelClosed(ChannelHandlerContext ctx,
				                ChannelStateEvent e) throws Exception
				        {
					        doClose();
				        }
				        
				        public void messageReceived(ChannelHandlerContext ctx,
				                MessageEvent e) throws Exception
				        {
					        localHandler.handleRawData(
					                ForwardRemoteHandler.this,
					                (ChannelBuffer) e.getMessage());
				        }
			        });
		}
		else
		{
            try {
                asyncHttpClient.prepareRequest(ObjectConverter.convert(req)).execute(new AsyncHandler<Response>() {
                    private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

                    /**
                     * Invoked when an unexpected exception occurs during the processing of the response. The exception may have been
                     * produced by implementation of onXXXReceived method invocation.
                     *
                     * @param t a {@link Throwable}
                     */
                    @Override
                    public void onThrowable(Throwable t) {
                        local.onProxyFailed(ForwardRemoteHandler.this,
                                req);
                        logger.error("asyncHttpClient error:"+t.getMessage());
                    }

                    /**
                     * Invoked as soon as some response body part are received. Could be invoked many times.
                     *
                     * @param bodyPart response's body part.
                     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
                     * @throws Exception if something wrong happens
                     */
                    @Override
                    public STATE onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
                        builder.accumulate(bodyPart);
                        return STATE.CONTINUE;
                    }

                    /**
                     * Invoked as soon as the HTTP status line has been received
                     *
                     * @param responseStatus the status code and test of the response
                     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
                     * @throws Exception if something wrong happens
                     */
                    @Override
                    public STATE onStatusReceived(final HttpResponseStatus responseStatus) throws Exception {
                        builder.accumulate(responseStatus);
                        return STATE.CONTINUE;
                    }

                    /**
                     * Invoked as soon as the HTTP headers has been received. Can potentially be invoked more than once if a broken server
                     * sent trailing headers.
                     *
                     * @param headers the HTTP headers.
                     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
                     * @throws Exception if something wrong happens
                     */
                    @Override
                    public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                        builder.accumulate(headers);
                        return STATE.CONTINUE;
                    }

                    /**
                     * Invoked once the HTTP response processing is finished.
                     * <p/>
                     * <p/>
                     * Gets always invoked as last callback method.
                     *
                     * @return T Value that will be returned by the associated {@link java.util.concurrent.Future}
                     * @throws Exception if something wrong happens
                     */
                    @Override
                    public Response onCompleted() throws Exception {
                        Response response = builder.build();
                        local.handleResponse(ForwardRemoteHandler.this,
                                ObjectConverter.convert(response));
                        return response;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
	}
	
	@Override
	public void handleChunk(LocalProxyHandler local, HttpChunk chunk)
	{
		if (null != proxyClientHandler)
		{
			proxyClientHandler.writeBody(chunk);
		}
		else
		{
			doClose();
		}
		
	}
	
	@Override
	public void handleRawData(LocalProxyHandler local, ChannelBuffer raw)
	{
		if (null != proxyTunnel)
		{
			proxyTunnel.getChannel().write(raw);
		}
		else
		{
			doClose();
		}
	}
	
	@Override
	public void close()
	{
//		if (null != proxyClientHandler)
//		{
//			proxyClientHandler.closeChannel();
//			proxyClientHandler = null;
//		}
//		if (null != proxyTunnel && proxyTunnel.getChannel().isOpen())
//		{
//			proxyTunnel.getChannel().close();
//		}
//		proxyTunnel = null;

	}
	
	private void doClose()
	{
//		close();
//		if (null != localHandler)
//		{
//			localHandler.close();
//			localHandler = null;
//		}
	}
	
	@Override
	public String getName()
	{
		return "Forward";
	}
	
}
