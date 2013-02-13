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

//import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.SelectionActionSpotlight;
import edu.emory.library.namedropper.plugins.PluginOptions;
import edu.emory.library.spotlight.SpotlightAnnotation;


//@RunWith(PowerMockRunner.class)
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
        //this.mockAction = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

//public void calculateOriginalSurfaceForm(String text, SpotlightAnnotation sa) {

    @Test
    public void testCalculateOriginalSurfaceForm() {
        JSONObject data = new JSONObject();
        data.put("@URI", "http://some.uri");
        data.put("@surfaceForm", "John Smith");
        data.put("@types", "DBpedia:Person");
        data.put("@support", "65");
        data.put("@offset", "0");
        data.put("@similarityScore", "0.1");
        data.put("@percentageOfSecondRank", "0.1");
        SpotlightAnnotation sa = new SpotlightAnnotation(data);
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
        data.put("@surfaceForm", "John");
        SpotlightAnnotation noSpace = new SpotlightAnnotation(data);
        // input text doesn't matter for terms with no spaces
        this.action.calculateOriginalSurfaceForm("", noSpace);
        assertEquals(noSpace.getSurfaceForm(), noSpace.getOriginalSurfaceForm());

     }


}
