NameDropper OxygenXML plugin
****************************

This is a plugin or add-on for the `Oxygen XML editor`_  (version 14.0) to
simplify the process of tagging names in XML and associating those names with
authoritative identifiers (currently using `VIAF`_).  See the top-level
`namedropper`_ repository for more information.

.. image:: https://travis-ci.org/emory-libraries-disc/namedropper-oxygen.png?branch=develop
  :alt: current build status for namedropper-py
  :target: https://travis-ci.org/emory-libraries-disc/namedropper-oxygen

.. _Oxygen XML editor: http://oxygenxml.com/
.. _VIAF: http://viaf.org/
.. _namedropper: https://github.com/emory-libraries-disc/name-dropper

Plugin installation via Oxygen Add-Ons
======================================

To install the current released version of the plugin as an Oxygen Add-On:

* Open Oxygen Preferences and select **Add-Ons**
* Add and save a new add-on site URL:
  https://raw.github.com/emory-libraries-disc/name-dropper/master/oxygen/res/OxygenAddons.xml
* Under the Oxygen Help menu, select **Manage add-ons...**
* Select **NameDropper** and then click install.  You should be prompted to accept the
  license and then restart Oxygen.
* As new versions are released, you should be able to update the NameDropper plugin by
  using the **Check for add-ons updates..** menu entry.

Using the plugin in Oxygen
--------------------------

To use the plugin once you have it installed:

* The first time you use the plugin, or if you need to switch between documents, use the
  NameDropper menu located in the main Oxygen menu bar to choose the type of document
  you will be editing (TEI or EAD).
* Highlight a name in your document and invoke the plugin either by using the top-level
  **NameDropper** menu and selecting **Lookup names** or by using the keyboard
  shortcut **Control + Shift + N**.

Current behavior is to add an appropriate name tag (based on the document type and the
type of record found) with a VIAF id for for the first VIAF match found.  You should
see a warning message if no match is found for the highlighted text, or if a name
tag is not valid in the current context.


See `CHANGELOG`_ for features and changes by version.

.. _CHANGELOG: CHANGELOG.rst

License
=======
NameDropper Oxygen plugin is distributed under the
`Apache 2.0 License <http://www.apache.org/licenses/LICENSE-2.0>`_.

-----

Developers should refer to `DEVELOPER-NOTES`_ for information on project setup, dependencies,
instructions for building the plugin, and other developer-specific information.

.. _DEVELOPER-NOTES: DEVELOPER-NOTES.rst
