/**
 * file src/edu/emory/library/namedropper/plugins/NameDropperStylesFilter.java
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

import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.css.URIContent;
import ro.sync.exml.view.graphics.Color;
import ro.sync.exml.view.graphics.Font;
import ro.sync.exml.plugin.author.css.filter.GeneralStylesFilterExtension;

public class NameDropperStylesFilter extends GeneralStylesFilterExtension {

    public static URIContent viafIcon = new URIContent("",
        "http://www.google.com/s2/u/0/favicons?domain=viaf.org");

    public String getDescription() {
        return "Styles Filter extension to customize display for elements " +
        "inserted or modified by the NameDropper plugin.";
    }

    public Styles filter(Styles styles, AuthorNode authorNode) {
        if (AuthorNode.NODE_TYPE_ELEMENT == authorNode.getType()
                && "persname".equals(authorNode.getName())) {
                // TODO: use a list of relevant node names to catch all

            // cast to element to get access to attributes
            AuthorElement el = (AuthorElement) authorNode;
            AttrValue source = el.getAttribute("source");
            // FIXME: this doesn't seem to be matching
            if (source != null && source.getValue() == "viaf") {
                styles.setProperty(Styles.KEY_BACKGROUND_IMAGE,
                    this.viafIcon);
                // FIXME: setting the background position requires a type of
                // ro.sync.ecss.css.BackgroundPosition, which seems to be undocumented.
                //styles.setProperty(Styles.KEY_BACKGROUND_POSITION,
                    //"left");  // -- errors
                styles.setProperty(Styles.KEY_BACKGROUND_REPEAT,
                    "none");
                styles.setProperty(Styles.KEY_BACKGROUND_COLOR, Color.COLOR_RED);
            } else {
                styles.setProperty(Styles.KEY_BACKGROUND_COLOR, Color.COLOR_LIGHT_YELLOW);
            }

            // from example code
            //styles.setProperty(Styles.KEY_FONT, new Font(null, Font.BOLD, 12));
        }
        return styles;
    }
}