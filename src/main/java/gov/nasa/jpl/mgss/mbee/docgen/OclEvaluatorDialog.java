/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mgss.mbee.docgen.RepeatInputComboBoxDialog.EditableListPanel;
import gov.nasa.jpl.mgss.mbee.docgen.RepeatInputComboBoxDialog.Processor;
import gov.nasa.jpl.mgss.mbee.docgen.actions.OclQueryAction;
import gov.nasa.jpl.mgss.mbee.docgen.actions.OclQueryAction.ProcessOclQuery;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JDialog;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 *
 */
public class OclEvaluatorDialog extends JDialog {

    private static final long serialVersionUID = -9114812582757129836L;

    private static OclEvaluatorDialog instance = null;
    // members for tracking input history
    protected static Object lastInput = null;
    protected static LinkedList< Object > inputHistory = new LinkedList< Object >();
    protected static HashSet< Object > pastInputs = new HashSet< Object >();
    protected static LinkedList< Object > choices = new LinkedList< Object >();
    protected static int maxChoices = 20;
    
    /**
     * callback for processing input
     */
    protected Processor processor;
    
    protected static RepeatInputComboBoxDialog.EditableListPanel editableListPanel = null;
    
    protected boolean cancelSelected = false;
    
    /**
     * 
     */
    public OclEvaluatorDialog() {
        super();
        init();
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog( Frame owner ) {
        super( owner, false );
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog( Dialog owner ) {
        super( owner, false );
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     */
    public OclEvaluatorDialog( Window owner ) {
        super( owner, ModalityType.MODELESS );
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog( Frame owner, String title ) {
        super( owner, title, false );
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog( Dialog owner, String title ) {
        super( owner, title, false );
        init();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param owner
     * @param title
     */
    public OclEvaluatorDialog( Window owner, String title ) {
        super( owner, title, ModalityType.MODELESS );
        init();
        // TODO Auto-generated constructor stub
    }

    protected void init() {
        editableListPanel =
                new RepeatInputComboBoxDialog.EditableListPanel( "Enter an OCL expression:",
                                                                 choices.toArray() );
        Collection< Element > selectedElements = MDUtils.getSelection( null );
        processor = new OclQueryAction.ProcessOclQuery( selectedElements );
        if ( lastInput != null ) {
            Object result = processor.process( lastInput );
            editableListPanel.setResultPanel( result );
        }
        Debug.outln( "lastInput = " + lastInput );

        editableListPanel.setVisible( true );
    }
    
    public static OclEvaluatorDialog getInstance() {
        return instance;
    }
    
    /**
     * @param args
     */
    public static void main( String[] args ) {
        OclEvaluatorDialog dialog = new OclEvaluatorDialog();
        dialog.setVisible( true );
    }

}
