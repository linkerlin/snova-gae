import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.misc.CompressEvent;
import org.arch.event.misc.EncryptEvent;
import org.snova.gae.common.EventHeaderTags;
import org.snova.gae.common.GAEConstants;
import org.snova.gae.common.GAEEventHelper;
import org.snova.gae.common.event.AuthRequestEvent;
import org.snova.gae.common.event.AuthResponseEvent;
import org.snova.gae.common.event.GAEEvents;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 */

/**
 * @author wqy
 *
 */
public class GoClient {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		AuthRequestEvent ev = new AuthRequestEvent();
		ev.appid = "1";
		ev.passwd = GAEConstants.ANONYMOUSE_NAME;
		ev.user = GAEConstants.ANONYMOUSE_NAME;
		
		HTTPRequestEvent httpev = new HTTPRequestEvent();
		EventHeaderTags tags = new EventHeaderTags();
		Buffer buf = GAEEventHelper.encodeEvent(tags, ev);
		
		//Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("g.cn", 80));
		URLConnection conn = new URL("http://192.168.56.101:8080/invoke").openConnection();
		HttpURLConnection hconn = (HttpURLConnection) conn;

		hconn.setRequestMethod("POST");
		hconn.setDoOutput(true);
		hconn.setDoOutput(true);
		hconn.setRequestProperty("Content-Type","application/octet-stream");
		byte[] content = buf.toArray();
		hconn.setFixedLengthStreamingMode(content.length);
		hconn.getOutputStream().write(content);
		
		byte[] xb = new byte[4096];
		int len = hconn.getInputStream().read(xb);
		Buffer resbuf = Buffer.wrapReadableContent(xb,  0, len);
		GAEEvents.init(null, false);
		Event xev = GAEEventHelper.parseEvent(resbuf);
		System.out.println(xev.getClass().getName());
		EncryptEvent eev = (EncryptEvent) xev;
		CompressEvent cev = (CompressEvent) eev.ev;
		AuthResponseEvent aev = (AuthResponseEvent) cev.ev;
		System.out.println(aev.appid);
		System.out.println(aev.token);
		System.out.println(aev.error);
	}

}
