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

import org.mockito.Mockito;
// import static org.mockito.Mockito.*;

/* PowerMock used to be able to mock static methods */
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import edu.emory.library.utils.EULHttpUtils;
import edu.emory.library.viaf.ViafResource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EULHttpUtils.class)
public class ViafResourceTest {

    ViafResource mockViafResource;

    // Fixtures
    static Document viafReturn;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // load fixture
        Builder xmlBuilder = new Builder();
        viafReturn = xmlBuilder.build(ViafClientTest.class.getResourceAsStream("viafReturn.xml"));
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
        assertEquals("http://viaf.org/viaf/67890/", res.getUri());
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
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString())).thenReturn("<wrong-xml/>");
        Mockito.when(this.mockViafResource.getXmlDetails()).thenCallRealMethod();
        assertEquals(null, nameType);
    }

    @Test
    public void testGetXmlDetails() throws Exception {
        PowerMockito.mockStatic(EULHttpUtils.class);
        Document details;
        Mockito.when(this.mockViafResource.getXmlDetails()).thenCallRealMethod();

        // empty or unparsable result
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString())).thenReturn("");
        assertNull(this.mockViafResource.getXmlDetails());
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString())).thenReturn("<unfinished-tag");
        assertNull(this.mockViafResource.getXmlDetails());

        // return fixture xml
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString())).thenReturn(viafReturn.toXML());
        details = this.mockViafResource.getXmlDetails();
        assertNotNull(details);
        assert(details instanceof Document);

        // caching - should not load url after first successful request
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString())).thenReturn("");
        assertEquals(details, this.mockViafResource.getXmlDetails());

    }
}
