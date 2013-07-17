/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.magicdraw.validation.ValidationRule;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.impl.PropertyNames;

/**
 * HERE!!!! TODO!!!
 * This class needs to implement SmartListenerConfigurationProvider like in
 * C:\Program Files\MagicDraw\SSCAE-MD17.0.2SP3-PackageK-build1039-20130425\openapi
 * \examples\validation\SmartListenerConfigurationProvider.java or
 * JavaConstantNameValidationRuleImpl.java.
 * 
 */
public class ValidateSomething implements ElementValidationRuleImpl, SmartListenerConfigurationProvider {

  private ArrayList< NMAction > mActions;

  public ValidateSomething() {
    mActions = new ArrayList<NMAction>();
    NMAction action = new NMAction("NO_INPUT_PARAMETERS", "No Input Parameters", null) {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed( ActionEvent paramActionEvent ) {
        JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Action " + getName() + " performed!");
     }
      
    };
    mActions.add(action);
    System.out.println("Constructing " + getClass().getName() + "!");
  }

  /* (non-Javadoc)
   * @see com.nomagic.magicdraw.validation.ValidationRule#init(com.nomagic.magicdraw.core.Project, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)
   */
  @Override
  public void init( Project paramProject, Constraint paramConstraint ) {
  }

  /* (non-Javadoc)
   * @see com.nomagic.magicdraw.validation.ValidationRule#dispose()
   */
  @Override
  public void dispose() {
  }

  /**
   * Executes the rule.
   * 
   * @param project
   *          a project of the constraint.
   * @param constraint
   *          constraint which defines validation rules.
   * @param elements
   *          collection of elements that have to be validated.
   * @return a set of <code>Annotation</code> objects which specifies invalid
   *         objects.
   */
  @Override
  public Set< Annotation > run( Project project, Constraint constraint,
                                Collection< ? extends Element > elements ) {
    Set<Annotation> result = new HashSet<Annotation>();
    if ( elements != null && !elements.isEmpty() ) {
      Annotation annotation = new Annotation(elements.iterator().next(), constraint, mActions);
      result.add( annotation );
    }
    return result;
  }

  @Override
  public Map< Class< ? extends Element >, Collection< SmartListenerConfig >>
      getListenerConfigurations() {
    Map< java.lang.Class< ? extends Element >, Collection< SmartListenerConfig >> configMap =
        new HashMap< java.lang.Class< ? extends Element >, Collection< SmartListenerConfig >>();
    Collection< SmartListenerConfig > configs =
        new ArrayList< SmartListenerConfig >();
    SmartListenerConfig config = new SmartListenerConfig();
    config.listenToNested( PropertyNames.ATTRIBUTE )
          .listenTo( PropertyNames.IS_STATIC )
          .listenTo( PropertyNames.IS_READ_ONLY ).listenTo( PropertyNames.NAME );
    configs.add( config );
    configMap.put( com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class,
                   configs );
    configMap.put( com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface.class,
                   configs );
    return configMap;
    //    Map< Class< ? extends Element >, Collection< SmartListenerConfig >> configMap =
//        new HashMap< Class< ? extends Element >, Collection< SmartListenerConfig >>();
//    SmartListenerConfig config = new SmartListenerConfig();
//    SmartListenerConfig nested = config.listenToNested( "ownedOperation" );
//    nested.listenTo( "name" );
//    nested.listenTo( "ownedParameter" );
//
//    Collection< SmartListenerConfig > configs =
//        Collections.singletonList( config );
//    configMap.put( com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class,
//                   configs );
//    configMap.put( Interface.class, configs );
//    return configMap;
  }

}
