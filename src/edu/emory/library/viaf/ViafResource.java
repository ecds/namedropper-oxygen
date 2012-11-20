/**
 * file oxygen/src/edu/emory/library/namedropper/viaf/ViafResource.java
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

package edu.emory.library.viaf;

// http requests
import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.StringBuffer;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// XML Parsing
import nu.xom.Builder;
import nu.xom.Document;

public class ViafResource {

    private String viafid;
    private String label;

    public static String viafNamespace = "http://viaf.org/viaf/terms#";

    private Document details = null;

    public ViafResource(String viafid, String label) {
        this.viafid = viafid;
        this.label = label;
    }

    public String getViafId() {
        return this.viafid;
    }

    public String getLabel() {
        return this.label;
    }

    public String toString() {
        return this.label;
    }

    public String getUri() {
        return String.format("http://viaf.org/viaf/%s/", this.viafid);
    }

    protected String getXmlUri() {
        return this.getUri() + "viaf.xml";
    }

    protected Document getXmlDetails() {
        // TODO: consider using RDF for viaf details instead of XML
        if (this.details == null) {
            // FIXME: can we do content-negotiation here instead?
            String result = "";
            try {
                // could throw java.net.MalformedURLException
                URL urlObj = new URL(this.getXmlUri());
                // FIXME: it should be possible to use the base URI with content negotiation
                //  - should use an HTTP_ACCEPT header of application/xml or text/xml
                // URL urlObj = new URL(this.getUri());
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                //connection.addRequestProperty("Accept", "application/xml");
                connection.setDoOutput(true);

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                result = sb.toString();
            } catch (Exception e) {
                // could be java.io.IOException or java.net.MalformedURLException

            }

            try {
                Builder xmlbuilder = new Builder();
                // Build doc from xml content returned by the http call
                this.details = xmlbuilder.build(result, this.getXmlUri());
            } catch (Exception e) {
                // could throw nu.xom.ParsingException
            }

        }
        return this.details;
    }

    public String getType() {
        return this.getXmlDetails().getRootElement().getFirstChildElement("nameType",
            this.viafNamespace).getValue();

    }

}