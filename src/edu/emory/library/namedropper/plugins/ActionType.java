/**
 * file src/edu/emory/library/namedropper/plugins/ActionType.java
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


import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import edu.emory.library.namedropper.plugins.SelectionAction;
import edu.emory.library.namedropper.plugins.SelectionActionViaf;
import edu.emory.library.namedropper.plugins.SelectionActionSpotlight;
import edu.emory.library.namedropper.plugins.SelectionActionGeoNames;


/**
 * Supported types of selection actions with an associated keyboard shoartcut,
 * for inclusion in the NameDropper plugin menu.
 */
public enum ActionType {

    VIAF,
    DBPEDIA_SPOTLIGHT,
    GEONAMES;

    /**
     * Get an instance of the SelectionAction subclass for a specified ActionType.
     * @param ws    plugin workspace access
     */
    public SelectionAction getAction(StandalonePluginWorkspace ws) {
        switch (this) {

            case VIAF:
                return new SelectionActionViaf(ws);

            case DBPEDIA_SPOTLIGHT:
                return new SelectionActionSpotlight(ws);

            case GEONAMES:
                return new SelectionActionGeoNames(ws);

            default:
                return null;
        }
    }

    public static ActionType fromSelectionActionShortName(String shortName) {
        if (shortName.equals(SelectionActionViaf.shortName)) {
            return VIAF;
        }
        if (shortName.equals(SelectionActionSpotlight.shortName)) {
            return DBPEDIA_SPOTLIGHT;
        }
        if (shortName.equals(SelectionActionGeoNames.shortName)) {
            return GEONAMES;
        }

        return null;
    }


 }
