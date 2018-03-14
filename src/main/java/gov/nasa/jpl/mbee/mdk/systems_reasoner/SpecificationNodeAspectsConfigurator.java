package gov.nasa.jpl.mbee.mdk.systems_reasoner;

import com.nomagic.magicdraw.ui.dialogs.specifications.ISpecificationComponent;
import com.nomagic.magicdraw.ui.dialogs.specifications.configurator.ISpecificationNodeConfigurator;
import com.nomagic.magicdraw.ui.dialogs.specifications.tree.node.ConfigurableNodeFactory;
import com.nomagic.magicdraw.ui.dialogs.specifications.tree.node.IConfigurableNode;
import com.nomagic.magicdraw.ui.dialogs.specifications.tree.node.ISpecificationNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.actions.AspectSelectionAction;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

/**
 * @author Johannes Gross
 */
public class SpecificationNodeAspectsConfigurator implements ISpecificationNodeConfigurator {
    @Override
    public void configure(IConfigurableNode node, Element element) {
        final IConfigurableNode myNode = ConfigurableNodeFactory.createConfigurableNode(new MyNode());
        node.insertNode(IConfigurableNode.DOCUMENTATION_HYPERLINKS, IConfigurableNode.Position.BEFORE, myNode);
    }

    private static class MyNode implements ISpecificationNode {
        @Override
        public String getID() {
            return "ASPECTS";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public String getText() {
            return "Aspects";
        }

        @Override
        public void dispose() {
        }

        @Override
        public ISpecificationComponent createSpecificationComponent(Element element) {
            return new AspectsSpecificationComponent(element);
        }

        @Override
        public void propertyChanged(Element element, PropertyChangeEvent event) {
        }

        @Override
        public boolean updateNode() {
            return false;
        }
    }

    private static class AspectsSpecificationComponent implements ISpecificationComponent {

        private final Element specifiedElement;

        public AspectsSpecificationComponent(Element element) {
            this.specifiedElement = element;
        }

        @Override
        public JComponent getComponent() {
            StringBuffer sb = new StringBuffer();
            if (specifiedElement instanceof Classifier) {
                AspectSelectionAction asa = new AspectSelectionAction((Classifier) specifiedElement);
                ArrayList<Classifier> aspects = new ArrayList<Classifier>();
                //asa.findAspectsOfClassifier(aspects);
            }
            return new JLabel(sb.toString());
        }

        @Override
        public void propertyChanged(Element element, PropertyChangeEvent event) {
        }

        @Override
        public void updateComponent() {
        }

        @Override
        public void dispose() {
        }
    }

    private static class MyInnerSpecificationNode implements ISpecificationNode {
        @Override
        public String getID() {
            return "MY_INNER_NODE";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public String getText() {
            return "My Inner Node";
        }

        @Override
        public void dispose() {
        }

        @Override
        public ISpecificationComponent createSpecificationComponent(Element element) {
            return new MyInnerSpecificationComponent();
        }

        @Override
        public void propertyChanged(Element element, PropertyChangeEvent event) {
        }

        @Override
        public boolean updateNode() {
            return false;
        }
    }

    private static class MyInnerSpecificationComponent implements ISpecificationComponent {
        @Override
        public JComponent getComponent() {
            return new JLabel("My Inner Specification Component");
        }

        @Override
        public void propertyChanged(Element element, PropertyChangeEvent event) {
        }

        @Override
        public void updateComponent() {
        }

        @Override
        public void dispose() {
        }
    }

}
