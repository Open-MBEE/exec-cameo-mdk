package gov.nasa.jpl.mgss.mbee.docgen.model.ui;

import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.model.MissionMapping;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import org.apache.commons.lang.ArrayUtils;

public class LibraryComponentChooserUI {

    @SuppressWarnings("unused")
    private static final JFrame                   frame       = new JFrame();
    private static Node<String, MissionComponent> missionNode;
    private static MissionMapping                 missionMapping;
    private static JList                          list        = new JList();
    private static JScrollPane                    scrollPane  = new JScrollPane(list);
    private static Object[]                       sortedComps = null;
    private static final JButton                  save        = new JButton("Save");
    private static final JButton                  cancel      = new JButton("Cancel");

    @SuppressWarnings("serial")
    public LibraryComponentChooserUI(Node<String, MissionComponent> node, MissionMapping mapping) {

        missionNode = node;
        missionMapping = mapping;
        frame.setTitle(node.getData().getName());
        frame.setVisible(true);
        String[] names = null;

        // display list in alphabetical order
        sortedComps = mapping.getLibraryComponents().toArray();
        Arrays.sort(sortedComps, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                LibraryComponent c1 = (LibraryComponent)o1;
                LibraryComponent c2 = (LibraryComponent)o2;
                return c1.getName().compareTo(c2.getName());
            }
        });

        names = new String[sortedComps.length];
        for (int i = 0; i < sortedComps.length; i++) {
            LibraryComponent c = (LibraryComponent)sortedComps[i];
            names[i] = c.getName();
        }
        list.setListData(names);

        // change interactivity of list selection
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // modify selection of list
        Object[] selectedComps = missionNode.getData().getLibraryComponents().toArray();
        ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
        for (int i = 0; i < sortedComps.length; i++) {
            for (int j = 0; j < selectedComps.length; j++) {
                if (sortedComps[i] == selectedComps[j]) {
                    selectedIndices.add(new Integer(i));
                }
            }
        }
        int[] intArray = ArrayUtils.toPrimitive(selectedIndices.toArray(new Integer[selectedIndices.size()]));
        list.setSelectedIndices(intArray);

        frame.setVisible(true);
    }

    static {
        // 'esc' to close out box
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        Action actionListner = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                frame.dispose();
            }
        };
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        scrollPane.getActionMap().put("ESCAPE", actionListner);

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                int[] selectedIndices = list.getSelectedIndices();

                Set<LibraryComponent> comps = missionNode.getData().getLibraryComponents();
                Set<LibraryComponent> toadd = new HashSet<LibraryComponent>();
                Set<LibraryComponent> toremove = new HashSet<LibraryComponent>();
                for (int i = 0; i < sortedComps.length; i++) {
                    boolean isSelected = false;
                    for (int j = 0; j < selectedIndices.length; j++) {
                        if (i == selectedIndices[j]) {
                            isSelected = true;
                        }
                    }
                    if (isSelected && !comps.contains(sortedComps[i])) {
                        toadd.add((LibraryComponent)sortedComps[i]);
                    } else if (!isSelected && comps.contains(sortedComps[i])) {
                        toremove.add((LibraryComponent)sortedComps[i]);
                    }
                }
                for (LibraryComponent lc: toadd) {
                    missionNode.getData().addLibraryComponent(lc);
                }
                for (LibraryComponent lc: toremove) {
                    missionNode.getData().removeLibraryComponent(lc);
                }
                frame.setVisible(false);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                frame.setVisible(false);
            }
        });

        frame.setBounds(100, 100, 450, 600);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        JPanel southPane = new JPanel(new FlowLayout());
        southPane.add(save);
        southPane.add(cancel);

        frame.getContentPane().setLayout(new BorderLayout());
        Container pane = frame.getContentPane();
        pane.add(scrollPane, BorderLayout.CENTER);
        pane.add(southPane, BorderLayout.SOUTH);
    }
}
