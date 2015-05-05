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
package gov.nasa.jpl.mgss.mbee.docgen.table;

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import com.nomagic.magicdraw.uml2.util.UML2ModelUtil;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * properties table for docgen 3 this has a lot of methods that can probably be
 * optimized and make more efficient and less convoluted need to revisit once we
 * have a more generic graph collection/filter capability
 * 
 * @author dlam
 * 
 */
public class PropertiesTable {

    private List<Stereotype>                            splitStereotype         = null;
    private List<Stereotype>                            systemIncludeStereotype = null;
    private List<Stereotype>                            systemExcludeStereotype = null;
    private List<String>                                systemIncludeTypeName   = null;
    private List<String>                                systemExcludeTypeName   = null;
    private List<String>                                systemIncludeName       = null;
    private List<String>                                systemExcludeName       = null;
    private Integer                                     systemAssociationType   = null;
    private Boolean                                     consolidateTypes        = null;
    private Boolean                                     showMultiplicity        = null;

    private Integer                                     floatingPrecision       = null;
    private Integer                                     maxDepth                = null;
    private List<String>                                topIncludeTypeName      = null;
    private List<String>                                topExcludeTypeName      = null;
    private List<Stereotype>                            topIncludeStereotype    = null;
    private List<Stereotype>                            topExcludeStereotype    = null;
    private List<String>                                topIncludeName          = null;
    private List<String>                                topExcludeName          = null;
    private Integer                                     topAssociationType      = null;
    private List<String>                                topOrder                = null;
    private Boolean                                     includeInherited;

    private List<Element>                               targets;

    Map<Class, Map<Class, Map<Property, Class>>>        filteredStructures;
    private Map<Class, Map<Class, Map<Class, Integer>>> consolidated;
    private int                                         numPropertyHeaders;
    private List<String>                                colspecs;
    private List<List<Map<String, String>>>             headers;

    private EditableTable                               et;

    public EditableTable getEt() {
        return et;
    }

    public PropertiesTable(List<Stereotype> topIncludeStereotype, List<Stereotype> topExcludeStereotype,
            List<String> topIncludeName, List<String> topExcludeName, List<String> topIncludeTypeName,
            List<String> topExcludeTypeName, List<String> topOrder, List<Stereotype> systemIncludeStereotype,
            List<Stereotype> systemExcludeStereotype, List<String> systemIncludeName,
            List<String> systemExcludeName, List<String> systemIncludeTypeName,
            List<String> systemExcludeTypeName, List<Stereotype> splitStereotype, int maxDepth,
            int systemAssociationType, int topAssociationType, List<Element> targets, boolean includeInherited) {
        this.splitStereotype = splitStereotype;
        this.systemIncludeStereotype = systemIncludeStereotype;
        this.systemExcludeStereotype = systemExcludeStereotype;
        this.systemIncludeTypeName = systemIncludeTypeName;
        this.systemExcludeTypeName = systemExcludeTypeName;
        this.systemIncludeName = systemIncludeName;
        this.systemExcludeName = systemExcludeName;
        this.systemAssociationType = systemAssociationType;
        this.consolidateTypes = true;
        this.showMultiplicity = true;
        this.floatingPrecision = -1;
        this.maxDepth = maxDepth;
        this.topIncludeTypeName = topIncludeTypeName;
        this.topExcludeTypeName = topExcludeTypeName;
        this.topIncludeStereotype = topIncludeStereotype;
        this.topExcludeStereotype = topExcludeStereotype;
        this.topIncludeName = topIncludeName;
        this.topExcludeName = topExcludeName;
        this.topAssociationType = topAssociationType;
        this.topOrder = topOrder;
        this.targets = targets;
        this.includeInherited = includeInherited;
    }

    public void showTable() {
        et.showTable();
    }

