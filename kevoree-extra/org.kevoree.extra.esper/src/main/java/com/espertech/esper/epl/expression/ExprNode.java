/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.agg.AggregationSupport;
import com.espertech.esper.epl.core.*;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.MetaDefItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Superclass for filter nodes in a filter expression tree. Allow
 * validation against stream event types and evaluation of events against filter tree.
 */
public abstract class ExprNode implements ExprValidator, MetaDefItem, Serializable
{
    private static final long serialVersionUID = 0L;

    private final ArrayList<ExprNode> childNodes;

    /**
     * Returns the expression node rendered as a string.
     * @return string rendering of expression
     */
    public abstract String toExpressionString();

    /**
     * Returns true if the expression node's evaluation value doesn't depend on any events data,
     * as must be determined at validation time, which is bottom-up and therefore
     * reliably allows each node to determine constant value.
     * @return true for constant evaluation value, false for non-constant evaluation value
     */
    public abstract boolean isConstantResult();

    /**
     * Return true if a expression node semantically equals the current node, or false if not.
     * <p>Concrete implementations should compare the type and any additional information
     * that impact the evaluation of a node.
     * @param node to compare to
     * @return true if semantically equal, or false if not equals
     */
    public abstract boolean equalsNode(ExprNode node);

    /**
     * Constructor creates a list of child nodes.
     */
    public ExprNode()
    {
        childNodes = new ArrayList<ExprNode>();
    }

    /**
     * Validates the expression node subtree that has this
     * node as root. Some of the nodes of the tree, including the
     * root, might be replaced in the process.
     * @param streamTypeService - serves stream type information
     * @param methodResolutionService - for resolving class names in library method invocations
     * @param viewResourceDelegate - delegates for view resources to expression nodes
     * @param timeProvider - provides engine current time
     * @param variableService - provides access to variable values
     * @param exprEvaluatorContext context for expression evaluation
     * @throws ExprValidationException when the validation fails
     * @return the root node of the validated subtree, possibly
     *         different than the root node of the unvalidated subtree
     */
    public final ExprNode getValidatedSubtree(StreamTypeService streamTypeService,
                                        MethodResolutionService methodResolutionService,
                                        ViewResourceDelegate viewResourceDelegate,
                                        TimeProvider timeProvider,
                                        VariableService variableService,
                                        ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        ExprNode result = this;

        for (int i = 0; i < childNodes.size(); i++)
        {
            childNodes.set(i, childNodes.get(i).getValidatedSubtree(streamTypeService, methodResolutionService, viewResourceDelegate, timeProvider, variableService, exprEvaluatorContext));
        }

        try
        {
            validate(streamTypeService, methodResolutionService, viewResourceDelegate, timeProvider, variableService, exprEvaluatorContext);
        }
        catch(ExprValidationException e)
        {
            if (this instanceof ExprIdentNode)
            {
                ExprIdentNode identNode = (ExprIdentNode) this;
                try
                {
                    result = resolveStaticMethodOrField(identNode, streamTypeService, methodResolutionService, e, timeProvider, variableService, exprEvaluatorContext);
                }
                catch(ExprValidationException ex)
                {
                    result = resolveAsStreamName(identNode, streamTypeService, e, exprEvaluatorContext);
                }
            }
            else if (this instanceof ExprStaticMethodNode)
            {
                ExprStaticMethodNode staticMethodNode = (ExprStaticMethodNode) this;
                result = resolveInstanceMethod(staticMethodNode, streamTypeService, methodResolutionService, e, exprEvaluatorContext);
            }
            else
            {
                throw e;
            }
        }

        return result;
    }

    private ExprNode resolveInstanceMethod(ExprStaticMethodNode staticMethodNode, StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprValidationException existingException, ExprEvaluatorContext exprEvaluatorContext)
            throws ExprValidationException
    {
        String streamName = staticMethodNode.getClassName();

        boolean streamFound = false;
        for (String name : streamTypeService.getStreamNames())
        {
            if (name.equals(streamName))
            {
                streamFound = true;
            }
        }

        ExprStreamInstanceMethodNode exprStream = new ExprStreamInstanceMethodNode(streamName, staticMethodNode.getChainSpec());
        try
        {
            exprStream.validate(streamTypeService, methodResolutionService, null, null, null, exprEvaluatorContext);
        }
        catch (ExprValidationException ex)
        {
            if (streamFound)
            {
                throw ex;
            }
            throw existingException;
        }

        return exprStream;
    }

