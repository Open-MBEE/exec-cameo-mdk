package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.DgvalidationDBSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSection;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DiagramTableTool;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.LibraryComponent;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.MissionComponent;
import gov.nasa.jpl.mgss.mbee.docgen.table.BillOfMaterials;
import gov.nasa.jpl.mgss.mbee.docgen.table.Deployment;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkPackageAssembly;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkpackageRollups;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class DocBookOutputVisitor extends AbstractModelVisitor {

	private boolean forViewEditor;
	private Stack<DBHasContent> parent;
	private String outputDir;
	
	public DocBookOutputVisitor(boolean forViewEditor) {
		this.forViewEditor = forViewEditor;
		this.parent = new Stack<DBHasContent>();
	}
	
	public DocBookOutputVisitor(boolean forViewEditor, String outputDir) {
		this.forViewEditor = forViewEditor;
		this.parent = new Stack<DBHasContent>();
		this.outputDir = outputDir;
	}
	
	public DBBook getBook() {
		if (!parent.isEmpty() && parent.get(0) instanceof DBBook)
			return (DBBook)parent.get(0);
		return null;
	}
	
	@Override
	public void visit(BulletedList bl) {
		if (bl.getIgnore())
			return;
		if (bl.getTargets() != null) {
			DBList l = new DBList();
			parent.peek().addElement(l);
			l.setOrdered(bl.isOrderedList());
      List< Element > targets =
          bl.isSortElementsByName() ? Utils.sortByName( bl.getTargets() )
                                   : bl.getTargets();
			if (bl.isShowTargets() || bl.isIncludeDoc()) {

				for (Element e: targets) {
					DBListItem li = new DBListItem();
					l.addElement(li);
					if (bl.isShowTargets() && e instanceof NamedElement) {
						li.addElement(new DBParagraph(((NamedElement)e).getName(), e, From.NAME));
					}
					if (bl.isIncludeDoc() && !ModelHelper.getComment(e).equals("")) {
						li.addElement(new DBParagraph(ModelHelper.getComment(e) ,e, From.DOCUMENTATION));
					}
					if (bl.getStereotypeProperties() != null && !bl.getStereotypeProperties().isEmpty()) {
						if (bl.isShowStereotypePropertyNames()) {
							DBList l2 = new DBList();
							l2.setOrdered(bl.isOrderedList());
							li.addElement(l2);
							for (Property p: bl.getStereotypeProperties()) {
								DBListItem li2 = new DBListItem();
								l2.addElement(li2);
								li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
								DBList l3 = new DBList();
								l3.setOrdered(bl.isOrderedList());
								li2.addElement(l3);
								bl.addStereotypeProperties(l3, e, p);
							}
						} else {
							DBList l2 = new DBList();
							l2.setOrdered(bl.isOrderedList());
							li.addElement(l2);
							for (Property p: bl.getStereotypeProperties()) {
								bl.addStereotypeProperties(l2, e, p);
							}
						}
					}
				}
			} else {
				for (Element e: targets) {
					if (bl.getStereotypeProperties() != null && !bl.getStereotypeProperties().isEmpty()) {
						if (bl.isShowStereotypePropertyNames()) {
							for (Property p: bl.getStereotypeProperties()) {
								DBListItem li2 = new DBListItem();
								li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
								l.addElement(li2);
								DBList l3 = new DBList();
								li2.addElement(l3);
								l3.setOrdered(bl.isOrderedList());
								bl.addStereotypeProperties(l3, e, p);
							}
						} else {
							for (Property p: bl.getStereotypeProperties()) {
								bl.addStereotypeProperties(l, e, p);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void visit(BillOfMaterialsTable bom) {
		bom.findWorkPackage();
		if (!(bom.getWorkpackage() instanceof NamedElement))
			return;
		int i = 0;
    List< Element > targets =
        bom.isSortElementsByName() ? Utils.sortByName( bom.getTargets() )
                                   : bom.getTargets();
		for (Element t: targets) {
			if (!(t instanceof Class) || ModelLib.isWorkPackage(t)) {
				continue;
			}
			BillOfMaterials b = new BillOfMaterials((Class)t, (NamedElement)bom.getWorkpackage(), bom.getFloatingPrecision(), false, bom.isSuppliesAsso(), bom.isAuthorizesAsso(), bom.isIncludeInherited(), bom.isShowProducts(), bom.isShowMassMargin());
			b.getBOMTable();
			
			List<List<DocumentElement>> bodyd = new ArrayList<List<DocumentElement>>();
			WorkpackageRollups rolld = bom.getDeploymentRollup(b, (Class)t, false, false, bodyd);
		
			List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
			WorkpackageRollups roll = bom.getBOMRollup(b, (Class)t, false, false, body);
			
			if (roll.isBad()) {
				parent.peek().addElement(new DBParagraph("<emphasis role=\"bold\">The Bill of Materials Mass Rollup did not pass validation!</emphasis>"));
				DBTable validation = new DBTable();
				validation.setBody(body);
				validation.setTitle("Mass Rollup Validation for Bill of Materials Table - " + ((NamedElement)bom.getWorkpackage()).getName());
				List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
				List<DocumentElement> headerline = new ArrayList<DocumentElement>();
				headerline.add(new DBText("Element"));
				headerline.add(new DBText("Property"));
				headerline.add(new DBText("Calculated"));
				headerline.add(new DBText("Model"));
				header.add(headerline);
				validation.setHeaders(header);
				validation.setCols(4);
				parent.peek().addElement(validation);	
			} 
			if (!roll.isBad() || forViewEditor) {
				EditableTable et = b.getBOMEditableTable();
				int depth = b.getWpDepth();
				DBTable dta = null;
				if (!forViewEditor)
					dta = Utils.getDBTableFromEditableTable(et, true, depth);
				else
					dta = Utils.getDBTableFromEditableTable(et, true);
				String title = et.getTitle();
				if (bom.getTitles() != null && bom.getTitles().size() > i)
					title = bom.getTitles().get(i);
				title = bom.getTitlePrefix() + title + bom.getTitleSuffix();
				dta.setTitle(title);
				if (bom.getCaptions() != null && bom.getCaptions().size() > i && bom.isShowCaptions())
					dta.setCaption(bom.getCaptions().get(i));
				parent.peek().addElement(dta);
			}
			if (rolld.isBad()) {
				parent.peek().addElement(new DBParagraph("<emphasis role=\"bold\">The deployment for " + ((NamedElement)t).getName() + " did not pass mass rollup validation!</emphasis>"));
				DBTable validation = new DBTable();
				validation.setBody(bodyd);
				validation.setTitle("Mass Rollup Validation For " + ((NamedElement)t).getName());
				List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
				List<DocumentElement> headerline = new ArrayList<DocumentElement>();
				headerline.add(new DBText("Element"));
				headerline.add(new DBText("Property"));
				headerline.add(new DBText("Calculated"));
				headerline.add(new DBText("Model"));
				header.add(headerline);
				validation.setHeaders(header);
				validation.setCols(4);
				parent.peek().addElement(validation);
			}
			i++;
		}
	}

	@Override
	public void visit(CombinedMatrix cm) {
    Debug.outln("entering visit(CombinedMatrix): " + cm);
		if (cm.getIgnore())
			return;
		DBTable dbTable = new DBTable();
		List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
		if (!cm.getHeaders().isEmpty()) {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			for (String h: cm.getHeaders())
				first.add(new DBText(h));
			dbTable.setCols(first.size());
		} else {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			/*first.add(new DBText("Name"));
			if (cm.isIncludeDoc())
				first.add(new DBText("Description"));*/
			for (Property p: cm.getStereotypeProperties()) 
				first.add(new DBText(p.getName()));
			for (Stereotype s: cm.getOutgoing())
				first.add(new DBText(s.getName()));
			for (Stereotype s: cm.getIncoming())
				first.add(new DBText(s.getName()));
			if (cm.getNameColumn() < cm.getDocColumn()) {
				first.add(cm.getNameColumn()-1, new DBText("Name"));
				if (cm.isIncludeDoc())
					first.add(cm.getDocColumn()-1, new DBText("Description"));
			}
			else {
				if (cm.isIncludeDoc())
					first.add(cm.getDocColumn()-1, new DBText("Description"));
				first.add(cm.getNameColumn()-1, new DBText("Name"));
			}
			dbTable.setCols(first.size());
		}
		dbTable.setHeaders(hs);
		String title = "";
		if (cm.getTitles() != null && cm.getTitles().size() > 0)
			title = cm.getTitles().get(0);
		title = cm.getTitlePrefix() + title + cm.getTitleSuffix();
		dbTable.setTitle(title);
		if (cm.getCaptions() != null && cm.getCaptions().size() > 0 && cm.isShowCaptions())
			dbTable.setCaption(cm.getCaptions().get(0));
		List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
    List< Element > targets =
        cm.isSortElementsByName() ? Utils.sortByName( cm.getTargets() )
                                 : cm.getTargets();
		for (Element e: targets) {
			if (cm.isSkipIfNoDoc() && ModelHelper.getComment(e).trim().equals(""))
				continue;
			List<DocumentElement> row = new ArrayList<DocumentElement>();
		/*	if (e instanceof NamedElement) {
				if (!forViewEditor)
					row.add(new DBText(DocGenUtils.addInvisibleSpace(DocGenUtils.fixString(((NamedElement)e).getName()))));
				else
					row.add(new DBParagraph(((NamedElement)e).getName(), e, From.NAME));
			} else
				row.add(new DBParagraph(e.getHumanName())); 
			if (cm.isIncludeDoc())
				row.add(new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION)); */
			for (Property p: cm.getStereotypeProperties()) 
				row.add(Common.getStereotypePropertyEntry(e, p, forViewEditor));
			for (Stereotype s: cm.getOutgoing()) {
				List<Object> blah = new ArrayList<Object>();
				blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 1, true, 1));
				row.add(Common.getTableEntryFromList(blah, true, forViewEditor));
			}
			for (Stereotype s: cm.getIncoming()) {
				List<Object> blah = new ArrayList<Object>();
				blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 2, true, 1));
				row.add(Common.getTableEntryFromList(blah, true, forViewEditor));
			}
			DocumentElement name = null;
			DocumentElement doc = null;
			if (e instanceof NamedElement) {
				if (!forViewEditor)
					name = new DBText(DocGenUtils.addInvisibleSpace(DocGenUtils.fixString(((NamedElement)e).getName())));
				else
					name = new DBParagraph(((NamedElement)e).getName(), e, From.NAME);
			} else
				name = new DBParagraph(e.getHumanName());
			doc = new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION);
			if (cm.getNameColumn() < cm.getDocColumn()) {
				row.add(cm.getNameColumn()-1, name);
				if (cm.isIncludeDoc())
					row.add(cm.getDocColumn()-1, doc);
			}
			else {
				if (cm.isIncludeDoc())
					row.add(cm.getDocColumn()-1, doc);
				row.add(cm.getNameColumn()-1, name);
			}
			body.add(row);
		}
		dbTable.setBody(body);
		List<DBColSpec> cslist = new ArrayList<DBColSpec>();
		if (cm.getColwidths() != null && !cm.getColwidths().isEmpty()) {
			int i = 1;
			for (String s: cm.getColwidths()) {
				DBColSpec cs = new DBColSpec(i);
				cs.setColwidth(s);
				cslist.add(cs);
				i++;
			}
		} else {
			DBColSpec cs = new DBColSpec(1);
			cs.setColwidth(".4*");
			cslist.add(cs);
		}
		dbTable.setColspecs(cslist);
		dbTable.setStyle(cm.getStyle());
		parent.peek().addElement(dbTable);
	}

	@Override
	public void visit(CustomTable customTable) {
	  Debug.outln("entering visit(CustomTable): " + customTable);
    if (customTable==null) {
      Debug.errln( "Can't create DocBook table from null CustomTable!" );
      return;
    }
    if (customTable.getIgnore()) {
      return;
    }
    if (Utils2.isNullOrEmpty(customTable.getColumns())) {
      Debug.errln( "No columns specified for CustomTable! "
                   + customTable.getColumns() );
      return;
    }
    if (Utils2.isNullOrEmpty(customTable.getTargets())) {
      Debug.errln( "No targets specified for CustomTable! "
                   + customTable.getTargets() );
      return;
    }
    Debug.outln( "visiting custom table " + customTable );
		DBTable dbTable = new DBTable();
		
		// get column headings
		List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
		if (!customTable.getHeaders().isEmpty()) {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			for (String h: customTable.getHeaders())
				first.add(new DBText(h));
			dbTable.setCols(first.size());
		} else {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			
	    if (Utils2.isNullOrEmpty(customTable.getColumns())) {
        Debug.errln( "No columns specified for CustomTable! "
                     + customTable.getColumns() );
	    } else {
	      for (String oclExpr: customTable.getColumns()) {
	        first.add(new DBText(oclExpr) );
	      }
	    }
//			for (Property p: cm.getStereotypeProperties()) 
//				first.add(new DBText(p.getName()));
			dbTable.setCols(first.size());
		}
		dbTable.setHeaders(hs);
		
		// get title
		String title = "";
		if (customTable.getTitles() != null && customTable.getTitles().size() > 0)
			title = customTable.getTitles().get(0);
		title = customTable.getTitlePrefix() + title + customTable.getTitleSuffix();
		dbTable.setTitle(title);
		
		// get caption
    if ( customTable.getCaptions() != null
         && customTable.getCaptions().size() > 0
         && customTable.isShowCaptions() ) {
      dbTable.setCaption( customTable.getCaptions().get( 0 ) );
    }
    
    // construct the main body of the table
		List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
    List< Element > targets =
        customTable.isSortElementsByName() ? Utils.sortByName( customTable.getTargets() )
                                 : customTable.getTargets();
    // construct row for each target
		for (Element e: targets) {
			List<DocumentElement> row = new ArrayList<DocumentElement>();
			// construct cell for each column
      for (String oclExpr: customTable.getColumns()) {
        Object result = customTable.evaluateOcl( e, oclExpr );
        row.add(Common.getTableEntryFromObject( result, false, forViewEditor ));
      }
			body.add(row);
		}
		dbTable.setBody(body);
		
		// set column widths
		List<DBColSpec> cslist = new ArrayList<DBColSpec>();
		if (customTable.getColwidths() != null && !customTable.getColwidths().isEmpty()) {
			int i = 1;
			for (String s: customTable.getColwidths()) {
				DBColSpec cs = new DBColSpec(i);
				cs.setColwidth(s);
				cslist.add(cs);
				i++;
			}
		} else {
			DBColSpec cs = new DBColSpec(1);
			cs.setColwidth(".4*");
			cslist.add(cs);
		}
		dbTable.setColspecs(cslist);

		// set style
		dbTable.setStyle(customTable.getStyle());
		
		parent.peek().addElement(dbTable);
    Debug.outln( "got custom DBTable " + dbTable );
	}
	
	@Override
	public void visit(DependencyMatrix dm) {
	}

	@Override
	public void visit(DeploymentTable dt) {
		int i = 0;
    List< Element > targets =
        dt.isSortElementsByName() ? Utils.sortByName( dt.getTargets() )
                                 : dt.getTargets();
    for (Element t: targets) {
			if (!(t instanceof Class)) {
				continue;
			}
			Deployment d = new Deployment((Class)t, dt.getFloatingPrecision(), dt.isSortByName(), dt.isSuppliesAsso(), dt.isAuthorizesAsso(), dt.isIncludeInherited());
			d.getDeploymentTable();
			List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
			WorkpackageRollups roll = dt.getRollup(d, (Class)t, false, false, body);
			if (roll.isBad()) {
				parent.peek().addElement(new DBParagraph("<emphasis role=\"bold\">The deployment for " + ((Class)t).getName() + " did not pass mass rollup validation!</emphasis>"));
				DBTable validation = new DBTable();
				validation.setBody(body);
				validation.setTitle("Mass Rollup Validation for Deployment Table - " + ((Class)t).getName());
				List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
				List<DocumentElement> headerline = new ArrayList<DocumentElement>();
				headerline.add(new DBText("Element"));
				headerline.add(new DBText("Property"));
				headerline.add(new DBText("Calculated"));
				headerline.add(new DBText("Model"));
				header.add(headerline);
				validation.setHeaders(header);
				validation.setCols(4);
				parent.peek().addElement(validation);
			} 
			if (!roll.isBad() || forViewEditor) {
			
				EditableTable et = d.getDeploymentEditableTable();
				int depth = d.getProductDepth();
				DBTable dta = null;
				if (!forViewEditor)
					dta = Utils.getDBTableFromEditableTable(et, true, depth);
				else
					dta = Utils.getDBTableFromEditableTable(et, true);
				String title = et.getTitle();
				if (dt.getTitles() != null && dt.getTitles().size() > i)
					title = dt.getTitles().get(i);
				title = dt.getTitlePrefix() + title + dt.getTitleSuffix();
				dta.setTitle(title);
				if (dt.getCaptions() != null && dt.getCaptions().size() > i && dt.isShowCaptions())
					dta.setCaption(dt.getCaptions().get(i));
				parent.peek().addElement(dta);
				dta.setStyle(dt.getStyle());
			}
			i++;
		}
	}

	@Override
	public void visit(GenericTable gt) {
		DiagramTableTool dtt = new DiagramTableTool();
		if (gt.getIgnore())
			return;
		int tableCount = 0;
    List< Element > targets =
        gt.isSortElementsByName() ? Utils.sortByName( gt.getTargets() )
                                  : gt.getTargets();
    for (Element e: targets) {
			if (e instanceof Diagram) {
				if (Application.getInstance().getProject().getDiagram((Diagram)e).getDiagramType().getType().equals("Generic Table")) {
					DBTable t = new DBTable();
					List<String> columnIds = dtt.getColumnIds((Diagram)e);
					t.setHeaders(gt.getHeaders((Diagram)e, columnIds, dtt));
					List<Element> rowElements = dtt.getRowElements((Diagram)e);
					t.setBody(gt.getBody((Diagram)e, rowElements, columnIds, dtt, forViewEditor));
					if (gt.getTitles() != null && gt.getTitles().size() > tableCount) {
						t.setTitle(gt.getTitlePrefix() + gt.getTitles().get(tableCount) + gt.getTitleSuffix());
					} else {
						t.setTitle(gt.getTitlePrefix() + ((Diagram)e).getName() + gt.getTitleSuffix());
					}
					if (gt.getCaptions() != null && gt.getCaptions().size() > tableCount && gt.isShowCaptions()) {
						t.setCaption(gt.getCaptions().get(tableCount));
					} else {
						t.setCaption(ModelHelper.getComment(e));
					}
					t.setCols(columnIds.size() -1);
					parent.peek().addElement(t);
					t.setStyle(gt.getStyle());
					tableCount++;
				}
			}
		}
		dtt.closeOpenedTables();
	}

	@Override
	public void visit(PropertiesTableByAttributes pt) {
		if (pt.getIgnore())
			return;
		if (forViewEditor) {
			EditableTable et = pt.getEditableTable();
			DBTable dtable = Utils.getDBTableFromEditableTable(et, true);
			dtable.setStyle(pt.getStyle());
			parent.peek().addElement(dtable);
		} else {
			List<DocumentElement> results = pt.getDocumentElement();
			for (DocumentElement de: results) {
				if (de instanceof DBTable) {
					((DBTable)de).setStyle(pt.getStyle());
				}
			}
			parent.peek().addElements(results);
		}
	}

	@Override
	public void visit(Paragraph para) {
		if (para.getIgnore())
			return;
		if (para.getText() != null) { 
			if (forViewEditor || !para.getText().trim().equals("")) 
				parent.peek().addElement(new DBParagraph(para.getText(), para.getDgElement(), para.getFrom()));
		} else if (para.getTargets() != null) {
	    List< Element > targets =
	        para.isSortElementsByName() ? Utils.sortByName( para.getTargets() )
	                                 : para.getTargets();
	    for (Element e: targets) {
				if (para.getStereotypeProperties() != null && !para.getStereotypeProperties().isEmpty()) {
					for (Property p: para.getStereotypeProperties()) {
						List<Object> ob = Utils.getStereotypePropertyValues(e, p);
						for (Object o: ob) {
							if (o instanceof String)
								parent.peek().addElement(new DBParagraph((String)o));
						}
					}
				} else 
					parent.peek().addElement(new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION));
			}
		}
	}

	public void visit(TableStructure ts) {
		if (ts.getIgnore())
			return;
		parent.peek().addElements(ts.visit(forViewEditor));
	}
	
	@Override
	public void visit(UserScript us) {
		if (us.getIgnore())
			return;
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("FixMode", "FixNone");
		inputs.put("ForViewEditor", forViewEditor);
		inputs.put("DocGenTitles", us.getTitles());
		if (this.outputDir != null)
			inputs.put("docgen_output_dir", this.outputDir);
		inputs.put("md_install_dir", ApplicationEnvironment.getInstallRoot());
		Map<?,?> o = us.getScriptOutput(inputs);
		if (o != null && o.containsKey("DocGenOutput")) {
			Object l = o.get("DocGenOutput");
			if (l instanceof List) {
				for (Object oo: (List<?>)l) {
					if (oo instanceof DocumentElement)
						parent.peek().addElement((DocumentElement)oo);
				}
			}
		}
		if (o != null && o.containsKey("docgenOutput")) {
			Object result = o.get("docgenOutput");
			if (result instanceof List) {
				for (Object res: (List<?>)result) {
					if (res instanceof NamedElement) {
						parent.peek().addElement(new DBText(((NamedElement)res).getName())); 
					} else if (res instanceof ViewElement) {
						parent.peek().addElement(DocGenUtils.ecoreTranslateView((ViewElement)res, forViewEditor));
					}
				}
			} 
		}
		if (o != null && o.containsKey("DocGenValidationOutput")) {
			Object l = o.get("DocGenValidationOutput");
			if (l instanceof List) {
				for (Object oo: (List<?>)l) {
					if (oo instanceof ValidationSuite)
						parent.peek().addElements(((ValidationSuite)oo).getDocBook());
				}
			}
		}
		if (o != null && o.containsKey("docgenValidationOutput")) {
			Object l = o.get("docgenValidationOutput");
			if (l instanceof List) {
				DgvalidationDBSwitch s = new DgvalidationDBSwitch();
				for (Object object: (List<?>)l) {
					if (object instanceof Suite)
						parent.peek().addElements(((ValidationSuite)s.doSwitch((Suite)object)).getDocBook());
				}
			}
		}
	}

	@Override
	public void visit(WorkpackageAssemblyTable wat) {
    List< Element > targets =
        wat.isSortElementsByName() ? Utils.sortByName( wat.getTargets() )
                                 : wat.getTargets();
		if (wat.getWorkpackage() == null) {
      for (Element t: targets) {
				if (ModelLib.isWorkPackage(t)) {
					wat.setWorkpackage((NamedElement) t);
				}
			}
		}
		if (!(ModelLib.isWorkPackage(wat.getWorkpackage())))
			return;
		int i = 0;
		for (Element t: targets) {
			if (!(t instanceof Class) || ModelLib.isWorkPackage(t)) {
				continue;
			}
			WorkPackageAssembly w = new WorkPackageAssembly((Class)t, (NamedElement)wat.getWorkpackage(), wat.getFloatingPrecision(), wat.isSuppliesAsso(), wat.isAuthorizesAsso(), wat.isIncludeInherited());
			w.getWPATable();
			
			WorkpackageRollups rolld = new WorkpackageRollups(w.getDeployment(), null, null, 
					w.getRealUnits(), null,
					w.getMass(), 
					w.getMassContingency(), 
					w.getCbeContingency(),
					w.getMassAllocation(),
					w.getMassMargin(),
					false, wat.getFloatingPrecision(), false, wat.isShowMassMargin());
			rolld.fillExpected((NamedElement)t);
			
			List<List<DocumentElement>> bodyd = new ArrayList<List<DocumentElement>>();
			rolld.validateOrFix((NamedElement)t, bodyd);
			
			WorkpackageRollups roll = new WorkpackageRollups(w.getWpDeployment(), 
				w.getWp2p(), w.getDeployment(), 
				null, w.getTotalUnits(),
				w.getMass(), 
				w.getMassContingency(), 
				w.getCbeContingency(),
				w.getMassAllocation(),
				w.getMassMargin(),
				false, wat.getFloatingPrecision(), false, wat.isShowMassMargin());
			roll.fillExpected((NamedElement)wat.getWorkpackage());
			
			List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
			roll.validateOrFix((NamedElement)wat.getWorkpackage(), body);
			if (rolld.isBad() || roll.isBad()) {
				if (rolld.isBad()) {
					parent.peek().addElement(new DBParagraph("<emphasis role=\"bold\">" + "The deployment for " + ((Class)t).getName() + " did not pass mass rollup validation!</emphasis>"));
					DBTable validation = new DBTable();
					validation.setBody(bodyd);
					validation.setTitle("Mass Rollup Validation For " + ((NamedElement)t).getName());
					List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
					List<DocumentElement> headerline = new ArrayList<DocumentElement>();
					headerline.add(new DBText("Element"));
					headerline.add(new DBText("Property"));
					headerline.add(new DBText("Calculated"));
					headerline.add(new DBText("Model"));
					header.add(headerline);
					validation.setHeaders(header);
					validation.setCols(4);
					parent.peek().addElement(validation);
				}
				if (roll.isBad()) {
					parent.peek().addElement(new DBParagraph("<emphasis role=\"bold\">The workpackage mass rollup did not pass mass rollup validation!</emphasis>"));
					DBTable validation = new DBTable();
					validation.setBody(body);
					validation.setTitle("Mass Rollup Validation for " + ((NamedElement)wat.getWorkpackage()).getName());
					List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
					List<DocumentElement> headerline = new ArrayList<DocumentElement>();
					headerline.add(new DBText("Element"));
					headerline.add(new DBText("Property"));
					headerline.add(new DBText("Calculated"));
					headerline.add(new DBText("Model"));
					header.add(headerline);
					validation.setHeaders(header);
					validation.setCols(4);
					parent.peek().addElement(validation);
				}
			} 
			if ((!rolld.isBad() && !roll.isBad()) || forViewEditor) {
				EditableTable et = w.getWPAEditableTable();
				int wpdepth = w.getWpaWpDepth();
				int pdepth = w.getWpaPDepth();
				DBTable dta = null;
				if (!forViewEditor)
					dta = Utils.getDBTableFromEditableTable(et, true, wpdepth, pdepth);
				else
					dta = Utils.getDBTableFromEditableTable(et, true);
				String title = et.getTitle();
				if (wat.getTitles() != null && wat.getTitles().size() > i)
					title = wat.getTitles().get(i);
				title = wat.getTitlePrefix() + title + wat.getTitleSuffix();
				dta.setTitle(title);
				if (wat.getCaptions() != null && wat.getCaptions().size() > i && wat.isShowCaptions())
					dta.setCaption(wat.getCaptions().get(i));
				parent.peek().addElement(dta);
				dta.setStyle(wat.getStyle());
			}
			i++;
		}
	}

	@Override
	public void visit(Image image) {
		if (image.getIgnore())
			return;
		if (image.getTargets() != null) {
	    List< Element > targets =
	        image.isSortElementsByName() ? Utils.sortByName( image.getTargets() )
	                                 : image.getTargets();
			for (int i = 0; i < targets.size(); i++) {
				Element e = targets.get(i);
				if (e instanceof Diagram) {
					DBImage im = new DBImage();
					im.setDiagram((Diagram)e);
					im.setDoNotShow(image.getDoNotShow());
					String title = "";
					if (image.getTitles() != null && image.getTitles().size() > i)
						title = image.getTitles().get(i);
					else
						title = ((Diagram)e).getName();
					if (image.getTitlePrefix() != null)
						title = image.getTitlePrefix() + title;
					if (image.getTitleSuffix() != null)
						title = title + image.getTitleSuffix();
					im.setTitle(title);
					if (image.getCaptions() != null && image.getCaptions().size() > i && image.getShowCaptions())
						im.setCaption(image.getCaptions().get(i));
					im.setId(e.getID());
					parent.peek().addElement(im);
				
					String doc = ModelHelper.getComment(e);
					if (doc != null && (forViewEditor || (!doc.trim().equals("") && !image.getDoNotShow()))) {
						parent.peek().addElement(new DBParagraph(doc, e, From.DOCUMENTATION));
					}
					
				}
			}
		}	
	}

	@Override
	public void visit(Document doc) {
		DBBook book = new DBBook();
		book.setTitle(doc.getTitle());
		if (doc.getTitle() == null || doc.getTitle().equals(""))
			book.setTitle("Default Title");
		book.setFrom(doc.getDgElement());
		book.setSubtitle(doc.getSubtitle());
		book.setLegalnotice(doc.getLegalnotice());
		book.setAcknowledgement(doc.getAcknowledgement());
		book.setCoverimage(doc.getCoverimage());
		book.setDocumentID(doc.getDocumentID());
		book.setDocumentVersion(doc.getDocumentVersion());
		book.setLogoAlignment(doc.getLogoAlignment());
		book.setLogoLocation(doc.getLogoLocation());
		book.setAbbreviatedProjectName(doc.getAbbreviatedProjectName());
		book.setDocushareLink(doc.getDocushareLink());
		book.setAbbreviatedTitle(doc.getAbbreviatedTitle());
		book.setTitlePageLegalNotice(doc.getTitlePageLegalNotice());
		book.setFooterLegalNotice(doc.getFooterLegalNotice());
		book.setCollaboratorEmail(doc.getCollaboratorEmail());
		book.setRemoveBlankPages(doc.getRemoveBlankPages());
		book.setAuthor(doc.getAuthor());
		book.setApprover(doc.getApprover());
		book.setConcurrance(doc.getConcurrance());
		book.setJPLProjectTitle(doc.getJPLProjectTitle());
		book.setRevisionHistory(doc.getRevisionHistory());
		book.setUseDefaulStylesheet(doc.getUseDefaultStylesheet());
		book.setLogoSize(doc.getLogoSize());
		
		parent.push(book);
		visitChildren(doc);
	}

	@Override
	public void visit(Section section) {
		if (section.getIgnore())
			return;
		DBSection sec = new DBSection();
		sec.setFrom(section.getDgElement());
		sec.isAppendix(section.isAppendix());
		sec.isChapter(section.isChapter());
		sec.setView(section.isView());
		sec.isNoSection(section.isNoSection());
		String title = "";
		if (section.getTitle() != null && !section.getTitle().equals(""))
			title = section.getTitle();
		if (section.getTitlePrefix() != null)
			title = section.getTitlePrefix() + title;
		if (section.getTitleSuffix() != null)
			title = title + section.getTitleSuffix();
		sec.setTitle(title);
		sec.setStringIfEmpty(section.getStringIfEmpty());
		sec.setSkipIfEmpty(section.getSkipIfEmpty());
		if (section.getId() != null)
			sec.setId(section.getId());
	
		parent.push(sec);
		visitChildren(section);
		parent.pop();
		
		if (section.isNoSection()) {
			for (DocumentElement de: sec.getChildren()) {
				if (de instanceof DBTable)
					de.setId(section.getId());
			}
		}
		if (sec.getChildren().isEmpty()) {
			if (section.getSkipIfEmpty())
				return;
			if (section.getStringIfEmpty() != null)
				sec.addElement(new DBParagraph(section.getStringIfEmpty()));
			else
				sec.addElement(new DBParagraph(""));
		}
		parent.peek().addElement(sec);
	}

	@Override
	public void visit(MissionMapping cm) {
		if (!cm.init())
			return;
		DBTable table = new DBTable();
		Node<String, MissionComponent> root = cm.getRoot();
		List<Element> chars = Utils.sortByName(cm.getLibraryCharacterizations());
		Set<LibraryComponent> comps = cm.getLibraryComponents();
		List<List<DocumentElement>> grid = new ArrayList<List<DocumentElement>>();
		List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
		addMissionRows(root, chars, grid, 1);
		table.setBody(grid);
		List<DocumentElement> headerrow = new ArrayList<DocumentElement>();
		headerrow.add(new DBText("Component"));
		headerrow.add(new DBText("Inherits From"));
		for (Element charr: chars) {
			headerrow.add(new DBText(((NamedElement)charr).getName()));
		}
		headers.add(headerrow);
		table.setHeaders(headers);
		table.setCols(headerrow.size());
		table.setTitle("Component Characterizations");
		parent.peek().addElement(table);
	}
	
	private void addMissionRows(Node<String, MissionComponent> cur, List<Element> chars, List<List<DocumentElement>> grid, int depth) {
		MissionComponent curc = cur.getData();
		List<DocumentElement> row = new ArrayList<DocumentElement>();
		row.add(new DBText(DocGenUtils.getIndented(curc.getName(), depth)));
		if (curc.isPackage()) {
			for (Element charr: chars) {
				row.add(new DBText(""));
			}
			row.add(new DBText(""));
		} else {
			String inherits = "";
			int i = 0;
			for (LibraryComponent lc: curc.getLibraryComponents()) {
				if (i == 0)
					inherits = inherits + lc.getName();
				else
					inherits = ", " + inherits + lc.getName();
			}
			row.add(new DBText(inherits));
			for (Element charr: chars) {
				if (curc.hasLibraryCharacterization((NamedElement)charr)) 
					row.add(new DBText("X"));
				 else
					row.add(new DBText(""));
			}
		}
		grid.add(row);
		for (Node<String, MissionComponent> child: cur.getChildrenAsList()) {
			addMissionRows(child, chars, grid, depth+1);
		}
	}
	
	@Override
	public void visit(LibraryMapping cm) {
		if (!cm.init())
			return;
		DBTable table = new DBTable();
		Node<String, LibraryComponent> root = cm.getRoot();
		List<Element> chars = Utils.sortByName(cm.getUsedChars());
		List<List<DocumentElement>> grid = new ArrayList<List<DocumentElement>>();
		List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
		addLibraryRows(root, chars, grid, 1);
		table.setBody(grid);
		List<DocumentElement> headerrow = new ArrayList<DocumentElement>();
		headerrow.add(new DBText("Component"));
		for (Element charr: chars) {
			headerrow.add(new DBText(((NamedElement)charr).getName()));
		}
		headers.add(headerrow);
		table.setHeaders(headers);
		table.setCols(headerrow.size());
		table.setTitle("Possible Component Characterizations");
		parent.peek().addElement(table);
		
	}
	
	private void addLibraryRows(Node<String, LibraryComponent> cur, List<Element> chars, List<List<DocumentElement>> grid, int depth) {
		LibraryComponent curc = cur.getData();
		List<DocumentElement> row = new ArrayList<DocumentElement>();
		row.add(new DBText(DocGenUtils.getIndented(curc.getName(), depth)));
		if (curc.isPackage()) {
			for (Element charr: chars) {
				row.add(new DBText(""));
			}
		} else {
			for (Element charr: chars) {
				if (curc.hasCharacterization((NamedElement)charr)) 
					row.add(new DBText("X"));
				 else
					row.add(new DBText(""));
			}
		}
		grid.add(row);
		for (Node<String, LibraryComponent> child: cur.getChildrenAsList()) {
			addLibraryRows(child, chars, grid, depth+1);
		}
	}
}
