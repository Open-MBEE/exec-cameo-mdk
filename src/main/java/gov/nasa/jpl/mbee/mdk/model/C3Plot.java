package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBTable;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.util.Debug;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import  gov.nasa.jpl.mbee.mdk.docgen.docbook.DBC3Plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by mw107 on 7/5/2017.
 */
public class C3Plot extends TableStructure
{
    protected String options;
    protected String functions;

    //TODO handle reverse
    protected boolean reverse;

    public C3Plot(DocumentValidator validator) {
        super(validator);
    }


    public String getOptions()
    {
        return options;
    }

    public void setOptions(String options)
    {
        this.options = options;
    }

    public String getFunctions()
    {
        return functions;
    }

    public void setFunctions(String functions)
    {
        this.functions = functions;
    }

    public void setReverse(boolean b) {
        reverse = b;
    }

    @Override
    public void parse()
    {
        super.parse();
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir)
    {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ignore) {
            return res;
        }

        DBC3Plot c3p = new DBC3Plot();
        c3p.setOptions(options);
        c3p.setFunctions(functions);
        List<DocumentElement> tres = super.visit(forViewEditor, outputDir);
        c3p.setTable((DBTable)tres.get(0));
        res.add(c3p);
        return res;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {

        Boolean reverse = (Boolean) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.c3PlotStereotype, "reverse", false);

        String options = (String) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.c3PlotStereotype, "options", "");
        String functions = (String) GeneratorUtils.getObjectProperty(dgElement,
                DocGenProfile.c3PlotStereotype, "functions", "");
        setReverse(reverse);
        setOptions(options);
        setFunctions(functions);


        //find dgelement for table structure.
        Element temp;
        if (dgElement instanceof StructuredActivityNode) {
            temp = dgElement;
        }
        else if (dgElement instanceof CallBehaviorAction) {
            temp = ((CallBehaviorAction) dgElement).getBehavior();
        }
        else {
            temp = null;
        }
        //replace dgElement for table
        InitialNode in = null;
        if (temp != null) {
            in = GeneratorUtils.findInitialNode(temp);
            if ( in != null)
            {
                Collection<ActivityEdge> outs = in.getOutgoing();
                if (outs != null && outs.size() == 1)
                {
                    ActivityNode next = outs.iterator().next().getTarget();
                    if (next instanceof StructuredActivityNode
                            && StereotypesHelper.hasStereotypeOrDerived(next, DocGenProfile.tableStructureStereotype))
                        dgElement = next;
                }
            }
            else //uwhen c3plot does not have initialNode to TableStructure
            {
                dgElement = temp.getOwnedElement().stream()
                        .filter( x-> (x instanceof StructuredActivityNode
                                && StereotypesHelper.hasStereotypeOrDerived(x, DocGenProfile.tableStructureStereotype)))
                        //.collect(java.util.stream.Collectors.toList()); //return List<Element>
                        .findAny().orElse(null);
                //dgElement = null;
            }
        }
        else
            dgElement = null;

        super.initialize();

    }}
