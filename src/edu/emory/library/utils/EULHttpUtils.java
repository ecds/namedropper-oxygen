/**
 * file src/edu/emory/library/utils/EULHttpUtils.java
 *
 * Copyright 2012 Emory University Library
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.emory.library.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// apache httpclient
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;


/**
 *  Utility class for reusable HTTP functionalty.
 */
public class EULHttpUtils {

    /**
     *  Utility method to GET the contents of a URL and read it into a string.
     *  @param url  url to be read
     *  @return contents of the URL on successful request
     */
    public static String readUrlContents(String url) throws Exception {
        HashMap nomap = new HashMap<String, String>();
        return EULHttpUtils.readUrlContents(url, nomap);
    }

    /**
     *  Utility method to GET the contents of a URL and read it into a string.
     *  @param url      url to be read
     *  @param headers  HashMap of request headers
     */
     public static String readUrlContents(String url, HashMap<String, String> headers) throws Exception {
        String response = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet getMethod = new HttpGet(url);

        // add any request headers specified
        for (Map.Entry<String, String> header : headers.entrySet()) {
            getMethod.addHeader(header.getKey(), header.getValue());
        }

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        // returns the response content as string on success
        response = client.execute(getMethod, responseHandler); // could throw HttpException or IOException
        // TODO: catch/handle errors (esp. periodic 503 when Spotlight is unavailable)
        getMethod.releaseConnection();

        return response;
    }

    /**
     *  Utility method to POST to a URL and read the result into a string
     *  @param url      url to be read
     *  @param headers  HashMap of request headers
     *  @param parameters  HashMap of parameters
     */
     public static String postUrlContents(String url, HashMap<String, String> headers,
        HashMap<String, String> parameters) throws Exception {
        String response = null;
        HttpClient client = new DefaultHttpClient();
        HttpPost postMethod = new HttpPost(url);

        // add any request headers specified
        for (Map.Entry<String, String> header : headers.entrySet()) {
            postMethod.addHeader(header.getKey(), header.getValue());
        }

        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            nvps.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        postMethod.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        // returns the response content as string on success
        response = client.execute(postMethod, responseHandler); // could throw HttpException or IOException
        // TODO: catch/handle errors (esp. periodic 503 when Spotlight is unavailable)
        postMethod.releaseConnection();

        return response;
    }


}