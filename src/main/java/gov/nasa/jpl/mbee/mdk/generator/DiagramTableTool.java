package gov.nasa.jpl.mbee.mdk.generator;

/**
 * This class is copied from NoMagic's DiagramTableTool in diagramtabletool.jar
 * in magicdraw reportwizard's extensions. Since we're not using reportwizard,
 * the class is duplicated here for convenience I think 17.0.2 has a better api
 * for dealing with generic tables, should hunt for it
 */

import com.nomagic.diagramtable.Table;
import com.nomagic.diagramtable.TableManager;
import com.nomagic.diagramtable.TableUtils;
import com.nomagic.diagramtable.rows.DiagramTableRow;
import com.nomagic.generictable.GenericTableSettings;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.ui.jideui.PropertyColumn;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.lib.Debug;

import java.util.*;

public class DiagramTableTool {

    private GenericTableSettings genericTableSettings;
    private ElementRowNumberCache elementRowNumberCache;
    private List<DiagramPresentationElement> openedTables;


    public DiagramTableTool() {
        this.genericTableSettings = new GenericTableSettings();
        this.elementRowNumberCache = new ElementRowNumberCache();
        this.openedTables = new ArrayList();
    }

    public List<Element> getRowElements(Diagram tableDiagram) {
        List rowElements = new ArrayList();
        Table table = getTable(tableDiagram);

        List<? extends DiagramTableRow> rows = table.getRows();
        for (DiagramTableRow diagramTableRow : rows) {
            rowElements.add(diagramTableRow.getElement());
        }
        return rowElements.isEmpty() ? Collections.emptyList() : rowElements;
    }

    public String getColumnName(Diagram tableDiagram, String columnId) {
        Table table = getTable(tableDiagram);
        PropertyColumn[] propertyColumns = this.genericTableSettings.getPropertyColumns(table,
                Arrays.asList(columnId));
        for (PropertyColumn propertyColumn : propertyColumns) {
            if (propertyColumn.getId().equals(columnId)) {
                return propertyColumn.getColumnName();
            }
        }
        return "";
    }

    public List<String> getColumnNames(Diagram tableDiagram, List<String> columnIds) {
        Table table = getTable(tableDiagram);
        PropertyColumn[] propertyColumns = this.genericTableSettings.getPropertyColumns(table, columnIds);
        List names = new ArrayList(propertyColumns.length);
        for (PropertyColumn propertyColumn : propertyColumns) {
            names.add(propertyColumn.getColumnName());
        }
        return names;
    }

    public List<String> getColumnIds(Diagram tableDiagram) {
        Table table = getTable(tableDiagram);
        List<String> allColumnIds = TableUtils.getColumnIds(table);
        List visibleColumnIds = new ArrayList();
        for (String colId : allColumnIds) {
            if (TableUtils.isColumnHidden(table, colId)) {
                continue;
            }
            visibleColumnIds.add(colId);
        }

        return visibleColumnIds.isEmpty() ? Collections.emptyList() : visibleColumnIds;
    }

    private Table getTable(Diagram tableDiagram) {
        DiagramPresentationElement tableDiagramPresentation = Project.getProject(tableDiagram).getDiagram(
                tableDiagram);
        tableDiagramPresentation.ensureLoaded();
        Table table = TableManager.getTable(tableDiagram);
        if (table == null) {
            tableDiagramPresentation.open();
            table = TableManager.getTable(tableDiagram);
            this.openedTables.add(tableDiagramPresentation);
        }
        return table;
    }

    public Object getCellValue(Diagram tableDiagram, Element rowElement, String columnId) {
        Table table = getTable(tableDiagram);
        Integer elementRowNumber = this.elementRowNumberCache.getElementRowNumber(table, rowElement);
        if (elementRowNumber == null) {
            return null;
        }
        if ("_NUMBER_".equals(columnId)) {
            return elementRowNumber.toString();
        }
        DiagramTableRow diagramTableRow = table.getRows().get(
                elementRowNumber.intValue() - 1);
        PropertyColumn column = getColumnById(table, columnId);
        if (column == null) {
            return null;
        }
        return diagramTableRow.getColumnValue(column);
    }

    public PropertyColumn getColumnById(Diagram tableDiagram, String columnId) {
        return getColumnById(getTable(tableDiagram), columnId);
    }

    private PropertyColumn getColumnById(Table table, String columnId) {
        if (table != null) {
            List columnIds = new ArrayList(1);
            columnIds.add(columnId);
            PropertyColumn[] propertyColumns = this.genericTableSettings.getPropertyColumns(table, columnIds);
            for (PropertyColumn propertyColumn : propertyColumns) {
                if (propertyColumn.getId().equals(columnId)) {
                    return propertyColumn;
                }
            }
        }
        return null;
    }

    public String getStringCellValue(Diagram tableDiagram, Element rowElement, String columnId) {
        Object cellValue = getCellValue(tableDiagram, rowElement, columnId);
        if ((cellValue instanceof Object[])) {
            Object[] cellValues = (Object[]) cellValue;
            StringBuilder stringValues = new StringBuilder();

            for (int i = 0; i < cellValues.length; i++) {
                if ((cellValues[i] instanceof BaseElement)) {
                    stringValues.append(RepresentationTextCreator.getRepresentedText((Element) cellValues[i]));
                }
                else if ((cellValues[i] instanceof Property)) {
                    stringValues.append(((Property) cellValues[i]).getValueStringRepresentation());
                }
                else {
                    stringValues.append(cellValues[i].toString());
                }
                if (i + 1 >= cellValues.length) {
                    continue;
                }
                stringValues.append("\n");
                Debug.outln("cellValues[" + i + "] = " + cellValues[i]);
            }

            return stringValues.toString();
        }
        if ((cellValue instanceof Element)) {
            return RepresentationTextCreator.getRepresentedText((Element) cellValue);
        }
        return cellValue != null ? cellValue.toString() : "";
    }

    public void closeOpenedTables() {
        for (DiagramPresentationElement table : this.openedTables) {
            table.close();
        }
        this.openedTables.clear();
    }

    private class ElementRowNumberCache {
        private Table lastUsedTable;
        private Map<Element, Integer> elementRowNumberMap;

        private ElementRowNumberCache() {
        }

        public Integer getElementRowNumber(Table table, Element rowElement) {
            if ((table == null) || (this.lastUsedTable != table)) {
                this.lastUsedTable = table;
                List rows = this.lastUsedTable.getRows();
                this.elementRowNumberMap = new HashMap(rows.size());

                for (int i = 0; i < rows.size(); i++) {
                    DiagramTableRow diagramTableRow = (DiagramTableRow) rows.get(i);
                    this.elementRowNumberMap.put(diagramTableRow.getElement(), Integer.valueOf(i + 1));
                }
            }
            return this.elementRowNumberMap.get(rowElement);
        }
    }
}
