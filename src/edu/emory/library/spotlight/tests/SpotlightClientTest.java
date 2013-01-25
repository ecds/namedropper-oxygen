/**
 * file src/edu/emory/library/namedropper/spotlight/tests/SpotlightClientTest.java
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


package edu.emory.library.spotlight.tests;

import java.util.List;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.mockito.Mockito;

/* PowerMock used to be able to mock static methods */
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.emory.library.spotlight.SpotlightClient;
import edu.emory.library.spotlight.SpotlightAnnotation;
import edu.emory.library.utils.EULHttpUtils;


@RunWith(PowerMockRunner.class)
@PrepareForTest(EULHttpUtils.class)
public class SpotlightClientTest {

    // sample text to be annotated
    static String text = "Michael Longley was born in Belfast, Northern Ireland.";
    // annotation result for above text
    static String anntotationResponse;

    SpotlightClient mockSpotlightClient;

    // method to read file into a string; used to load fixtures
    // FIXME: duplicated code copied from other tests!!
    public static String readFile( String file ) throws IOException {
        // changed to getResourceAsStream to read from classpath inside jar
        InputStream is = SpotlightClientTest.class.getResourceAsStream(file);
        BufferedReader reader = new BufferedReader( new InputStreamReader(is));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // load fixture data
        anntotationResponse = readFile("spotlightReturn.json");
    }

    @Before
    public void setUp() {
        // init mock client
        this.mockSpotlightClient = Mockito.mock(SpotlightClient.class);
    }

    @After
    public void tearDown() {
        this.mockSpotlightClient = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testAnnotate() throws Exception {
        PowerMockito.mockStatic(EULHttpUtils.class);

        Mockito.when(this.mockSpotlightClient.annotate(text)).thenCallRealMethod();

        HashMap headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(headers))).thenReturn(anntotationResponse);
        List<SpotlightAnnotation> results = this.mockSpotlightClient.annotate(this.text);

        // inspect annotations initialized from fixture
        assertEquals(3, results.size());
        assertTrue(results.get(0) instanceof SpotlightAnnotation);

        // do some minimal inspection of the three results
        SpotlightAnnotation anno = results.get(0);
        assertEquals("http://dbpedia.org/resource/Michael_Longley", anno.getUri());
        assertEquals("Michael_Longley", anno.getId());
        assertEquals("Michael Longley", anno.getSurfaceForm());
        Integer expectedOffset = 0;
        assertEquals(expectedOffset, anno.getOffset());
        assertEquals("Personal", anno.getType());

        anno = results.get(1);
        assertEquals("born", anno.getSurfaceForm());
        expectedOffset = 20;
        assertEquals(expectedOffset, anno.getOffset());

        anno = results.get(2);
        assertEquals("Belfast, Northern Ireland", anno.getSurfaceForm());
        expectedOffset = 28;
        assertEquals(expectedOffset, anno.getOffset());
        assertEquals("Geographic", anno.getType());

    }

    // TODO: can we test getLabel and getAbstract without querying dbpedia in unit tests?
    //@Test   // NOTE: disabling for now; re-enable to test when needed
    public void testDBpediaProperties() throws Exception {
        PowerMockito.mockStatic(EULHttpUtils.class);

        Mockito.when(this.mockSpotlightClient.annotate(text)).thenCallRealMethod();

        HashMap headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        Mockito.when(EULHttpUtils.readUrlContents(Mockito.anyString(),
            Mockito.eq(headers))).thenReturn(anntotationResponse);
        List<SpotlightAnnotation> results = this.mockSpotlightClient.annotate(this.text);

        SpotlightAnnotation anno = results.get(0);
        assertEquals("Michael Longley", anno.getLabel());
        assertEquals("Michael Longley, CBE (born 27 July 1939) is a Northern Irish poet from Belfast.",
            anno.getAbstract());

    }

}



