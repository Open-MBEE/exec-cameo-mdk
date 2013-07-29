/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.text.JTextComponent;

public class RepeatInputComboBoxDialog implements Runnable {

  @SuppressWarnings( "unused" )
  private static final long serialVersionUID = 6516421214781803070L;

  /**
   * A callback interface for processing input and returning results for display
   * in the dialog.
   * 
   */
  public static interface Processor {
    public Object process( Object input );
  }

  // RepeatInputComboBoxDialog members

  // usual JOptionPane static dialog parameters
  protected Component parentComponent = null;
  protected Object message = null;
  protected Object[] items = new Object[]{};
  protected String title = null;
  protected int optionType = JOptionPane.OK_CANCEL_OPTION;
  protected int messageType = JOptionPane.PLAIN_MESSAGE;
  protected Icon icon = null;
  // some extra parameters for customizing process button
//  protected String processButtonLabel;
//  protected Icon processButonIcon;

  // members for tracking input history
  protected static Object lastInput = null;
  protected static LinkedList< Object > inputHistory = new LinkedList< Object >();
  protected static HashSet< Object > pastInputs = new HashSet< Object >();
  protected static LinkedList< Object > choices = new LinkedList< Object >();
  protected static int maxChoices = 10;
  
  /**
   * callback for processing input
   */
  protected Processor processor = null;
  
  protected static EditableListPanel editableListPanel = null;
  
  protected boolean cancelSelected = false;
  
  
//  protected ComponentAdapter shownListener = new ComponentAdapter() {
//    @Override
//    public void componentShown( ComponentEvent ce ) {
//      if ( editableListPanel != null ) editableListPanel.setUpFocus();
//    }
//  };
//  ComponentAdapter hiddenListener = new ComponentAdapter() {
//    @Override
//    public void componentHidden( ComponentEvent ce ) {
//      if ( editableListPanel != null ) editableListPanel.focusSet = false;
//    }
//  };
  
  /**
   * @param message
   * @param title
   * @param processor
   */
  public RepeatInputComboBoxDialog( Object message, String title,
                                    Processor processor ) {
    super();
    this.message = message;
    this.title = title;
    this.processor = processor;
  }

  /**
   * Initialize options but do not create the dialog.
   * 
   * @param parentComponent
   * @param message
   * @param items
   * @param title
   * @param optionType
   * @param messageType
   * @param icon
   * @param choices
   * @param maxChoices
   *          max number of previous inputs to remember and provide as choices
   *          in a combo box
   * @param processor
   *          callback for processing input
   */
  public RepeatInputComboBoxDialog( Component parentComponent, Object message,
                                    Object[] items, String title,
                                    int optionType, int messageType, Icon icon,
                                    LinkedList< Object > choices,
                                    int maxChoices, Processor processor ) {
    super();
    this.parentComponent = parentComponent;
    this.message = message;
    this.items = items;
    this.title = title;
    this.optionType = optionType;
    this.messageType = messageType;
    this.icon = icon;
    this.choices = choices;
    this.maxChoices = maxChoices;
    this.processor = processor;
  }

  public static void showRepeatInputComboBoxDialog( Object message, String title,
                                    Processor processor ) {
    RepeatInputComboBoxDialog dialog =
        new RepeatInputComboBoxDialog( message, title, processor );
    dialog.show();
  }
  
  public static void showRepeatInputComboBoxDialog( Component parentComponent, Object message,
                                    Object[] items, String title,
                                    int optionType, int messageType, Icon icon,
                                    LinkedList< Object > choices,
                                    int maxChoices, Processor processor ) {
    RepeatInputComboBoxDialog dialog =
        new RepeatInputComboBoxDialog( parentComponent, message, items, title,
                                       optionType, messageType, icon, choices,
                                       maxChoices, processor );
    dialog.show();
  }
  
  public void show() {
    try {
      SwingUtilities.invokeLater( this );
    } catch ( Throwable t ) {
      t.printStackTrace();
    }
  }

