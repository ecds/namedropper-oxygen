package edu.emory.library.oxygen_plugin.tests;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import ro.sync.exml.plugin.selection.SelectionPluginContext;

import edu.emory.library.oxygen_plugin.NameDropper.NameDropperPluginExtension;
import edu.emory.library.oxygen_plugin.NameDropper.ResultChoice;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.File;
import javax.swing.JOptionPane;

import nu.xom.Builder;
import nu.xom.Document;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.contentcompletion.xml.CIElement;



public class NameDropperTest {
    // Mock plugin
    NameDropperPluginExtension mockND;
    
    SelectionPluginContext mockContext;   
    // Mock xml builder
    static Builder realXmlBuilder = new Builder();
    Builder mockXmlBuilder;
    
    
    // Fixtures
    static String autoSuggestReturn;
    static Document viafReturn;
    
    public NameDropperTest() {
    }
    
    //Read file into string used to get fixtures
    public static String readFile( String file ) throws IOException {
        // changed to getResourceAsStream to read from classpath inside jar
        InputStream is = NameDropperTest.class.getResourceAsStream(file);
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
        viafReturn = realXmlBuilder.build(NameDropperTest.class.getResourceAsStream("viafReturn.xml"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        // create mocks
        
        try {
            this.mockND = mock(NameDropperPluginExtension.class);
            this.mockContext = mock(SelectionPluginContext.class);
            this.mockXmlBuilder = mock(Builder.class);
            viafReturn = realXmlBuilder.build(NameDropperTest.class.getResourceAsStream("viafReturn.xml"));
          
    }catch (Exception e){}
    }
    
    @After
    public void tearDown() {
        this.mockND = null;
        this.mockXmlBuilder = null;
    }
    
     @Rule
     public ExpectedException exception = ExpectedException.none();

    
    
     @Test
     public void testQueryViaf() {
         
         String result = "";
         String docType = ""; 
         
         try {
          String searchTerm = "Smith";
           HashMap h = new HashMap();
           h.put("query", searchTerm);
           
           // setup corret returns for the method calls
           when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn(autoSuggestReturn);
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
                      
           // EAD version of tags
           docType = "EAD";
           when(this.mockND.queryVIAF(searchTerm, docType)).thenCallRealMethod();
           when(this.mockND.getTagName(docType, "Corporate")).thenReturn("corpname");
           
           // Corp
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<corpname source=\"viaf\" authfilenumber=\"159021806\">Smith</corpname>");
           
           // Person
           
           // Change value to Person 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Personal");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Personal")).thenReturn("persname");
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<persname source=\"viaf\" authfilenumber=\"159021806\">Smith</persname>");
           
           // Geo
           
           // Change value to Geographic 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Geographic");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Geographic")).thenReturn("geogname");
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<geogname source=\"viaf\" authfilenumber=\"159021806\">Smith</geogname>");
           
           
          
           // TEI version of tags
           docType = "TEI";
           when(this.mockND.queryVIAF(searchTerm, docType)).thenCallRealMethod();
           when(this.mockND.getTagName(docType, "Geographic")).thenReturn("name");
           
           // Place
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"place\">Smith</name>");
           
           // Person
           
