Releasing the plugin for installation as an Oxygen add-on
*********************************************************

The released version of the plugin is set up to be installable directly from
GitHub.

Because this project follows git flow conventions, the master branch will
always be the most recent released version of the code.   So, the Oxygen
AddOns xml file can be referenced directly from the raw version of the master file
in the resource subdirectory (``res/OxygenAddons.xml``)

Releasing a new version
-----------------------

There are a few manual steps required to update the version of this plugin that can be installed using the add-ons interface within Oxygen.

* Run ``ant`` to generate the plugin version of the jar file.
* Rename the plugin jar file (``dist/NameDropper-plugin.jar``) to reflect the
  version you are releasing, e.g. ``NameDropper-0.1.0.jar``
* If you are creating a final release, sign the jar file (see :ref:`jar-signing`).
* Upload the file to the project on GitHub using the **Downloads** file management tools.
* Update the download location in the add-on xml file ``res/OxygenAddons.xml``
  with the full URL to the download file you just uploaded (commit and push
 to github).

Before finalizing a new release branch and merging the code into the master
branch, the version listed in the OxygenAddons.xml file should be updated, so
that Oxygen will recognize a new version is available and can prompt users to
upgrade.

Suggested naming convention for non-final releases is to use a dev or pre
label, e.g. ``NameDropper-0.1.0-dev.jar`` or ``NameDropper-0.1.0-pre.jar``.

.. _jar-signing:

Signing the jar file
--------------------

Oxygen will warn users if the plugin jar is unsigned, so the jar file for
final released versions should be signed before upload to GitHub.

* Get the appropriate cert files from your systems administrator.

* If cert files are provided as .cert and .pem, convert to .pfx::

    openssl pkcs12 -inkey mykey.pem -in mycert.cert -export -out mycert.pfx -name my-cert-alias

  This will prompt you for a password for the pfx keystore you are
  generating.  Openssl will allow you to leave it blank, but the jarsigner tool
  will not, so enter something here.  The ``-name`` option is a cert alias you will use
  when signing the jar file.

* Use jarsigner and the .pfx file to sign the plugin jar::

    jarsigner -storetype pkcs12 -keystore mycert.pfx dist/NameDropper-plugin.jar my-cert-alias

  Where ``mycert.pfx`` is the pfx file you created above and ``my-cert-alias`` is the name
  you gave it above.  You should be prompted for a passphrase to use the keystore; use the
  same password you used when generating the pfx file with openssl.

* Verify the signature with jarsigner::

    jarsigner -verify dist/NameDropper-plugin.jar


