/**
 * file src/edu/emory/library/namedropper/plugins/NameDropperMenuPluginExtension.java
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

package edu.emory.library.namedropper.plugins;

import javax.swing.JMenuBar;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.NameDropperMenu;

/**
 * Plugin extension - workspace access extension.
 */
public class NameDropperMenuPluginExtension implements WorkspaceAccessPluginExtension {

  /**
   * Plugin workspace access.
   */
  private StandalonePluginWorkspace pluginWorkspaceAccess;

  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    this.pluginWorkspaceAccess = pluginWorkspaceAccess;

    pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() {

      public void customizeMainMenu(JMenuBar mainMenuBar) {
        // Add the NameDropper menu just before the last menu in the bar (Help menu)
        mainMenuBar.add(new NameDropperMenu(pluginWorkspaceAccess), mainMenuBar.getMenuCount() - 1);
      }
    });

 }

  // required because we are implementing Interface WorkspaceAccessPluginExtension
  public boolean applicationClosing() {
    return true;  // it's ok with this plugin for the application to close
  }
}