package org.snova.framework.proxy.gae;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.arch.event.Event;
import org.arch.event.EventHandler;
import org.arch.event.EventHeader;
import org.arch.util.ListSelector;
import org.arch.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.framework.event.CommonEventConstants;
import org.snova.framework.event.CommonEvents;
import org.snova.framework.event.gae.AdminResponseEvent;
import org.snova.framework.event.gae.AuthRequestEvent;
import org.snova.framework.event.gae.AuthResponseEvent;
import org.snova.framework.event.gae.RequestSharedAppIDEvent;
import org.snova.framework.event.gae.RequestSharedAppIDResultEvent;
import org.snova.framework.proxy.RemoteProxyHandler;
import org.snova.framework.proxy.RemoteProxyManager;
import org.snova.framework.proxy.RemoteProxyManagerHolder;
import org.snova.framework.proxy.c4.C4;

public class GAE
{
	protected static Logger	           logger	= LoggerFactory
	                                                    .getLogger(GAE.class);
	public static boolean	           enable;
	static ListSelector<GAEServerAuth>	servers	= new ListSelector<GAEServerAuth>();
	
	public static class GAERemoteProxyManager implements RemoteProxyManager
	{
		@Override
		public String getName()
		{
			return "GAE";
		}
		
		@Override
		public RemoteProxyHandler createProxyHandler(Map<String, String> attr)
		{
			if (attr.containsKey("App"))
			{
				String appid = attr.get("App");
				for (int i = 0; i < servers.size(); i++)
				{
					if (servers.get(i).appid.equals(appid))
					{
						return new GAERemoteHandler(servers.get(i));
					}
				}
			}
			GAERemoteHandler handler = new GAERemoteHandler(null);
			handler.injectRange = attr.containsKey("Range");
			return handler;
		}
	}
	
	private static boolean auth(final GAEServerAuth server)
	{
		AuthRequestEvent event = new AuthRequestEvent();
		event.appid = server.appid.trim();
		event.user = server.user.trim();
		event.passwd = server.passwd.trim();
		final GAERemoteHandler handler = new GAERemoteHandler(server);
		handler.requestEvent(event, new EventHandler()
		{
			@Override
			public void onEvent(EventHeader header, Event event)
			{
				if (header.type == CommonEventConstants.AUTH_RESPONSE_EVENT_TYPE)
				{
					AuthResponseEvent res = (AuthResponseEvent) event;
					server.token = res.token;
					if (!StringHelper.isEmptyString(res.token))
					{
						logger.info("Auth token is " + res.token);
					}
					if (!StringHelper.isEmptyString(res.error))
					{
						logger.error("Failed to connect GAE server:"
						        + res.error);
					}
				}
				synchronized (handler)
				{
					handler.notify();
				}
			}
		});
		synchronized (handler)
		{
			try
			{
				handler.wait(60000);
			}
			catch (InterruptedException e)
			{
			}
		}
		return !StringHelper.isEmptyString(server.token);
	}
	
	private static List<GAEServerAuth> fetchSharedAppIDs()
	{
		GAEServerAuth auth = new GAEServerAuth();
		auth.appid = GAEConfig.masterAppID;
		auth.user = "anonymouse";
		auth.passwd = "anonymouse";
		final List<GAEServerAuth> appids = new ArrayList<GAEServerAuth>();
		GAERemoteHandler handler = new GAERemoteHandler(auth);
		handler.requestEvent(new RequestSharedAppIDEvent(), new EventHandler()
		{
			@Override
			public void onEvent(EventHeader header, Event event)
			{
				if (header.type == CommonEventConstants.REQUEST_SHARED_APPID_RESULT_EVENT_TYPE)
				{
					RequestSharedAppIDResultEvent res = (RequestSharedAppIDResultEvent) event;
					for (String s : res.appids)
					{
						GAEServerAuth tmp = new GAEServerAuth();
						tmp.appid = s;
						tmp.user = "anonymouse";
						tmp.passwd = "anonymouse";
						appids.add(tmp);
					}
				}
				else if (header.type == CommonEventConstants.ADMIN_RESPONSE_EVENT_TYPE)
				{
					AdminResponseEvent ev = (AdminResponseEvent) event;
					logger.error("Failed to get shared appid:" + ev.errorCause);
				}
				synchronized (appids)
				{
					appids.notify();
				}
			}
		});
		synchronized (appids)
		{
			try
			{
				appids.wait(30000);
			}
			catch (InterruptedException e)
			{
			}
			return appids;
		}
	}
	
	public static boolean init()
	{
		
		if (!GAEConfig.init())
		{
			return false;
		}
		logger.info("GAE init.");
		if (GAEConfig.appids.isEmpty())
		{
			if (C4.enable)
			{
				enable = false;
				logger.warn("Failed to init GAE since none appid configured.");
				return false;
			}
			GAEConfig.appids = fetchSharedAppIDs();
		}
		
		for (GAEServerAuth server : GAEConfig.appids)
		{
			if (auth(server))
			{
				servers.add(server);
			}
		}
		if (servers.size() == 0)
		{
			logger.warn("Failed to init GAE since none appid fetched.");
			return false;
		}
		
		RemoteProxyManagerHolder
		        .registerRemoteProxyManager(new GAERemoteProxyManager());
		enable = true;
		return true;
	}
	
}
