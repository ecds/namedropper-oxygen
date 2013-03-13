Usage & Installation
********************

Plugin installation via Oxygen Add-Ons
======================================

To install the current released version of the plugin as an Oxygen Add-On:

* Open Oxygen Preferences and select **Add-Ons**
* Click **Add** and save a new add-on site URL: `OxygenAddons.xml`_
  (Right click and select "copy link location" or "copy link address" to get the full url).
* Click **OK** and exit Preferences. 
* Under the Oxygen Help menu, select **Manage add-ons...**
* Select **NameDropper** and then click install.  You should be prompted to **accept the Apache 2
  license** and then to restart Oxygen.
* As new versions are released, you should be able to update the NameDropper plugin by
  using the **Check for add-ons updates..** menu entry under Help.

.. _OxygenAddons.xml: ../res/OxygenAddons.xml?raw=true

Configure CSS to style NameDropper tags and attributes in Author mode (Optional)
--------------------------------------------------------------------------------

To allow for an easier way to review tagged names and access the
related resources, we have created a CSS file to style the tags and attributes
inserted by NameDropper, for use in Oxygen's Author mode. To configure this CSS:

* Open **Oxygen Preferences** and select **Document Type Association**
* Select the Document type(s) you will be working with, e.g. **EAD** or **TEI P5**,
  and click **edit**
* Switch to the **Author** tab and click on the **+** icon to add a new CSS.
* Add the URL for `namedropper-oxygen.css`_ (right click and copy link location
  to get the full URL), an optional label (for example "NameDropper CSS), and click **OK** until out of Preferences.

When you switch from **Text** mode to **Author** mode in Oxygen, you should see the
styles take effect. For both TEI and EAD documents, name tags will be highlighted,
icons should be displayed for VIAF, GeoNames, or DBpedia resources, and the icon or
text immediately after the name will be a clickable link to the corresponding resource.
For EAD documents, source names and identifiers will be listed after the name.

.. _namedropper-oxygen.css: ../res/namedropper-oxygen.css?raw=true

Using the plugin in Oxygen
==========================

Generally, the NameDropper plugin is intended to be used in the ``Text`` display mode
within the Oxygen editor.

To use the plugin once you have it installed:

* The first time you use the plugin on a particular document, or if you need to switch between documents, use the
  NameDropper menu located in the main Oxygen menu bar to choose the type of document
  you will be editing (**TEI** or **EAD**) so that the correct tags will be inserted.

* Generally, you will select text in your document and invoke one of the available
  actions either by using the top-level **NameDropper** menu or by using the appropriate
  keyboard shortcuts, which are listed in the menu.

The NameDropper plugin currently provides the following functionality:

* Highlight a person, place, or corporate/organization name to be looked
  up in `VIAF`_ (Virtual International Authority File).  If any matches are found,
  you should see a list of names to select from; when you choose one, the appropriate
  tags and identifier attributes should be inserted into your document.

* Highlight a place name to be looked up in `GeoNames.org`_ and select from the list
  (works largely the same as the VIAF lookup).  The GeoNames lookup currently requires
  that you configure a `GeoNames.org API username`_; plugin settings can be accessed
  via the NameDropper menu.

* Highlight a passage of text (multiple sentence or paragraphs, which can include tags)
  to be annotated with `DBpedia Spotlight`_.  Recognized entities will be displayed in
  a side panel where you can review and select them for insertion into your document.
  DBpedia Spotlight confidence and support settings can be configured via the NameDropper
  menu.  When possible, VIAF identifiers and URIs will be used for DBpedia person entities,
  and GeoNames.org identifiers and URIs will be used for DBpedia place entities.

 .. _VIAF: http://viaf.org/
 .. _GeoNames.org: http://geonames.org/
 .. _GeoNames.org API username: http://www.geonames.org/login
 .. _DBpedia Spotlight: http://spotlight.dbpedia.org/


----

See `CHANGELOG`_ for features and changes by version.

.. _CHANGELOG: ../CHANGELOG.rst


Known Limitations
=================

* The NameDropper plugin does not interact very well with existing name tags in the XML:

  * there is curently no support for adding attributes to existing name tags or filtering
    a VIAF lookup based on the type of name tag
  * entities recognized by DBpedia Spotlight annotation are filtered based on schema,
    which could exclude recognized names in EAD that are tagged but do not have source and
    authfilenumber; because the TEI <name> tag may contain <name>, annotating TEI text
    with tagged names may result in nested name tags

* The DBpedia Spotlight annotation API request is fairly slow, and the plugin has not
  yet been modified to make this request in the background. Consequently, Oxygen may appear to hang for a while when making an annotation request.
* VIAF and GeoNames ids are not always available for DBpedia persons and places, respectively.
* The plugin uses Oxygen's built-in schema to determine if tags are allowed at the selected
  location; an invalid document may need to be corrected before using NameDropper actions.
* The selected document type (TEI or EAD) is not auto-detected, nor is it stored per
  document, so if you switch between working on TEI and EAD documents, you will need to manually change the NameDropper document type.
* DBpedia Spotlight annotations are currently not restricted to the current document, so
  if you annotate text and switch to a different document, the annotations will not match
  the text.  Current recommended workflow is to annotate and then process the results before
  switching to a different document.
* While DBpedia Spotlight lookup works on passages of text, VIAF and GeoNames lookup only work on single terms. These terms can be multiple words (example, "Battle of Gettysburg"). 
* DBpedia Spotlight lookup depends on context. Results vary based on how much text is selected when starting lookup.
