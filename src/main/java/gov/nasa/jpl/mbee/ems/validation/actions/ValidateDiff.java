package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ValidateDiff extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

	private static final long serialVersionUID = 1L;
	private Element element;
	private String webDoc;
	private String modelDoc;
    private JSONObject webData;
    private HashSet<String> keys = new HashSet<String>();

    private JList webList = new JList();
    private JList modelList = new JList();

    public ValidateDiff(Element element, JSONObject webData) {
    	this(element, "Web", "Model", webData, false);
    }
    
    public ValidateDiff(Element element, String web, String model, JSONObject webData, Boolean editable) {
        super("ValidateDiff", "Detail Diff", null, null);
        this.element = element;
        this.webDoc = web;
        this.modelDoc = model;
        this.webData = webData;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JSONObject modelData = ExportUtility.fillElement(element, null);

        keys.addAll(modelData.keySet());
        keys.addAll(webData.keySet());
        modelList = buildList(modelData);
        webList = buildList(webData);

        // add event listeners here
        // addListeners(modelList, webList);
        // addListeners(webList, modelList);

        JTabbedPane modelpane = buildPane("MD Model", modelList);
        JTabbedPane webpane = buildPane("MMS Web", webList);

        JSplitPane top = new JSplitPane();
        
        JDialog show = new JDialog();
        show.setTitle("Comparison");
        show.setSize(1200, 600);
        show.getContentPane().add(top);
        top.setResizeWeight(0.5);
        top.setDividerLocation(0.5);
        top.setLeftComponent(modelpane);
        top.setRightComponent(webpane);
        show.setVisible(true);
    }

    // private void addListeners(final JList source, final JList other) {
    //     source.addListSelectionListener(new ListSelectionListener() {
    //         @Override
    //         public void valueChanged(ListSelectionEvent listEvent) {
    //             if (!listEvent.getValueIsAdjusting()) {
    //                 // this does not handle things that well yet
    //                 int selected = source.getSelectedIndex();
    //                 other.setSelectedIndex(selected);
    //             }
    //         }
    //     });
    // }

    // class AttribCellRenderer extends JPanel implements ListCellRenderer {

    //     protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    //     @Override
    //     public Component getListCellRendererComponent(final JList list, final Object value, final int index,
    //             final boolean isSelected, final boolean hasFocus) {

    //         // JTextArea renderer = (JTextArea) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
    //         // return renderer;

    //     }

    // }

    private JList buildList(JSONObject data) {
        DefaultListModel<String> attribs = new DefaultListModel<String>();
        // final JList dataList = new JList(attribs) {
        //     @Override
        //     public boolean getScrollableTracksViewportWidth() {
        //         return true;
        //     }
        // };
        final JList dataList= new JList(attribs);
        // dataList.setCellRenderer(new AttribCellRenderer());
        // ComponentListener compListener = new ComponentAdapter() {
        //     @Override
        //     public void componentResized(ComponentEvent e) {
        //         dataList.setFixedCellHeight(10);
        //         dataList.setFixedCellHeight(-1);
        //     }
        // };
        System.out.println(data);
        // dataList.addComponentListener(compListener);
        for (Object o: keys) {
            String key = (String) o;
            Object value = data.get(key);
            String result = ""; 
            if (value != null) {
                attribs = addAttributes(attribs, key, value);
            }
            else {
                result += key + " (UNKNOWN) : [UNKNOWN]";
                attribs.addElement(result);
            }
        }
        return dataList;
    }

    private DefaultListModel<String> addAttributes(DefaultListModel<String> attribs, Object key, Object value) {
        return addAttributes(attribs, key, value, "");
    }

    private DefaultListModel<String> addAttributes(DefaultListModel<String> attribs, Object key, Object value, String level) {
        String datatype;
        if (value == null) {
            datatype = "UNKNOWN";
            System.out.println(key);
        } else {
            datatype = value.getClass().getSimpleName();
        }
        String result = (String)key + " (" + datatype + ") : ";
        if (value instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) value;
            // result += "{";
            attribs.addElement(level + key + " (JSONObject) ...");
            level += "-- ";
            for (Object objKey: jsonObj.keySet()) {
                // attribs.addElement("arg");
                // val could be null
                String subKey = (String) objKey;
                attribs = addAttributes(attribs, subKey, jsonObj.get(subKey), level);
                // Object val = jsonObj.get(subKey);
                // String subType = val.getClass().getSimpleName();
                // String subResult = String.valueOf(subKey) + " (" + String.valueOf(subType) + ") : ";
                // subResult += addAttributes(attribs, (String)subKey, jsonObj.get(subKey));
                // attribs.addElement(subResult);
            }
            // result = result.substring(0, result.length()-2) + "}";
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            attribs.addElement(level + key + "(JSONArray) ...");
            level += "-- ";
            for (int i = 0; i < array.size(); i++) {
                // String header = level + key;
                attribs = addAttributes(attribs, key, array.get(i), level);
                // String subResult = String.valueOf(array.get(i));
                // subResult += "[" + addAttributes(attribs, "", array.get(i)) + "]";
                // attribs.addElement(subResult);
            }
        } else if (value instanceof String) {
            String valString = (String) value;
            // try to get the element name from ID
            Element target = ExportUtility.getElementFromID(valString);
            if (target instanceof NamedElement) {
                NamedElement namedTarget = (NamedElement)target;
                result += '"' + (namedTarget.getName()) + '"';
            } else {
                result +=  '"' + valString + '"';
            }
            attribs.addElement(level + result);
        } else {
            result += String.valueOf(value);
            attribs.addElement(level + result);
        }
        return attribs;
    }

    private JTabbedPane buildPane(String name, JList dataList) {
        JTabbedPane pane = new JTabbedPane();
        JScrollPane scroll = new JScrollPane(dataList);
        scroll.setName(name);
        pane.addTab(name, scroll);
        return pane;
    }


}