package com.realogy.schdeular.service.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.realogy.schdeular.service.HttpClient;

@Designate( ocd = HttpClinetImpl.Config.class )
@Component( service = HttpClient.class, immediate = true )
public class HttpClinetImpl
    implements HttpClient {

    @ObjectClassDefinition( name = "Http service" )
    public static @interface Config {

        @AttributeDefinition( name = "Proxy", description = "Need proxy or not" )
        boolean proxy() default false;
    }

    private final static String ERROR_MESSAGE = "Failed to Make the API call";
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private boolean proxyNeeded = true;

    @Activate
    private void activate( Config config ) {
        proxyNeeded = config.proxy();
    }

    @Override
    public JsonObject getData() {

        try ( CloseableHttpClient httpClient = HttpClientBuilder.create().build() ) {

            HttpGet getRequest =
                new HttpGet( "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=MSFT&apikey=4ULM5MM4J3D2B8RW" );
            getRequest.addHeader( "accept", "application/json" );

            HttpResponse response;
            response = httpClient.execute( getRequest );

            if ( response.getStatusLine().getStatusCode() != 200 ) {
                throw new RuntimeException( "Failed : HTTP error code : " + response.getStatusLine().getStatusCode() );
            }

            HttpEntity entity = response.getEntity();
            Header encodingHeader = entity.getContentEncoding();

            // you need to know the encoding to parse correctly
            Charset encoding =
                encodingHeader == null ? StandardCharsets.UTF_8 : Charsets.toCharset( encodingHeader.getValue() );

            // use org.apache.http.util.EntityUtils to read json as string
            String json = EntityUtils.toString( entity, StandardCharsets.UTF_8 );
            Gson g = new Gson();
            JsonObject jsonObject = g.fromJson( json, JsonObject.class );

            logger.info( json );
            httpClient.close();
            return jsonObject;
        } catch ( Exception e ) {
            logger.error( ERROR_MESSAGE, e );
        }
        return null;

    }

}
