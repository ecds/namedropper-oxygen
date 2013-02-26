USAGE
*****

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

.. _CHANGELOG: ../CHANGELOG.rst
