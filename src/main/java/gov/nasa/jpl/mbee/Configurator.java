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
package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.lib.ClassUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.MostAbstractFirst;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.junit.Assert;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.BrowserToolbarAMConfigurator;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.actions.DiagramContextToolbarAMConfigurator;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.actions.MenuCreatorFactory;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.DiagramType;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * A general configurator for MagicDraw. Here's how to use it:
 * 
 * Configurator c = new Configurator(); Method copyElementMethod =
 * ElementUtils.class.getDeclaredMethods()[ 0 ]; c.addConfiguration( "General",
 * "copyElement", "Element Utils", copyElementMethod ); Method copyPackageMethod
 * = PackageUtils.class.getDeclaredMethods()[ 0 ]; c.addConfiguration(
 * "BrowserToolbar", "copyPackage", "Package Utils", copyPackageMethod );
 * ActionsConfiguratorsManager acm = ActionsConfiguratorsManager.getInstance();
 * acm.addContainmentBrowserContextConfigurator(c);
 * acm.addBaseDiagramContextConfigurator("Class Diagram", c);
 * acm.addBaseDiagramContextConfigurator("Activity Diagram", c);
 */
public class Configurator implements ConfiguratorWithPriority, BrowserContextAMConfigurator,
        DiagramContextAMConfigurator, AMConfigurator, BrowserToolbarAMConfigurator,
        DiagramContextToolbarAMConfigurator {
    /*
     * public static class DCTAC implements DiagramContextToolbarAMConfigurator
     * {
     * 
     * @Override public int getPriority() { // TODO Auto-generated method stub
     * return 0; }
     * 
     * @Override public void configure( ActionsManager paramActionsManager,
     * PresentationElement paramPresentationElement ) { // TODO Auto-generated
     * method stub
     * 
     * }
     * 
     * } public static class BTAC implements BrowserToolbarAMConfigurator {
     * 
     * @Override public int getPriority() { // TODO Auto-generated method stub
     * return 0; }
     * 
     * @Override public void configure( ActionsManager paramActionsManager, Tree
     * paramTree ) { // TODO Auto-generated method stub
     * 
     * }
     * 
     * } public static class DCAC implements DiagramContextAMConfigurator {
     * 
     * @Override public int getPriority() { // TODO Auto-generated method stub
     * return 0; }
     * 
     * @Override public void configure( ActionsManager paramActionsManager,
     * DiagramPresentationElement paramDiagramPresentationElement,
     * PresentationElement[] paramArrayOfPresentationElement,
     * PresentationElement paramPresentationElement ) { // TODO Auto-generated
     * method stub
     * 
     * }
     * 
     * }
     * 
     * public static class AC implements AMConfigurator {
     * 
     * @Override public int getPriority() { // TODO Auto-generated method stub
     * return 0; }
     * 
     * @Override public void configure( ActionsManager paramActionsManager ) {
     * // TODO Auto-generated method stub
     * 
     * }
     * 
     * } public static class BCAC implements BrowserContextAMConfigurator {
     * 
     * @Override public int getPriority() { // TODO Auto-generated method stub
     * return 0; }
     * 
     * @Override public void configure( ActionsManager paramActionsManager, Tree
     * paramTree ) { // TODO Auto-generated method stub
     * 
     * }
     * 
     * }
     */

    public static Context lastContext             = null;
    protected static boolean lastContextIsDiagram = false;
    //protected static boolean invokedFromMenu      = false;

    /**
     * A Context is a place from which the user accesses menus that are
     * populated by configurators.
     * 
     */
    // public static enum Context {
    // Browser, BrowserToolbar, Diagram, DiagramToolbar, General;
    public static enum Context {
        // ContainmentBrowser, ContainmentBrowserToolbar, StructureBrowser,
        // StructureBrowserToolbar, InheritanceBrowser,
        // InheritanceBrowserToolbar, DiagramsBrowser, DiagramsBrowserToolbar,
        // Browser, BrowserToolbar, Diagram, DiagramToolbar, General;
        BaseDiagramContext, BaseDiagramContextToolbar, ContainmentBrowserContext,
        ContainmentBrowserShortcuts, ContainmentBrowserToolbar, CustomizableShortcuts, DiagramCommandBar,
        DiagramContext, DiagramContextToolbar, DiagramsBrowserContext, DiagramsBrowserShortcuts,
        DiagramsBrowserToolbar, DiagramShortcuts, DiagramToolbarActionsProvider, DiagramToolbar,
        ExtensionsBrowserContext, ExtensionsBrowserShortcuts, ExtensionsBrowserToolbar,
        InheritanceBrowserContext, InheritanceBrowserShortcuts, InheritanceBrowserToolbar,
        LockViewBrowserContext, LockViewBrowserShortcuts, LockViewBrowserToolbar, MainMenu, MainShortcuts,
        MainToolbar, SearchBrowserContext, SearchBrowserShortcuts, SearchBrowserToolbar, TargetElementAM;

        public Method getAddConfiguratorMethod(String subcontext) {
            // find the acm.add*Configurator() method corresponding to this
            // context
            String addMethodString = "add" + toString()
                    + (this.equals(DiagramToolbarActionsProvider) ? "" : "Configurator");

            Method[] addMethods = ClassUtils.getMethodsForName(ActionsConfiguratorsManager.class,
                    addMethodString);
            if (addMethods == null || addMethods.length == 0) {
                Debug.error(true, true, "Error! " + toString() + ".getAddConfiguratorMethod(" + subcontext
                        + "): add method, " + addMethodString + ", for context " + toString() + " not found");
                return null;
            }
            Method addMethod = addMethods[0];
            if (addMethods.length > 1 && addMethod.getParameterTypes().length > 1 && subcontext == null) {
                // find that doesn't take an extra String argument (subcontext)
                for (int i = 1; i < addMethods.length; ++i) {
                    if (addMethods[i].getParameterTypes() != null
                            && addMethods[i].getParameterTypes().length == 1) {
                        // Assert.assertTrue(
                        // Configurator.class.isAssignableFrom( addMethods[ i
                        // ].getParameterTypes()[ 0 ] ) );
                        addMethod = addMethods[i];
                        break;
                    }
                }
            }
            return addMethod;
        }

        public static Context fromString(String contextString) {
            for (Context c: contexts) {
                if (contextString.toLowerCase().equals(c.toString().toLowerCase()))
                    return c;
            }
            return null;
        }
    }

    public static enum DiagramContext {
        Activity, BlockDefinition, Class, Communication, // UML only
        Component, // UML only
        CompositeStructure, // UML only
        Deployment, // UML only
        InteractionOverview, // UML Only
        InternalBlockDefinition, Object, // UML only
        Package, Parametric, // SysML only
        Profile, Requirement, // SysML only
        Sequence, StateMachine, Timing, // UML only
        UseCase;
        public Method getAddConfiguratorMethod() {
            return Context.BaseDiagramContext.getAddConfiguratorMethod(toString());
        }
    }

    public static ActionEvent lastActionEvent = null;

    public static class GenericMDAction extends MDAction {

        private static final long serialVersionUID     = 6943254131859224755L;

        Method                    actionMethod         = null;
        Object                    objectInvokingAction = null;

        public GenericMDAction(String id, String name, Integer mnemonic, String group, Method actionMethod,
                Object objectInvokingMethod) {
            super(id, name, mnemonic, group);
            Debug.outln("GenericMDAction( id=" + id + ", name=" + name + ", mnemonic=" + mnemonic
                    + ", group=" + group + ", actionMethod="
                    + ((actionMethod == null) ? "null" : actionMethod.getName()) + ", objectInvokingMethod="
                    + objectInvokingMethod + " )");
            this.actionMethod = actionMethod;
            this.objectInvokingAction = objectInvokingMethod;
        }

        public GenericMDAction(String id, String name, KeyStroke keyStroke, String group,
                Method actionMethod, Object objectInvokingMethod) {
            super(id, name, keyStroke, group);
            Debug.outln("GenericMDAction( id=" + id + ", name=" + name + ", keyStroke=" + keyStroke
                    + ", group=" + group + ", actionMethod="
                    + ((actionMethod == null) ? "null" : actionMethod.getName()) + ", objectInvokingMethod="
                    + objectInvokingMethod + " )");
            this.actionMethod = actionMethod;
            this.objectInvokingAction = objectInvokingMethod;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // super.actionPerformed( actionEvent ); // don't call super -- it
            // tries
            // to execute
            // com.nomagic.magicdraw.actions.ActionsExecuter.execute(NMAction,
            // ActionEvent)
            // actionPerformed()
            // }
            //
            // public static void actionPerformed( ActionEvent e, Method
            // actionMethod,
            // Object objectInvokingAction ) {
            if (actionMethod == null)
                return;
            ArrayList<Object> args = new ArrayList<Object>();
            if (actionMethod.getParameterTypes() != null && actionMethod.getParameterTypes().length > 0) {
                for (Class<?> c: actionMethod.getParameterTypes()) {
                    if (c.equals(ActionEvent.class) || c.equals(AWTEvent.class)
                            || c.equals(EventObject.class)) {
                        args.add(actionEvent);
                    } else {
                        if (c.equals(Element.class)) {
                            // passs null for Element
                            args.add(null);
                        } else {
                            // unrecognized argument
                            Debug.error(true, false, "Warning! Action " + actionMethod.getName()
                                    + " is getting passed null " + c.getSimpleName() + "!");
                        }
                    }
                }
            }
            lastActionEvent  = actionEvent;
            // Pair< Boolean, Object > p =
            ClassUtils.runMethod(false, objectInvokingAction, actionMethod, args.toArray());
        }

    }

    /**
     * An array of the Context constants
     */
    protected static Context[] contexts = getContexts();

    public static Context[] getContexts() {
        if (contexts == null)
            contexts = new Context[] { // Context.Browser,
                    // Context.BrowserToolbar,
                    // Context.Diagram,
                    // Context.DiagramToolbar,
                    // Context.General
                    Context.BaseDiagramContext, Context.BaseDiagramContextToolbar,
                    Context.ContainmentBrowserContext, Context.ContainmentBrowserShortcuts,
                    Context.ContainmentBrowserToolbar, Context.CustomizableShortcuts,
                    Context.DiagramCommandBar, Context.DiagramCommandBar, Context.DiagramContext,
                    Context.DiagramContextToolbar, Context.DiagramsBrowserContext,
                    Context.DiagramsBrowserShortcuts, Context.DiagramsBrowserToolbar,
                    Context.DiagramShortcuts, Context.DiagramToolbarActionsProvider, Context.DiagramToolbar,
                    Context.ExtensionsBrowserContext, Context.ExtensionsBrowserShortcuts,
                    Context.ExtensionsBrowserToolbar, Context.InheritanceBrowserContext,
                    Context.InheritanceBrowserShortcuts, Context.InheritanceBrowserToolbar,
                    Context.LockViewBrowserContext, Context.LockViewBrowserShortcuts,
                    Context.LockViewBrowserToolbar, Context.MainMenu, Context.MainShortcuts,
                    Context.MainToolbar, Context.SearchBrowserContext, Context.SearchBrowserShortcuts,
                    Context.SearchBrowserToolbar, Context.TargetElementAM};
        return contexts;
    }

    /**
     * An array of the Context constants
     */
    protected static DiagramContext[] diagramContexts = getDiagramContexts();

    public static DiagramContext[] getDiagramContexts() {
        if (diagramContexts == null)
            diagramContexts = new DiagramContext[] {DiagramContext.Activity, DiagramContext.BlockDefinition,
                    DiagramContext.Class, DiagramContext.Communication, // UML
                                                                        // only
                    DiagramContext.Component, // UML only
                    DiagramContext.CompositeStructure, // UML only
                    DiagramContext.Deployment, // UML only
                    DiagramContext.InteractionOverview, // UML Only
                    DiagramContext.InternalBlockDefinition, DiagramContext.Object, // UML
                                                                                   // only
                    DiagramContext.Package, DiagramContext.Parametric, // SysML
                                                                       // only
                    DiagramContext.Profile, DiagramContext.Requirement, // SysML
                                                                        // only
                    DiagramContext.Sequence, DiagramContext.StateMachine, DiagramContext.Timing, // UML
                                                                                                 // only
                    DiagramContext.UseCase};
        return diagramContexts;
    }

    public static Map<Configurator.Context, Class<?>> typeForContext = initTypeForContext();

    public static Map<Configurator.Context, Class<?>> getTypeForContext() {
        if (typeForContext == null)
            initTypeForContext();
        return typeForContext;
    }

    public static Map<Configurator.Context, Class<?>> initTypeForContext() {
        // Debug.turnOn();
        typeForContext = new HashMap<Configurator.Context, Class<?>>();
        for (Context c: getContexts()) {
            if (c == null) {
                Debug.error("Error! Null context from getContexts()?!!");
            }
            Method addMethod = c.getAddConfiguratorMethod("");
            Class<?> cls = Configurator.class;
            Class<?> configCls = Configurator.class;
            Class<?>[] pTypes = null;
            if (addMethod != null)
                pTypes = addMethod.getParameterTypes();
            if (pTypes != null) {
                int j;
                for (j = 0; j < pTypes.length; ++j) {
                    Class<?> pType = pTypes[j];
                    if (pType != null && pType.isAssignableFrom(configCls)) {// configCls.isAssignableFrom(
                                                                             // pType
                                                                             // )
                                                                             // )
                                                                             // {
                        cls = addMethod.getParameterTypes()[j];
                        break;
                    }
                }
                if (j < pTypes.length) {
                    Debug.error(false, "No type for context! " + c);
                }
            } else {
                Debug.error("No add config method found for " + c);
            }
            Debug.outln("typeForContext.put(" + c + ", " + cls.getName() + ")");
            typeForContext.put(c, cls);
        }
        Debug.outln("typeForContext = " + MoreToString.Helper.toString(typeForContext));
        return typeForContext;
    }

    public static Map<Class<?>, Set<Configurator.Context>> contextsForType = initContextsForType();

    public static Map<Class<?>, Set<Configurator.Context>> getContextsForType() {
        if (contextsForType == null)
            initContextsForType();
        return contextsForType;
    }

    public static Map<Class<?>, Set<Configurator.Context>> initContextsForType() {
        contextsForType = new HashMap<Class<?>, Set<Context>>();
        for (Entry<Context, Class<?>> e: getTypeForContext().entrySet()) {
            Set<Configurator.Context> theContexts = contextsForType.get(e.getValue());
            if (theContexts == null) {
                theContexts = new HashSet<Configurator.Context>();
                Debug.outln("contextsForType.put(" + e.getValue() + ", " + theContexts + ")");
                contextsForType.put(e.getValue(), theContexts);
            }
            Debug.outln("theContexts.add(" + e.getKey() + ")");
            theContexts.add(e.getKey());
        }
        Debug.outln("contextsForType = " + MoreToString.Helper.toString(contextsForType));
        return contextsForType;
    }

    /**
     * A map from contexts (e.g., DiagramToolbar) to sub-contexts (e.g.,
     * "Class Diagram") to categories to menu item names to actions.
     */
    Map<Context, Map<String, Map<String, Map<String, MDAction>>>> menus = null;

    /**
     * @param menus
     *            a map from contexts (General, Browser, Diagram,
     *            BrowserToolbar, DiagramToolbar) to categories to menu item
     *            names to actions.
     */
    public Configurator(Map<Context, Map<String, Map<String, Map<String, MDAction>>>> menus) {
        // this();
        this.menus = menus;
        if (menus == null)
            initMenus();
    }

    public Configurator() {
        initMenus();
    }

    public Map<Context, Map<String, Map<String, Map<String, MDAction>>>> getMenus() {
        if (menus == null)
            initMenus();
        return menus;
    }

    public void initMenus() {
        menus = new TreeMap<Context, Map<String, Map<String, Map<String, MDAction>>>>();
        for (Context c: getContexts()) {
            menus.put(c, new TreeMap<String, Map<String, Map<String, MDAction>>>());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nomagic.magicdraw.actions.ConfiguratorWithPriority#getPriority()
     */
    @Override
    public int getPriority() {
        return MEDIUM_PRIORITY;
    }

    /**
     * @return the lastContextIsDiagram
     */
    public static boolean isLastContextDiagram() {
        return lastContextIsDiagram;
    }

    /**
     * @param lastContextIsDiagram the lastContextIsDiagram to set
     */
    public static void setLastContextIsDiagram( boolean lastContextIsDiagram ) {
        Configurator.lastContextIsDiagram = lastContextIsDiagram;
    }

    /**
     * @return invokedFromMenu
     */
    public static boolean isInvokedFromMainMenu() {
        // HACK -- FIXME
        if ( lastActionEvent != null ) {
            Object source = lastActionEvent.getSource();
            if ( source instanceof Component ) {
                source = ( (Component)source ).getParent();
                if ( source instanceof JPopupMenu ) {
                    Component invoker = ( (JPopupMenu)source ).getInvoker();
                    if ( invoker != null ) {
                        String invokerClassName = invoker.getName();
                        invokerClassName = invoker.getClass().getName();
                        if ( invokerClassName.contains( "MenuCreatorFactory" ) ) {
                            // if ( invoker instanceof MenuCreatorFactory ) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nomagic.magicdraw.actions.DiagramContextToolbarAMConfigurator#configure
     * (com.nomagic.actions.ActionsManager,
     * com.nomagic.magicdraw.uml.symbols.PresentationElement)
     */
    @Override
    public void configure(ActionsManager manager, PresentationElement diagram) {
        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Debug.outln("configure(manager=" + manager + ", diagram=" + diagram
                + ") for DiagramContextToolbarAMConfigurator");
        Debug.errln("configure(manager=" + manager + ", diagram=" + diagram
                + ") for DiagramContextToolbarAMConfigurator");
        if (wasOn)
            Debug.turnOn();
        lastContext = Context.DiagramContext;
        setLastContextIsDiagram( true );
        //invokedFromMenu = false;
        if (diagram instanceof DiagramPresentationElement) {
            // Configurator.Context context =
            // getContextForType( DiagramContextToolbarAMConfigurator.class );
            Pair<Context, String> p = getContextForType(DiagramContextAMConfigurator.class,
                    ((DiagramPresentationElement)diagram).getDiagramType().getType());
            lastContext = p.first;
            addDiagramActions(manager, diagram, getMenus().get(p.first).get(p.second));
        } else {
            Assert.assertTrue(false);
        }
    }

    /**
     * More than one context may may be associated with a configurator type.
     * Just pick the first one found in the menus map.
     * 
     * @param cls
     * @return
     */
    protected Context getContextForType(Class<?> cls) {
        return getContextForType(cls, null).first;
    }

    /**
     * Get the contexts stored for the class most closely related to the input
     * class.
     * 
     * @param cls
     * @return
     */
    protected Set<Context> getContextsForType(Class<?> cls) {
        Set<Context> ctxts = getContextsForType().get(cls);
        if (Utils2.isNullOrEmpty(ctxts)) {
            TreeMap<Class<?>, Set<Context>> map = new TreeMap<Class<?>, Set<Context>>(
                    MostAbstractFirst.instance());
            for (Entry<Class<?>, Set<Context>> e: getContextsForType().entrySet()) {
                if (cls.isAssignableFrom(e.getKey())) {
                    map.put(e.getKey(), e.getValue());
                }
            }
            if (!map.isEmpty())
                return map.firstEntry().getValue();
            for (Entry<Class<?>, Set<Context>> e: getContextsForType().entrySet()) {
                if (e.getKey().isAssignableFrom(cls)) {
                    map.put(e.getKey(), e.getValue());
                }
            }
            if (!map.isEmpty())
                return map.firstEntry().getValue();
        }
        return ctxts;
    }

    /**
     * More than one context may may be associated with a configurator type.
     * Just pick the first one found in the menus map that has the closest
     * subcontext.
     * 
     * @param cls
     * @param subcontext
     * @return
     */
    protected Pair<Context, String> getContextForType(Class<?> cls, String subcontext) {
        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Pair<Context, String> pcs = null;
        Set<Context> ctxts = getContextsForType(cls);
        if (Utils2.isNullOrEmpty(ctxts)) {
            pcs = new Pair<Configurator.Context, String>(contexts[0], null);
            Debug.outln("getContextForType(" + cls + ", " + subcontext + "): no save contexts returning "
                    + pcs);
            if (wasOn)
                Debug.turnOn();
            return pcs;
        }
        Context contextKey[] = {null, null};
        int numMatch[] = {0, 0, 0, 0};
        int numDontMatch[] = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        String subcontextKey[] = {null, null};
        // get best matching subcontext
        for (Context c: ctxts) {
            Pair<Integer, Integer> p = prefixOverlapScore(c.toString(), subcontext);
            boolean betterContext = p.first > numMatch[0]
                    || (p.first == numMatch[0] && p.second < numDontMatch[0]);
            if (betterContext) {
                contextKey[0] = c;
                numMatch[0] = p.first;
                numDontMatch[0] = p.second;
            }
            int idx = (betterContext ? 1 : 3);
            int idxKey = (betterContext ? 0 : 1);
            // get best matching subcontext
            Map<String, Map<String, Map<String, MDAction>>> subcontexts = getMenus().get(c);
            if (!Utils2.isNullOrEmpty(subcontexts)) {
                if (subcontext != null) {
                    for (String subc: subcontexts.keySet()) {
                        Map<String, Map<String, MDAction>> configs = subcontexts.get(subc);
                        if (configs != null && !configs.isEmpty()) {
                            p = prefixOverlapScore(subc, subcontext);
                            boolean betterSubcontext = (p.first > numMatch[idx] || (p.first == numMatch[idx] && p.second < numDontMatch[idx]));
                            if (betterSubcontext) {
                                subcontextKey[idxKey] = subc;
                                if (!betterContext) {
                                    contextKey[1] = c;
                                }
                                numMatch[idx] = p.first;
                                numDontMatch[idx] = p.second;
                            }
                        }
                    }
                    // If match to subcontext is also better than any seen,
                    // record it.
                    if (betterContext) {
                        if (numMatch[1] > numMatch[3]
                                || (numMatch[1] == numMatch[3] && numDontMatch[1] < numDontMatch[3])) {
                            subcontextKey[1] = subcontextKey[0];
                            numMatch[3] = numMatch[1];
                            numDontMatch[3] = numDontMatch[1];
                            contextKey[1] = c;
                        }
                    }
                }
                // Map< String, Map< String, MDAction > > configs =
                // subcontexts.get(
                // subcontextKey );
                // if ( configs != null && !configs.isEmpty() ) {
                // return c;//new Pair< Configurator.Context, String >( c,
                // subcontextKey[0] );
                // }
            }
        }
        Debug.outln("getContextForType(" + cls + ", " + subcontext + "): numMatch="
                + MoreToString.Helper.toString(numMatch) + ", numDontMatch="
                + MoreToString.Helper.toString(numDontMatch) + ", contextKey="
                + MoreToString.Helper.toString(contextKey) + ", subcontextKey="
                + MoreToString.Helper.toString(subcontextKey));
        if (numMatch[0] > numMatch[3] || (numMatch[0] == numMatch[3] && numDontMatch[0] <= numDontMatch[3])) {
            pcs = new Pair<Configurator.Context, String>(contextKey[0], subcontextKey[0]);
            Debug.outln("getContextForType(" + cls + ", " + subcontext + "): got a winner! " + pcs);
            if (wasOn)
                Debug.turnOn();
            return pcs;
        }
        pcs = new Pair<Configurator.Context, String>(contextKey[1], subcontextKey[1]);
        Debug.outln("getContextForType(" + cls + ", " + subcontext + "): got a winner! " + pcs);
        if (wasOn)
            Debug.turnOn();
        return pcs;
    }

    public static Pair<Integer, Integer> prefixOverlapScore(String s1, String s2) {
        if (Utils2.isNullOrEmpty(s1) || Utils2.isNullOrEmpty(s2)) {
            return new Pair<Integer, Integer>(0, 0);
        }
        // String subcontextKey = null;
        int numMatch = 0;
        int numDontMatch = Integer.MAX_VALUE;
        if (s1.contains(s2)) {
            // if ( numMatch < s2.length() ) {
            // // subcontextKey = s2;
            numMatch = s2.length();
            numDontMatch = s1.length() - s2.length();
            // } else if ( numMatch == s2.length() ) {
            // if ( s1.length() - s2.length() < numDontMatch ) {
            // // subcontextKey = s2;
            // numDontMatch = s1.length() - s2.length();
            // }
            // }
        } else if (s2.contains(s1)) {
            // if ( numMatch < s1.length() ) {
            // // subcontextKey = s1;
            numMatch = s1.length();
            numDontMatch = s2.length() - s1.length();
            // } else if ( numMatch == s1.length() ) {
            // if ( s2.length() - s1.length() < numDontMatch ) {
            // // subcontextKey = s1;
            // numDontMatch = s2.length() - s1.length();
            // }
            // }
        }
        return new Pair<Integer, Integer>(numMatch, numDontMatch);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nomagic.actions.AMConfigurator#configure(com.nomagic.actions.
     * ActionsManager )
     */
    @Override
    public void configure(ActionsManager manager) {
        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Debug.outln("configure(manager=" + manager + ") for AMConfigurator");
        Debug.errln("configure(manager=" + manager + ") for AMConfigurator");
        if (wasOn)
            Debug.turnOn();
        Pair<Context, String> p = getContextForType(AMConfigurator.class, manager.getClass().getSimpleName());
        lastContext = p.first;
        //invokedFromMenu = true;
        //setLastContextIsDiagram( false );
        if (p == null || p.first == null || p.second == null) {
            Debug.errln("Could not addElementActions: getContextForType( AMConfigurator.class, \""
                    + manager.getClass().getSimpleName() + "\") returned " + p);
        } else {
            addElementActions(manager, null, getMenus().get(p.first).get(p.second));// Context.General
                                                                                    // )
                                                                                    // );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nomagic.magicdraw.actions.DiagramContextAMConfigurator#configure(com
     * .nomagic.actions.ActionsManager,
     * com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement,
     * com.nomagic.magicdraw.uml.symbols.PresentationElement[],
     * com.nomagic.magicdraw.uml.symbols.PresentationElement)
     */
    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement diagram,
            PresentationElement[] selected, PresentationElement requestor) {
        if ( DocGenConfigurator.repainting() ) {
            return;
        }
        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Debug.outln("configure(manager=" + manager + ", diagram=" + diagram + ", selected="
                + Utils2.toString(selected) + ", requestor=" + requestor
                + ") for DiagramContextAMConfigurator");
        Debug.errln("configure(manager=" + manager + ", diagram=" + diagram + ", selected="
                + Utils2.toString(selected) + ", requestor=" + requestor
                + ") for DiagramContextAMConfigurator");
        if (wasOn)
            Debug.turnOn();

        DiagramType dType = diagram.getDiagramType();
        Pair<Context, String> p = getContextForType(DiagramContextAMConfigurator.class, dType.getType());
        lastContext = p.first;
        setLastContextIsDiagram( true );
        //invokedFromMenu = false;
        if (p == null || p.first == null || p.second == null) {
            Debug.errln("Could not addElementActions: getContextForType( DiagramContextAMConfigurator.class, \""
                    + dType.getType() + "\") returned " + p);
        } else {
            if (requestor != null) {
                Element e = requestor.getElement();
                addElementActions(manager, e, getMenus().get(p.first).get(p.second));
            } else {
                addDiagramActions(manager, diagram, getMenus().get(p.first).get(p.second));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nomagic.magicdraw.actions.BrowserContextAMConfigurator#configure(com
     * .nomagic.actions.ActionsManager, com.nomagic.magicdraw.ui.browser.Tree)
     */
    @Override
    public void configure(ActionsManager manager, Tree browser) {
        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Debug.outln("configure(manager=" + manager + ", browser=" + browser
                + ") for BrowserContextAMConfigurator");
        Debug.errln("configure(manager=" + manager + ", browser=" + browser
                + ") for BrowserContextAMConfigurator");
        if (wasOn)
            Debug.turnOn();
        Node no = browser.getSelectedNode();
        lastContext = null;
        if (no == null) {
            Debug.errln("No selected node for adding action.");
            return;
        }
        Object o = no.getUserObject();
        if (!(o instanceof Element)) {
            Debug.errln("Selected node = " + no);
            Debug.errln("User object is not an Element but a " + (o == null ? "null" : o.getClass()) + ": "
                    + o);
            return;
        }
        String browserType = browser.getName();
        if (Utils2.isNullOrEmpty(browserType)) {
            browserType = browser.getClass().getSimpleName();
        }
        Pair<Context, String> p = getContextForType(BrowserContextAMConfigurator.class, browserType);
        lastContext = p.first;
        setLastContextIsDiagram( false );
        //invokedFromMenu = false;
        if (p == null || p.first == null || p.second == null) {
            Debug.errln("Could not addElementActions: getContextForType( BrowserContextAMConfigurator.class, \""
                    + browser.getName() + "\") returned " + p);
        } else {
            addElementActions(manager, (Element)o, getMenus().get(p.first).get(p.second));
        }
    }

    protected void addDiagramActions(ActionsManager manager, PresentationElement diagram,
            Map<String, Map<String, MDAction>> actionCategories) {
        if (diagram == null)
            return;
        Element element = diagram.getActualElement();

        if (element == null)
            return;
        Element owner = element.getOwner();
        if (owner == null || !(owner instanceof NamedElement))
            return;
        // Map< String, Map< String, MDAction > > > actionCategories =
        // subcontexts.get(element.)
        addElementActions(manager, owner, actionCategories);
    }

    protected void addElementActions(ActionsManager manager, Element e,
            Map<String, Map<String, MDAction>> actionCategories) {
        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Debug.outln("addElementActions( manager=" + manager + ", element=" + e + ", actionCategories="
                + actionCategories + ")");
        Debug.errln("addElementActions( manager=" + manager + ", element=" + e + ", actionCategories="
                + actionCategories + ")");
        if (wasOn)
            Debug.turnOn();
        for (Entry<String, Map<String, MDAction>> category: actionCategories.entrySet()) {
            ActionsCategory c = myCategory(manager, category.getKey(), category.getKey());
            for (Entry<String, MDAction> action: category.getValue().entrySet()) {
                c.addAction(action.getValue());
            }
        }
    }

    /**
     * Gets the specified category, creates it if necessary.
     * 
     * @param manager
     * @param id
     * @param name
     * @return category with given id/name
     */
    private ActionsCategory myCategory(ActionsManager manager, String id, String name) {
        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Debug.outln("myCategory( manager=" + manager + ", id=" + id + ", name=" + name + ")");
        if (wasOn)
            Debug.turnOn();
        ActionsCategory category = manager.getCategory(id); // .getActionFor(id);
        if (category == null) {
            category = new MDActionsCategory(id, name);
            category.setNested(true);
            manager.addCategory(category); // by default, it adds to end
            // manager.addCategory(0, category); // adds at front
        }
        return category;
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod) {
        return addConfiguration(context, subcontext, actionName, category, actionMethod, null);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod) {
        return addConfiguration(context, subcontext, actionName, category, actionMethod,
                objectInvokingMethod, "", (KeyStroke)null, null);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod, String id, KeyStroke k, String group) {
        return addConfiguration(context, subcontext, actionName, category, actionMethod,
                objectInvokingMethod, id, null, k, group);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod, String id, Integer mnemonic, String group) {
        return addConfiguration(context, subcontext, actionName, category, actionMethod,
                objectInvokingMethod, id, mnemonic, null, group);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod, String id, Integer mnemonic, KeyStroke k,
            String group) {
        MDAction mdAction = makeMDAction(id, actionName, mnemonic, k, group, actionMethod,
                objectInvokingMethod);
        if (mdAction == null)
            return null;
        Context c = Context.fromString(context);
        if (c == null) {
            Debug.error(true, true, "Error! addConfiguration( context=" + context + ", actionName="
                    + actionName + ", category=" + category + ", actionMethod=" + actionMethod
                    + ", objectInvokingMethod=" + objectInvokingMethod + ", id=" + id + ", mnemonic="
                    + mnemonic + ", k=" + k + ", group=" + category + " ): Unrecognized context, " + context);
            return null;
        }

        boolean wasOn = Debug.isOn();
        Debug.turnOff();
        Debug.outln("addConfiguration( context=" + context + ", actionName=" + actionName + ", category="
                + category + ", actionMethod=" + actionMethod + ", objectInvokingMethod="
                + objectInvokingMethod + ", id=" + id + ", mnemonic=" + mnemonic + ", k=" + k + ", group="
                + category + " )");
        if (wasOn)
            Debug.turnOn();

        Utils2.put(getMenus().get(c), subcontext, category, Utils2.isNullOrEmpty(id) ? actionName : id,
                mdAction);
        return mdAction;
    }

    public static MDAction makeMDAction(String id, String name, Integer mnemonic, KeyStroke k, String group,
            Method actionMethod, Object objectInvokingMethod) {
        if (actionMethod == null)
            return null;
        MDAction mda = null;
        if (mnemonic != null) {
            mda = new GenericMDAction(id, name, mnemonic, group, actionMethod, objectInvokingMethod);
        } else {
            mda = new GenericMDAction(id, name, k, group, actionMethod, objectInvokingMethod);
        }
        return mda;
    }

}
