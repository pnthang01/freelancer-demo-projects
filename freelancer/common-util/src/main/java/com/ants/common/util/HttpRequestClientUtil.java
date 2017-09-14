package com.ants.common.util;


import com.sun.research.ws.wadl.HTTPMethods;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by thangpham on 25/07/2017.
 */
public class HttpRequestClientUtil {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:9.0) Gecko/20100101 Firefox/9.0";
    public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; DROID2 GLOBAL Build/S273) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

    private static HttpRequestClientUtil _instance = null;

    private ConcurrentMap<Integer, CloseableHttpClient> httpClientPool;
    private int MAX_SIZE = 10;
    private int DEFAULT_TIMEOUT = 300 * 1000;//15 seconds

    public static HttpRequestClientUtil load() {
        if (null == _instance) {
            init(10);
        }
        return _instance;
    }

    public static HttpRequestClientUtil init(int size) {
        synchronized (HttpRequestClientUtil.class) {
            _instance = new HttpRequestClientUtil(size);
        }
        return _instance;
    }

    public HttpRequestClientUtil(int size) {
        MAX_SIZE = size;
        httpClientPool = new ConcurrentHashMap<>(MAX_SIZE);
    }

    public final CloseableHttpClient getThreadSafeClient() throws Exception {
        int slot = (int) (Math.random() * (MAX_SIZE + 1));
        return getThreadSafeClient(slot);
    }

    public final CloseableHttpClient getThreadSafeClient(int slot) throws Exception {
        CloseableHttpClient httpClient = httpClientPool.get(slot);
        if (null == httpClient) {
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(DEFAULT_TIMEOUT)
                    .setConnectTimeout(DEFAULT_TIMEOUT)
                    .setSocketTimeout(DEFAULT_TIMEOUT).build();
            cm.setMaxTotal(20);
            httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
            httpClientPool.put(slot, httpClient);
        }
        return httpClient;
    }

    public String executePost(String url) throws Exception {
        return executeRequest(new URL(url), HTTPMethods.POST, null, null, null, null);
    }

    public String executePost(String url, String bodyContent) throws Exception {
        return executeRequest(new URL(url), HTTPMethods.POST, bodyContent, null, null, null);
    }

    public String executePost(String url, String bodyContent, String userAgent, String acceptCharset, String accept) throws Exception {
        return executeRequest(new URL(url), HTTPMethods.POST, bodyContent, userAgent, acceptCharset, accept);
    }

    public String executeGet(String url) throws Exception {
        return executeRequest(new URL(url), HTTPMethods.GET, null, null, null, null);
    }

    public String executeDelete(String url) throws Exception {
        return executeRequest(new URL(url), HTTPMethods.DELETE, null, null, null, null);
    }

    private String executeRequest(URL url, HTTPMethods method, String bodyContent, String userAgent, String acceptCharset, String accept) throws Exception {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            HttpRequestBase httpRequest;
            if (HTTPMethods.GET.equals(method)) {
                httpRequest = new HttpGet(url.toURI());
            } else if (HTTPMethods.POST.equals(method)) {
                HttpPost tmp = new HttpPost(url.toURI());
                if (!StringUtil.isNullOrEmpty(bodyContent)) tmp.setEntity(new StringEntity(bodyContent));
                httpRequest = tmp;
            } else if (HTTPMethods.DELETE.equals(method)) {
                httpRequest = new HttpDelete(url.toURI());
            } else {
                throw new IllegalArgumentException("Http Method does not satisfy");
            }
            if (!StringUtil.isNullOrEmpty(userAgent)) httpRequest.setHeader("User-Agent", userAgent);
            if (!StringUtil.isNullOrEmpty(userAgent)) httpRequest.setHeader("Accept-Charset", acceptCharset);
            if (!StringUtil.isNullOrEmpty(userAgent)) httpRequest.setHeader("Accept", accept);
            httpClient = getThreadSafeClient();
            response = httpClient.execute(httpRequest);
            int code = response.getStatusLine().getStatusCode();
            if (code == 200 || code == 202) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String html = EntityUtils.toString(entity, "UTF-8");
                    EntityUtils.consume(entity);
                    return html;
                }
            } else if (code == 404) {
                return "404";
            } else {
                return "500";
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception err) {
            }
        }
        return "";
    }

}