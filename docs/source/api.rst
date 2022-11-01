********************
Developer Resources 
********************

API
====

The Javadoc for MDK is included in the plugin and can be found in the MagicDraw plugins folder after installation. Note that only the classes found in gov.nasa.jpl.mbee.mdk.api are intended to be used by other plugins/tools and that the rest of the code can change without any notification.

MDK Environment Options
========================

Options for advanced users to customize certain behaviors or enable MDK functionality are available in MagicDraw's Environment Options.
- LOG_JSON: Enables logging of MMS request and response information, including sent and received JSON, to the MagicDraw log. To access these settings, navigate to the "Options Menu" -> "Environment" dialog, and select the "MDK" section. Several [DEVELOPER] options are also available, but are generally hidden from users. They are included here for completeness - we strongly recommend they not be modified unless for development purposes as they will likely result in data loss.
- PERSIST_CHANGELOG: [DEVELOPER] Enables persisting of the changelog in the \_MMSSync\_ package for uneditable model elements. Disabling this option will cause these changelogs to be lost after CSync and may cause loss of model parity.
- ENABLE_CHANGE_LISTENER: [DEVELOPER] Enables listeners for model changes in MagicDraw and MMS. Disabling this option will cause changes to not be tracked and may cause loss of model parity.
- ENABLE_COORDINATED_SYNC: [DEVELOPER] Enables Coordinated Sync on Teamwork Cloud project commit. Disabling this option will cause CSync to be skipped on Teamwork Cloud commit and may cause loss of model parity.


.. autosummary::
   :toctree: generated