/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Represents the CAST(expression, type) function is an expression tree.
 */
public class ExprCastNode extends ExprNode implements ExprEvaluator
{
    private final String classIdentifier;
    private Class targetType;
    private transient CasterParserComputer casterParserComputer;
    private transient ExprEvaluator evaluator;
    private static final long serialVersionUID = 7448449031028156455L;

    /**
     * Ctor.
     * @param classIdentifier the the name of the type to cast to
     */
    public ExprCastNode(String classIdentifier)
    {
        this.classIdentifier = classIdentifier;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    /**
     * Returns the name of the type of cast to.
     * @return type name
     */
    public String getClassIdentifier()
    {
        return classIdentifier;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 1)
        {
            throw new ExprValidationException("Cast function node must have exactly 1 child node");
        }

        evaluator = this.getChildNodes().get(0).getExprEvaluator();
        Class fromType = evaluator.getType();

        // identify target type
        // try the primitive names including "string"
        SimpleTypeCaster caster;
        targetType = JavaClassHelper.getPrimitiveClassForName(classIdentifier.trim());
        boolean numeric;
        if (targetType != null)
        {
            targetType = JavaClassHelper.getBoxedType(targetType);
            caster = SimpleTypeCasterFactory.getCaster(fromType, targetType);
            numeric = caster.isNumericCast();
        }
        else if (classIdentifier.trim().toLowerCase().equals("BigInteger".toLowerCase()))
        {
            targetType = BigInteger.class;
            caster = SimpleTypeCasterFactory.getCaster(fromType, targetType);
            numeric = true;
        }
        else if (classIdentifier.trim().toLowerCase().equals("BigDecimal".toLowerCase()))
        {
            targetType = BigDecimal.class;
            caster = SimpleTypeCasterFactory.getCaster(fromType, targetType);
            numeric = true;
        }
        else
        {
            try
            {
                targetType = JavaClassHelper.getClassForName(classIdentifier.trim());
            }
            catch (ClassNotFoundException e)
            {
                throw new ExprValidationException("Class as listed in cast function by name '" + classIdentifier + "' cannot be loaded", e);
            }
            numeric = JavaClassHelper.isNumeric(targetType);
            if (numeric)
            {
                caster = SimpleTypeCasterFactory.getCaster(fromType, targetType);
            }
            else
            {
                caster = new SimpleTypeCasterAnyType(targetType);
            }
        }

        // to-string
        if (targetType == String.class)
        {
            casterParserComputer = new StringXFormComputer();
        }
        // parse
        else if (fromType == String.class)
        {
            SimpleTypeParser parser = SimpleTypeParserFactory.getParser(JavaClassHelper.getBoxedType(targetType));
            casterParserComputer = new StringParserComputer(parser);
        }
        // numeric cast with check
        else if (numeric)
        {
            casterParserComputer = new NumberCasterComputer(caster);
        }
        // non-numeric cast
        else
        {
            casterParserComputer = new NonnumericCasterComputer(caster);
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return targetType;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object result = evaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (result == null)
        {
            return null;
        }
        return casterParserComputer.compute(result);
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("cast(");
        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(", ");
        buffer.append(classIdentifier);
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprCastNode))
        {
            return false;
        }
        ExprCastNode other = (ExprCastNode) node;
        if (other.classIdentifier.equals(this.classIdentifier))
        {
            return true;
        }

        return false;
    }

    /**
     * Casting and parsing computer.
     */
    public interface CasterParserComputer
    {
        /**
         * Compute an result performing casting and parsing.
         * @param input to process
         * @return cast or parse result
         */
        public Object compute(Object input);
    }

    /**
     * Casting and parsing computer.
     */
    public static class StringXFormComputer implements CasterParserComputer
    {
        public Object compute(Object input)
        {
            return input.toString();
        }
    }

    /**
     * Casting and parsing computer.
     */
    public static class NumberCasterComputer implements CasterParserComputer
    {
        private final SimpleTypeCaster numericTypeCaster;

        /**
         * Ctor.
         * @param numericTypeCaster caster
         */
        public NumberCasterComputer(SimpleTypeCaster numericTypeCaster)
        {
            this.numericTypeCaster = numericTypeCaster;
        }

        public Object compute(Object input)
        {
            if (input instanceof Number)
            {
                return numericTypeCaster.cast(input);
            }
            return null;
        }
    }

    /**
     * Casting and parsing computer.
     */
    public static class StringParserComputer implements CasterParserComputer
    {
        private final SimpleTypeParser parser;

        /**
         * Ctor.
         * @param parser parser
         */
        public StringParserComputer(SimpleTypeParser parser)
        {
            this.parser = parser;
        }

        public Object compute(Object input)
        {
            return parser.parse(input.toString());
        }
    }

    /**
     * Casting and parsing computer.
     */
    public static class NonnumericCasterComputer implements CasterParserComputer
    {
        private final SimpleTypeCaster caster;

        /**
         * Ctor.
         * @param numericTypeCaster caster
         */
        public NonnumericCasterComputer(SimpleTypeCaster numericTypeCaster)
        {
            this.caster = numericTypeCaster;
        }

        public Object compute(Object input)
        {
            return caster.cast(input);
        }
    }

}
