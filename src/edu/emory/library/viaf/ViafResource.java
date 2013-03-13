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

import java.util.logging.Logger;
import java.util.HashMap;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

// XML Parsing
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

// RDF parsing
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.helpers.StatementCollector;

import edu.emory.library.utils.EULHttpUtils;

public class ViafResource {

    private String viafid;
    private String label;

    public static String viafNamespace = "http://viaf.org/viaf/terms#";

    private Document details = null;

    private final static Logger LOGGER = Logger.getLogger(ViafResource.class.getName());

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
        return String.format("http://viaf.org/viaf/%s", this.viafid);
    }

    public String getXmlUri() {
        return this.getUri() + "/viaf.xml";
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
                LOGGER.warning(String.format("Exception reading url %s : %s",
                    this.getUri(), e.getMessage()));
            }

            if (! result.isEmpty()) {
                try {
                    Builder xmlbuilder = new Builder();
                // Build doc from xml content returned by the http call
                    this.details = xmlbuilder.build(result, this.getXmlUri());
                } catch (Exception e) {
                // could throw nu.xom.ParsingException
                    LOGGER.warning(String.format("Error parsing XML %s : %s",
                        this.getXmlUri(), e.getMessage()));
                }
            }
        }
        return this.details;
    }

    private Graph rdfGraph = null;

    public Graph getRdfDetails() throws Exception {
        // graph data is saved the first time it is requested
        // if already set, return without making any http requests
        if (rdfGraph != null) { return rdfGraph; }

        String result = "";
        try {
            // Request canonical resource URI with an accept header of
            // application/rdf+xml to get the rdf record
            HashMap headers = new HashMap<String, String>();
            headers.put("Accept", "application/rdf+xml");
            result = EULHttpUtils.readUrlContents(this.getUri(), headers);

        } catch (Exception e) {
            // could be java.io.IOException or java.net.MalformedURLException
            LOGGER.warning(String.format("Error reading URL %s : %s",
                this.getUri(), e.getMessage()));
        }

        // convert response to an inputstream the rdf parser can use; preserve utf-8
        InputStream str = new ByteArrayInputStream(result.getBytes("UTF-8"));
        try {
            RDFParser rdfParser = new RDFXMLParser();
            rdfGraph = new GraphImpl();
            StatementCollector collector = new StatementCollector(rdfGraph);
            rdfParser.setRDFHandler(collector);
            rdfParser.parse(str, this.getUri());
            rdfParser.setStopAtFirstError(false);
        } catch (RDFParseException rpe) {
            LOGGER.warning(String.format("Error parsing RDF for %s : %s",
                this.getUri(), rpe.getMessage()));

            // clear out graph (parse error results in empty graph)
            rdfGraph = null;
        }
        return rdfGraph;
    }

    /**
     * Get the URI for this ViafResource as a Resource
     * for use in querying an RDF graph.
     */
    public Resource getUriResource() {
        return new URIImpl(this.getUri());
    }

    // sameAs URI relation, for querying RDF graph
    private static URI sameAs = new URIImpl("http://www.w3.org/2002/07/owl#sameAs");

    /**
     * Test if the RDF for the current ViafResource indicates
     * that has an owl:sameAs relation to a specified URI.
     */
    public boolean isSameAs(String uri) {
        Graph data = null;
        try {
            data = this.getRdfDetails();
        } catch (Exception e) {
            // any errors loading RDF should be logged in getRdfDetails
        }

        if (data != null) {
            Resource object = new URIImpl(uri);
            return data.match(this.getUriResource(), sameAs, object).hasNext();
        }
        return false;
    }

    public String getType() {
        // TODO: convert to use rdfDetails instead
        // probably query for isA based on type of record
        String type = null;
        Document details = this.getXmlDetails();
        if (details != null) {
            Element nameType = this.getXmlDetails().getRootElement().getFirstChildElement("nameType",
                this.viafNamespace);
            if (nameType != null) {
                type = nameType.getValue();
            }
        }
        return type;
    }

}