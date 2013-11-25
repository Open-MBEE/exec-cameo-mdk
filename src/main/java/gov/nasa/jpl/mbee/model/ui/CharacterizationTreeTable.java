package gov.nasa.jpl.mbee.model.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTreeTable;

public class CharacterizationTreeTable extends JXTreeTable {

    public CharacterizationTreeTable(CharacterizationTreeTableModel model) {
        super(model);
    }

    // gray out non-editable checkboxes
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        if (comp instanceof JCheckBox) {
            JCheckBox box = (JCheckBox)comp;

            if (!isCellEditable(row, column)) {
                box.setBackground(new Color(240, 240, 240));
                box.setEnabled(false);
                return box;
            }
        }
        return comp;
    }
}
