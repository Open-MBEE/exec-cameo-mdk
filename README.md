# MagicDraw Model Development Kit (MDK)

## OCL patch

### To use MDK's ocl support or expressions in DocGen, do the following in MagicDraw 18.0
- replace md.install.dir/lib/org.eclipse.emf.ecore.change_2.7.0.v20110408-2116.jar with ocl_libraries/org.eclipse.emf.ecore.change_2.7.1.201210251512.jar
- replace md.install.dir/lib/org.eclipse.emf.ecore.xmi_2.7.0.v20110411-2239.jar with ocl_libraries/org.eclipse.emf.ecore.xmi_2.7.0.201210251512.jar
- replace md.install.dir/lib/org.eclipse.emf.ecore_2.7.0.v20110513-1719.jar with ocl_libraries/org.eclipse.emf.ecore_2.7.0.201210251512.jar
- replace md.install.dir/lib/org.eclipse.ocl_3.0.1.R30x_v201008251030.jar with ocl_libraries/org.eclipse.ocl_3.2.0.201210251512.jar
- replace md.install.dir/lib/org.eclipse.ocl.ecore_3.0.1.R30x_v201008251030.jar with ocl_libraries/org.eclipse.ocl.ecore_3.2.2.201210251512.jar
- replace md.install.dir/lib/org.eclipse.ocl.ecore.edit_3.0.0.R30x_v201008251030.jar with ocl_libraries/org.eclipse.ocl.ecore.edit_3.1.0.v20110526-1523.jar
- modify the classpath in md.install.dir/bin/magicdraw.properties accordingly

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
- gov.nasa.jpl.mbee.viewedit - Contain visitors that deal with making output for view editor.
- gov.nasa.jpl.mgss.mbee.docgen.docbook - Contain classes that model components of a document, originally based on subset of docbook model.
- gov.nasa.jpl.mgss.mbee.docgen.validation - Classes that mirror magicdraw's validation model, can be used to show results in validation window or user validation script.
- gov.nasa.jpl.ocl - Ocl evaluation related things.

