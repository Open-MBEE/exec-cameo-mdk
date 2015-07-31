package gov.nasa.jpl.mbee.ems.sync;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.nomagic.magicdraw.core.Application;

import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;

public class OutputQueueStatusAction extends SRAction {	
	private static final long serialVersionUID = 1L;
	
	private final OutputQueueDetailWindow outputQueueDetailWindow = new OutputQueueDetailWindow();

	public static final String NAME = "MMS Queue";
	
	public OutputQueueStatusAction() {
		super(NAME);
	}
	
	public void update() {
		update(false);
	}
	
	public void update(final boolean plusOne) {
		setName(OutputQueueStatusAction.NAME + ": " + (OutputQueue.getInstance().size() + (plusOne ? 1 : 0)));
		if (outputQueueDetailWindow.isVisible()) {
			outputQueueDetailWindow.update();
		}
		//Application.getInstance().getGUILog().log(getName());
		//System.out.println(getName());
	}
	
	@Override
	public void actionPerformed(final ActionEvent event) {
		outputQueueDetailWindow.setVisible(!outputQueueDetailWindow.isVisible());
		if (outputQueueDetailWindow.isVisible())
			outputQueueDetailWindow.update();
	}
	
	protected class OutputQueueDetailWindow extends JFrame {
		
		private JTable table;
		private final Vector<String> columns = new Vector<String>();
		private final Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		
		{
			columns.addElement("#");
			columns.addElement("Method");
			columns.addElement("URL");
		}
		
		//private AbstractTableModel tableModel;
		
		protected OutputQueueDetailWindow() {
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			//this.setSize(new Dimension(500, 200));
			this.setLocationRelativeTo(Application.getInstance().getMainFrame());
			
			//tableModel = new OutputQueueTableModel();
			table = new JTable(data, columns);
			table.setFillsViewportHeight(true);
			
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.getColumnModel().getColumn(0).setPreferredWidth(20);
			table.getColumnModel().getColumn(1).setPreferredWidth(100);
			table.getColumnModel().getColumn(2).setPreferredWidth(370);
			
			final JScrollPane tableScrollPane = new JScrollPane(table);
			tableScrollPane.setPreferredSize(new Dimension(500, 200));
			
			final JPanel panel  = new JPanel(new BorderLayout());
			panel.add(tableScrollPane, BorderLayout.CENTER);
			
			this.setContentPane(panel);
			this.pack();
		}
		
		public void update() {
			//final Vector<Vector<Object>> data = new Vector<Vector<Object>>();
			data.clear();
			final Iterator<Request> it = OutputQueue.getInstance().iterator();
			int counter = 1;
			while (it.hasNext()) {
				final Request r = it.next();
				if (r == null) {
					// just in case of concurrent modification issues
					break;
				}
				final Vector<Object> row = new Vector<Object>();
				row.addElement(counter);
				row.addElement(r.getMethod());
				row.addElement(r.getUrl());
				
				data.addElement(row);
				
				counter++;
			}
			/*table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			if (table.getModel() instanceof AbstractTableModel) {
				((AbstractTableModel) table.getModel()).fireTableStructureChanged();
			}*/
			table.repaint();
			//tableModel.fireTableDataChanged();
		}
		
	}
	
	// Tried doing it "properly" with a table model, but it wants to be able to get values arbitrarily by row and column
	// would cause excessive iteration of the queue so was abandoned for a more manual approach.
	
	/*protected class OutputQueueTableModel extends AbstractTableModel implements TableModel {
		
		private Map<String, Class<?>> headers = new LinkedHashMap<String, Class<?>>();
		
		{
			headers.put("#", Integer.class);
			headers.put("Type", String.class);
			headers.put("URL", String.class);
		}

		@Override
		public int getRowCount() {
			return OutputQueue.getInstance().size();
		}

		@Override
		public int getColumnCount() {
			return headers.size();
		}

		@Override
		public String getColumnName(int columnIndex) {
			final Iterator<String> it = headers.keySet().iterator();
			for (int i = 0; i < columnIndex; i++) {
				it.next();
			}
			return it.next();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			final Iterator<Class<?>> it = headers.values().iterator();
			for (int i = 0; i < columnIndex; i++) {
				it.next();
			}
			return it.next();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= OutputQueue.getInstance().size()) {
				return null;
			}
			final Request request = null;
			try {
				request = OutputQueue.getInstance().
			} catch (ArrayIndexOutOfBoundsException ignored) {
				// crude way to prevent errors caused by concurrent modification of the output queue
			}
			if (request == null) {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub
			
		}
		
	}*/
}