    /**
     * makes all the necessary data structures and tables
     * 
     * @return
     */
    public void doMainThings() {
        filteredStructures = new HashMap<Class, Map<Class, Map<Property, Class>>>();
        List<Map<String, List<String>>> propertiesStructure = new ArrayList<Map<String, List<String>>>();
        Map<String, String> types = new HashMap<String, String>();
        List<Class> nptElements = new ArrayList<Class>();
        for (Element e: targets) {
            if (e instanceof Class)
                nptElements.add((Class)e);
        }
        int maxTreeDepth = 0;
        for (Element e: nptElements) {
            Map<Class, Map<Property, Class>> filteredStructure = new HashMap<Class, Map<Property, Class>>();
            populateAttributesStructures((Class)e, filteredStructure, propertiesStructure, types);
            int depth = getDepthOfCompositionalMap(filteredStructure, (Class)e);
            if (depth > maxTreeDepth)
                maxTreeDepth = depth;
            filteredStructures.put((Class)e, filteredStructure);
        }

        numPropertyHeaders = getNumPropertyHeaders(propertiesStructure);

        DefaultMutableTreeNode tree = sortPropertiesStructure(propertiesStructure);
        colspecs = new ArrayList<String>();
        getColSpecsFromTree(tree, colspecs);

        int columns = maxTreeDepth + numPropertyHeaders;
        List<List<Object>> table = new ArrayList<List<Object>>();

        List<List<PropertyEnum>> whatToShow = new ArrayList<List<PropertyEnum>>();
        List<List<Boolean>> editable = new ArrayList<List<Boolean>>();
        consolidated = new HashMap<Class, Map<Class, Map<Class, Integer>>>();
        consolidateTypes(filteredStructures, consolidated);
        if (this.consolidateTypes)
            populateTableModelConsolidate(table, whatToShow, editable, nptElements, consolidated, colspecs,
                    maxTreeDepth, columns);
        else
            populateTableModel(table, whatToShow, editable, nptElements, filteredStructures, colspecs,
                    maxTreeDepth, columns);

        headers = getHeadersFromTree(tree, types, propertiesStructure.size());

        List<String> etheaders = new ArrayList<String>();
        for (int i = 0; i < maxTreeDepth; i++)
            etheaders.add("");
        if (this.showMultiplicity)
            etheaders.add("Multiplicity");
        for (String colspec: colspecs)
            etheaders.add("<html>" + colspec.replaceAll(";", "<br/>") + "</html>");

        et = new EditableTable("Properties Table", table, etheaders, editable, whatToShow, floatingPrecision);
        et.prepareTable();
    }