  public void run() {
    Object selectedItem = "";
    editableListPanel = null;

    while ( selectedItem != null ) {
      if ( editableListPanel == null ) {
        editableListPanel = new EditableListPanel( (String)message, choices.toArray() );//,
        //processButtonLabel, processButonIcon );
        message = editableListPanel;
      } else {
        editableListPanel.setItems( choices.toArray() );
      }

      if ( lastInput != null ) {
        Object result =
            RepeatInputComboBoxDialog.this.processor.process( lastInput );
        editableListPanel.setResultPanel( result );
      }
      Debug.outln( "lastInput = " + lastInput );

      editableListPanel.setVisible( true );
      int option =
          JOptionPane.showConfirmDialog( parentComponent, message, title,
                                         optionType, messageType, icon );
      selectedItem = getSelectedItem( message );
      if (option == JOptionPane.CANCEL_OPTION) {
        selectedItem = null;
        System.out.println( "CANCEL! EditableListPanel value: " + selectedItem );
      } else if (option == JOptionPane.CLOSED_OPTION) {
        selectedItem = null;
        System.out.println( "CLOSED! EditableListPanel value: " + selectedItem );
      } else {
        System.out.println( "EditableListPanel value: " + selectedItem );
      }

      if ( selectedItem == null || Utils2.isNullOrEmpty( selectedItem.toString() ) ) {
        selectedItem = null;
      } else {
        lastInput = selectedItem;
        inputHistory.push( selectedItem );
        if ( pastInputs.contains( selectedItem ) ) {
          choices.remove( selectedItem );
        } else {
          pastInputs.add( selectedItem );
        }
        choices.push( selectedItem );
        while ( choices.size() > maxChoices ) {
          choices.pollLast();
        }
      }
    }
  }

  public static Object[] getSelectedObjects(Component c) {
    if ( c instanceof EditableListPanel ) {
      return new Object[]{ ( (EditableListPanel)c ).getValue() };
    }
    if ( c instanceof ItemSelectable ) {
      return ( (ItemSelectable)c ).getSelectedObjects();
    }
    if ( c instanceof Container ) {
      for ( Component sub : ((Container)c).getComponents() ) {
        Object[] selection = getSelectedObjects( sub );
        if ( selection != null ) return selection;
      }
    }
    return null;
  }
  
  public static Object getSelectedItem( Object component ) {
    Object selectedItem = null;
    if ( component instanceof EditableListPanel ) {
      selectedItem = ( (EditableListPanel)component ).getValue();
    } else if ( component instanceof Component ) {
      Object[] selection = getSelectedObjects( (Component)component );
      if ( selection.length == 1 ) {
        selectedItem = selection[0];
      } else {
        selectedItem = selection;
      }
    }
    return selectedItem;
  }
  
  private class EditableListPanel extends JPanel {

    private static final long serialVersionUID = 8166263196543269359L;

    // private JTextField value;
    public JComboBox jcb = null;
    //public JPanel resultPanel = new JPanel( new BorderLayout( 5, 5 ) );
    public JComponent resultPane = null;
    public JScrollPane resultScrollPane = null;
//        new JScrollPane( resultPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

    private boolean focusSet = false;

