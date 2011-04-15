/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.schedule.TimeProvider;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents the INSTANCEOF(a,b,...) function is an expression tree.
 */
public class ExprInstanceofNode extends ExprNode implements ExprEvaluator
{
    private final String[] classIdentifiers;

    private Class[] classes;
    private CopyOnWriteArrayList<Pair<Class, Boolean>> resultCache = new CopyOnWriteArrayList<Pair<Class, Boolean>>();
    private transient ExprEvaluator evaluator;
    private static final long serialVersionUID = 3358616797009364727L;

    /**
     * Ctor.
     * @param classIdentifiers is a list of type names to check type for
     */
    public ExprInstanceofNode(String[] classIdentifiers)
    {
        this.classIdentifiers = classIdentifiers;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 1)
        {
            throw new ExprValidationException("Instanceof node must have 1 child expression node supplying the expression to test");
        }
        if ((classIdentifiers == null) || (classIdentifiers.length == 0))
        {
            throw new ExprValidationException("Instanceof node must have 1 or more class identifiers to verify type against");
        }

        evaluator = this.getChildNodes().get(0).getExprEvaluator();
        Set<Class> classList = getClassSet(classIdentifiers);
        synchronized(this) {
            classes = classList.toArray(new Class[classList.size()]);
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object result = evaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (result == null)
        {
            return false;
        }

        // return cached value
        for (Pair<Class, Boolean> pair : resultCache)
        {
            if (pair.getFirst() == result.getClass())
            {
                return pair.getSecond();
            }
        }

        return checkAddType(result.getClass());
    }

    // Checks type and adds to cache
    private synchronized Boolean checkAddType(Class type)
    {
        // check again in synchronized block
        for (Pair<Class, Boolean> pair : resultCache)
        {
            if (pair.getFirst() == type)
            {
                return pair.getSecond();
            }
        }

        // get the types superclasses and interfaces, and their superclasses and interfaces
        Set<Class> classesToCheck = new HashSet<Class>();
        JavaClassHelper.getSuper(type, classesToCheck);
        classesToCheck.add(type);

        // check type against each class
        boolean fits = false;
        for (Class clazz : classes)
        {
            if (classesToCheck.contains(clazz))
            {
                fits = true;
                break;
            }
        }

        resultCache.add(new Pair<Class, Boolean>(type, fits));
        return fits;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("instanceof(");
        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(", ");

        String delimiter = "";
        for (int i = 0; i < classIdentifiers.length; i++)
        {
            buffer.append(delimiter);
            buffer.append(classIdentifiers[i]);
            delimiter = ", ";
        }
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprInstanceofNode))
        {
            return false;
        }
        ExprInstanceofNode other = (ExprInstanceofNode) node;
        if (Arrays.equals(other.classIdentifiers, classIdentifiers))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns the list of class names or types to check instance of.
     * @return class names
     */
    public String[] getClassIdentifiers()
    {
        return classIdentifiers;
    }

    private Set<Class> getClassSet(String[] classIdentifiers)
            throws ExprValidationException
    {
        Set<Class> classList = new HashSet<Class>();
        for (String className : classIdentifiers)
        {
            Class clazz;

            // try the primitive names including "string"
            clazz = JavaClassHelper.getPrimitiveClassForName(className.trim());
            if (clazz != null)
            {
                classList.add(clazz);
                classList.add(JavaClassHelper.getBoxedType(clazz));
                continue;
            }

            // try to look up the class, not a primitive type name
            try
            {
                clazz = JavaClassHelper.getClassForName(className.trim());
            }
            catch (ClassNotFoundException e)
            {
                throw new ExprValidationException("Class as listed in instanceof function by name '" + className + "' cannot be loaded", e);
            }

            // Add primitive and boxed types, or type itself if not built-in
            classList.add(JavaClassHelper.getPrimitiveType(clazz));
            classList.add(JavaClassHelper.getBoxedType(clazz));
        }
        return classList;
    }
}
