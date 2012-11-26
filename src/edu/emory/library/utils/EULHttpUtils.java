/**
 * file oxygen/src/edu/emory/library/utils/EULHttpUtils.java
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

// http requests
import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.StringBuffer;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *  Utility class for reusable HTTP functionalty.
 */
public class EULHttpUtils {

    /**
     *  Utility method to get the contents of a URL into a string.
     */
    public static String readUrlContents(String url) throws Exception {
        URL urlObj = new URL(url);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) urlObj.openConnection();
        connection.setDoOutput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();
        String result = "";
        result = sb.toString();
        return result;
    }

}