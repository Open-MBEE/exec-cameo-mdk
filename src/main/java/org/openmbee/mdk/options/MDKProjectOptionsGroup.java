package org.openmbee.mdk.options;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.options.ProjectOptions;
import com.nomagic.magicdraw.core.options.ProjectOptionsConfigurator;
import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.ChoiceProperty;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyResourceProvider;
import org.openmbee.mdk.fileexport.ContextExportLevel;

import java.util.Arrays;

public class MDKProjectOptionsGroup implements ProjectOptionsConfigurator {

	public static final String PROPERTYGROUP_MDK = "PROPERTYGROUP_MDK";

	public static final String PROPERTY_AUTOSAVE_MDKMODEL = "PROPERTY_AUTOSAVE_MDKMODEL_ID";
	public static final String PROPERTY_AUTOSAVE_MDKZIP = "PROPERTY_AUTOSAVE_MDKZIP_ID";
	public static final String PROPERTY_CONTEXT_EXPORT_LEVEL = "PROPERTY_CONTEXT_EXPORT_LEVEL_ID";

	public static final PropertyResourceProvider PROPERTY_RESOURCE_PROVIDER = (key, property) -> ProjectOptionsResources.getString(key);

	public static boolean isAutosaveMDKModel(Project project) {
		Property property = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_AUTOSAVE_MDKMODEL);
		if (property != null) {
		    return (boolean) property.getValue();
		} else {
			return false;
		}

	}

	public static boolean isAutosaveMDKZip(Project project) {
		Property property = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_AUTOSAVE_MDKZIP);
		if (property != null) {
		    return (boolean) property.getValue();
		} else {
			return false;
		}

	}
	
	public static ContextExportLevel getContextExportLevel(Project project) {
		Property property = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_CONTEXT_EXPORT_LEVEL);
		if (property != null) {
			return ContextExportLevel.valueOf(property.getValueStringRepresentation());
		} else {
			return ContextExportLevel.None;
		}
	}

	@Override
	public void afterLoad(ProjectOptions arg0) {

	}

	@Override
	public void configure(ProjectOptions projectOptions) {
		setAutosaveProperty(projectOptions);
		
		setAutosaveMdkzipProperty(projectOptions);

		setContextExportLevelProperty(projectOptions);
	}

	private void setAutosaveProperty(ProjectOptions projectOptions) {
		Property autosaveProperty = projectOptions.getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_AUTOSAVE_MDKMODEL);
		if (autosaveProperty == null) {
			autosaveProperty = new BooleanProperty(PROPERTY_AUTOSAVE_MDKMODEL, false);
			autosaveProperty.setGroup(PROPERTYGROUP_MDK);
			autosaveProperty.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
			projectOptions.addProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, autosaveProperty);
		}
	}

	private void setAutosaveMdkzipProperty(ProjectOptions projectOptions) {
		Property autosaveMdkzipProperty = projectOptions.getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_AUTOSAVE_MDKZIP);
		if (autosaveMdkzipProperty == null) {
			autosaveMdkzipProperty = new BooleanProperty(PROPERTY_AUTOSAVE_MDKZIP, false);
			autosaveMdkzipProperty.setGroup(PROPERTYGROUP_MDK);
			autosaveMdkzipProperty.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
			projectOptions.addProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, autosaveMdkzipProperty);
		}
	}

	private void setContextExportLevelProperty(ProjectOptions projectOptions) {
		Property contextExportLevelProperty = projectOptions.getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_CONTEXT_EXPORT_LEVEL);
		if (contextExportLevelProperty == null) {

			contextExportLevelProperty = new ChoiceProperty(PROPERTY_CONTEXT_EXPORT_LEVEL, "", Arrays.asList(ContextExportLevel.values()));
			contextExportLevelProperty.setGroup(PROPERTYGROUP_MDK);
			contextExportLevelProperty.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
			contextExportLevelProperty.setValue(ContextExportLevel.Containment);
			projectOptions.addProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, contextExportLevelProperty);
		}
	}

}
