/**
 * file src/edu/emory/library/namedropper/viaf/tests/ViafClientTest.java
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


package edu.emory.library.viaf.tests;

import java.util.HashMap;

import nu.xom.Builder;
import nu.xom.Document;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.openrdf.model.Graph;

import org.mockito.Mockito;
// import static org.mockito.Mockito.*;

/* PowerMock used to be able to mock static methods */
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import edu.emory.library.utils.EULHttpUtils;
import edu.emory.library.viaf.ViafResource;
import edu.emory.library.viaf.tests.ViafClientTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EULHttpUtils.class)
public class ViafResourceTest {

    ViafResource mockViafResource;

    // Fixtures
    static Document viafReturn;
    static HashMap xmlheader = new HashMap<String, String>();

    //static Document viafRdfRecord;
    static String viafRdfRecord;
    static HashMap rdfxmlheader = new HashMap<String, String>();

    @BeforeClass
    public static void setUpClass() throws Exception {
        // load fixture
        Builder xmlBuilder = new Builder();
        viafReturn = xmlBuilder.build(ViafClientTest.class.getResourceAsStream("viafReturn.xml"));
        xmlheader.put("Accept", "application/xml");

        viafRdfRecord = ViafClientTest.readFile("viafRdfRecord.xml");
        rdfxmlheader.put("Accept", "application/rdf+xml");
    }

    @Before
    public void setUp() {
        // init mock resource
        this.mockViafResource = Mockito.mock(ViafResource.class);
    }

    @After
    public void tearDown() {
        this.mockViafResource = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testInit() {
        String viafid = "67890";
        String term = "Joe Schmo";
        ViafResource res = new ViafResource(viafid, term);

        assertEquals(viafid, res.getViafId());
        assertEquals(term, res.getLabel());

        // test other basic properties
        assertEquals(term, res.toString());
        assertEquals("http://viaf.org/viaf/67890", res.getUri());
        assertEquals("http://viaf.org/viaf/67890/viaf.xml", res.getXmlUri());
    }

    @Test
    public void testGetType() throws Exception {
        PowerMockito.mockStatic(EULHttpUtils.class);
        String nameType;

        Mockito.when(this.mockViafResource.getXmlDetails()).thenReturn(viafReturn);
        Mockito.when(this.mockViafResource.getType()).thenCallRealMethod();

        nameType = this.mockViafResource.getType();
        assertEquals("Corporate", nameType);

        // details unavailable - should not raise an exception
        Mockito.when(this.mockViafResource.getXmlDetails()).thenReturn(null);
        nameType = this.mockViafResource.getType();
        assertEquals(null, nameType);

        // wrong xml - should not raise an exception

        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(xmlheader))).thenReturn("<wrong-xml/>");
        Mockito.when(this.mockViafResource.getXmlDetails()).thenCallRealMethod();
        assertEquals(null, nameType);
    }

    @Test
    public void testGetXmlDetails() throws Exception {
        PowerMockito.mockStatic(EULHttpUtils.class);
        Document details;
        Mockito.when(this.mockViafResource.getXmlDetails()).thenCallRealMethod();

        // empty or unparsable result
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(xmlheader))).thenReturn("");
        assertNull(this.mockViafResource.getXmlDetails());
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(xmlheader))).thenReturn("<unfinished-tag");
        assertNull(this.mockViafResource.getXmlDetails());

        // return fixture xml
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(xmlheader))).thenReturn(viafReturn.toXML());
        details = this.mockViafResource.getXmlDetails();
        assertNotNull(details);
        assert(details instanceof Document);

        // caching - should not load url after first successful request
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(xmlheader))).thenReturn("");
        assertEquals(details, this.mockViafResource.getXmlDetails());

    }

    @Test
    public void testGetRdfDetails() throws Exception {
        PowerMockito.mockStatic(EULHttpUtils.class);

        Mockito.when(this.mockViafResource.getRdfDetails()).thenCallRealMethod();
        Mockito.when(this.mockViafResource.getUri()).thenReturn("http://viaf.org/viaf/39398205");

        // return empty string (invalid rdf)
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(rdfxmlheader))).thenReturn("");
        Graph data = this.mockViafResource.getRdfDetails();
        assertNull(data);

        // return rdf fixture
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(rdfxmlheader))).thenReturn(viafRdfRecord);
        data = this.mockViafResource.getRdfDetails();
        assertNotNull(data);

    }

    @Test
    public void testIsSameAs() throws Exception {
        PowerMockito.mockStatic(EULHttpUtils.class);

        Mockito.when(this.mockViafResource.getRdfDetails()).thenCallRealMethod();
        Mockito.when(this.mockViafResource.isSameAs(Mockito.anyString())).thenCallRealMethod();
        Mockito.when(this.mockViafResource.getUri()).thenReturn("http://viaf.org/viaf/39398205");
        // use rdf fixture
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(rdfxmlheader))).thenReturn(viafRdfRecord);


        // URIs for a few of the sameAs relations in the fixture RDF
        String dbpediaUri = "http://dbpedia.org/resource/Michael_Longley";
        String bnfUri = "http://data.bnf.fr/ark:/12148/cb12058813z#foaf:Person";
        String idref = "http://www.idref.fr/02883416X/id";

        // should match
        assertEquals(true, this.mockViafResource.isSameAs(dbpediaUri));
        assertEquals(true, this.mockViafResource.isSameAs(bnfUri));
        assertEquals(true, this.mockViafResource.isSameAs(idref));
        // should not match
        assertEquals(false, this.mockViafResource.isSameAs("http://some.other/uri"));

    }

}
