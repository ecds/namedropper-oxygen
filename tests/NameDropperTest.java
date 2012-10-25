/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import edu.emory.library.oxygen_plugin.NameDropper.NameDropperPluginExtension;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.File;

import nu.xom.Builder;
import nu.xom.Document;
import org.junit.*;
import org.junit.rules.ExpectedException;


public class NameDropperTest {
    // Mock plugin
    NameDropperPluginExtension mockND;
    
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
       BufferedReader reader = new BufferedReader( new FileReader (file));
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
        autoSuggestReturn = readFile("tests/autoSuggestReturn.json");
        viafReturn = realXmlBuilder.build(new File("tests/viafReturn.xml"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        // create mocks
        
        try {
            this.mockND = mock(NameDropperPluginExtension.class);
            this.mockXmlBuilder = mock(Builder.class);
            autoSuggestReturn = readFile("tests/autoSuggestReturn.json");
            viafReturn = realXmlBuilder.build(new File("tests/viafReturn.xml"));
          
    }catch (Exception e){
        e.printStackTrace();
    }}
    
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
           
           // Corp
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<corpname source=\"viaf\" authfilenumber=\"159021806\">Smith</corpname>");
           
           // Person
           
           // Change value to Person 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Personal");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<persname source=\"viaf\" authfilenumber=\"159021806\">Smith</persname>");
           
           // Geo
           
           // Change value to Geographic 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Geographic");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<geogname source=\"viaf\" authfilenumber=\"159021806\">Smith</geogname>");
           
           
          
           // TEI version of tags
           docType = "TEI";
           when(this.mockND.queryVIAF(searchTerm, docType)).thenCallRealMethod();
           
           // Place
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"place\">Smith</name>");
           
           // Person
           
           // Change value to Person 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Personal");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"person\">Smith</name>");
           
           // Org
           
           // Change value to Geographic 
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Corporate");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           result = this.mockND.queryVIAF(searchTerm, docType);
           assertEquals(result, "<name ref=\"http://viaf.org/viaf/159021806\" type=\"org\">Smith</name>");          

         } catch (Exception e){
             e.printStackTrace();
         }
         
         
     }
     
     @Test
     public void testQueryViafNoResults() throws Exception{
         
         try {
         exception.expect(Exception.class);
         exception.expectMessage("No Results");
              String result = "";
              String searchTerm = "jdfkjdfkjdfkj";
               HashMap h = new HashMap();
               h.put("query", searchTerm);
           
               // setup corret returns for the method calls
               when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn("{\"query\": \"jjfkdjkfjdk\",\"result\": null}");
               when(this.mockND.queryVIAF(searchTerm, "EAD")).thenCallRealMethod();
               result = this.mockND.queryVIAF(searchTerm, "EAD");
         }catch (Exception e){
            throw e;
         }
     }
     
     @Test
     public void testQueryViafInvalidNameType() throws Exception {
         
         try {
         exception.expect(Exception.class);
         exception.expectMessage("Unsupported nameType: Invalid");
              String result = "";
              String searchTerm = "Smth";
               HashMap h = new HashMap();
               h.put("query", searchTerm);
               
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").removeChild(0);
           viafReturn.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").appendChild("Invalid");
           when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
           
               // setup corret returns for the method calls
               when(this.mockND.query("http://viaf.org/viaf/AutoSuggest", h)).thenReturn(autoSuggestReturn);
               when(this.mockND.queryVIAF(searchTerm, "EAD")).thenCallRealMethod();
               when(this.mockND.query("http://viaf.org/viaf/159021806/viaf.xml", new HashMap())).thenReturn(viafReturn.toXML());
               
               result = this.mockND.queryVIAF(searchTerm, "EAD");
         }catch (Exception e){
             throw e;
         }
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
         }catch (Exception e){
            throw e;
         }
     }
}
