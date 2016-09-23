/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.mdk.model.ui;

import gov.nasa.jpl.mbee.mdk.model.MissionMapping;
import gov.nasa.jpl.mbee.mdk.tree.Node;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class CharacterizationChooserUI {

    private JFrame frame;
    private MissionMapping mapping;
    private JXTreeTable treeTable;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    CharacterizationChooserUI window = new CharacterizationChooserUI(null);
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
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
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                     int column) {
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
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

            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
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
        getFrame().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JLabel lblCharacterizationChooser = new JLabel("Characterization Chooser");
        frame.getContentPane().add(lblCharacterizationChooser, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        CharacterizationTreeTableModel treeTableModel = new CharacterizationTreeTableModel(mapping);
        treeTable = new CharacterizationTreeTable(treeTableModel);
        scrollPane.setViewportView(treeTable);
        treeTable.setHorizontalScrollEnabled(true);
        treeTable.setShowGrid(true);
        treeTable.setGridColor(Color.LIGHT_GRAY);
        treeTable.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        treeTable.addMouseListener(new JTableButtonMouseListener(treeTable));
        treeTable.getColumnModel().getColumn(1).setCellRenderer(new JTableButtonRenderer());

        treeTable.expandAll();
        treeTable.getColumn(0).setMinWidth(200);

        // Add new row to table
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Find selected row if exists
                Object selectedNode = null;
                TreeSelectionModel selectModel = treeTable.getTreeSelectionModel();
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
                        || !((Node<String, MissionComponent>) selectedNode).getData().isPackage()) {
                    parentNode = mapping.getRoot();
                }
                else {
                    parentNode = (Node<String, MissionComponent>) selectedNode;
                }

                // Create and insert new element
                Node<String, MissionComponent> node = new Node<String, MissionComponent>("Untitled",
                        new MissionComponent("Untitled"));
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
            private static final long serialVersionUID = 8212840052063420469L;

            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                frame.dispose();
            }
        };
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        scrollPane.getActionMap().put("ESCAPE", actionListener);
    }

    public JFrame getFrame() {
        return frame;
    }
}
