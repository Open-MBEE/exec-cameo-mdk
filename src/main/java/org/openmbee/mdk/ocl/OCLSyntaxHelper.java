package org.openmbee.mdk.ocl;

import org.openmbee.mdk.util.Debug;
import lpg.runtime.IToken;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.expressions.*;
import org.eclipse.ocl.helper.Choice;
import org.eclipse.ocl.helper.ChoiceKind;
import org.eclipse.ocl.helper.ConstraintKind;
import org.eclipse.ocl.utilities.ExpressionInOCL;

import java.util.List;

public class OCLSyntaxHelper<PK, C, O, P, EL, PM, S, COA, SSA, CT, CLS, E> extends
        org.eclipse.ocl.internal.helper.OCLSyntaxHelper<PK, C, O, P, EL, PM, S, COA, SSA, CT, CLS, E> {

    protected class ASTVisitor extends org.eclipse.ocl.internal.helper.OCLSyntaxHelper.ASTVisitor {
        Object lastVisited = null;

        @Override
        protected ConstraintKind getConstraintType() {
            // TODO Auto-generated method stub
            return super.getConstraintType();
        }

        @Override
        public List visitAssociationClassCallExp(AssociationClassCallExp exp) {
            lastVisited = exp;
            visit("visitAssociationClassCallExp");
            // TODO Auto-generated method stub
            return super.visitAssociationClassCallExp(exp);
        }

        @Override
        public List visitBooleanLiteralExp(BooleanLiteralExp exp) {
            lastVisited = exp;
            visit("visitBooleanLiteralExp");
            // TODO Auto-generated method stub
            return super.visitBooleanLiteralExp(exp);
        }

        @Override
        public List visitCollectionItem(CollectionItem item) {
            lastVisited = item;
            visit("visitCollectionItem");
            // TODO Auto-generated method stub
            return super.visitCollectionItem(item);
        }

        @Override
        public List visitCollectionLiteralExp(CollectionLiteralExp exp) {
            lastVisited = exp;
            visit("visitCollectionLiteralExp");
            // TODO Auto-generated method stub
            return super.visitCollectionLiteralExp(exp);
        }

        @Override
        public List visitCollectionRange(CollectionRange range) {
            lastVisited = range;
            visit("visitCollectionRange");
            // TODO Auto-generated method stub
            return super.visitCollectionRange(range);
        }

        @Override
        public List visitConstraint(Object constraint) {
            lastVisited = constraint;
            visit("visitConstraint");
            // TODO Auto-generated method stub
            return super.visitConstraint(constraint);
        }

        @Override
        public List visitEnumLiteralExp(EnumLiteralExp enumliteralexp) {
            lastVisited = enumliteralexp;
            visit("visitEnumLiteralExp");
            // TODO Auto-generated method stub
            return super.visitEnumLiteralExp(enumliteralexp);
        }

        @Override
        public List visitExpressionInOCL(ExpressionInOCL expression) {
            lastVisited = expression;
            visit("visitExpressionInOCL");
            // TODO Auto-generated method stub
            return super.visitExpressionInOCL(expression);
        }

        @Override
        public List visitIfExp(IfExp exp) {
            lastVisited = exp;
            visit("visitIfExp");
            // TODO Auto-generated method stub
            return super.visitIfExp(exp);
        }

        @Override
        public List visitIntegerLiteralExp(IntegerLiteralExp exp) {
            lastVisited = exp;
            visit("visitIntegerLiteralExp");
            // TODO Auto-generated method stub
            return super.visitIntegerLiteralExp(exp);
        }

        @Override
        public List visitInvalidLiteralExp(InvalidLiteralExp il) {
            lastVisited = il;
            visit("visitInvalidLiteralExp");
            // TODO Auto-generated method stub
            return super.visitInvalidLiteralExp(il);
        }

        @Override
        public List visitIterateExp(IterateExp exp) {
            lastVisited = exp;
            visit("visitIterateExp");
            // TODO Auto-generated method stub
            return super.visitIterateExp(exp);
        }

        @Override
        public List visitIteratorExp(IteratorExp exp) {
            lastVisited = exp;
            visit("visitIteratorExp");
            // TODO Auto-generated method stub
            return super.visitIteratorExp(exp);
        }

        @Override
        public List visitLetExp(LetExp letexp) {
            lastVisited = letexp;
            visit("visitLetExp");
            // TODO Auto-generated method stub
            return super.visitLetExp(letexp);
        }

        @Override
        public List visitMessageExp(MessageExp m) {
            lastVisited = m;
            visit("visitMessageExp");
            // TODO Auto-generated method stub
            return super.visitMessageExp(m);
        }

        @Override
        public List visitNullLiteralExp(NullLiteralExp il) {
            lastVisited = il;
            visit("visitNullLiteralExp");
            // TODO Auto-generated method stub
            return super.visitNullLiteralExp(il);
        }

        @Override
        public List visitOperationCallExp(OperationCallExp exp) {
            lastVisited = exp;
            visit("visitOperationCallExp");
            // TODO Auto-generated method stub
            return super.visitOperationCallExp(exp);
        }

        @Override
        public List visitPropertyCallExp(PropertyCallExp propertycallexp) {
            lastVisited = propertycallexp;
            visit("visitPropertyCallExp");
            // TODO Auto-generated method stub
            return super.visitPropertyCallExp(propertycallexp);
        }

        @Override
        public List visitRealLiteralExp(RealLiteralExp exp) {
            lastVisited = exp;
            visit("visitRealLiteralExp");
            // TODO Auto-generated method stub
            return super.visitRealLiteralExp(exp);
        }

        @Override
        public List visitStateExp(StateExp s) {
            lastVisited = s;
            visit("visitStateExp");
            // TODO Auto-generated method stub
            return super.visitStateExp(s);
        }

        @Override
        public List visitStringLiteralExp(StringLiteralExp exp) {
            lastVisited = exp;
            visit("visitStringLiteralExp");
            // TODO Auto-generated method stub
            return super.visitStringLiteralExp(exp);
        }

        @Override
        public List visitTupleLiteralExp(TupleLiteralExp tupleliteralexp) {
            lastVisited = tupleliteralexp;
            visit("visitTupleLiteralExp");
            // TODO Auto-generated method stub
            return super.visitTupleLiteralExp(tupleliteralexp);
        }

        @Override
        public List visitTupleLiteralPart(TupleLiteralPart tp) {
            lastVisited = tp;
            visit("visitTupleLiteralPart");
            // TODO Auto-generated method stub
            return super.visitTupleLiteralPart(tp);
        }

        @Override
        public List visitTypeExp(TypeExp typeExp) {
            lastVisited = typeExp;
            visit("visitTypeExp");
            // TODO Auto-generated method stub
            return super.visitTypeExp(typeExp);
        }

        @Override
        public List visitUnlimitedNaturalLiteralExp(UnlimitedNaturalLiteralExp exp) {
            lastVisited = exp;
            visit("visitUnlimitedNaturalLiteralExp");
            // TODO Auto-generated method stub
            return super.visitUnlimitedNaturalLiteralExp(exp);
        }

        @Override
        public List visitUnspecifiedValueExp(UnspecifiedValueExp unspecifiedvalueexp) {
            lastVisited = unspecifiedvalueexp;
            visit("alueexp;visitUnspecifiedValueExp");
            // TODO Auto-generated method stub
            return super.visitUnspecifiedValueExp(unspecifiedvalueexp);
        }

        @Override
        public List visitVariable(Variable variabledeclaration) {
            lastVisited = variabledeclaration;
            visit("eclaration;visitVariable");
            // TODO Auto-generated method stub
            return super.visitVariable(variabledeclaration);
        }

        @Override
        public List visitVariableExp(VariableExp variableexp) {
            lastVisited = variableexp;
            visit("visitVariableExp");
            // TODO Auto-generated method stub
            return super.visitVariableExp(variableexp);
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return super.toString();
        }

        protected ASTVisitor(String text, int position, ConstraintKind constraintType) {
            super(text, position, constraintType);
            Debug.outln("constructed ASTVisitor(" + text + ", " + position + ", " + constraintType + ")");
        }

        public String visit(String m) {
            String s = "calling " + m + ", visiting " + lastVisited;
            System.out.println(s);
            return s;
        }

    }

    /**
     * @param env
     */
    public OCLSyntaxHelper(Environment env) {
        super(env);
    }

    @Override
    protected ASTVisitor createASTVisitor(ConstraintKind constraintType, String txt, int position) {
        return new ASTVisitor(txt, position, constraintType);
    }

    @Override
    protected Choice createChoice(String name, String description, ChoiceKind kind, Object element) {
        // TODO Auto-generated method stub
        return super.createChoice(name, description, kind, element);
    }

    @Override
    protected List<Choice> getChoices(OCLExpression<C> expression, ConstraintKind constraintType) {
        // TODO Auto-generated method stub
        return super.getChoices(expression, constraintType);
    }

    @Override
    protected String getDescription(Object namedElement) {
        // TODO Auto-generated method stub
        return super.getDescription(namedElement);
    }

    @Override
    protected Environment<PK, C, O, P, EL, PM, S, COA, SSA, CT, CLS, E> getEnvironment() {
        // TODO Auto-generated method stub
        return super.getEnvironment();
    }

    @Override
    protected List<Choice> getPropertyChoices(C eClass) {
        // TODO Auto-generated method stub
        return super.getPropertyChoices(eClass);
    }

    @Override
    public List<Choice> getSyntaxHelp(ConstraintKind constraintType, String txt) {
        // TODO Auto-generated method stub
        return super.getSyntaxHelp(constraintType, txt);
    }

    @Override
    protected boolean isOclIsInState(IToken token) {
        // TODO Auto-generated method stub
        return super.isOclIsInState(token);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
