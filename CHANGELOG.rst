Change & Version Information
============================

The following is a summary of changes and improvements to the Oxygen
NameDropper plugin.  New features in each version should be listed, with any
necessary information about installation or upgrade notes.

0.3
---

* An Oxygen user can use the NameDropper menu to see and change the action (VIAF lookup or DBpedia Spotlight annotation) to be taken when they invoke the plugin OR trigger individual actions by keyboard shortcut, so that they have options for using the plugin functionality.
* Oxygen users can choose to configure a CSS from the plugin to make added tags more visible when working in the Author view so they can review tags and enhancements more easily.
* bugfix: The VIAF lookup will not insert <geogname> tags where they are not allowed (but other name tags are allowed) in an EAD document.

**DBpedia Spotlight** integration:

* An Oxygen user can highlight a section of text with no embedded tags and invoke the namedropper plugin to see a list of recognized names from DBpedia Spotlight so that named entities can be extracted.
* When Oxygen users invoke the DBpedia lookup, they can see the results in an Oxygen-style view so they can review results while looking at the document.
* Oxygen users can mouse over a name in the sidebar that has been identified by DBpedia Spotlight lookup to see brief DBpedia information so they can check if it is the correct resource.
* When Oxygen users select a name found by DBpedia spotlight, they can see where it appears in the document so they can verify that it has been recognized correctly.
* Oxygen users can highlight text that includes tags and add DBpedia Spotlight annotations without overwriting the original tags so the mixed content can be enhanced.
* An Oxygen user can click a button to approve all names in the list of returned results and have tags added to the recognized names in the EAD or TEI document so the document can be enhanced easily.
* Oxygen users can approve selected names from the list of returned spotlight annotation results so they can enhance an EAD or TEI document quickly without inserting tags for incorrectly recognized terms.
* When an Oxygen user annotates text with DBpedia Spotlight, results are automatically selected for insertion so that only unwanted results need to be unselected.
* When an Oxygen user selects and inserts DBpedia Spotlight annotations, the unselected names are automatically cleared from the list so that unwanted names drop off the list.
* When Oxygen users approve DBpedia Spotlight results, the VIAF identifier is automatically retrieved and inserted for person names so they can be link to an authoritative record.
* Oxygen users can change the confidence and support settings for DBpedia Spotlight API in the namedropper plugin so that they can optimize annotation results.
* An Oxygen user can process ~200 lines of text when using DBpedia Spotlight annotation so that they aren't limited to very small sections of content.
* When an Oxygen user annotates text with DBpedia Spotlight, identified terms where name tags are not allowed are not displayed in the list of annotations, in order to avoid adding invalid tags.
* An Oxygen user can insert a DBpedia spotlight tag for a name that wraps over a line without having the wrapped portion duplicated, so that names with line breaks can be tagged.
* When an Oxygen user runs DBpedia Spotlight annotation, they are not allowed to update the document until they have inserted or cleared the recognized resources so that the Spotlight offsets will still match the document text.
* When Oxygen users approve returned results from DBpedia Spotlight, the geonames identifier is automatically retrieved and inserted for place names so that names will be linked to a more authoritative record.

**GeoNames** integration:

* An Oxygen user can select a place name in an EAD or TEI document and see a list of matching places from GeoNames, in order to link the term to a geonames.org record.

Code and developer-specific changes:

* Developers can check out the namedropper oxygen plugin code and build and edit it with any  editor they choose, so that they can get started on actual development more quickly.
* Refactored and re-organized code to make it more logical, and easier to add new actions.
* Added maven project file for a more automated way of managing Java
  dependencies.
* Split out namedropper-python and namedropper-oxygen code into
  separate git submodules.
* Added developer documentation for adding a new selection action, based on the GeoNames functionality.

0.2
---

* An Oxygen/NameDropper plugin user can see which format (of EAD or TEI) is
  currently selected in the plugin menu, so that they know if they need to
  change the setting.
* An oXygen XML user can select a name for lookup in an EAD or TEI XML file
  and see a list of suggested results in order to choose the best match for a
  name.
* An Oxygen user cannot use the NameDropper plugin to tag a name in a context
  where that name tag is not allowed, so that using the plugin does not
  generate invalid xml.


0.1
---

* An Oxygen XML user can install the NameDropper plugin using the Oxygen
  "manage add-ons" feature, so that they can easily add the plugin without
  manually copying files.
* An oXygen XML user can select a person name in an EAD XML document for
  lookup in a name authority system and have the first match added to the
  document in order to automate linking personal names to authoritative
  identifiers.
* An oXygen XML user can select a person, place, or organization name in an
  EAD XML document for lookup in a name authority system and have the first
  match added to the document, with the appropriate tag in order to automate
  linking multiple types of names to authoritative identifiers.
* An oXygen XML user can select a name in a TEI XML document for lookup in a
  name authority system and have the first match added to the document in
  order to automate linking names to authoritative identifiers.
* An Oxygen XML user can invoke the NameDropper plugin via a keyboard shortcut
  in order to enrich the content efficiently and quickly.
