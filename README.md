# MagicDraw Model Development Kit (MDK)

## Packages

- gov.nasa.jpl.mbee - Contains classes for configuring and initializing the plugin, profile stereotype strings, and DocGen specific utilities.
- gov.nasa.jpl.mbee.actions.docgen - Contains context menu actions that deal with validating the document model and generating locally to docbook.
- gov.nasa.jpl.mbee.actions.ems - Contains menu actions that deal with validating the model and documents/views with MMS, and starting/stopping auto/delta sync.
- gov.nasa.jpl.mbee.ems - Contains utilities for import/export to MMS
- gov.nasa.jpl.mbee.ems.sync - This contains classes for doing autosync/deltasync with MMS
- gov.nasa.jpl.mbee.ems.validation - Classes for actually doing comparisons and validation with MMS
- gov.nasa.jpl.mbee.ems.validation.actions - This contains actions that can be triggered from the validation window after running a model or view validation with alfresco
- gov.nasa.jpl.mbee.generator - Contains the core classes for parsing the document model and generating it.
- gov.nasa.jpl.mbee.lib - Contains utility classes and methods for filtering, collecting, converting model elements and running scripts.
- gov.nasa.jpl.mbee.model - Contain classes that model the document based on the docgen profile.
- gov.nasa.jpl.mbee.mdk.viewedit - Contain visitors that deal with making output for view editor.
- gov.nasa.jpl.mgss.mbee.docgen.docbook - Contain classes that model components of a document, originally based on subset of docbook model.
- gov.nasa.jpl.mgss.mbee.docgen.validation - Classes that mirror magicdraw's validation model, can be used to show results in validation window or user validation script.
- gov.nasa.jpl.mbee.mdk.ocl - Ocl evaluation related things.

## Classes
