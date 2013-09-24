package gov.nasa.jpl.mgss.mbee.docgen.model.ui;

import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.model.MissionMapping;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXTreeTable;

public class CharacterizationChooserUI {

	private JFrame frame;
	private MissionMapping mapping;
	private JXTreeTable treeTable;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CharacterizationChooserUI window = new CharacterizationChooserUI(
							null);
					window.getFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CharacterizationChooserUI(MissionMapping mapping) {
		this.mapping = mapping;
		initialize();
	}

	class ButtonsRenderer implements TableCellRenderer {
		private JButton jb = new JButton("Test");

		public ButtonsRenderer() {
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Test");
				}
			});
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return jb;
		}
	}

	class ButtonsEditor implements TableCellEditor {
		private JButton jb = new JButton("Test");

		public ButtonsEditor() {
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Test");
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			return jb;
		}

		@Override
		public Object getCellEditorValue() {
			return "";
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			return false;
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		@Override
		public boolean stopCellEditing() {
			return true;
		}

		@Override
		public void cancelCellEditing() {
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
		}
	}

	public class JTableButtonRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JButton button = (JButton) value;
			return button;
		}
	}

	public class JTableButtonMouseListener extends MouseAdapter {
		private final JTable table;

		public JTableButtonMouseListener(JTable table) {
			this.table = table;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int column = table.getColumnModel().getColumnIndexAtX(e.getX());
			int row = e.getY() / table.getRowHeight();

			if (row < table.getRowCount() && row >= 0
					&& column < table.getColumnCount() && column >= 0) {
				Object value = table.getValueAt(row, column);
				if (value instanceof JButton) {
					((JButton) value).doClick();
				}
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		getFrame().setBounds(100, 100, 1450, 300);
		getFrame().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JLabel lblCharacterizationChooser = new JLabel(
				"Characterization Chooser");
		frame.getContentPane().add(lblCharacterizationChooser,
				BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		CharacterizationTreeTableModel treeTableModel = new CharacterizationTreeTableModel(
				mapping);
		treeTable = new CharacterizationTreeTable(treeTableModel);
		scrollPane.setViewportView(treeTable);
		treeTable.setHorizontalScrollEnabled(true);
		treeTable.setShowGrid(true);
		treeTable.setGridColor(Color.LIGHT_GRAY);
		treeTable.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		treeTable.addMouseListener(new JTableButtonMouseListener(treeTable));
		treeTable.getColumnModel().getColumn(1)
				.setCellRenderer(new JTableButtonRenderer());

		treeTable.expandAll();
		treeTable.getColumn(0).setMinWidth(200);

		// Add new row to table
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Find selected row if exists
				Object selectedNode = null;
				TreeSelectionModel selectModel = treeTable
						.getTreeSelectionModel();
				if (selectModel != null) {
					TreePath selectPath = selectModel.getSelectionPath();
					if (selectPath != null) {
						selectedNode = selectPath.getLastPathComponent();
					}
				}

				// If no selected row or if selected is not a package, get root
				// node
				Node<String, MissionComponent> parentNode = null;
				if (selectedNode == null
						|| !((Node<String, MissionComponent>) selectedNode)
								.getData().isPackage()) {
					parentNode = mapping.getRoot();
				} else {
					parentNode = (Node<String, MissionComponent>) selectedNode;
				}

				// Create and insert new element
				Node<String, MissionComponent> node = new Node<String, MissionComponent>(
						"Untitled", new MissionComponent("Untitled"));
				parentNode.addChild(node);
				treeTable.updateUI();
			}
		});

		JButton btnRefactor = new JButton("Save and Refactor");
		btnRefactor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				mapping.refactor();
				frame.dispose();
			}
		});
		
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				mapping.apply();
				frame.dispose();
			}
		});

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				frame.dispose();
			}
		});

		// define layout after all buttons have been created
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(btnAdd);
		panel.add(Box.createHorizontalGlue());
		panel.add(btnRefactor);
		panel.add(btnSave);
		panel.add(btnCancel);

		// 'esc' to close out box
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		Action actionListener = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				frame.dispose();
			}
		};
		InputMap inputMap = scrollPane
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		scrollPane.getActionMap().put("ESCAPE", actionListener);
	}
	
	public JFrame getFrame() {
		return frame;
	}
}