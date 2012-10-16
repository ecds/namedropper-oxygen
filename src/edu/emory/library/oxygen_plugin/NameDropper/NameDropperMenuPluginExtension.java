package edu.emory.library.oxygen_plugin.NameDropper;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.structure.AuthorPopupMenuCustomizer;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.InputURLChooser;
import ro.sync.exml.workspace.api.standalone.InputURLChooserCustomizer;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import ro.sync.exml.workspace.api.standalone.ditamap.TopicRefInfo;
import ro.sync.exml.workspace.api.standalone.ditamap.TopicRefTargetInfo;
import ro.sync.exml.workspace.api.standalone.ditamap.TopicRefTargetInfoProvider;
import ro.sync.exml.workspace.api.standalone.ui.Menu;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.exml.workspace.api.util.RelativeReferenceResolver;
import ro.sync.ui.Icons;

/**
 * Plugin extension - workspace access extension.
 */
public class NameDropperMenuPluginExtension implements WorkspaceAccessPluginExtension {
  /**
   * Map between the URL of the temporary local file that contains the content of the 
   * checked out file and the URL of the checked out file.
   */
  private Map<URL, URL> openedCheckedOutUrls = new HashMap<URL, URL>();

  /**
   * If <code>true</code> the editor will be verified for Check In on close.  
   */
  private boolean verifyCheckInOnClose = true;
  
  /**
   * If <code>true</code> the document is Checked Out, it will be Checked In on 
   * close.  
   */
  private boolean forceCheckIn;

  /**
   * The CMS messages area.
   */
  private JTextArea cmsMessagesArea;

  /**
   * Plugin workspace access.
   */
  private StandalonePluginWorkspace pluginWorkspaceAccess;
  
//  /**
//   * The Oxygen menu bar. You can set this field on startup
//   * and then provide custom actions or filters when a certain file is opened.
//   */
//  private JMenuBar oxygenMenuBar;
  
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    
    // Set EAD action
    final Action setEADAction = new AbstractAction() {
      public void actionPerformed(ActionEvent arg0) {
        pluginWorkspaceAccess.showInformationMessage(pluginWorkspaceAccess.getOptionsStorage().getOption("docType","*"));  
        pluginWorkspaceAccess.getOptionsStorage().setOption("docType", "EAD");
        pluginWorkspaceAccess.showInformationMessage("EAD SET");
        pluginWorkspaceAccess.showInformationMessage(pluginWorkspaceAccess.getOptionsStorage().getOption("docType","*"));
      }
    };

    // Set TEI action
    final Action setTEIAction = new AbstractAction() {
      public void actionPerformed(ActionEvent arg0) {
        pluginWorkspaceAccess.showInformationMessage(pluginWorkspaceAccess.getOptionsStorage().getOption("docType","*"));  
        pluginWorkspaceAccess.getOptionsStorage().setOption("docType", "TEI");
        pluginWorkspaceAccess.showInformationMessage("TEI SET");
        pluginWorkspaceAccess.showInformationMessage(pluginWorkspaceAccess.getOptionsStorage().getOption("docType","*"));
      }
    };
    
    pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() { 

      /**
       * @see ro.sync.exml.workspace.api.standalone.MenuBarCustomizer#customizeMainMenu(javax.swing.JMenuBar)
       */
      public void customizeMainMenu(JMenuBar mainMenuBar) {
//        oxygenMenuBar = mainMenuBar;
        // CMS menu
        JMenu menuCMS = createCMSMenu(setEADAction, setTEIAction);
        // Add the CMS menu before the Help menu
        mainMenuBar.add(menuCMS, mainMenuBar.getMenuCount() - 1);
        
      }
    });
 }
  
  /**
   * Create CMS menu that contains the following actions:
   * <code>Check In</code>, <code>Check Out</code> and <code>Show Selection Source<code/>, <code>Surround with</code>
   * 
   * @param setEADAction The check in action.
   * @param setTEIAction The check out action.
   
   * 
   * @return The CMS menu.
   */
  private JMenu createCMSMenu(
      final Action setEAD, 
      final Action setTEI) {
    // CMS menu
    Menu menuCMS = new Menu("NameDropper", true); 
    
    // Add setEAD action on the menu
    final JMenuItem setEADItem = new JMenuItem(setEAD); 
    setEADItem.setText("Set EAD");
    menuCMS.add(setEADItem);

    // Add setTEI action on the menu
    final JMenuItem setTEIItem = new JMenuItem(setTEI); 
    setTEIItem.setText("Set TEI");
    menuCMS.add(setTEIItem);
    
    
    return menuCMS;
  }


  //aparently required for some reason
  public boolean applicationClosing() {return true;}
}
