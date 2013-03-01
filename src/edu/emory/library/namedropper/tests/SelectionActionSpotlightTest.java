/**
 * file src/edu/emory/library/namedropper/tests/SelectionActionSpotlightTest.java
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


package edu.emory.library.namedropper.tests;

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

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

//import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.SelectionActionSpotlight;
import edu.emory.library.namedropper.plugins.PluginOptions;
import edu.emory.library.spotlight.SpotlightAnnotation;
import edu.emory.library.spotlight.SpotlightClient;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpotlightClient.class, SelectionActionSpotlight.class,
    WSTextEditorPage.class})
public class SelectionActionSpotlightTest {

    SelectionActionSpotlight action;

    @Before
    public void setUp() {
        // NOTE: not currently mocking; mock or spy should be used to test
        // any methods that call spotlight APIs
        this.action = new SelectionActionSpotlight((StandalonePluginWorkspace)PluginOptions.getWorkspace());
    }

    @After
    public void tearDown() {
        this.action = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // utility methods for building annotation object
    JSONObject getAnnotationData() {
        JSONObject data = new JSONObject();
        data.put("@URI", "http://some.uri");
        data.put("@surfaceForm", "John Smith");
        data.put("@types", "DBpedia:Person");
        data.put("@support", "65");
        data.put("@offset", "0");
        data.put("@similarityScore", "0.1");
        data.put("@percentageOfSecondRank", "0.1");
        return data;
    }

    SpotlightAnnotation getTestAnnotation() {
        return new SpotlightAnnotation(this.getAnnotationData());
    }


    @Test
    public void testCalculateOriginalSurfaceForm() {
        SpotlightAnnotation sa = this.getTestAnnotation();
        // name with extra whitespace
        String name = "John      Smith";

        // name at the beginning of text
        String text = name + "some other text after that";

        this.action.calculateOriginalSurfaceForm(text, sa);
        assertEquals(name, sa.getOriginalSurfaceForm());

        // name in the middle of text
        String pretext = "some text before the name ";
        text = pretext + name + " something else";
        sa.adjustOffset(pretext.length());
        sa.setOriginalSurfaceForm("");   // zero out before recalculating
        this.action.calculateOriginalSurfaceForm(text, sa);
        assertEquals(name, sa.getOriginalSurfaceForm());

        // name not in the text
        sa.setOriginalSurfaceForm("");
        text = pretext + " something else";
        this.action.calculateOriginalSurfaceForm(text, sa);
        // surface form used when name not found in text via regex
        assertEquals(sa.getSurfaceForm(), sa.getOriginalSurfaceForm());

        // name with no whitespace
        JSONObject data = this.getAnnotationData();
        data.put("@surfaceForm", "John");
        SpotlightAnnotation noSpace = new SpotlightAnnotation(data);
        // input text doesn't matter for terms with no spaces
        this.action.calculateOriginalSurfaceForm("", noSpace);
        assertEquals(noSpace.getSurfaceForm(), noSpace.getOriginalSurfaceForm());

    }

    @Test
    public void testfindAnnotations() throws Exception {

        // init a mock spotlight client and configure to turn empty result
        SpotlightClient mockSpotlightClient = Mockito.mock(SpotlightClient.class);
        List<SpotlightAnnotation> spotlightResult = new ArrayList<SpotlightAnnotation>();
        Mockito.when(mockSpotlightClient.annotate(Mockito.anyString())).thenReturn(spotlightResult);
        // use powermock to override constructor to return mock spotlight client
        PowerMockito.whenNew(SpotlightClient.class).withArguments(Mockito.anyDouble(),
            Mockito.anyInt(), Mockito.anyString()).thenReturn(mockSpotlightClient);
        WSTextEditorPage mockPage = Mockito.mock(WSTextEditorPage.class);

        SelectionActionSpotlight mockAction = Mockito.mock(SelectionActionSpotlight.class);

        Mockito.when(mockAction.getCurrentPage()).thenReturn(mockPage);
        // settings are static methods
        PowerMockito.mockStatic(SelectionActionSpotlight.class);
        Mockito.when(SelectionActionSpotlight.getSpotlightConfidence()).thenReturn("0.4");
        Mockito.when(SelectionActionSpotlight.getSpotlightSupport()).thenReturn("250");
        Mockito.when(SelectionActionSpotlight.getSpotlightUrl()).thenReturn(SpotlightClient.defaultUrl);
        // finally, call the actual method we want to test
        Mockito.when(mockAction.findAnnotations(Mockito.anyString())).thenCallRealMethod();

        exception.expect(Exception.class);
        exception.expectMessage("No resources were identified in the selected text");
        mockAction.findAnnotations("some text to annotate");

        // verify that document was set uneditable and then made editable
        Mockito.verify(mockPage).setEditable(false);
        Mockito.verify(mockPage).setEditable(true);

        // simulate spotlight result found
        spotlightResult.add(this.getTestAnnotation());

        mockAction.findAnnotations("some text to annotate");
        Mockito.verify(mockPage).setEditable(false);
        // not switched back to editable because results were found
        Mockito.verify(mockPage, Mockito.times(0)).setEditable(true);

        /* NOTE: currently untested, but might be worth adding tests for
          (and maybe splitting out into smaller functions):
          - tag cleanup from highlighted text
          - spotlight calling / interaction
          - offset adjustments
          - filtering based on allowed tags
        */
    }

}
