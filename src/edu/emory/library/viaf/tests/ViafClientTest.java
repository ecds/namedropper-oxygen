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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.List;

import java.net.URLEncoder;

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

import edu.emory.library.viaf.ViafClient;
import edu.emory.library.viaf.ViafResource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ViafClient.class)
public class ViafClientTest {

    // Fixtures
    static String autoSuggestReturn;
    static Document viafReturn;

    // method to read file into a string; used to load fixtures
    public static String readFile( String file ) throws IOException {
        // changed to getResourceAsStream to read from classpath inside jar
        InputStream is = ViafClientTest.class.getResourceAsStream(file);
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
        // load fixtures
        autoSuggestReturn = readFile("autoSuggestReturn.json");
        Builder xmlBuilder = new Builder();
        viafReturn = xmlBuilder.build(ViafClientTest.class.getResourceAsStream("viafReturn.xml"));
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testSuggest() throws Exception {
        PowerMockito.mockStatic(ViafClient.class);

        String term = "John Smith";
        String expectedUri = String.format("%s/AutoSuggest?query=%s", ViafClient.baseUrl,
            URLEncoder.encode(term, "UTF-8"));
        Mockito.when(ViafClient.suggest(term)).thenCallRealMethod();

        // use mock to simulate no response
        Mockito.when(ViafClient.readUrlContents(expectedUri)).thenReturn("");
        // empty or unparsable result should return an empty list
        List<ViafResource> results = ViafClient.suggest(term);
        assertEquals(0, results.size());

        // use mock to return fixture result
        Mockito.when(ViafClient.readUrlContents(expectedUri)).thenReturn(autoSuggestReturn);

        results = ViafClient.suggest(term);
        assertEquals(2, results.size());

        // inspect that results were initialized correctly
        ViafResource vr = results.get(0);
        assertEquals("Smithsonian Institution. Bureau of American Ethnology", vr.getLabel());
        assertEquals("159021806", vr.getViafId());

        vr = results.get(1);
        assertEquals("Smithsonian American art museum Washington, D.C", vr.getLabel());
        assertEquals("146976922", vr.getViafId());
    }

}



