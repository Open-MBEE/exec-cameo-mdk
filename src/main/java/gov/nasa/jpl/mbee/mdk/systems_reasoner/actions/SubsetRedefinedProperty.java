package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.actions.NMAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Created by johannes on 3/15/17.
 */
public class SubsetRedefinedProperty extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_ID = "Set subsetted property in redefining property.";
    private final Property redefinedElement;
    private final Property redefiningElement;
    public List<InstanceSpecification> instances;


    public SubsetRedefinedProperty(Property redefEl, Property redefingEl) {
        super(DEFAULT_ID);
        this.redefinedElement = redefEl;
        this.redefiningElement = redefingEl;

    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {

        for(Property p : redefinedElement.getSubsettedProperty()){
            for(RedefinableElement r : p.get_redefinableElementOfRedefinedElement()){
                if(r instanceof Property)
                    if(!redefiningElement.getSubsettedProperty().contains((Property) r)) {
                        redefiningElement.getSubsettedProperty().add((Property) r);
                    }
            }
        }
    }
}