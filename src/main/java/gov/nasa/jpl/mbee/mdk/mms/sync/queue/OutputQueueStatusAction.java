package gov.nasa.jpl.mbee.mdk.mms.sync.queue;

import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.actions.SRAction;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;

public class OutputQueueStatusAction extends SRAction {
    private static final long serialVersionUID = 1L;

    private final OutputQueueDetailWindow outputQueueDetailWindow = new OutputQueueDetailWindow();

    public static final String NAME = "MMS Queue";

    public OutputQueueStatusAction() {
        super(NAME);
    }

    public void update() {
        boolean haveCurrent = OutputQueue.getInstance().getCurrent() != null;
        setName(OutputQueueStatusAction.NAME + ": " + (OutputQueue.getInstance().size() + (haveCurrent ? 1 : 0)));
        if (outputQueueDetailWindow.isVisible()) {
            outputQueueDetailWindow.update();
        }
        MDKPlugin.updateMainToolbarCategory();
        //Application.getInstance().getGUILog().log(getName());
        //System.out.println(getName());
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (!outputQueueDetailWindow.isVisible()) {
            outputQueueDetailWindow.setVisible(true);
        }
        else {
            outputQueueDetailWindow.update();
            outputQueueDetailWindow.toFront();
        }
    }

    protected class OutputQueueDetailWindow extends JFrame {

        private JTable table;
        private final Vector<String> columns = new Vector<String>();
        private final Vector<Vector<Object>> data = new Vector<Vector<Object>>();

        {
            columns.addElement("#");
            columns.addElement("Method");
            columns.addElement("# Elements");
            columns.addElement("Type");
            columns.addElement("URL");
            columns.addElement("");
        }

        //private AbstractTableModel tableModel;

        protected OutputQueueDetailWindow() {
            this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            //this.setSize(new Dimension(500, 200));
            this.setLocationRelativeTo(Application.getInstance().getMainFrame());

            //tableModel = new OutputQueueTableModel();
            table = new JTable(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 5;
                }
            };
            table.setFillsViewportHeight(true);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.getColumnModel().getColumn(0).setPreferredWidth(20);
            table.getColumnModel().getColumn(1).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(3).setPreferredWidth(200);
            table.getColumnModel().getColumn(4).setPreferredWidth(370);
            table.getColumnModel().getColumn(5).setPreferredWidth(100);

            final JScrollPane tableScrollPane = new JScrollPane(table);
            tableScrollPane.setPreferredSize(new Dimension(500, 200));

            final JPanel panel = new JPanel(new BorderLayout());
            panel.add(tableScrollPane, BorderLayout.CENTER);

            TableButtonColumn buttonEditor = new TableButtonColumn(table);
            table.getColumnModel().getColumn(5).setCellRenderer(buttonEditor);
            //table.getColumnModel().getColumn(5).setCellEditor(buttonEditor);


            this.setContentPane(panel);
            this.pack();
        }

        public void update() {
            //final Vector<Vector<Object>> data = new Vector<Vector<Object>>();
            data.clear();
            final Iterator<Request> it = OutputQueue.getInstance().iterator();
            Request current = OutputQueue.getInstance().getCurrent();
            int counter = 1;
            if (current != null) {
                final Vector<Object> row = new Vector<Object>();
                row.addElement(0);
                row.addElement(current.getRequest().getMethod());
                row.addElement(NumberFormat.getInstance().format(current.getCount()));
                row.addElement(current.getName());
                row.addElement(current.getRequest().getURI().toString());
                row.addElement("Continue in Background");
                data.addElement(row);
            }
            while (it.hasNext()) {
                final Request r = it.next();
                if (r == null) {
                    // just in case of concurrent modification issues
                    break;
                }
                final Vector<Object> row = new Vector<Object>();
                row.addElement(counter);
                row.addElement(r.getRequest().getMethod());
                row.addElement(NumberFormat.getInstance().format(r.getCount()));
                row.addElement(r.getName());
                row.addElement(r.getRequest().getURI().toString());
                row.addElement("Delete");
                data.addElement(row);

                counter++;
            }
            /*table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            if (table.getModel() instanceof AbstractTableModel) {
				((AbstractTableModel) table.getModel()).fireTableStructureChanged();
			}*/
            table.repaint();
//            this.setAlwaysOnTop(true);
            //tableModel.fireTableDataChanged();
        }

    }

    public class TableButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

        private JTable table;
        private Action action;
        private JButton renderButton;
        private JButton editButton;

        public TableButtonColumn(JTable table) {
            this.table = table;
            this.action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int modelRow = Integer.valueOf(e.getActionCommand());
                    if (modelRow == 0) {
                        //Utils.guilog("Cancel Pressed.");
                        OutputQueue.getInstance().setCurrent(null); //let the table update when it actually moves on
                            /*SwingUtilities.invokeLater(new Runnable() {
		                        @Override
		                        public void run() {
		                            OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
		                        }
		                    });*/
                    }
                    else {
                        //Utils.guilog("Delete Pressed.");
                        OutputQueue.getInstance().remove(modelRow);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
                            }
                        });
                    }
                }
            };

            renderButton = new JButton();
            editButton = new JButton();
            editButton.setFocusPainted(false);
            editButton.addActionListener(this);

            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(5).setCellRenderer(this);
            columnModel.getColumn(5).setCellEditor(this);
            //table.addMouseListener( this );
        }

        /*
           *	The button has been pressed. Stop editing and invoke the custom Action
           */
        public void actionPerformed(ActionEvent e) {
            int row = table.convertRowIndexToModel(table.getEditingRow());
            fireEditingStopped();

            //  Invoke the Action
            ActionEvent event = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + row);
            action.actionPerformed(event);
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            //Utils.guilog("getTableCellEditorComponent - value is null");
            if (value != null) {
                //Utils.guilog("getTableCellEditorComponent: " + value.toString());
                editButton.setText(value.toString());
                editButton.setIcon(null);
                return editButton;
            }
            return null;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            //Utils.guilog("getTableCellRendererComponent- value is null");
            if (value != null) {
                //Utils.guilog("getTableCellRendererComponent: " + value.toString());
                renderButton.setText(value.toString());
                renderButton.setIcon(null);
                return renderButton;
                //}
            }
            return null;
        }
    }
}