    public EditableListPanel( String msg, Object[] items ) { //, String processButtonLabel, Icon processButtonIcon ) {
      super( new BorderLayout( 5, 5 ) );
      setItems( items );
      add( new JLabel( msg ), BorderLayout.NORTH );
      add( jcb, BorderLayout.CENTER );
      
      resultPane = createEditorPane(" ");
      resultScrollPane =
          new JScrollPane( resultPane,
                           JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
//      resultScrollPanel.setVerticalScrollBarPolicy(
//                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      resultScrollPane.setPreferredSize(new Dimension(250, 145));
      resultScrollPane.setMinimumSize(new Dimension(10, 10));
    
      
      add( resultScrollPane, BorderLayout.SOUTH );
//      resultPanel.setSize( 400, 400 );
//      resultScrollPanel.setBounds( 100, 100, 300, 300 );
//      resultPanel.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
//      resultPanel.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
      addAncestorListener( new RequestFocusListener() );
      //jcb.getEditor().selectAll(); // TODO -- This isn't working. WHy?
    }

    private JEditorPane createEditorPane(String html) {
      JEditorPane editorPane = new JEditorPane();
      editorPane.setEditable(false);
      editorPane.setText(html);
      editorPane.setContentType("text/html");
      return editorPane;
    }
    
    public void setItems( Object[] items ) {
      getJcb().setModel( new DefaultComboBoxModel( items ) );
    }
    
    public JComboBox getJcb() {
      if ( jcb == null ) {
        jcb = new JComboBox();
        jcb.setEditable( true );
        ComboBoxEditor editor = jcb.getEditor();
        Component cmp = editor.getEditorComponent();
        if ( cmp instanceof JTextField ) {
          JTextField field = (JTextField)cmp;
          field.addAncestorListener( new RequestFocusListener() );
        }
        jcb.addAncestorListener( new RequestFocusListener() );
//        //setUpFocus();
//        jcb.addComponentListener(new ComponentAdapter(){  
//          public void componentShown(ComponentEvent ce){  
//            jcb.requestFocusInWindow();
//          }  
//        });
      }
      return jcb;
    }

    protected boolean isFocusSet() {
      return focusSet;
    }

    public String toHtml( String s ) {
      if ( s.contains( "<html>" ) ) return s;
      return "<html>" + s.replaceAll( "\\n", "<br>\\n" ) + "</html>";
    }
    
//    private Component makeComponent( Object result ) {
//      if ( result instanceof Component ) {
//        return (Component)result;
//      }
//      if ( result instanceof Icon ) {
//        return new JLabel( (Icon)result );
//      }
//      return new JTextArea( toHtml(result.toString()) );
//    }

    public void setResultPanel( Object result ) {
      System.out.println("setResultPanel(" + result + ")" );
      if ( resultPane instanceof JEditorPane ) {
        if ( result == null ) result = "null";
        ( (JEditorPane)resultPane ).setText( //toHtml( 
        		result.toString() );
      } else {
      JComponent newResultPane = null;
      if ( result instanceof JComponent ) {
        newResultPane = (JComponent)result;
      } else if ( result instanceof Icon ) {
        newResultPane = new JLabel( (Icon)result );
      } else { //if ( result instanceof String ) {
        newResultPane =
            createEditorPane( //toHtml(
            		result == null ? "null" : result.toString() );
        //resultPane.setText( toHtml( (String)result ) );
      }
      if ( newResultPane != null ) {
        resultScrollPane.remove( resultPane );
        resultPane = newResultPane;
        System.out.println("Hello!!!");
        resultScrollPane.add( resultPane );
      }
      }
      if ( this.isVisible() ) {
        System.out.println("IS visible");
        setVisible( false );
        setVisible( true );
      } else {
        System.out.println("is NOT visible");
      }
      //resultScrollPane.update(getGraphics());
//      Component c = makeComponent( result );
//      if ( resultPane.getComponentCount() == 1 ) {
//        resultPane.remove( 0 );
//      }
//      if ( resultPane.getComponentCount() == 0 ) {
//        resultPane.add( c );
//      }
    }
    
    public String getValue() {
      return (String)jcb.getSelectedItem();
    }

    public ComboBoxEditor getEditor() {
      return jcb.getEditor();
    }

//    protected void setUpFocus() {
//      ComboBoxEditor editor = getEditor();
//      if ( editor == null ) {
//        Debug.outln("DOH!");
//        return;
//      }
//      Object item = editor.getEditorComponent();
//      final JTextComponent tf = (JTextComponent)( item instanceof JTextComponent ? item : null );
//      if ( tf == null ) {
//        Debug.outln("BUMMER!");
//      } else {
//        tf.selectAll();
//        tf.requestFocusInWindow();
//        //tf.requestFocus();
//        if ( !Arrays.asList( tf.getComponentListeners()).contains( shownListener ) ) {
//          tf.addComponentListener(shownListener);
//        }
//        if ( !Arrays.asList( tf.getComponentListeners()).contains( hiddenListener ) ) {
//          tf.addComponentListener(hiddenListener);
//        }
//        focusSet = true;
//      }
//    }
    
//    @Override
//    public void setVisible( boolean aFlag ) {
//      //jcb.getEditor().selectAll(); // TODO -- This isn't working. WHy?
//      super.setVisible( aFlag );
//      //setUpFocus();
//    }

  }
 
//  private class MyCellRenderer extends BasicComboBoxRenderer.UIResource implements ListCellRenderer {
//    public MyCellRenderer() {
//        setOpaque(true);
//    }
//
//    @Override
//    public Component getListCellRendererComponent(JList list,
//                                                  Object value,
//                                                  int index,
//                                                  boolean isSelected,
//                                                  boolean cellHasFocus) {
//      if ( !isSelected && RepeatInputComboBoxDialog.this.message instanceof EditableListPanel ) {
//        //( (EditableListPanel)message ).getEditor().selectAll();
//        //isSelected = true;
//      }
//      super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
//
//      return this;
//    }
//  }