    private void populateTableModel(List<List<Object>> tableModel, List<List<PropertyEnum>> whatToShow,
            List<List<Boolean>> editable, List<Class> systemElements,
            Map<Class, Map<Class, Map<Property, Class>>> systemStructure, List<String> colspecs,
            int maxTreeDepth, int columns) {
        Collections.sort(systemElements, new Comparator<Class>() {
            @Override
            public int compare(Class arg0, Class arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
        for (Class systemElement: systemElements) {
            populateTableModelRecursive(tableModel, whatToShow, editable, null, systemElement,
                    systemStructure.get(systemElement), colspecs, maxTreeDepth, columns, 1);
        }
    }

    private void populateTableModelRecursive(List<List<Object>> tableModel,
            List<List<PropertyEnum>> whatToShow, List<List<Boolean>> editable, Property systemProperty,
            Class systemElement, Map<Class, Map<Property, Class>> systemStructure, List<String> colspecs,
            int maxTreeDepth, int columns, int curDepth) {
        List<Object> row = new ArrayList<Object>();
        List<PropertyEnum> what = new ArrayList<PropertyEnum>();
        List<Boolean> edits = new ArrayList<Boolean>();
        if (curDepth == 1) {
            row.add(systemElement);
            edits.add(true);
            for (int i = 1; i < maxTreeDepth; i++) {
                row.add("");
                edits.add(false);
            }
        } else {
            for (int i = 0; i < curDepth - 1; i++) {
                row.add("");
                edits.add(false);
            }
            row.add(systemProperty);
            edits.add(true);
            for (int i = curDepth; i < maxTreeDepth; i++) {
                row.add("");
                edits.add(false);
            }
        }
        for (int i = 0; i < maxTreeDepth; i++)
            what.add(PropertyEnum.NAME);
        if (this.showMultiplicity) {
            if (curDepth > 1)
                row.add(Utils.getMultiplicity(systemProperty));
            else
                row.add("");
            what.add(PropertyEnum.NAME);
            edits.add(false);
        }
        for (String colspec: colspecs) {
            row.add(getPropertyElement(systemElement, Arrays.asList(colspec.split(";")), includeInherited));
            what.add(PropertyEnum.VALUE);
            edits.add(true);
        }

        tableModel.add(row);
        whatToShow.add(what);
        editable.add(edits);
        Map<Property, Class> children = systemStructure.get(systemElement);
        List<Property> sortedProperties = sortProperty(new ArrayList<Property>(children.keySet()));
        for (Property p: sortedProperties) {
            populateTableModelRecursive(tableModel, whatToShow, editable, p, children.get(p),
                    systemStructure, colspecs, maxTreeDepth, columns, curDepth + 1);
        }
    }

    private void populateTableModelConsolidate(List<List<Object>> tableModel,
            List<List<PropertyEnum>> whatToShow, List<List<Boolean>> editable, List<Class> systemElements,
            Map<Class, Map<Class, Map<Class, Integer>>> consolidated, List<String> colspecs,
            int maxTreeDepth, int columns) {
        Collections.sort(systemElements, new Comparator<Class>() {
            @Override
            public int compare(Class arg0, Class arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
        for (Class systemElement: systemElements) {
            populateTableModelConsolidateRecursive(tableModel, whatToShow, editable, systemElement,
                    consolidated.get(systemElement), null, colspecs, maxTreeDepth, columns, 1);
        }
    }

    private void populateTableModelConsolidateRecursive(List<List<Object>> tableModel,
            List<List<PropertyEnum>> whatToShow, List<List<Boolean>> editable, Class systemElement,
            Map<Class, Map<Class, Integer>> consolidate, Map<Class, Integer> units, List<String> colspecs,
            int maxTreeDepth, int columns, int curDepth) {
        List<Object> row = new ArrayList<Object>();
        List<PropertyEnum> what = new ArrayList<PropertyEnum>();
        List<Boolean> edits = new ArrayList<Boolean>();

        for (int i = 0; i < curDepth - 1; i++) {
            row.add("");
            edits.add(false);
        }
        row.add(systemElement);
        edits.add(true);
        for (int i = curDepth; i < maxTreeDepth; i++) {
            row.add("");
            edits.add(false);
        }

        for (int i = 0; i < maxTreeDepth; i++)
            what.add(PropertyEnum.NAME);
        if (this.showMultiplicity) {
            if (curDepth > 1)
                row.add(units.get(systemElement));
            else
                row.add("");
            what.add(PropertyEnum.NAME);
            edits.add(false);
        }
        for (String colspec: colspecs) {
            row.add(getPropertyElement(systemElement, Arrays.asList(colspec.split(";")), includeInherited));
            what.add(PropertyEnum.VALUE);
            edits.add(true);
        }

        tableModel.add(row);
        whatToShow.add(what);
        editable.add(edits);
        Map<Class, Integer> children = consolidate.get(systemElement);
        List<Class> childrenTypes = new ArrayList<Class>(children.keySet());
        Collections.sort(childrenTypes, new Comparator<Class>() {
            @Override
            public int compare(Class arg0, Class arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
        for (Class p: childrenTypes) {
            populateTableModelConsolidateRecursive(tableModel, whatToShow, editable, p, consolidate,
                    children, colspecs, maxTreeDepth, columns, curDepth + 1);
        }
    }

    static class PropertyNameComparator implements Comparator<Property> {
        @Override
        public int compare(Property arg0, Property arg1) {
            return arg0.getName().compareTo(arg1.getName());
        }
    }

    public static List<Property> sortProperty(List<Property> props) {
        Collections.sort(props, new PropertyNameComparator());
        return props;
    }

    public static int getDepthOfCompositionalMap(Map<Class, Map<Property, Class>> map, Class c) {
        return getDepthOfCompositionalMapRecursive(map, 1, c);
    }

    private static int getDepthOfCompositionalMapRecursive(Map<Class, Map<Property, Class>> map, int cur,
            Class c) {
        int max = cur;
        Map<Property, Class> children = map.get(c);
        for (Map.Entry<Property, Class> entry: children.entrySet()) {
            int branch = getDepthOfCompositionalMapRecursive(map, cur + 1, entry.getValue());
            if (branch > max)
                max = branch;
        }
        return max;
    }

    private void populateAttributesStructures(Class root, Map<Class, Map<Property, Class>> filteredStructure,
            List<Map<String, List<String>>> propertiesStructure, Map<String, String> types) {

        boolean getall = false;
        boolean checkExclude = false;
        if (systemIncludeStereotype.isEmpty() && systemIncludeName.isEmpty()
                && systemIncludeTypeName.isEmpty()) {
            if (systemExcludeStereotype.isEmpty() && systemExcludeName.isEmpty()
                    && systemExcludeTypeName.isEmpty())
                getall = true;
            else
                checkExclude = true;
        }
        populateAttributes(root, filteredStructure, propertiesStructure, types, getall, checkExclude, 1,
                new HashSet<String>(), null);
    }

    private void populateAttributes(Class root, Map<Class, Map<Property, Class>> filteredStructure,
            List<Map<String, List<String>>> propertiesStructure, Map<String, String> types, boolean getall,
            boolean checkExclude, int curDepth, Set<String> propIgnore, Element from) {

        if (maxDepth > 0 && curDepth > maxDepth)
            return;

        // printToLog("populating " + root.getQualifiedName());
        Map<Property, Class> children = new HashMap<Property, Class>();
        List<Property> iterate = new ArrayList<Property>(root.getOwnedAttribute());
        if (includeInherited)
            iterate.addAll(getInheritedProperties(root));
        for (Property attr: iterate) {
            Type attrType = attr.getType();
            if (attrType != null && attrType == from)
                continue;
            if (attrType != null && attrType instanceof Class) {
                if (StereotypesHelper.hasStereotypeOrDerived(attrType, splitStereotype)
                        || StereotypesHelper.hasStereotypeOrDerived(attr, splitStereotype)) {
                    if (topIncludeStereotype.isEmpty() && topIncludeName.isEmpty()
                            && topIncludeTypeName.isEmpty()) {
                        if (topExcludeStereotype.isEmpty() && topExcludeName.isEmpty()
                                && topExcludeTypeName.isEmpty()) {
                            populateTopStructure(attr, propertiesStructure, types, 0, "", true, false,
                                    propIgnore, null);
                        } else {
                            populateTopStructure(attr, propertiesStructure, types, 0, "", false, true,
                                    propIgnore, null);
                        }
                    } else {
                        populateTopStructure(attr, propertiesStructure, types, 0, "", false, false,
                                propIgnore, null);
                    }
                    continue;
                }
                if ((systemAssociationType == 1 && (attr.getAggregation() == AggregationKindEnum.SHARED || attr
                        .getAggregation() == AggregationKindEnum.NONE))
                        || (systemAssociationType == 2 && (attr.getAggregation() == AggregationKindEnum.COMPOSITE || attr
                                .getAggregation() == AggregationKindEnum.NONE)))
                    continue;
                if (StereotypesHelper.hasStereotypeOrDerived(attr, systemExcludeStereotype)
                        || StereotypesHelper.hasStereotypeOrDerived(attrType, systemExcludeStereotype)
                        || systemExcludeName.contains(attr.getName())
                        || systemExcludeTypeName.contains(attrType.getName()))
                    continue;
                if (getall || checkExclude
                        || StereotypesHelper.hasStereotypeOrDerived(attr, systemIncludeStereotype)
                        || StereotypesHelper.hasStereotypeOrDerived(attrType, systemIncludeStereotype)
                        || systemIncludeName.contains(attr.getName())
                        || systemIncludeTypeName.contains(attrType.getName())) {
                    children.put(attr, (Class)attrType);
                    populateAttributes((Class)attrType, filteredStructure, propertiesStructure, types,
                            getall, true, curDepth + 1, propIgnore, root);
                    continue;
                }
                if (includeAttribute((Class)attrType, root)) {
                    children.put(attr, (Class)attrType);
                    populateAttributes((Class)attrType, filteredStructure, propertiesStructure, types,
                            getall, checkExclude, curDepth + 1, propIgnore, root);
                    continue;
                }
            } else {
                if (topIncludeStereotype.isEmpty() && topIncludeName.isEmpty()
                        && topIncludeTypeName.isEmpty()) {
                    if (topExcludeStereotype.isEmpty() && topExcludeName.isEmpty()
                            && topExcludeTypeName.isEmpty()) {
                        populateTopStructure(attr, propertiesStructure, types, 0, "", true, false,
                                propIgnore, null);
                    } else {
                        populateTopStructure(attr, propertiesStructure, types, 0, "", false, true,
                                propIgnore, null);
                    }
                } else {
                    populateTopStructure(attr, propertiesStructure, types, 0, "", false, false, propIgnore,
                            null);
                }
            }
        }
        if (maxDepth > 0 && curDepth + 1 > maxDepth)
            filteredStructure.put(root, new HashMap<Property, Class>());
        else
            filteredStructure.put(root, children);
    }

    private boolean includeAttribute(Class system, Element from) {
        List<Property> iterate = new ArrayList<Property>(system.getOwnedAttribute());
        if (includeInherited)
            iterate.addAll(getInheritedProperties(system));
        for (Property attr: iterate) {
            Type attrType = attr.getType();
            if (attrType != null && attrType == from)
                continue;
            if (attrType != null && attrType instanceof Class) {
                if (StereotypesHelper.hasStereotypeOrDerived(attrType, splitStereotype)
                        || StereotypesHelper.hasStereotypeOrDerived(attr, splitStereotype))
                    continue;
                if ((systemAssociationType == 1 && (attr.getAggregation() == AggregationKindEnum.SHARED || attr
                        .getAggregation() == AggregationKindEnum.NONE))
                        || (systemAssociationType == 2 && (attr.getAggregation() == AggregationKindEnum.COMPOSITE || attr
                                .getAggregation() == AggregationKindEnum.NONE)))
                    continue;
                if (StereotypesHelper.hasStereotypeOrDerived(attr, systemExcludeStereotype)
                        || StereotypesHelper.hasStereotypeOrDerived(attrType, systemExcludeStereotype)
                        || systemExcludeName.contains(attr.getName())
                        || systemExcludeTypeName.contains(attrType.getName()))
                    continue;
                if (StereotypesHelper.hasStereotypeOrDerived(attr, systemIncludeStereotype)
                        || StereotypesHelper.hasStereotypeOrDerived(attrType, systemIncludeStereotype)
                        || systemIncludeName.contains(attr.getName())
                        || systemIncludeTypeName.contains(attrType.getName()))
                    return true;
                if (includeAttribute((Class)attrType, system))
                    return true;
            }
        }
        return false;
    }

    /**
     * [ {"prop1a":["prop2a", "prop2b", "c"], "prop1b":["d", "e"], "prop1c":[]},
     * {"prop1a;prop2a":["f", "g"], "prop1a;prop2b":["m", "n"], "prop1a;c":[],
     * "prop1b;d":[], "prop1b;e":[]}, {"prop1a;prop2a;f":[],
     * "prop1a;prop2a;g":[], "prop1a;prop2b;m":[], "prop1a;prop2b;n":[]} ] use
     * semicolon for delimiters
     */
    private boolean populateTopStructure(Property systemProperty,
            List<Map<String, List<String>>> propertiesStructure, Map<String, String> types, int curDepth,
            String prefix, boolean getall, boolean checkExclude, Set<String> propIgnore, Element from) {
        // if no filters are set, getall should be true
        // if no includes are set, checkExclude should be true
        String propName = systemProperty.getName();
        if (curDepth > 0)
            propName = prefix + ";" + propName;
        if (propIgnore.contains(propName))
            return false;
        // printToLog("populatePropertiesStructure " +
        // systemProperty.getQualifiedName());
        Type propType = systemProperty.getType();
        if (propType != null && propType == from)
            return false;
        Map<String, List<String>> propMap = null;
        if (propertiesStructure.size() == curDepth) {
            propMap = new HashMap<String, List<String>>();
            propertiesStructure.add(propMap);
        } else
            propMap = propertiesStructure.get(curDepth);
        List<String> children = propMap.get(propName);
        boolean newProp = false;
        if (children == null) {
            children = new ArrayList<String>();
            propMap.put(propName, children);
            newProp = true;
        }

        // check association type
        if ((topAssociationType == 1 && (systemProperty.getAggregation() == AggregationKindEnum.SHARED || systemProperty
                .getAggregation() == AggregationKindEnum.NONE))
                || (topAssociationType == 2 && (systemProperty.getAggregation() == AggregationKindEnum.COMPOSITE || systemProperty
                        .getAggregation() == AggregationKindEnum.NONE))) {
            if (newProp) {
                propMap.remove(propName);
                propIgnore.add(propName);
            }
            return false;
        }

        // get everything from here on
        if (getall) {
            types.put(propName, propType == null ? "" : propType.getName());
            if (propType != null && propType instanceof Class) {
                List<Property> iterate = new ArrayList<Property>(((Class)propType).getOwnedAttribute());
                if (includeInherited)
                    iterate.addAll(getInheritedProperties((Class)propType));
                for (Property p: iterate) {
                    if (populateTopStructure(p, propertiesStructure, types, curDepth + 1, propName, getall,
                            checkExclude, propIgnore, systemProperty.getOwner()))
                        if (!children.contains(p.getName()))
                            children.add(p.getName());
                }
            }
            return true;
        }

        // check excludes
        if (StereotypesHelper.hasStereotypeOrDerived(systemProperty, topExcludeStereotype)
                || propType != null
                && StereotypesHelper.hasStereotypeOrDerived(propType, topExcludeStereotype)
                || topExcludeName.contains(systemProperty.getName()) || propType != null
                && topExcludeTypeName.contains(propType.getName())) {
            if (newProp) {// in the chance that the model is messed up....some
                          // are included before and now it's exlcude becuase of
                          // missing stereotypes, etc
                propMap.remove(propName);
                propIgnore.add(propName);
            }
            return false;
        }

        // include until exclude
        if (checkExclude) {
            types.put(propName, propType == null ? "" : propType.getName());
            if (propType != null && propType instanceof Class) {
                List<Property> iterate = new ArrayList<Property>(((Class)propType).getOwnedAttribute());
                if (includeInherited)
                    iterate.addAll(getInheritedProperties((Class)propType));
                for (Property p: iterate) {
                    if (populateTopStructure(p, propertiesStructure, types, curDepth + 1, propName, getall,
                            checkExclude, propIgnore, systemProperty.getOwner())) {
                        if (!children.contains(p.getName())) {
                            children.add(p.getName());
                        }
                    }
                }
            }
            return true;
        }
        // check include
        if (StereotypesHelper.hasStereotypeOrDerived(systemProperty, topIncludeStereotype)
                || propType != null
                && StereotypesHelper.hasStereotypeOrDerived(propType, topIncludeStereotype)
                || topIncludeName.contains(systemProperty.getName()) || propType != null
                && topIncludeTypeName.contains(propType.getName())) {
            types.put(propName, propType == null ? "" : propType.getName());
            if (topExcludeName.isEmpty() && topExcludeTypeName.isEmpty() && topExcludeStereotype.isEmpty()) {
                // no exclude filters, include everything
                if (propType != null && propType instanceof Class) {
                    List<Property> iterate = new ArrayList<Property>(((Class)propType).getOwnedAttribute());
                    if (includeInherited)
                        iterate.addAll(getInheritedProperties((Class)propType));
                    for (Property p: iterate) {
                        if (populateTopStructure(p, propertiesStructure, types, curDepth + 1, propName, true,
                                false, propIgnore, systemProperty.getOwner()))
                            if (!children.contains(p.getName()))
                                children.add(p.getName());
                    }
                }
            } else {
                // need to check for excludes for children, but will include
                // stuff if exclude doesn't match
                if (propType != null && propType instanceof Class) {
                    List<Property> iterate = new ArrayList<Property>(((Class)propType).getOwnedAttribute());
                    if (includeInherited)
                        iterate.addAll(getInheritedProperties((Class)propType));
                    for (Property p: iterate) {
                        if (populateTopStructure(p, propertiesStructure, types, curDepth + 1, propName,
                                false, true, propIgnore, systemProperty.getOwner())) {
                            if (!children.contains(p.getName())) {
                                children.add(p.getName());
                            }
                        }
                    }
                }
            }
            return true;
        }

        // check if this is part of a branch that should be included
        if (includeTop(systemProperty, from)) {
            types.put(propName, propType == null ? "" : propType.getName());
            if (propType != null && propType instanceof Class) {
                List<Property> iterate = new ArrayList<Property>(((Class)propType).getOwnedAttribute());
                if (includeInherited)
                    iterate.addAll(getInheritedProperties((Class)propType));
                for (Property p: iterate) {
                    if (populateTopStructure(p, propertiesStructure, types, curDepth + 1, propName, getall,
                            checkExclude, propIgnore, systemProperty.getOwner())) {
                        if (!children.contains(p.getName())) {
                            children.add(p.getName());
                        }
                    }
                }
            }
            return true;
        }
        if (newProp) {
            propMap.remove(propName);
            propIgnore.add(propName);
        }
        return false;
    }

    private boolean includeTop(Property systemProperty, Element from) {
        // systemProperty has already been checked by populateTopStructure and
        // it's unclear, need to check its children
        if ((topAssociationType == 1 && (systemProperty.getAggregation() == AggregationKindEnum.SHARED || systemProperty
                .getAggregation() == AggregationKindEnum.NONE))
                || (topAssociationType == 2 && (systemProperty.getAggregation() == AggregationKindEnum.COMPOSITE || systemProperty
                        .getAggregation() == AggregationKindEnum.NONE)))
            return false;

        Type propType = systemProperty.getType();
        if (propType != null && propType == from)
            return false;

        // check for exclude, then include, then recurse
        if (StereotypesHelper.hasStereotypeOrDerived(systemProperty, topExcludeStereotype)
                || propType != null
                && StereotypesHelper.hasStereotypeOrDerived(propType, topExcludeStereotype)
                || topExcludeName.contains(systemProperty.getName()) || propType != null
                && topExcludeTypeName.contains(propType.getName()))
            return false;
        if (StereotypesHelper.hasStereotypeOrDerived(systemProperty, topIncludeStereotype)
                || propType != null
                && StereotypesHelper.hasStereotypeOrDerived(propType, topIncludeStereotype)
                || topIncludeName.contains(systemProperty.getName()) || propType != null
                && topIncludeTypeName.contains(propType.getName()))
            return true;

        if (propType != null && propType instanceof Class) {
            List<Property> iterate = new ArrayList<Property>(((Class)propType).getOwnedAttribute());
            if (includeInherited)
                iterate.addAll(getInheritedProperties((Class)propType));
            for (Property p: iterate) {
                if (includeTop(p, systemProperty.getOwner()))
                    return true;
            }
        }
        return false;
    }

    public static String getPropertyValue(Class e, List<String> propSpec, boolean includeInherited) {
        Property p = getPropertyElement(e, propSpec, includeInherited);
        if (p == null)
            return "n/a";
        return UML2ModelUtil.getDefault(p);
    }

    public static Property getPropertyElement(Class e, List<String> propSpec, boolean includeInherited) {
        List<Property> iterate = new ArrayList<Property>(e.getOwnedAttribute());
        if (includeInherited)
            iterate.addAll(getInheritedProperties(e));
        if (propSpec.size() < 2) {
            for (Property p: iterate) {
                if (p.getName().equals(propSpec.get(0)))
                    return p;
            }
            return null;
        }
        for (Property p: iterate) {
            if (p.getName().equals(propSpec.get(0)) && p.getType() != null && p.getType() instanceof Class)
                return getPropertyElement((Class)p.getType(), propSpec.subList(1, propSpec.size()),
                        includeInherited);
        }
        return null;
    }

    private int getNumPropertyHeaders(List<Map<String, List<String>>> struct) {
        int res = 0;
        for (Map<String, List<String>> level: struct) {
            for (Map.Entry<String, List<String>> entry: level.entrySet()) {
                if (entry.getValue().isEmpty())
                    res++;
            }
        }
        return res;
    }

    private DefaultMutableTreeNode sortPropertiesStructure(List<Map<String, List<String>>> propertiesStructure) {
        DefaultMutableTreeNode res = new DefaultMutableTreeNode();
        if (propertiesStructure.isEmpty())
            return res;
        Map<String, List<String>> row = propertiesStructure.get(0);
        sortPropertiesStructureRecursive(res, propertiesStructure, 0, row.keySet(), "");
        return res;
    }

    private void sortPropertiesStructureRecursive(DefaultMutableTreeNode parentNode,
            List<Map<String, List<String>>> propertiesStructure, int curDepth, Set<String> props,
            String prefix) {
        Map<String, List<String>> row = propertiesStructure.get(curDepth);
        List<String> emptyProp = new ArrayList<String>();
        List<String> drillProp = new ArrayList<String>();
        for (String key: props) {
            String mapKey = key;
            if (!prefix.equals(""))
                mapKey = prefix + ";" + key;
            if (row.get(mapKey).isEmpty())
                emptyProp.add(key);
            else
                drillProp.add(key);
        }
        List<String> emptyUserAlphaSorted = sortUserInput(emptyProp);
        List<String> drillUserAlphaSorted = sortUserInput(drillProp);
        for (String s: emptyUserAlphaSorted) {
            String mapKey = s;
            if (!prefix.equals(""))
                mapKey = prefix + ";" + s;
            DefaultMutableTreeNode child = new DefaultMutableTreeNode();
            child.setUserObject(mapKey);
            parentNode.add(child);
        }
        for (String s: drillUserAlphaSorted) {
            String mapKey = s;
            if (!prefix.equals(""))
                mapKey = prefix + ";" + s;
            DefaultMutableTreeNode child = new DefaultMutableTreeNode();
            child.setUserObject(mapKey);
            parentNode.add(child);
            sortPropertiesStructureRecursive(child, propertiesStructure, curDepth + 1, new HashSet<String>(
                    row.get(mapKey)), mapKey);
        }
    }

    private List<String> sortUserInput(Collection<String> strings) {
        List<String> res = new ArrayList<String>();
        for (String user: topOrder) {
            if (strings.contains(user)) {
                res.add(user);
            }
        }
        List<String> alpha = new ArrayList<String>();
        for (String prop: strings) {
            if (!res.contains(prop))
                alpha.add(prop);
        }
        Collections.sort(alpha);
        res.addAll(alpha);
        return res;
    }

    private List<List<Map<String, String>>> getHeadersFromTree(DefaultMutableTreeNode tree,
            Map<String, String> types, int headerDepth) {
        List<List<Map<String, String>>> res = new ArrayList<List<Map<String, String>>>();
        getHeadersFromTreeRecursive(tree, types, res, 1, headerDepth);
        return res;
    }

    @SuppressWarnings("rawtypes")
    private void getHeadersFromTreeRecursive(DefaultMutableTreeNode tree, Map<String, String> types,
            List<List<Map<String, String>>> res, int curDepth, int headerDepth) {
        Enumeration children = tree.children();
        if (!children.hasMoreElements())
            return;
        List<Map<String, String>> props = null;
        if (res.size() < curDepth) {
            props = new ArrayList<Map<String, String>>();
            res.add(props);
        } else
            props = res.get(curDepth - 1);
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
            Map<String, String> propDetail = new HashMap<String, String>();
            String propQName = (String)child.getUserObject();
            String[] blah = propQName.split(";");
            String propName = blah[blah.length - 1];
            propDetail.put("name", propName);
            if (!child.children().hasMoreElements() && curDepth < headerDepth)
                propDetail.put("morerows", Integer.toString(headerDepth - curDepth));
            if (child.children().hasMoreElements()) {
                String namest = findSpanStartTree(child);
                String nameend = findSpanEndTree(child);
                if (!namest.equals(nameend)) {
                    propDetail.put("namest", namest);
                    propDetail.put("nameend", nameend);
                }
            }
            propDetail.put("type", types.get(propQName));
            props.add(propDetail);
            getHeadersFromTreeRecursive(child, types, res, curDepth + 1, headerDepth);
        }
    }

    private String findSpanStartTree(DefaultMutableTreeNode tree) {
        if (tree.getChildCount() > 0) {
            return findSpanStartTree((DefaultMutableTreeNode)tree.getFirstChild());
        }
        return (String)tree.getUserObject();
    }

    private String findSpanEndTree(DefaultMutableTreeNode tree) {
        if (tree.getChildCount() > 0) {
            return findSpanEndTree((DefaultMutableTreeNode)tree.getLastChild());
        }
        return (String)tree.getUserObject();
    }

    @SuppressWarnings("rawtypes")
    private void getColSpecsFromTree(DefaultMutableTreeNode tree, List<String> res) {
        Enumeration children = tree.children();
        if (!children.hasMoreElements() && tree.getUserObject() != null)
            res.add((String)tree.getUserObject());
        while (children.hasMoreElements()) {
            getColSpecsFromTree((DefaultMutableTreeNode)children.nextElement(), res);
        }
    }

    public static void consolidateTypes(Map<Class, Map<Class, Map<Property, Class>>> structures,
            Map<Class, Map<Class, Map<Class, Integer>>> typeUnits) {
        for (Class top: structures.keySet()) {
            Map<Class, Map<Property, Class>> structure = structures.get(top);
            if (!typeUnits.containsKey(top))
                typeUnits.put(top, new HashMap<Class, Map<Class, Integer>>());
            Map<Class, Map<Class, Integer>> typeunit = typeUnits.get(top);
            for (Class c: structure.keySet()) {
                if (!typeunit.containsKey(c))
                    typeunit.put(c, new HashMap<Class, Integer>());
                Map<Class, Integer> units = typeunit.get(c);
                Map<Property, Class> props = structure.get(c);
                for (Property p: props.keySet()) {
                    Class type = props.get(p);
                    if (!units.containsKey(type))
                        units.put(type, getMultiplicity(p));
                    else
                        units.put(type, units.get(type) + getMultiplicity(p));
                }
            }
        }
    }

    public static int getMultiplicity(Property p) {
        int lower = p.getLower();
        int upper = p.getUpper();
        if (lower == upper)
            return lower;
        if (upper == -1 && lower > 0)
            return lower;
        return 1;
    }

    public static List<Property> getInheritedProperties(Class c) {
        List<Property> owned = new ArrayList<Property>(c.getOwnedAttribute());
        Collection<NamedElement> inherited = new ArrayList<NamedElement>(c.getInheritedMember());
        List<NamedElement> inheritedCopy = new ArrayList<NamedElement>(inherited);
        List<Property> res = new ArrayList<Property>();
        for (NamedElement ne: inherited) {
            if (ne instanceof Property) {
                for (Property redef: ((Property)ne).getRedefinedProperty()) {
                    inheritedCopy.remove(redef);
                }
            } else
                inheritedCopy.remove(ne);
        }
        for (Property p: owned) {
            for (Property redef: p.getRedefinedProperty()) {
                inheritedCopy.remove(redef);
            }
        }
        for (NamedElement e: inheritedCopy)
            res.add((Property)e);
        return res;
    }

    public Map<Class, Map<Class, Map<Property, Class>>> getFilteredStructures() {
        return filteredStructures;
    }

    public Map<Class, Map<Class, Map<Class, Integer>>> getConsolidated() {
        return consolidated;
    }

    public int getNumPropertyHeaders() {
        return numPropertyHeaders;
    }

    public List<String> getColspecs() {
        return colspecs;
    }

    public List<List<Map<String, String>>> getHeaders() {
        return headers;
    }
}