           // Change value to Person 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Personal");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Personal")).thenReturn("name");
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"person\">Smith</name>");
           
           // Org
           
           // Change value to Geographic 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Corporate");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           when(this.mockND.getTagName(docType, "Corporate")).thenReturn("name");
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"org\">Smith</name>");          

         } catch (Exception e){}
         
         
     }
          
     @Test
     public void testQueryViafNoDocType() throws Exception {
         
         try {
              exception.expect(Exception.class);
              exception.expectMessage("No DocType selected");
              String result = "";
              String searchTerm = "Smth";
              HashMap h = new HashMap();
              h.put("query", searchTerm);
           
               // setup corret returns for the method calls
               when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn(autoSuggestReturn);
               when(this.mockND.queryVIAF(searchTerm, "")).thenCallRealMethod();
               when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
               
               result = this.mockND.queryVIAF(searchTerm, "");
         } catch (Exception e){
            throw e;
            
         }
     }
     
     @Test
     public void testGetTagName() {
         
         NameDropperPluginExtension nd = new NameDropperPluginExtension();
         String result = "";
         String docType = ""; 
         
         // no docType set
         result = nd.getTagName(docType);
         assertEquals(null, result);
         
         // TEI document
         docType = "TEI";
         result = nd.getTagName(docType);
         assertEquals("name", result);
         
         // EAD document, no name type
         docType = "EAD";
         result = nd.getTagName(docType);
         assertEquals(null, result);
         
         // EAD with name type
         String nameType = "Personal";
         result = nd.getTagName(docType, nameType);
         assertEquals("persname", result);
         nameType = "Corporate";
         result = nd.getTagName(docType, nameType);
         assertEquals("corpname", result);
         nameType = "Geographic";
         result = nd.getTagName(docType, nameType);
         assertEquals("geogname", result);
         nameType = "Bogus Type";
         result = nd.getTagName(docType, nameType);
         assertEquals(null, result);
         
     }
     
     @Test
     public void testTagAllowed() throws Exception {
         
        int wsId = StandalonePluginWorkspace.MAIN_EDITING_AREA;
        StandalonePluginWorkspace mockWS = mock(StandalonePluginWorkspace.class);

        // simulate editor unavailable - should be null
        when(this.mockContext.getPluginWorkspace()).thenReturn(mockWS);
        when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(null);
        when(this.mockND.tagAllowed("EAD", this.mockContext)).thenCallRealMethod();
        assertEquals(null, this.mockND.tagAllowed("EAD", this.mockContext));

        // simulate editor but no page available - should be null
        WSEditor mockEd = mock(WSEditor.class);
        when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(mockEd);
        when(mockEd.getCurrentPage()).thenReturn(null);
        assertEquals(null, this.mockND.tagAllowed("EAD", this.mockContext));

        // simulate full schema access, no elements allowed; should be false 
        WSXMLTextEditorPage mockPage = mock(WSXMLTextEditorPage.class);
        WSTextXMLSchemaManager mockSchema = mock(WSTextXMLSchemaManager.class);
        when(mockEd.getCurrentPage()).thenReturn(mockPage);
        when(mockPage.getXMLSchemaManager()).thenReturn(mockSchema);
        int offset = 1;
        when(mockPage.getSelectionStart()).thenReturn(offset);
        // getSelectionStart could throw a javax.swing.text.BadLocationException
        WhatElementsCanGoHereContext mockContext;
        mockContext = mock(WhatElementsCanGoHereContext.class);
        when(mockSchema.createWhatElementsCanGoHereContext(offset)).thenReturn(mockContext);
        java.util.List<CIElement> elements = new java.util.ArrayList<CIElement>();
        when(mockSchema.whatElementsCanGoHere(mockContext)).thenReturn(elements);
        assertEquals(false, this.mockND.tagAllowed("EAD", this.mockContext));
        
        // schema access and tag matches an allowed element; should be true 
        CIElement el = mock(CIElement.class);
        when(el.getName()).thenReturn("persname");
        elements.add(el);
        assertEquals(true, this.mockND.tagAllowed("EAD", this.mockContext));
        
     }
     
     @Test
     public void testMakeChoice() {
         try{
             when(this.mockND.makeChoices(autoSuggestReturn)).thenCallRealMethod();
             Object[] choices = this.mockND.makeChoices(autoSuggestReturn);
             ResultChoice choice1 = (ResultChoice)choices[0];
             ResultChoice choice2 = (ResultChoice)choices[1];             
             assertEquals("Smithsonian Institution. Bureau of American Ethnology", choice1.getTerm());
             assertEquals("159021806", choice1.getViafid());
             assertEquals("Smithsonian American art museum Washington, D.C", choice2.getTerm());
             assertEquals("146976922", choice2.getViafid());
         } catch (Exception e) {}
         
     }
     
     @Test
     public void testMakeChoiceNoResults() throws Exception {
         try{
             exception.expect(Exception.class);
             exception.expectMessage("No Results");
             
             String noResultStr = "{\"query\": \"jjfkdjkfjdk\",\"result\": null}";
             
             when(this.mockND.makeChoices(noResultStr)).thenCallRealMethod();
             Object[] choices = this.mockND.makeChoices(noResultStr);
         } catch (Exception e){
             throw e;
         }
         
     }
     
     @Test 
     public void testMakeTag() {
         try{
             String tag = null;
             
             when(this.mockND.makeTag("12345", "someName", "Personal", "EAD")).thenCallRealMethod();
             when(this.mockND.makeTag("12345", "someName", "Corporate", "EAD")).thenCallRealMethod();
             when(this.mockND.makeTag("12345", "someName", "Geographic", "EAD")).thenCallRealMethod();
             when(this.mockND.makeTag("12345", "someName", "Personal", "TEI")).thenCallRealMethod();
             when(this.mockND.makeTag("12345", "someName", "Corporate", "TEI")).thenCallRealMethod();
             when(this.mockND.makeTag("12345", "someName", "Geographic", "TEI")).thenCallRealMethod();
             
             
             tag = this.mockND.makeTag("12345", "someName", "Personal", "EAD");
             assertEquals(tag, "<persname source=\"viaf\" authfilenumber=\"12345\">someName</persname>");
             tag = this.mockND.makeTag("12345", "someName", "Corporate", "EAD");
             assertEquals(tag, "<corpname source=\"viaf\" authfilenumber=\"12345\">someName</corpname>");
             tag = this.mockND.makeTag("12345", "someName", "Geographic", "EAD");
             assertEquals(tag, "<geogname source=\"viaf\" authfilenumber=\"12345\">someName</geogname>");
             tag = this.mockND.makeTag("12345", "someName", "Personal", "TEI");
             assertEquals(tag, "<name ref=\"http://viaf.org/viaf/12345\" type=\"person\">someName</name>");
             tag = this.mockND.makeTag("12345", "someName", "Corporate", "TEI");
             assertEquals(tag, "<name ref=\"http://viaf.org/viaf/12345\" type=\"org\">someName</name>");
             tag = this.mockND.makeTag("12345", "someName", "Geographic", "TEI");
             assertEquals(tag, "<name ref=\"http://viaf.org/viaf/12345\" type=\"place\">someName</name>");
             
             
             
         } catch (Exception e) {}
         
     }
     
     @Test
     public void testMakeTagNoResults() throws Exception {
         try{
             exception.expect(Exception.class);
             exception.expectMessage("Unsupported nameType: BadNameType");
                          
             when(this.mockND.makeTag("12345", "someName", "BadNameType", "EAD")).thenCallRealMethod();
             this.mockND.makeTag("12345", "someName", "BadNameType", "EAD");
         } catch (Exception e){
             throw e;
         }
         
     }
     
     @Test
     public void testResultChoice(){
     ResultChoice rc1 = new ResultChoice();
     
     assertTrue(rc1.getTerm() == null);
     assertTrue(rc1.getViafid() == null);
     
     rc1.settViafid("viaf1");
     rc1.setTerm("term1");
     assertEquals("viaf1", rc1.getViafid());
     assertEquals("term1", rc1.getTerm());
     
     ResultChoice rc2 = new ResultChoice("v2", "t2");
     assertEquals("v2", rc2.getViafid());
     assertEquals("t2", rc2.getTerm());
     
     }
}