    private ExprNode resolveAsStreamName(ExprIdentNode identNode, StreamTypeService streamTypeService, ExprValidationException existingException, ExprEvaluatorContext exprEvaluatorContext)
            throws ExprValidationException
    {
        ExprStreamUnderlyingNode exprStream = new ExprStreamUnderlyingNode(identNode.getUnresolvedPropertyName(), false);

        try
        {
            exprStream.validate(streamTypeService, null, null, null, null, exprEvaluatorContext);
        }
        catch (ExprValidationException ex)
        {
            throw existingException;
        }

        return exprStream;
    }

    /**
     * Accept the visitor. The visitor will first visit the parent then visit all child nodes, then their child nodes.
     * <p>The visitor can decide to skip child nodes by returning false in isVisit.
     * @param visitor to visit each node and each child node.
     */
    public void accept(ExprNodeVisitor visitor)
    {
        if (visitor.isVisit(this))
        {
            visitor.visit(this);

            for (ExprNode childNode : childNodes)
            {
                childNode.accept(visitor);
            }
        }
    }

    /**
     * Accept the visitor. The visitor will first visit the parent then visit all child nodes, then their child nodes.
     * <p>The visitor can decide to skip child nodes by returning false in isVisit.
     *
     * @param visitor to visit each node and each child node.
     */
    public void accept(ExprNodeVisitorWithParent visitor)
    {
        if (visitor.isVisit(this))
        {
            visitor.visit(this, null);

            for (ExprNode childNode : childNodes)
            {
                childNode.acceptChildnodes(visitor, this);
            }
        }
    }

    /**
     * Accept a visitor that receives both parent and child node.
     * @param visitor to apply
     * @param parent node
     */
    protected void acceptChildnodes(ExprNodeVisitorWithParent visitor, ExprNode parent)
    {
        if (visitor.isVisit(this))
        {
            visitor.visit(this, parent);

            for (ExprNode childNode : childNodes)
            {
                childNode.acceptChildnodes(visitor, this);
            }
        }
    }

    /**
     * Adds a child node.
     * @param childNode is the child evaluation tree node to add
     */
    public final void addChildNode(ExprNode childNode)
    {
        childNodes.add(childNode);
    }

    /**
     * Returns list of child nodes.
     * @return list of child nodes
     */
    public final ArrayList<ExprNode> getChildNodes()
    {
        return childNodes;
    }