  /**
   * http://tips4java.wordpress.com/2010/03/14/dialog-focus/
   * 
   *  Convenience class to request focus on a component.
   *
   *  When the component is added to a realized Window then component will
   *  request focus immediately, since the ancestorAdded event is fired
   *  immediately.
   *
   *  When the component is added to a non realized Window, then the focus
   *  request will be made once the window is realized, since the
   *  ancestorAdded event will not be fired until then.
   *
   *  Using the default constructor will cause the listener to be removed
   *  from the component once the AncestorEvent is generated. A second constructor
   *  allows you to specify a boolean value of false to prevent the
   *  AncestorListener from being removed when the event is generated. This will
   *  allow you to reuse the listener each time the event is generated.
   */
  public static class RequestFocusListener implements AncestorListener
  {
    private boolean removeListener;

    /*
     *  Convenience constructor. The listener is only used once and then it is
     *  removed from the component.
     */
    public RequestFocusListener()
    {
      this(true);
    }

    /*
     *  Constructor that controls whether this listen can be used once or
     *  multiple times.
     *
     *  @param removeListener when true this listener is only invoked once
     *                        otherwise it can be invoked multiple times.
     */
    public RequestFocusListener(boolean removeListener)
    {
      this.removeListener = removeListener;
      System.out.println( "zowie!" );
    }

    @Override
    public void ancestorAdded(AncestorEvent e)
    {
      System.out.println( "ancestorAdded(" + e + ")" );
      JComponent component = e.getComponent();
      component.requestFocusInWindow();
      if ( component instanceof JTextField ) {
        ( (JTextField)component ).selectAll();
      }

      //JRootPane rootPane = component.getRootPane();
      //rootPane
      //rootPane.setLocation( 100, 100 );
      JWindow top = getTopComponentOfType( component, JWindow.class );
      //top.setSize( new Dimension( 300, 300 ) );
      if ( top != null ) {
        top.setMaximumSize( new Dimension(500,500) );
        System.out.println( "rootPane = " + component.getRootPane().toString() );
        System.out.println( "top = " + top.toString() );
      }
      System.out.println( "wow!" );
      
      if (removeListener)
        component.removeAncestorListener( this );
    }

    @Override
    public void ancestorMoved(AncestorEvent e) {}

    @Override
    public void ancestorRemoved(AncestorEvent e) {}
  }
  
  
  /**
   * @param args
   */
  public static void main( String[] args ) {
    Debug.turnOn();
    String[] items = { "Apple", "Banana", "Grape", "Cherry" };
    RepeatInputComboBoxDialog.Processor processor =
        new RepeatInputComboBoxDialog.Processor() {
          @Override
          public Object process( Object input ) {
            System.out.println("processing " + input);
            return "processed " + input;
          }
        };
    RepeatInputComboBoxDialog.showRepeatInputComboBoxDialog( "Enter an OCL expression:",
                                                             "OCL Evaluation",
                                                             processor );
  }

  /**
   * @param component
   * @return the highest [grand]parent of component that is a JComponent or, if
   *         no such parent exists, component
   */
  public static < T extends Component > T
      getTopComponentOfType( Component component, Class< T > type ) {
    if ( component == null ) return null;
    if ( type == null ) type = (Class< T >)JComponent.class;
    Container parent = component.getParent();
    while ( parent != null ) {//&& !( type.isInstance( parent ) ) ) {
      if ( type.isInstance( parent ) ) {
        return getTopComponentOfType( (JComponent)parent, type );
      }
      parent = parent.getParent();
    }
    return (T)( type.isInstance( component ) ? component : null );
  }

}
