package com.nanlabs.grails.plugin.logicaldelete;

import java.lang.reflect.Modifier;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class LogicalDeleteASTTRansformation implements ASTTransformation {

    private static final Log log = LogFactory.getLog(LogicalDeleteASTTRansformation.class);

    public final static String DELETED_FIELD_NAME = "deleted";
    public final static int CLASS_NODE_ORDER = 1;

    public final static String DOMAIN_CONSTRAINTS_PROPERTY = "constraints";
    public final static String DOMAIN_CONSTRAINT_NULLABLE_NAME = "nullable";

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!validate(nodes)) return;
        ClassNode classNode = (ClassNode) nodes[CLASS_NODE_ORDER];
        addDeletedProperty(classNode);
        implementDeletedDomainClassInterface(classNode);
    }

    private boolean validate(ASTNode[] nodes) {
        return nodes != null && nodes[0] != null && nodes[1] != null;
    }

    private void addDeletedProperty(ClassNode node) {
        if (!GrailsASTUtils.hasOrInheritsProperty(node, DELETED_FIELD_NAME)) {
            node.addProperty(DELETED_FIELD_NAME, Modifier.PUBLIC, new ClassNode(Date.class), null, null, null);
            PropertyNode constraintsField = node.getProperty(DOMAIN_CONSTRAINTS_PROPERTY);
            if (constraintsField != null) {
                if (constraintsField.getInitialExpression() instanceof ClosureExpression) {
                    ClosureExpression constraintClosure = (ClosureExpression) constraintsField.getInitialExpression();
                    Statement nullableConstraintExpression = getNullableConstraintExpression();
                    ((BlockStatement) constraintClosure.getCode()).addStatement(nullableConstraintExpression);
                } else {
                    log.error("Do not know how to add constraint expression to non ClosureExpression" + constraintsField.getInitialExpression());
                }
            } else {
                log.error("Domain class has not constraint closure. Pending to implement it");
            }
        }

    }

    private Statement getNullableConstraintExpression() {
        NamedArgumentListExpression argumentListExpression = new NamedArgumentListExpression();
        argumentListExpression.addMapEntryExpression(new MapEntryExpression(new ConstantExpression(DOMAIN_CONSTRAINT_NULLABLE_NAME), ConstantExpression.TRUE));
        MethodCallExpression mce = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, DELETED_FIELD_NAME, argumentListExpression);
        return new ExpressionStatement(mce);
    }

    private void implementDeletedDomainClassInterface(ClassNode node) {
        ClassNode iNode = new ClassNode(LogicalDeleteDomainClass.class);
        if (!iNode.implementsInterface(iNode)) {
            node.addInterface(iNode);
        }
    }

}
