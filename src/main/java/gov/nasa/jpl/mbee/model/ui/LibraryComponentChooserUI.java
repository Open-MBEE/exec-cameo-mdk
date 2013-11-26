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
package gov.nasa.jpl.mbee.model.ui;

import gov.nasa.jpl.mbee.model.MissionMapping;
import gov.nasa.jpl.mbee.tree.Node;

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

    private static final JFrame                   frame       = new JFrame();
    private static Node<String, MissionComponent> missionNode;
    private static JList                          list        = new JList();
    private static JScrollPane                    scrollPane  = new JScrollPane(list);
    private static Object[]                       sortedComps = null;
    private static final JButton                  save        = new JButton("Save");
    private static final JButton                  cancel      = new JButton("Cancel");

    public LibraryComponentChooserUI(Node<String, MissionComponent> node, MissionMapping mapping) {

        missionNode = node;
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
            private static final long serialVersionUID = -5354413965998806550L;

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
