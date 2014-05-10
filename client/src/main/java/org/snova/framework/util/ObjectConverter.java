package org.snova.framework.util;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.RequestBuilder;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by linkerlin on 5/10/14.
 */
public class ObjectConverter {
    public static com.ning.http.client.Request convert(org.jboss.netty.handler.codec.http.HttpRequest req){
        RequestBuilder builder = new RequestBuilder(req.getMethod().getName());

        for(Map.Entry<String, String> entry:req.getHeaders()){
            builder.setHeader(entry.getKey(),entry.getValue());
        }
        builder.setBody(req.getContent().array());

        return builder.build();
    }

    public static HttpResponse convert(com.ning.http.client.Response response){
        DefaultHttpResponse r=new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.getStatusCode()));
        FluentCaseInsensitiveStringsMap headers=response.getHeaders();
        for(Map.Entry<String, List<String>> entry:headers){
            r.setHeader(entry.getKey(),entry.getValue());
        }

        try {
            r.setContent(ChannelBuffers.wrappedBuffer(response.getResponseBodyAsBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return r;
    }
}
