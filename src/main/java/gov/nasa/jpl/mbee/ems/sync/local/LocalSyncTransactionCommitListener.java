package gov.nasa.jpl.mbee.ems.sync.local;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.jmi.UML2MetamodelConstants;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.impl.PropertyNames;
import com.nomagic.uml2.transaction.TransactionCommitListener;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class responds to commits done in the document.
 *
 * @author igomes
 */
public class LocalSyncTransactionCommitListener implements TransactionCommitListener {
    private static final List<String> IGNORED_PROPERTY_CHANGE_EVENT_NAMES = Arrays.asList(
            PropertyNames.PACKAGED_ELEMENT,
            UML2MetamodelConstants.ID,
            PropertyNames.NESTED_CLASSIFIER
    );

    private Project project;

    /**
     * Allow listener to be disabled during imports.
     */
    private boolean disabled;
    private Changelog<String, Element> inMemoryLocalChangelog = new Changelog<>();

    {
        if (MDUtils.isDeveloperMode()) {
            inMemoryLocalChangelog.setShouldLogChanges(true);
        }
    }

    public LocalSyncTransactionCommitListener(Project project) {
        this.project = project;
    }

    public synchronized boolean isDisabled() {
        return disabled;
    }

    public synchronized void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Changelog<String, Element> getInMemoryLocalChangelog() {
        return inMemoryLocalChangelog;
    }

    @Override
    public Runnable transactionCommited(Collection<PropertyChangeEvent> events) {
        if (isDisabled() || !MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled()) {
            return null;
        }
        return new TransactionCommitHandler(events, project);
    }

    /**
     * Adapter to call handleChangeEvent() from the TransactionCommitListener
     * interface.
     */
    private class TransactionCommitHandler implements Runnable {
        private final Collection<PropertyChangeEvent> events;
        private final Project project;

        TransactionCommitHandler(final Collection<PropertyChangeEvent> events, Project project) {
            this.events = events;
            this.project = project;
        }

        @Override
        public void run() {
            try {
                for (PropertyChangeEvent event : events) {
                    Object source = event.getSource();
                    if (!(source instanceof Element) || ProjectUtilities.isElementInAttachedProject((Element) source)) {
                        continue;
                    }
                    Element sourceElement = (Element) source;
                    String changedPropertyName = event.getPropertyName();
                    if (changedPropertyName == null || changedPropertyName.startsWith("_") || IGNORED_PROPERTY_CHANGE_EVENT_NAMES.contains(changedPropertyName)) {
                        continue;
                    }
                    if ((event.getNewValue() == null && event.getOldValue() == null) || (event.getNewValue() != null && event.getNewValue().equals(event.getOldValue()))) {
                        continue;
                    }

                    if (!changedPropertyName.equals(UML2MetamodelConstants.INSTANCE_DELETED)) {
                        Element root = sourceElement;
                        while (root.getOwner() != null) {
                            root = root.getOwner();
                        }
                        if (!root.equals(project.getModel())) {
                            continue;
                        }
                    }

                    // START PRE-PROCESSING
                    Element e;
                    if (sourceElement instanceof Comment && ExportUtility.isElementDocumentation((Comment) sourceElement) && changedPropertyName.equals(PropertyNames.BODY)) {
                        sourceElement = sourceElement.getOwner();
                    }
                    else if ((sourceElement instanceof ValueSpecification) && (changedPropertyName.equals(PropertyNames.VALUE)) ||
                            (sourceElement instanceof OpaqueExpression) && (changedPropertyName.equals(PropertyNames.BODY)) ||
                            (sourceElement instanceof Expression) && (changedPropertyName.equals(PropertyNames.OPERAND))) {
                        // Need to find the actual element that needs to be sent (most likely a Property or Slot that's the closest owner of this element)
                        sourceElement = sourceElement.getOwner();
                        // There may be multiple ValueSpecification changes so go up the chain of owners until we find the actual owner that should be submitted
                        while (sourceElement instanceof ValueSpecification) {
                            sourceElement = sourceElement.getOwner();
                        }
                    }

                    if (sourceElement instanceof Constraint && (e = ExportUtility.getViewFromConstraint((Constraint) sourceElement)) != null) {
                        sourceElement = e;
                    }
                    // END PRE-PROCESSING

                    if (!ExportUtility.shouldAdd(sourceElement)) {
                        continue;
                    }
                    String elementID = ExportUtility.getElementID(sourceElement);
                    if (elementID == null) {
                        continue;
                    }
                    if (elementID.matches(ModelValidator.HOLDING_BIN_PACKAGE_ID_REGEX)) {
                        continue;
                    }

                    Changelog.ChangeType changeType = Changelog.ChangeType.UPDATED;
                    switch (changedPropertyName) {
                        case UML2MetamodelConstants.INSTANCE_DELETED:
                            changeType = Changelog.ChangeType.DELETED;
                            break;
                        case UML2MetamodelConstants.INSTANCE_CREATED:
                            changeType = Changelog.ChangeType.CREATED;
                            break;
                    }
                    inMemoryLocalChangelog.addChange(elementID, sourceElement, changeType);
                }
            } catch (Exception e) {
                Application.getInstance().getGUILog().log("[ERROR] LocalSyncTransactionCommitListener had an unexpected error: " + e.getMessage());
                Utils.printException(e);
                throw e;
            }
        }
    }
}
