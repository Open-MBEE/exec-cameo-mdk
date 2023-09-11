package org.openmbee.mdk.ocl.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.base.ModelObject;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.emf.EmfUtils;
import org.openmbee.mdk.ocl.OclEvaluator;
import org.openmbee.mdk.ocl.ui.OclQueryDialog;
import org.openmbee.mdk.ocl.ui.RepeatInputComboBoxDialog;
import org.openmbee.mdk.util.Debug;
import org.openmbee.mdk.util.MoreToString;
import org.openmbee.mdk.util.Utils2;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.ParserException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class OclQueryAction extends MDAction {
    public static final String DEFAULT_ID = "OclQuery";
    public static final String actionText = "Run OCL Query";

    protected String lastInput = "";
    protected LinkedList<String> inputHistory = new LinkedList<String>();
    protected TreeSet<String> pastInputs = new TreeSet<String>();
    protected LinkedList<String> choices = new LinkedList<String>();

    //protected static boolean selectionInDiagram = true;

    public static OclQueryDialog dialog = null;

    public static boolean useNewOclEvaluator = true;

    public OclQueryAction() {
        super(DEFAULT_ID, actionText, null, null);
    }

    public static class ProcessOclQuery implements RepeatInputComboBoxDialog.Processor {

        private Object context;
        public List<String> choiceStrings = new LinkedList<String>();
        protected Object completionSource;

        public ProcessOclQuery(Object context) {
            super();
            setContext(context);
        }

        @Deprecated
        public Object parseAndProcess(Object input, Object context) {
            setContext(context);
            return parseAndProcess(input);
        }

        public Object getContext() {
            return context;
        }

        public void setContext(final Object context) {
            this.context = context;
        }

        /*public void setContext(Collection<Element> context) {
            getContext().clear();
            getContext().addAll(context);
        }*/

        /**
         * Parse and evaluate OCL and additional helper syntax:
         * <ol>
         * <li>Parse as OCL.
         * <li>If the parse succeeds, return the result.
         * <li>Get the evaluation result up to the point where parse failed.
         * <li>Try to parse using additional syntax where the parse failed in
         * the context of the result from the previous step.
         * <li>If the parse fails, return with failure.
         * <li>If there is more to parse, continue at step 1 in the context of
         * the new result with the remaining unparsed input string.
         * <li>TODO -- How does this work when expressions are nested and not
         * just chained?
         * </ol>
         *
         * @param input
         * @return the result of the evaluation of the input expression
         */
        @Deprecated
        public Object parseAndProcess(Object input) {
            final String oclString = input != null ? input.toString() : null;
            Object result = null;
            OclEvaluator evaluator = null;
            //  OCLSyntaxHelper syntaxHelper = null;
            try {
                //result = OclEvaluator.evaluateQuery(contextEObject, oclString, true);
                result = OclEvaluator.evaluateQuery(input, oclString, true);
                evaluator = OclEvaluator.instance;
            } catch (ParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (evaluator == null || !evaluator.isValid() || Utils2.isNullOrEmpty(result)) {
                result = null;
            }

            // If the parse succeeds, return the result.
            /*if (evaluator != null && ( evaluator.isValid() || !Utils2.isNullOrEmpty( result ) ) ) {
                return result;
            } else {
                outputList.add(null);
            }*/

            // Get the evaluation result up to the point where parse
            // failed.
            //syntaxHelper = new OCLSyntaxHelper(evaluator.getOcl().getEnvironment());
            //List completions = syntaxHelper.getSyntaxHelp(ConstraintKind.INVARIANT, oclString);
            //Debug.outln("completions = " + completions);
            return result;
        }

        /*@SuppressWarnings("rawtypes")
        public Object parseAndProcess(Object input) {
            String oclString = input == null ? null : input.toString();
            //ArrayList<Object> outputList = new ArrayList<Object>();
            //ArrayList<Object> localContext = new ArrayList<Object>();
            //localContext.addAll(getContext());

            OCLSyntaxHelper syntaxHelper = null;

            // // TODO -- How does this work when expressions are nested and not
            // just chained?
            // // Parse as OCL.
            EObject contextEObject = null;
            // for ( Object o : localContext ) {
            // if ( o instanceof Element ) {
            // contextEObject = (EObject)o;
            // break;
            // }
            // }
            // if ( contextEObject == null ) {
            if (context instanceof Collection) {
            	final List<Object> outputList = new ArrayList<Object>();
	            for (Object o: (Collection) context) {
                //if (o instanceof EObject) {
                    //contextEObject = (EObject) o;
                    // break;
                    // }
                    // }
                    // }
                    Object result = null;
                    OclEvaluator evaluator = null;
                    try {
                        //result = OclEvaluator.evaluateQuery(contextEObject, oclString, true);
                    	result = OclEvaluator.evaluateQuery(o, oclString, true);
                        evaluator = OclEvaluator.instance;
                    } catch (ParserException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    // If the parse succeeds, return the result.
                    if (evaluator != null && ( evaluator.isValid() || !Utils2.isNullOrEmpty( result ) ) ) {
                        // return result;
                        outputList.add(result);
                    } else {
                        outputList.add(null);
                    }

                    // Get the evaluation result up to the point where parse
                    // failed.
                    syntaxHelper = new OCLSyntaxHelper(evaluator.getOcl().getEnvironment());
                    List completions = syntaxHelper.getSyntaxHelp(ConstraintKind.INVARIANT, oclString);
                    Debug.outln("completions = " + completions);

                    // Try to parse using additional syntax where the parse
                    // failed in the
                    // context of the result from the previous step.
                    // TODO

                    // If the parse fails, return with failure.
                    // TODO

                    // If there is more to parse, continue at step 1 in the
                    // context of the
                    // new result with the remaining unparsed input string.
                    // TODO

                    // try to parse as OCL
                    // TODO

	                }

	            }
	            return OclEvaluator.evaluateQuery(input, oclString, true);
            }*/

        // ALTERNATIVE APPROACH
        // Try to parse in pieces using "." and "->" as delimiters.
            /*if (syntaxHelper == null) {
                int pos = 0;
                boolean found = true;
                while (!found) {
                    int nextPos1 = oclString.indexOf('.', pos);
                    int nextPos2 = oclString.indexOf("->", pos);
                    // TODO -- HERE!!
                }
            }*/

        // String[] split = oclString.split( "[.]|([-][>])" );
        // ArrayList<String> tokens = new ArrayList< String >();
        // int insideExpression = 0;
        // for ( String s : split ) {
        // if (Utils2.count("\"", s) % 2 != insideExpression ) {
        // // expression is still open
        // }
        // }

        //return outputList;
        //}

        public static String getTypeName(Object o) {
            if (o == null) {
                return null;
            }
            EObject eo = (EObject) (o instanceof EObject ? o : null);
            Class<?> c = (eo != null ? EmfUtils.getType(eo) : o.getClass());
            if (c == null) {
                return null;
            }
            String type = c.getSimpleName();
            return type;
        }

        public ArrayList<Object> process(Element elem, String oclString) {
            ArrayList<Object> outputList = new ArrayList<Object>();
            Object result = null;
            String output = null;
            OclEvaluator evaluator = null;
            try {
                if (elem == null) {
                    return null;
                }
                completionSource = elem;
                result = OclEvaluator.evaluateQuery(elem, oclString, true);
                if (result instanceof EObject) {
                    completionSource = result;  // TODO -- what if the result is a collection?
                }
                evaluator = OclEvaluator.instance;
                output = toString(result);
                if (!evaluator.isValid() && Utils2.isNullOrEmpty(result)) {
                    output = output
                            + "\nOclInvalid\nThis may be the result of a problem with a shortcut/blackbox function.";
                }

                output = output + " : " + getTypeName(result);
                Debug.outln("evaluated \"" + oclString + "\" for element " + toString(elem) + "\n"
                        + "    result = " + output + "\n");

                outputList.add(output);
            } catch (Exception ex) {
                String errorMsg = ex.getLocalizedMessage();
                outputList.add("Error: " + errorMsg);
                errorMsg = errorMsg + " for OCL query \""
                        + OclEvaluator.queryObjectToStringExpression(oclString) + "\" on "
                        + EmfUtils.toString(elem);
                Debug.error(false, false, errorMsg);
            }
            choiceStrings.clear();
            if (evaluator != null) {
                choiceStrings.addAll(evaluator.commandCompletionChoiceStrings(OclEvaluator.instance.getHelper(), completionSource, oclString));
                Collections.sort(choiceStrings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (o1 == o2) {
                            return 0;
                        }
                        if (o1 == null) {
                            return -1;
                        }
                        if (o2 == null) {
                            return 1;
                        }
                        o1 = o1.replaceFirst("[^A-Za-z0-9]*", "");
                        o2 = o2.replaceFirst("[^A-Za-z0-9]*", "");
                        return o1.compareTo(o2);
                    }
                });
                System.out.println("completionSource: " + completionSource);
                Debug.outln("CS: " + choiceStrings.toString());
            }
            return outputList;
        }

        @Override
        public Object process(Object input) {
            final String oclString = input != null ? input.toString() : null;
            Object result = null;
            try {
                result = OclEvaluator.evaluateQuery(getContext(), oclString, true);
            } catch (Exception e) {
                String errorMsg = e.getLocalizedMessage();
                //outputList.add("Error: " + errorMsg);
                errorMsg = "Error: \"" + errorMsg + "\" for OCL query \""
                        + OclEvaluator.queryObjectToStringExpression(oclString) + "\" on "
                        + EmfUtils.toString(context);
                Debug.error(false, false, errorMsg);
                // TODO Auto-generated catch block
                //e.printStackTrace();
                choiceStrings.clear();
                choiceStrings.add(errorMsg);
                return null;
            }
            //System.out.println("GETTING HERE");
            completionSource = result;
            choiceStrings.clear();
            if (OclEvaluator.instance != null) {
                choiceStrings.addAll(OclEvaluator.instance.commandCompletionChoiceStrings(OclEvaluator.instance.getHelper(), completionSource, oclString));
                Collections.sort(choiceStrings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (o1 == o2) {
                            return 0;
                        }
                        if (o1 == null) {
                            return -1;
                        }
                        if (o2 == null) {
                            return 1;
                        }
                        o1 = o1.replaceFirst("[^A-Za-z0-9]*", "");
                        o2 = o2.replaceFirst("[^A-Za-z0-9]*", "");
                        return o1.compareTo(o2);
                    }
                });
                //System.out.println("CS: " + choiceStrings);
                Debug.outln(choiceStrings.toString());
            }
            return result;

            /*final String oclString = input == null ? null : input.toString();
            //ArrayList<Object> outputList = new ArrayList<Object>();
            // outputList = parseAndProcess( input );
            // if ( Utils2.isNullOrEmpty( outputList ) ) {
            // outputList = new ArrayList< Object >();
            // } else {
            // return outputList;
            // }
            // Ensure user-defined shortcut functions are updated
            OclEvaluator.resetEnvironment();

            if (Utils2.isNullOrEmpty(getContext())) {
                outputList = process(null, oclString);
            } else
                for (Element elem: getContext()) {
                    ArrayList<Object> results = process(elem, oclString);
                    if (results != null)
                        outputList.addAll(results);
                }
            if (outputList != null && outputList.size() == 1)
                return outputList.get(0);
            return outputList;*/
        }

        public static String toString(Object result) {
            String s = null;
            if (result instanceof Collection) {
                StringBuffer sb = new StringBuffer();
                sb.append(MoreToString.Helper.toString(result));
                s = sb.toString();
            }
            if (Utils2.isNullOrEmpty(s) && result instanceof BaseElement) {
                BaseElement be = ((BaseElement) result);
                s = be.getHumanName();
            }
            if (Utils2.isNullOrEmpty(s) && result instanceof ModelObject) {
                s = ((ModelObject) result).get_representationText();
            }
            if (Utils2.isNullOrEmpty(s) && result != null) {
                s = result.toString();
            }

            if (s == null) {
                s = "null";
            }
            return s;
        }

        @Override
        public List<String> getCompletionChoices() {
            return choiceStrings;
        }

        @Override
        public Object getSourceOfCompletion() {
            // TODO Auto-generated method stub
            return completionSource;
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Ensure user-defined shortcut functions are updated
        Window owner = null;
        try {
            owner = Application.getInstance().getMainFrame();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            if (dialog == null) {
                dialog = new OclQueryDialog(owner, "OCL Evaluation");
            }
            dialog.diagramCB.setSelected(true);
            dialog.browserCB.setSelected(true);
            dialog.getEditableListPanel().setResult("");
            dialog.setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