    /**
     * Recursively print out all nodes.
     * @param prefix is printed out for naming the printed info
     */
    @SuppressWarnings({"StringContatenationInLoop"})
    public final void dumpDebug(String prefix)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".dumpDebug " + prefix + this.toString());
        }
        for (ExprNode node : childNodes)
        {
            node.dumpDebug(prefix + "  ");
        }
    }

    // Since static method calls such as "Class.method('a')" and mapped properties "Stream.property('key')"
    // look the same, however as the validation could not resolve "Stream.property('key')" before calling this method,
    // this method tries to resolve the mapped property as a static method.
    // Assumes that this is an ExprIdentNode.
    private ExprNode resolveStaticMethodOrField(ExprIdentNode identNode, StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ExprValidationException propertyException, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext)
    throws ExprValidationException
    {
        // Reconstruct the original string
        StringBuffer mappedProperty = new StringBuffer(identNode.getUnresolvedPropertyName());
        if(identNode.getStreamOrPropertyName() != null)
        {
            mappedProperty.insert(0, identNode.getStreamOrPropertyName() + '.');
        }

        // Parse the mapped property format into a class name, method and single string parameter
        MappedPropertyParseResult parse = parseMappedProperty(mappedProperty.toString());
        if (parse == null)
        {
            ExprConstantNode constNode = resolveIdentAsEnumConst(mappedProperty.toString(), methodResolutionService);
            if (constNode == null)
            {
                throw propertyException;
            }
            else
            {
                return constNode;
            }
        }

        // If there is a class name, assume a static method is possible
        if (parse.getClassName() != null)
        {
            List<ExprNode> parameters = Collections.singletonList((ExprNode) new ExprConstantNode(parse.getArgString()));
            List<ExprChainedSpec> chain = Collections.singletonList(new ExprChainedSpec(parse.getMethodName(), parameters));
            ExprNode result = new ExprStaticMethodNode(parse.getClassName(), chain, methodResolutionService.isUdfCache());

            // Validate
            try
            {
                result.validate(streamTypeService, methodResolutionService, null, timeProvider, variableService, exprEvaluatorContext);
            }
            catch(ExprValidationException e)
            {
                throw new ExprValidationException("Failed to resolve " + mappedProperty + " as either an event property or as a static method invocation");
            }

            return result;
        }

        // There is no class name, try a single-row function
        String functionName = parse.getMethodName();
        try
        {
            Pair<Class, String> classMethodPair = methodResolutionService.resolveSingleRow(functionName);
            List<ExprNode> params = Collections.singletonList((ExprNode) new ExprConstantNode(parse.getArgString()));
            List<ExprChainedSpec> chain = Collections.singletonList(new ExprChainedSpec(classMethodPair.getSecond(), params));
            ExprNode result = new ExprPlugInSingleRowNode(functionName, classMethodPair.getFirst(), chain, false);

            // Validate
            try
            {
                result.validate(streamTypeService, methodResolutionService, null, timeProvider, variableService, exprEvaluatorContext);
            }
            catch (RuntimeException e)
            {
                throw new ExprValidationException("Plug-in aggregation function '" + parse.getMethodName() + "' failed validation: " + e.getMessage());
            }

            return result;
        }
        catch (EngineImportUndefinedException e)
        {
            // Not an single-row function
        }
        catch (EngineImportException e)
        {
            throw new IllegalStateException("Error resolving single-row function: " + e.getMessage(), e);
        }

        // There is no class name, try an aggregation function
        try
        {
            AggregationSupport aggregation = methodResolutionService.resolveAggregation(parse.getMethodName());
            ExprNode result = new ExprPlugInAggFunctionNode(false, aggregation, parse.getMethodName());
            result.addChildNode(new ExprConstantNode(parse.getArgString()));

            // Validate
            try
            {
                result.validate(streamTypeService, methodResolutionService, null, timeProvider, variableService, exprEvaluatorContext);
            }
            catch (RuntimeException e)
            {
                throw new ExprValidationException("Plug-in aggregation function '" + parse.getMethodName() + "' failed validation: " + e.getMessage());
            }

            return result;
        }
        catch (EngineImportUndefinedException e)
        {
            // Not an aggregation function
        }
        catch (EngineImportException e)
        {
            throw new IllegalStateException("Error resolving aggregation: " + e.getMessage(), e);
        }

        // absolutly cannot be resolved
        throw propertyException;
    }

    private ExprConstantNode resolveIdentAsEnumConst(String constant, MethodResolutionService methodResolutionService)
            throws ExprValidationException
    {
        Object enumValue = JavaClassHelper.resolveIdentAsEnumConst(constant, methodResolutionService, null);
        if (enumValue != null)
        {
            return new ExprConstantNode(enumValue);
        }
        return null;
    }

    /**
     * Parse the mapped property into classname, method and string argument.
     * Mind this has been parsed already and is a valid mapped property.
     * @param property is the string property to be passed as a static method invocation
     * @return descriptor object
     */
    protected static MappedPropertyParseResult parseMappedProperty(String property)
    {
        // get argument
        int indexFirstDoubleQuote = property.indexOf("\"");
        int indexFirstSingleQuote = property.indexOf("'");
        int startArg;
        if ((indexFirstSingleQuote == -1) && (indexFirstDoubleQuote == -1))
        {
            return null;
        }
        if ((indexFirstSingleQuote != -1) && (indexFirstDoubleQuote != -1))
        {
            if (indexFirstSingleQuote < indexFirstDoubleQuote)
            {
                startArg = indexFirstSingleQuote;
            }
            else
            {
                startArg = indexFirstDoubleQuote;
            }
        }
        else if (indexFirstSingleQuote != -1)
        {
            startArg = indexFirstSingleQuote;
        }
        else
        {
            startArg = indexFirstDoubleQuote;
        }

        int indexLastDoubleQuote = property.lastIndexOf("\"");
        int indexLastSingleQuote = property.lastIndexOf("'");
        int endArg;
        if ((indexLastSingleQuote == -1) && (indexLastDoubleQuote == -1))
        {
            return null;
        }
        if ((indexLastSingleQuote != -1) && (indexLastDoubleQuote != -1))
        {
            if (indexLastSingleQuote > indexLastDoubleQuote)
            {
                endArg = indexLastSingleQuote;
            }
            else
            {
                endArg = indexLastDoubleQuote;
            }
        }
        else if (indexLastSingleQuote != -1)
        {
            endArg = indexLastSingleQuote;
        }
        else
        {
            endArg = indexLastDoubleQuote;
        }
        String argument = property.substring(startArg + 1, endArg);

        // get method
        String splitDots[] = property.split("[\\.]");
        if (splitDots.length == 0)
        {
            return null;
        }

        // find which element represents the method, its the element with the parenthesis
        int indexMethod = -1;
        for (int i = 0; i < splitDots.length; i++)
        {
            if (splitDots[i].contains("("))
            {
                indexMethod = i;
                break;
            }
        }
        if (indexMethod == -1)
        {
            return null;
        }

        String method = splitDots[indexMethod];
        int indexParan = method.indexOf("(");
        method = method.substring(0, indexParan);
        if (method.length() == 0)
        {
            return null;
        }

        if (splitDots.length == 1)
        {
            // no class name
            return new MappedPropertyParseResult(null, method, argument);
        }


        // get class
        StringBuffer clazz = new StringBuffer();
        for (int i = 0; i < indexMethod; i++)
        {
            if (i > 0)
            {
                clazz.append('.');
            }
            clazz.append(splitDots[i]);
        }

        return new MappedPropertyParseResult(clazz.toString(), method, argument);
    }

    public final void replaceChildNode(ExprNode nodeToReplace, ExprNode newNode) {
        int index = getChildNodes().indexOf(nodeToReplace);
        if (index == -1) {
            replaceUnlistedChildNode(nodeToReplace, newNode);
        }
        else {
            getChildNodes().set(index, newNode);
        }
    }

    protected void replaceUnlistedChildNode(ExprNode nodeToReplace, ExprNode newNode) {
        // Override to replace child expression nodes that are chained or otherwise not listed as child nodes
    }

    public static void replaceChainChildNode(ExprNode nodeToReplace, ExprNode newNode, List<ExprChainedSpec> chainSpec) {
        for (ExprChainedSpec chained : chainSpec) {
            int index = chained.getParameters().indexOf(nodeToReplace);
            if (index != -1) {
                chained.getParameters().set(index, newNode);
            }
        }
    }

    /**
     * Encapsulates the parse result parsing a mapped property as a class and method name with args.
     */
    protected static class MappedPropertyParseResult
    {
        private String className;
        private String methodName;
        private String argString;

        /**
         * Returns class name.
         * @return name of class
         */
        public String getClassName()
        {
            return className;
        }

        /**
         * Returns the method name.
         * @return method name
         */
        public String getMethodName()
        {
            return methodName;
        }

        /**
         * Returns the method argument.
         * @return arg
         */
        public String getArgString()
        {
            return argString;
        }

        /**
         * Returns the parse result of the mapped property.
         * @param className is the class name, or null if there isn't one
         * @param methodName is the method name
         * @param argString is the argument
         */
        public MappedPropertyParseResult(String className, String methodName, String argString)
        {
            this.className = className;
            this.methodName = methodName;
            this.argString = argString;
        }
    }

    public static void acceptChain(ExprNodeVisitor visitor, List<ExprChainedSpec> chainSpec) {
        for (ExprChainedSpec chain : chainSpec) {
            for (ExprNode param : chain.getParameters()) {
                param.accept(visitor);
            }
        }
    }

    public static void acceptChain(ExprNodeVisitorWithParent visitor, List<ExprChainedSpec> chainSpec) {
        for (ExprChainedSpec chain : chainSpec) {
            for (ExprNode param : chain.getParameters()) {
                param.accept(visitor);
            }
        }
    }

    public static void acceptChain(ExprNodeVisitorWithParent visitor, List<ExprChainedSpec> chainSpec, ExprNode parent) {
        for (ExprChainedSpec chain : chainSpec) {
            for (ExprNode param : chain.getParameters()) {
                param.acceptChildnodes(visitor, parent);
            }
        }
    }    

    private static final Log log = LogFactory.getLog(ExprNode.class);
}
