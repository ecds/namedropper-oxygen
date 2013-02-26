Usage & Installation
********************

Plugin installation via Oxygen Add-Ons
======================================

To install the current released version of the plugin as an Oxygen Add-On:

* Open Oxygen Preferences and select **Add-Ons**
* Add and save a new add-on site URL: `OxygenAddons.xml`_
  (Right click and select "copy link location" or "copy link address" to get the full url)
* Under the Oxygen Help menu, select **Manage add-ons...**
* Select **NameDropper** and then click install.  You should be prompted to accept the
  license and then restart Oxygen.
* As new versions are released, you should be able to update the NameDropper plugin by
  using the **Check for add-ons updates..** menu entry.

.. _OxygenAddons.xml: ../res/OxygenAddons.xml?raw=true

Configure CSS to style NameDropper tags and attributes in Author mode (Optional)
--------------------------------------------------------------------------------

As a convenience, to allow for an easier way to review tagged names and access the
related resources, we have created a CSS file to style the tags and attributes
inserted by NameDropper, for use in Author mode. To configure this CSS:

* Open Oxygen Preferences and select **Document Type Association**
* Select the Document type(s) you will be working with, e.g. **EAD** or **TEI P5**,
  and click **edit**
* Switch to the **Author** tab and click on the **+** icon to add a new CSS.
* Add the URL for `namedropper-oxygen.css`_ (right click and copy link location
  to get the full URL), an optional label, and save.

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

* The first time you use the plugin, or if you need to switch between documents, use the
  NameDropper menu located in the main Oxygen menu bar to choose the type of document
  you will be editing (**TEI** or **EAD** so that the correct tags will be inserted).

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
