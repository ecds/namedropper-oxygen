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
    
    pluginWorkspaceAccess.addEditorChangeListener(
        new WSEditorChangeListener() {
          @Override
          public void editorOpened(URL editorLocation) {
            customizePopupMenu();
            // Show 'Edit' toolbar 
            pluginWorkspaceAccess.showToolbar("Edit");
          };

          // Customize popup menu
          private void customizePopupMenu() {
            WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
            // Customize menu for Author page
            if (editorAccess != null && EditorPageConstants.PAGE_AUTHOR.equals(editorAccess.getCurrentPageID())) {
              WSAuthorEditorPage authorPageAccess = (WSAuthorEditorPage) editorAccess.getCurrentPage();
              authorPageAccess.setPopUpMenuCustomizer(new AuthorPopupMenuCustomizer() {
                // Customize popup menu 
                public void customizePopUpMenu(Object popUp, AuthorAccess authorAccess) {
                  // CMS menu
                  JMenu menuCMS = createCMSMenu(setEADAction, setTEIAction);
                  // Add the CMS menu 
                  ((JPopupMenu)popUp).add(menuCMS, 0);
                  // Add 'Open in external application' action

                  final URL selectedUrl;
                  try {
                    final String selectedText = authorAccess.getEditorAccess().getSelectedText();
                    if (selectedText != null) {
                      selectedUrl = new URL(selectedText);
                      // Open selected url in system application
                      ((JPopupMenu)popUp).add(new JMenuItem(new AbstractAction("Open in system application") {
                        public void actionPerformed(ActionEvent e) {
                          pluginWorkspaceAccess.openInExternalApplication(selectedUrl, true);
                        }
                      }), 0);
                    }
                  } catch (MalformedURLException e) {}
                }
              });
            }
          }

          @Override
          public void editorClosed(URL editorLocation) {
            URL checkedOutUrl = openedCheckedOutUrls.get(editorLocation);
            if (checkedOutUrl != null) {
              openedCheckedOutUrls.remove(editorLocation);
            }
          };

          /**
           * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorAboutToBeClosed(java.net.URL)
           */
          @Override
          public boolean editorAboutToBeClosed(URL editorLocation) {
            URL checkedOutUrl = openedCheckedOutUrls.get(editorLocation);
            if(checkedOutUrl != null) {
              if (verifyCheckInOnClose) {
                if (forceCheckIn) {
                  // Save the current file.
                } else if(pluginWorkspaceAccess.showConfirmDialog(
                    "Close", 
                    "The closed file " + editorLocation + " is Checked Out.\n Do you want to Check In?", 
                    new String[] {"Ok", "Cancel"}, 
                    new int[] {0, 1}) == 0) {
                  // Save the current file.
                } else {
                  //Reject the close, user did not want to check in.
                  return false;
                }
              }
            }
            return true;
          }

          /**
           * The editor was relocated (Save as was called).
           * 
           * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorRelocated(java.net.URL, java.net.URL)
           */
          @Override
          public void editorRelocated(URL previousEditorLocation, URL newEditorLocation) {
            //Refresh the mappings.
            URL previousCheckedOutUrl = openedCheckedOutUrls.get(previousEditorLocation);
            if(previousCheckedOutUrl != null) {
              openedCheckedOutUrls.remove(previousEditorLocation);
              openedCheckedOutUrls.put(newEditorLocation, previousCheckedOutUrl);
            }
          }

          @Override
          public void editorPageChanged(URL editorLocation) { 
            customizePopupMenu();
          };

          @Override
          public void editorSelected(URL editorLocation) {
            customizePopupMenu();
          };

          @Override
          public void editorActivated(URL editorLocation) {
            customizePopupMenu();            
          }
        }, 
        StandalonePluginWorkspace.MAIN_EDITING_AREA);


    pluginWorkspaceAccess.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
      /**
       * @see ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer#customizeToolbar(ro.sync.exml.workspace.api.standalone.ToolbarInfo)
       */
      public void customizeToolbar(ToolbarInfo toolbarInfo) {
        //The toolbar ID is defined in the "plugin.xml"
        if("SampleWorkspaceAccessToolbarID".equals(toolbarInfo.getToolbarID())) {
          List<JComponent> comps = new ArrayList<JComponent>(); 
          JComponent[] initialComponents = toolbarInfo.getComponents();
          boolean hasInitialComponents = initialComponents != null && initialComponents.length > 0; 
          if (hasInitialComponents) {
            // Add initial toolbar components
            for (JComponent toolbarItem : initialComponents) {
              comps.add(toolbarItem);
            }
          }

          // Check In
          ToolbarButton checkInButton = new ToolbarButton(setEADAction, true);
          checkInButton.setText("Check In");

          // Check Out
          ToolbarButton checkOutButton = new ToolbarButton(setTEIAction, true);
          checkOutButton.setText("Check Out");

          // Add in toolbar
          comps.add(checkInButton);
          comps.add(checkOutButton);
          toolbarInfo.setComponents(comps.toArray(new JComponent[0]));

          // Set title
          String initialTitle = toolbarInfo.getTitle();
          String title  = "";
          if (hasInitialComponents && initialTitle != null && initialTitle.trim().length() > 0) {
            // Include initial tile
            title += initialTitle + " | " ;
          }
          title  += "CMS";
          toolbarInfo.setTitle(title);
        } else if("Author_custom_actions1".equals(toolbarInfo.getToolbarID())) {
          //Contribute a new action directly in the Author toolbar which was dynamically created from the document type
          //associated to the XML file. You can add a new action or remove an existing one.
          //See the Javadoc for:
          //ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace.addToolbarComponentsCustomizer(ToolbarComponentsCustomizer)
          List<JComponent> comps = new ArrayList<JComponent>(Arrays.asList(toolbarInfo.getComponents()));
          comps.add(new ToolbarButton(new AbstractAction("MY ACTION") {
            @Override
            public void actionPerformed(ActionEvent e) { 
              //You can obtain the current editor, get access to its WSAuthorPage and modify it using the API.
              System.err.println("Perform action on " + pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA).getEditorLocation());
            }
          }, true));
          toolbarInfo.setComponents(comps.toArray(new JComponent[0]));
        }
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

  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  public boolean applicationClosing() {
    if (!openedCheckedOutUrls.isEmpty()) {
      int result = pluginWorkspaceAccess.showConfirmDialog(
          "Close", 
          "There are some opened Checked Out files.\n Do you want to Check In?", 
          new String[] {"Check In All", "Don't Check In", "Cancel"}, 
          new int[] {0, 1, 2});
      // Check In
      if (result == 0) {
        verifyCheckInOnClose = true;
        forceCheckIn = true;
        // Don't Check In
      } else if (result == 1) {
        verifyCheckInOnClose = false;
        // Cancel
      } else if (result == 2) {
        return false;
      }
    }
    return true;
  }
}
