/**
 * file src/edu/emory/library/namedropper/plugins/PluginOptions.java
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

import javax.swing.AbstractAction;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class PluginOptions {

    // option keys
    public static String DOCUMENT_TYPE = "NameDropper:DocumentType";
    public static String DEFAULT_ACTION = "NameDropper:DefaultAction";

    // static variables for option keys
    // methods for getting/setting options

    /**
     * Convenience method for accessing the current plugin workspace.
     * Wrapper around PluginWorkspaceProvider.getPluginWorkspace().
     */
    public static PluginWorkspace getWorkspace() {
        return PluginWorkspaceProvider.getPluginWorkspace();
    }


    /**
     * Convenience method to get a plugin option by name.
     * Currently defaults to empty string of option is not set.
     * @param optionName    name of the option to retrieve
     */
    public static String getOption(String optionName) {
        return PluginOptions.getOption(optionName, "");  // FIXME: use null instead?
    }

    /**
     * Same as getOption, but with a configurable default value.
     */
    public static String getOption(String optionName, String def) {
        PluginWorkspace ws = PluginOptions.getWorkspace();
        return ws.getOptionsStorage().getOption(optionName, def);
    }

    /**
     * Convenience method to set a plugin option.
     * @param optionName  name of the option to be updated
     * @param value     value to be set
     */
    public static void setOption(String optionName, String value) {
        PluginWorkspace ws = PluginOptions.getWorkspace();
        ws.getOptionsStorage().setOption(optionName, value);
    }

    /**
     * Get the current user-selected document type.
     */
    public static String getDocumentType() {
        return PluginOptions.getOption(PluginOptions.DOCUMENT_TYPE);
    }

    /*
     * Set the current user-selected document type.
     */
    public static void setDocumentType(String value) {
        PluginOptions.setOption(PluginOptions.DOCUMENT_TYPE, value);
    }

    /*
     * Get the current default action.
     */
    public static String getDefaultAction() {
        return PluginOptions.getOption(PluginOptions.DEFAULT_ACTION);
    }

    /**
     * Set the current default action.
     */
    public static void setDefaultAction(String value) {
        PluginOptions.setOption(PluginOptions.DEFAULT_ACTION, value);
    }

}
