/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.*;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an invocation of a static library method in the expression tree.
 */
public class ExprStaticMethodNode extends ExprNode implements ExprNodeInnerNodeProvider
{
    private static final Log log = LogFactory.getLog(ExprStaticMethodNode.class);
    private static final long serialVersionUID = -2237283743896280252L;

    private final String className;
    private final List<ExprChainedSpec> chainSpec;
    
    private final boolean isUseCache;
    private transient boolean isReturnsConstantResult;
    private transient ExprEvaluator evaluator;

    /**
	 * Ctor.
	 * @param chainSpec - the class and name of the method that this node will invoke plus parameters
     * @param isUseCache - configuration whether to use cache
	 */
	public ExprStaticMethodNode(String className, List<ExprChainedSpec> chainSpec, boolean isUseCache)
	{
        this.className = className;
		this.chainSpec = chainSpec;
        this.isUseCache = isUseCache;
    }

    @Override
    public ExprEvaluator getExprEvaluator() {
        return evaluator;
    }

    public String getClassName()
    {
        return className;
    }

    public List<ExprChainedSpec> getChainSpec()
    {
        return chainSpec;
    }

    @Override
    public boolean isConstantResult()
    {
        return isReturnsConstantResult;
    }

	public String toExpressionString()
	{
        StringBuilder buffer = new StringBuilder();
		buffer.append(className);
        ExprNodeUtility.toExpressionString(chainSpec, buffer);
		return buffer.toString();
	}

	public boolean equalsNode(ExprNode node)
	{
		if(!(node instanceof ExprStaticMethodNode))
		{
			return false;
		}
        ExprStaticMethodNode other = (ExprStaticMethodNode) node;
        if (other.chainSpec.size() != this.chainSpec.size()) {
            return false;
        }
        for (int i = 0; i < chainSpec.size(); i++) {
            if (!(this.chainSpec.get(i).equals(other.chainSpec.get(i)))) {
                return false;
            }
        }
        return other.className.equals(this.className);
	}

	public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
	{
        ExprNodeUtility.validate(chainSpec, streamTypeService, methodResolutionService, viewResourceDelegate, timeProvider, variableService, exprEvaluatorContext);

        // get first chain item
        List<ExprChainedSpec> chainList = new ArrayList<ExprChainedSpec>(chainSpec);
        ExprChainedSpec firstItem = chainList.remove(0);

		// Get the types of the parameters for the first invocation
		Class[] paramTypes = new Class[firstItem.getParameters().size()];
        ExprEvaluator[] childEvals = new ExprEvaluator[firstItem.getParameters().size()];
		int count = 0;
        
        boolean allConstants = true;
        for(ExprNode childNode : firstItem.getParameters())
		{
            ExprEvaluator eval = childNode.getExprEvaluator();
            childEvals[count] = eval;
			paramTypes[count] = eval.getType();
            count++;
            if (!(childNode.isConstantResult()))
            {
                allConstants = false;
            }
        }
        boolean isConstantParameters = allConstants && isUseCache;
        isReturnsConstantResult = isConstantParameters && chainList.isEmpty();

        // Try to resolve the method
        FastMethod staticMethod;
		try
		{
			Method method = methodResolutionService.resolveMethod(className, firstItem.getName(), paramTypes);
			FastClass declaringClass = FastClass.create(Thread.currentThread().getContextClassLoader(), method.getDeclaringClass());
			staticMethod = declaringClass.getMethod(method);
		}
		catch(Exception e)
		{
			throw new ExprValidationException(e.getMessage());
		}

        ExprDotEval[] eval = ExprDotNodeUtility.getChainEvaluators(staticMethod.getReturnType(), chainList, methodResolutionService, false);
        evaluator = new ExprStaticMethodEvalInvoke(className, staticMethod, childEvals, isConstantParameters, eval);
	}

    @Override
    public void accept(ExprNodeVisitor visitor) {
        super.accept(visitor);
        ExprNode.acceptChain(visitor, chainSpec);
    }

    @Override
    public void accept(ExprNodeVisitorWithParent visitor) {
        super.accept(visitor);
        ExprNode.acceptChain(visitor, chainSpec);
    }

    @Override
    protected void acceptChildnodes(ExprNodeVisitorWithParent visitor, ExprNode parent) {
        super.acceptChildnodes(visitor, parent);
        ExprNode.acceptChain(visitor, chainSpec, this);
    }

    @Override
    protected void replaceUnlistedChildNode(ExprNode nodeToReplace, ExprNode newNode) {
        ExprNode.replaceChainChildNode(nodeToReplace, newNode, chainSpec);
    }

    @Override
    public List<ExprNode> getAdditionalNodes() {
        return ExprNodeUtility.collectChainParameters(chainSpec);
    }
}
