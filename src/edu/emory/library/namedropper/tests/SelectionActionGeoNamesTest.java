/**
 * file src/edu/emory/library/namedropper/tests/SelectionActionGeoNamesTest.java
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
import javax.swing.JOptionPane;
import java.awt.Component;
import javax.swing.Icon;

import org.geonames.WebService;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.SelectionActionGeoNames;
import edu.emory.library.namedropper.plugins.PluginOptions;


@RunWith(PowerMockRunner.class)
@PrepareForTest({PluginOptions.class, WebService.class, JOptionPane.class})
public class SelectionActionGeoNamesTest {
    SelectionActionGeoNames action;

    @Before
    public void setUp() {
        // initialize mock action
        this.action = Mockito.mock(SelectionActionGeoNames.class);
    }

    @After
    public void tearDown() {
        this.action = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNoDoctype() throws Exception {
        String term = "Belfast";
        Mockito.when(this.action.queryGeoNames(term)).thenCallRealMethod();
        // no doctype
        assertEquals(null, this.action.queryGeoNames(term));
    }

    @Test
    public void testQueryNoUsername() throws Exception {
        PowerMockito.mockStatic(PluginOptions.class);
        exception.expect(Exception.class);
        exception.expectMessage("Please set a GeoNames.org username");

        // make sure username option is empty
        //Mockito.when(SelectionActionGeoNames.getGeoNamesUsername()).thenReturn("");
        Mockito.when(PluginOptions.getOption(SelectionActionGeoNames.GEONAMES_USERNAME)).thenReturn("");
        String term = "Belfast";
        Mockito.when(this.action.queryGeoNames(term)).thenCallRealMethod();
        // doctype must be set
        this.action.docType = DocumentType.EAD;
        this.action.queryGeoNames(term);
    }

    @Test
    public void testQueryNoResults() throws Exception {
        PowerMockito.mockStatic(PluginOptions.class);
        PowerMockito.mockStatic(WebService.class);
        exception.expect(Exception.class);
        exception.expectMessage("No Results");

        String term = "Belfast";
        String username = "user";
        // make sure username option is not empty
        Mockito.when(PluginOptions.getOption(SelectionActionGeoNames.GEONAMES_USERNAME)).thenReturn(username);
        // set mocked service to return an empty search result
        ToponymSearchResult emptyResult = new ToponymSearchResult();
        emptyResult.setTotalResultsCount(0);
        Mockito.when(WebService.search((ToponymSearchCriteria)Mockito.anyObject())).thenReturn(emptyResult);

        Mockito.when(this.action.queryGeoNames(term)).thenCallRealMethod();
        // doctype must still be set
        this.action.docType = DocumentType.EAD;
        this.action.queryGeoNames(term);
        PowerMockito.verifyStatic();
        WebService.setUserName(username);
    }

    @Test
    public void testGetUserSelection() {
        PowerMockito.mockStatic(JOptionPane.class);
        List<Toponym> places = new ArrayList<Toponym>();
        Toponym p1 = new Toponym();
        p1.setName("Irish Sea");
        p1.setCountryName("");
        places.add(p1);
        Toponym p2 = new Toponym();
        p2.setName("Dublin");
        p2.setCountryName("Ireland");
        places.add(p2);

        String[] expectedLabels = new String[2];
        expectedLabels[0] = "Irish Sea";
        expectedLabels[1] = "Dublin (Ireland)";

        Mockito.when(this.action.getUserSelection(places)).thenCallRealMethod();
        // no selection
        Mockito.when(JOptionPane.showInputDialog((Component)Mockito.anyObject(),
            Mockito.anyObject(), Mockito.anyString(),
            Mockito.anyInt(), (Icon)Mockito.anyObject(),
            Mockito.eq(expectedLabels), Mockito.anyObject())).thenReturn(null);
        Toponym result = this.action.getUserSelection(places);
        assertEquals(null, result);

        // confirm label selection maps to correct Toponym object
        Mockito.when(JOptionPane.showInputDialog((Component)Mockito.anyObject(),
            Mockito.anyObject(), Mockito.anyString(),
            Mockito.anyInt(), (Icon)Mockito.anyObject(),
            Mockito.eq(expectedLabels), Mockito.anyObject())).thenReturn(expectedLabels[0]);
        result = this.action.getUserSelection(places);
        assertEquals(p1, result);

        Mockito.when(JOptionPane.showInputDialog((Component)Mockito.anyObject(),
            Mockito.anyObject(), Mockito.anyString(),
            Mockito.anyInt(), (Icon)Mockito.anyObject(),
            Mockito.eq(expectedLabels), Mockito.anyObject())).thenReturn(expectedLabels[1]);
        result = this.action.getUserSelection(places);
        assertEquals(p2, result);
    }


}
