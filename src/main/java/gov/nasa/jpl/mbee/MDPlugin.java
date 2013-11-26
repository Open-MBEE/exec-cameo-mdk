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

import gov.nasa.jpl.mbee.Configurator.Context;
import gov.nasa.jpl.mbee.lib.ClassUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.KeyStroke;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.evaluation.EvaluationConfigurator;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * An abstract MagicDraw {@link Plugin} that simplifies configuration. To create
 * a new plugin,
 * <ol>
 * <li>subclass this class, MDPlugin,
 * <li>redefine {@link #initConfigurations()} to add menu labels that invoke
 * your code, and
 * <li>deploy to a subdirectory of MagicDraw's plugins directory, editing your
 * plugin xml to point to your subclass as the plugin to load. See other plugin
 * subdirectories for examples.
 * </ol>
 */
public abstract class MDPlugin extends Plugin {
    // TODO? implements ProjectEventListener { // see DocGenPlugin and
    // ApplicationSyncEventSubscriber

    Configurator                        configForAll                  = new Configurator();
    ActionsConfiguratorsManager         acm                           = null;
    protected boolean                   registeringBinaryImplementers = true;
    protected Class<? extends NMAction> nmActionClass                 = null;

    // protected

    // protected boolean listeningToProjects = true;

    // /** Makes sure application event listeners are only set up once. */
    // private static final AtomicBoolean subscribed = new AtomicBoolean(false);
    //
    // /** Keeps track of project-specific event subscribers */
    // //private static final Map<String, ProjectSyncEventSubscriber> projects =
    // new ConcurrentHashMap<String, ProjectSyncEventSubscriber>();
    // //private static final Map<String, Boolean> commitStates = new
    // ConcurrentHashMap<String, Boolean>();

    /**
     * Redefine to specify menu configurations. For example, the body may look
     * something like this:
     * 
     * <pre>
     * {
     *     &#064;code
     *     // Find the Method to be invoked from a menu selection--in this case, the
     *     // TestPlugin.ElementUtils has only one method, so just grab the first.
     *     Method copyElementMethod = TestPlugin.ElementUtils.class.getDeclaredMethods()[0];
     *     // Add the Method in MagicDraw's main menu.
     *     addConfiguration(&quot;MainMenu&quot;, &quot;&quot;, &quot;copyElement&quot;, &quot;Element Utils&quot;, copyElementMethod);
     * 
     *     // Get the copyPackage() Method more carefully.
     *     copyPackageMethod = ClassUtils.getMethodForArgTypes(TestPlugin.PackageUtils.class, &quot;copyPackage&quot;,
     *             ActionEvent.class, Element.class);
     *     // Make the action available from right-clicking Elements in the Containment
     *     // Browser and two different types of diagrams.
     *     addConfiguration(&quot;ContainmentBrowserContext&quot;, &quot;&quot;, &quot;copyPackage&quot;, &quot;Package Utils&quot;, copyPackageMethod);
     *     addConfiguration(&quot;BaseDiagramContext&quot;, &quot;Class Diagram&quot;, &quot;copyPackage&quot;, &quot;Package Utils&quot;, copyPackageMethod);
     *     addConfiguration(&quot;BaseDiagramContext&quot;, &quot;Activity Diagram&quot;, &quot;copyPackage&quot;, &quot;Package Utils&quot;,
     *             copyPackageMethod);
     * }
     * </pre>
     */
    public abstract void initConfigurations();

    /**
     * Add a configuration, a context for the action name to appear in new or
     * existing category where the static action {@link Method} will be invoked.
     * For example,
     * 
     * <pre>
     * {@code
     * addConfiguration( "BaseDiagramContext", "Class Diagram", "copyPackage",
     *                   "Package Utils", copyPackageMethod );
     * }
     * </pre>
     * 
     * @param context
     *            the place in the application (diagram, browser, or menu) where
     *            the action is to be available; this name should match a
     *            {@link Context} exactly.
     * @param subcontext
     *            a more specific context within that named by the
     *            {@code context} parameter; for example, the type of diagram.
     * @param actionName
     *            the label for an item in the menu (available in a window menu
     *            of the application or a right-click menu according to the
     *            context) that, when selected, invokes the {@code actionMethod}
     * @param category
     *            a higher level menu item label that, when selected, provides a
     *            drop-down menu with the {@code actionName} item.
     * @param actionMethod
     *            a static {@link Method} with ActionEvent and Element parameter
     *            types that is invoked from a menu in this context.
     * @return
     */
    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod) {
        return configForAll.addConfiguration(context, subcontext, actionName, category, actionMethod);
    }

    public MDAction addConfiguration(Configurator.Context context, String subcontext, String actionName,
            String category, Method actionMethod) {
        return configForAll.addConfiguration(context.toString(), subcontext, actionName, category,
                actionMethod);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod) {
        return configForAll.addConfiguration(context, subcontext, actionName, category, actionMethod,
                objectInvokingMethod);
    }

    public MDAction addConfiguration(Configurator.Context context, String subcontext, String actionName,
            String category, Method actionMethod, Object objectInvokingMethod) {
        return configForAll.addConfiguration(context.toString(), subcontext, actionName, category,
                actionMethod, objectInvokingMethod);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod, String id, KeyStroke k, String group) {
        return configForAll.addConfiguration(context, subcontext, actionName, category, actionMethod,
                objectInvokingMethod, id, k, group);
    }

    public MDAction addConfiguration(Configurator.Context context, String subcontext, String actionName,
            String category, Method actionMethod, Object objectInvokingMethod, String id, KeyStroke k,
            String group) {
        return configForAll.addConfiguration(context.toString(), subcontext, actionName, category,
                actionMethod, objectInvokingMethod, id, k, group);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod, String id, Integer mnemonic, String group) {
        return configForAll.addConfiguration(context, subcontext, actionName, category, actionMethod,
                objectInvokingMethod, id, mnemonic, group);
    }

    public MDAction addConfiguration(Configurator.Context context, String subcontext, String actionName,
            String category, Method actionMethod, Object objectInvokingMethod, String id, Integer mnemonic,
            String group) {
        return configForAll.addConfiguration(context.toString(), subcontext, actionName, category,
                actionMethod, objectInvokingMethod, id, mnemonic, group);
    }

    public MDAction addConfiguration(String context, String subcontext, String actionName, String category,
            Method actionMethod, Object objectInvokingMethod, String id, Integer mnemonic, KeyStroke k,
            String group) {
        return configForAll.addConfiguration(context, subcontext, actionName, category, actionMethod,
                objectInvokingMethod, id, mnemonic, k, group);
    }

    public MDAction addConfiguration(Configurator.Context context, String subcontext, String actionName,
            String category, Method actionMethod, Object objectInvokingMethod, String id, Integer mnemonic,
            KeyStroke k, String group) {
        return configForAll.addConfiguration(context.toString(), subcontext, actionName, category,
                actionMethod, objectInvokingMethod, id, mnemonic, k, group);
    }

    /**
   * 
   */
    public MDPlugin() {
        super();
    }

    public MDPlugin(Class<? extends NMAction> cls) {
        super();
        nmActionClass = cls;
    }

    @SuppressWarnings("unchecked")
    public <T extends NMAction> void nmActionMethod(ActionEvent event, Element element) {
        T action = null;
        Constructor<T> ctor = (Constructor<T>)ClassUtils.getConstructorForArgs(nmActionClass,
                new Object[] {element});
        try {
            if (ctor == null || Utils2.isNullOrEmpty(ctor.getParameterTypes())) {
                action = (T)nmActionClass.newInstance();
            } else {
                action = ctor.newInstance(element);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        action.actionPerformed(event);
    }

    public static Method getNmActionMethod() {
        return ClassUtils.getMethodsForName(MDPlugin.class, "nmActionMethod")[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nomagic.magicdraw.plugins.Plugin#close()
     */
    @Override
    public boolean close() {
        Debug.errln("closing " + getClass().getSimpleName());
        Debug.outln("closing " + getClass().getSimpleName());
        return true;
    }

    /*
     * No need to override this method.
     * 
     * (non-Javadoc)
     * 
     * @see com.nomagic.magicdraw.plugins.Plugin#init()
     */
    @Override
    public void init() {
        // Debug.turnOn();
        Debug.errln("initializing " + getClass().getSimpleName());
        Debug.outln("initializing " + getClass().getSimpleName());

        initConfigurations(); // Override this called method, not init() unless
                              // this
                              // is called as super.init();

        acm = ActionsConfiguratorsManager.getInstance();

        Map<String, Map<String, MDAction>> actionCategories = null;
        Map<String, Map<String, Map<String, MDAction>>> subcontexts = null;

        for (Configurator.Context context: Configurator.getContexts()) {
            subcontexts = configForAll.getMenus().get(context);

            for (Entry<String, Map<String, Map<String, MDAction>>> e: subcontexts.entrySet()) {
                actionCategories = e.getValue();

                // Create a separate configurator object for each configuration
                // since
                // some can interact (at least with the one for the main menu).
                if (!Utils2.isNullOrEmpty(actionCategories)) {
                    Configurator c = new Configurator();
                    Map<String, Map<String, Map<String, MDAction>>> newSubcontext = new TreeMap<String, Map<String, Map<String, MDAction>>>();
                    newSubcontext.put(e.getKey(), e.getValue());

                    c.getMenus().put(context, newSubcontext);
                    addConfigurator(c, context, e.getKey());
                }
            }
        }
        // This somehow allows things to be loaded to evaluate opaque
        // expressions or something.
        if (registeringBinaryImplementers) {
            EvaluationConfigurator.getInstance().registerBinaryImplementers(this.getClass().getClassLoader());
        }
        // TODO -- Subscribe as a ProjectEventListener; see DocGenPlugin and
        // ApplicationSyncEventSubscriber
        // if ( listeningToProjects ) {
        // subscribe();
        // }
    }

    protected void addConfigurator(Configurator cfgtor, Configurator.Context context) {
        addConfigurator(cfgtor, context, null);
    }

    protected void addConfigurator(Configurator cfgtor, Configurator.Context context, String subcontext) {

        // manager to whom configurator is added
        ActionsConfiguratorsManager acm = ActionsConfiguratorsManager.getInstance();

        if (acm == null) {
            Debug.error(true, true, "Error! addConfigurator(): No ActionsConfiguratorsManager!");
            return;
        }

        // Use reflection to find and invoke the acm.add*Configurator() method
        // corresponding to this context.
        Method addMethod = context.getAddConfiguratorMethod(subcontext);
        if (addMethod == null) {
            Debug.error(true, false, "Error! addConfigurator(" + cfgtor + ", " + context + ", " + subcontext
                    + "): Could not find add-configurator method for context=" + context + " and subcontext="
                    + subcontext);
            return;
        }

        // create argument array for the add method
        List<Object> arguments = new ArrayList<Object>();
        for (int j = 0; j < addMethod.getParameterTypes().length; ++j) {
            Class<?> pType = addMethod.getParameterTypes()[j];
            if (pType == null) {
                Debug.error(false, "null parameter type in Method " + addMethod);
                arguments.add(null);
            } else if (pType.equals(String.class)) {
                arguments.add(subcontext);
            } else if (ConfiguratorWithPriority.class.isAssignableFrom(pType) || pType.isInstance(cfgtor)) {
                arguments.add(cfgtor);
            } else {
                Debug.error(true, true, "Error! addConfigurator(" + cfgtor + ", " + context + ", "
                        + subcontext + "): Unexpected " + pType.getSimpleName()
                        + " parameter type in add method, " + addMethod.getName() + "()=" + addMethod
                        + ", for context " + context);
                arguments.add(null);
            }
        }
        ClassUtils.runMethod(false, acm, addMethod, arguments.toArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nomagic.magicdraw.plugins.Plugin#isSupported()
     */
    @Override
    public boolean isSupported() {
        return true;
    }

    /**
     * @return the registeringBinaryImplementers
     */
    public boolean isRegisteringBinaryImplementers() {
        return registeringBinaryImplementers;
    }

    /**
     * @param registeringBinaryImplementers
     *            the registeringBinaryImplementers to set
     */
    public void setRegisteringBinaryImplementers(boolean registeringBinaryImplementers) {
        this.registeringBinaryImplementers = registeringBinaryImplementers;
    }

    // REVIEW -- Consider providing services from DocGenPlugin's
    // ApplicationSyncEventSubscriber.

}
