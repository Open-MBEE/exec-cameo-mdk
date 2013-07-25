package gov.nasa.jpl.mgss.mbee.docgen;

import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.*;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.event.*;

class ShowBothInputs {

  public static void main( String[] args ) {
    try {
      Runnable r = new Runnable() {

        public void run() {
          String lastInput = "";
          LinkedList< String > inputHistory = new LinkedList< String >();
          TreeSet< String > pastInputs = new TreeSet< String >();
          LinkedList< String > choices = new LinkedList< String >();
          int maxChoices = 10;

          String[] items = { "Apple", "Banana", "Grape", "Cherry" };

          String oclString = "";
          while ( oclString != null ) {
            // what was requested
            EditableListPanel elp = new EditableListPanel( "get stuff:", choices.toArray() );
//            oclString =
//                (String)JOptionPane.showInputDialog( (Component)null,
//                                                     elp, // "Enter an OCL expression:",
//                                                     "OCL Evaluation",
//                                                     JOptionPane.PLAIN_MESSAGE );//,
////                                                     (Icon)null,
////                                                     inputHistory.toArray(),
////                                                     lastInput );
//            JOptionPane.showMessageDialog( null,
//                                           elp, // );
//                                           "OCL Evaluation",
//                                           JOptionPane.QUESTION_MESSAGE,
//                                           (Icon)null );
            int option = JOptionPane.showConfirmDialog( null,
                                           elp, // );
                                           "OCL Evaluation",
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.PLAIN_MESSAGE,
                                           (Icon)null );
            oclString = elp.getValue();
            if (option == JOptionPane.CANCEL_OPTION) {
              oclString = null;
              System.out.println( "CANCEL! EditableListPanel value: " + elp.getValue() );
            } else {
              System.out.println( "EditableListPanel value: " + elp.getValue() );
            }

            if ( Utils2.isNullOrEmpty( oclString ) ) {
              oclString = null;
            } else {
              lastInput = oclString;
              inputHistory.push( oclString );
              if ( pastInputs.contains( oclString ) ) {
                choices.remove( oclString );
              } else {
                pastInputs.add( oclString );
              }
              choices.push( oclString );
              while ( choices.size() > maxChoices ) {
                choices.pollLast();
              }
            }
          }

          // // probably what this UI actually needs
          // JComboBox jcb = new JComboBox(items);
          // jcb.setEditable(true);
          // JOptionPane.showMessageDialog(null, jcb);
          // System.out.println( "JComboBox value: " + jcb.getSelectedItem() );
        }
      };
      SwingUtilities.invokeLater( r );
    } catch ( Throwable t ) {
      t.printStackTrace();
    }
  }
}

class EditableListPanel extends JPanel {

  // private JTextField value;
  JComboBox jcb = null;// new JComboBox(new Object[]{});

  EditableListPanel( String msg, Object[] items ) {
    super( new BorderLayout( 5, 5 ) );

    jcb = new JComboBox( items );
    // jcb.setModel( new DefaultComboBoxModel( items ) );
    jcb.setEditable( true );
    // JOptionPane.showMessageDialog(null, jcb);
    // System.out.println( "JComboBox value: " + jcb.getSelectedItem() );

    // // final JList list = new JList( items );
    // list.addListSelectionListener( new ListSelectionListener(){
    // public void valueChanged(ListSelectionEvent lse) {
    // value.setText( (String)jcb.getSelectedItem() );
    // }
    // } );
    // add( list, BorderLayout.CENTER );
    add( new JLabel( msg ), BorderLayout.NORTH );
    add( jcb, BorderLayout.CENTER );
    jcb.getEditor().selectAll();

    // //value = new JTextField("", 20);
    // add( value, BorderLayout.NORTH );
  }

  public String getValue() {
    return (String)jcb.getSelectedItem();
  }
  
  public ComboBoxEditor getEditor() {
    return jcb.getEditor();
  }
  
}