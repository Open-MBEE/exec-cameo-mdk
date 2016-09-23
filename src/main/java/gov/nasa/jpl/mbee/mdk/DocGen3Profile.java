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
package gov.nasa.jpl.mbee.mdk;


public class DocGen3Profile {
    public static final String sectionStereotype = "Dynamic View";
    public static final String paragraphStereotype = "Paragraph";
    public static final String documentStereotype = "zzDocument";
    public static final String imageStereotype = "Image";
    public static final String nextStereotype = "Next";
    public static final String firstStereotype = "First";
    public static final String oldQueriesStereotype = "Queries";
    public static final String queriesStereotype = "Expose";
    public static final String nosectionStereotype = "NoSection";
    public static final String appendixStereotype = "zzAppendix";
    public static final String viewpointStereotype = "Viewpoint";
    public static final String viewStereotype = "View";
    public static final String dgviewStereotype = "view";
    public static final String oldDgviewStereotype = "DGView";
    public static final String appendixViewStereotype = "AppendixView";
    public static final String structuredQueryStereotype = "StructuredQuery";
    public static final String tableStereotype = "Table";
    public static final String bulletedListStereotype = "BulletedList";
    public static final String temporalDiffStereotype = "TemporalDiff";
    public static final String combinedMatrixStereotype = "zzCombinedMatrix";
    public static final String methodStereotype = "Method";

    public static final String customTableStereotype = "zzCustomTable";
    public static final String tableStructureStereotype = "TableStructure";
    public static final String tablePropertyColumnStereotype = "TablePropertyColumn";
    public static final String tableExpressionColumnStereotype = "TableExpressionColumn";
    public static final String tableAttributeColumnStereotype = "TableAttributeColumn";
    public static final String tableSumRowStereotype = "TableSumRow";
    public static final String tableColumnStereotype = "TableColumn";

    public static final String dependencyMatrixStereotype = "zzDependencyMatrix";
    public static final String genericTableStereotype = "GenericTable";
    public static final String hierarchicalPropertiesTableStereotype = "HierarchicalPropertiesTable";
    public static final String propertiesTableByAttributesStereotype = "PropertiesTableByAttributes";

    public static final String viewpointConstraintStereotype = "ViewpointConstraint";
    public static final String userScriptStereotype = "UserScript";
    public static final String userScriptCFStereotype = "Collect/Filter UserScript";
    public static final String validationScriptStereotype = "ValidationScript";
    public static final String missionMappingStereotype = "MissionMapping";
    public static final String libraryChooserStereotype = "LibraryMapping";
    public static final String javaExtensionStereotype = "JavaExtension";

    public static final String collectionStereotype = "CollectionAndFilterGroup";
    public static final String documentMetaStereotype = "DocumentMeta";
    public static final String documentViewStereotype = "Product";
    public static final String accountableForStereotype = "accountableFor";
    public static final String approvesStereotype = "approves";
    public static final String concursStereotype = "concurs";
    public static final String projectStaffStereotype = "ProjectStaff";
    public static final String roleStereotype = "Role";

    public static final String filterDiagramTypeStereotype = "FilterByDiagramType";
    public static final String filterNameStereotype = "FilterByNames";
    public static final String filterMetaclassStereotype = "FilterByMetaclasses";
    public static final String filterStereotypeStereotype = "FilterByStereotypes";
    public static final String filterExpressionStereotype = "FilterByExpression";
    public static final String collectRelMetaclassStereotype = "CollectByDirectedRelationshipMetaclasses";
    public static final String collectRelStereotypeStereotype = "CollectByDirectedRelationshipStereotypes";
    public static final String collectStereotypePropStereotype = "CollectByStereotypeProperties";
    public static final String collectOwnedElementStereotype = "CollectOwnedElements";
    public static final String collectOwnerStereotype = "CollectOwners";
    public static final String collectAssociationStereotype = "CollectByAssociation";
    public static final String collectTypeStereotype = "CollectTypes";
    public static final String collectDiagram = "CollectThingsOnDiagram";
    public static final String collectClassifierAttributes = "CollectClassifierAttributes";
    public static final String collectExpressionStereotype = "CollectByExpression";

    public static final String associationChoosable = "AssociationTypeChoosable";
    public static final String depthChoosable = "DepthChoosable";
    public static final String derivedChoosable = "ConsiderDerivedChoosable";
    public static final String directionChoosable = "DirectionChoosable";
    public static final String stereotypeChoosable = "StereotypeChoosable";
    public static final String metaclassChoosable = "MetaclassChoosable";
    public static final String includeChoosable = "IncludeChoosable";
    public static final String nameChoosable = "NameChoosable";
    public static final String diagramTypeChoosable = "DiagramTypeChoosable";
    public static final String expressionChoosable = "ExpressionChoosable";
    public static final String expression = "Expression";
    public static final String sortable = "Sortable";
    public static final String propertyChoosable = "PropertyChoosable";
    public static final String attributeChoosable = "AttributeChoosable";
    public static final String editableChoosable = "EditableChoosable";
    public static final String expressionLibrary = "ExpressionLibrary";

    public static final String stereotypePropertyChoosable = "StereotypePropertiesChoosable";
    public static final String documentationChoosable = "DocumentationChoosable";
    public static final String headersChoosable = "HeadersChoosable";
    public static final String stereotypedRelChoosable = "StereotypedRelationshipsChoosable";
    public static final String precisionChoosable = "PrecisionChoosable";
    public static final String inheritedChoosable = "IncludeInheritedChoosable";
    public static final String hasCaptions = "HasCaptions";
    public static final String docSkippable = "DocumentationSkippable";

    public static final String parallel = "Parallel";
    public static final String intersection = "Intersection";
    public static final String union = "Union";
    public static final String removeDuplicates = "RemoveDuplicates";

    public static final String sortByName = "SortByName";
    public static final String sortByAttribute = "SortByAttribute";
    public static final String sortByProperty = "SortByProperty";
    public static final String sortByExpression = "SortByExpression";

    public static final String conformStereotype = "Conforms";

    public static final String templateStereotype = "FormattingAndDisplayTemplate";

    public static final String collectFilterStereotype = "CollectOrFilter";
    public static final String ignorableStereotype = "Ignorable";

    public static final String editableTableStereotype = "EditableTable";
    public static final String sysmlProfile = "SysML";

    public static final String documentCommentStereotype = "DocumentComment";
    public static final String viewCommentStereotype = "ViewComment";

    public static final String constraintStereotype = "Constraint";
}
