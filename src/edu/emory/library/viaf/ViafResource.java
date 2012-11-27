/**
 * file src/edu/emory/library/namedropper/viaf/ViafResource.java
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

import java.util.HashMap;

// XML Parsing
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import edu.emory.library.utils.EULHttpUtils;

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
        // NOTE: would be nice if VIAF base url could be set somewhere common
        // and shared with ViafClient
        return String.format("http://viaf.org/viaf/%s/", this.viafid);
    }

    public String getXmlUri() {
        return this.getUri() + "viaf.xml";
    }

    public Document getXmlDetails() {
        // TODO: consider using RDF for viaf details instead of XML
        if (this.details == null) {
            // FIXME: can we do content-negotiation here instead?
            String result = "";
            try {
                // Request canonical resource URI with an accept header of
                // application/xml to get the xml content (content negotiation)
                HashMap headers = new HashMap<String, String>();
                headers.put("Accept", "application/xml");
                result = EULHttpUtils.readUrlContents(this.getUri(), headers);

            } catch (Exception e) {
                // could be java.io.IOException or java.net.MalformedURLException
            }

            // FIXME: test that result is non-empty? (expected behavior ?)
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
        String type = null;
        Document details = this.getXmlDetails();
        if (details != null) {
            // FIXME: this may be rather dependent on current document structure;
            // it might be better to use an XPath or similar here.
            Element nameType = this.getXmlDetails().getRootElement().getFirstChildElement("nameType",
                this.viafNamespace);
            if (nameType != null) {
                type = nameType.getValue();
            }
        }
        return type;
    }

}