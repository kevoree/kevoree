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
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.core.*;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.epl.parse.ASTFilterSpecHelper;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.LevenshteinDistance;

import java.util.Map;

/**
 * Represents an stream property identifier in a filter expressiun tree.
 */
public class ExprIdentNode extends ExprNode implements ExprEvaluator
{
    // select myprop from...        is a simple property, no stream supplied
    // select s0.myprop from...     is a simple property with a stream supplied, or a nested property (cannot tell until resolved)
    // select indexed[1] from ...   is a indexed property

    private final String unresolvedPropertyName;
    private String streamOrPropertyName;

    private String resolvedStreamName;
    private String resolvedPropertyName;
    private transient EventPropertyGetter propertyGetter;
    private int streamNum = -1;
    private Class propertyType;
    private static final long serialVersionUID = 5882493771230745244L;

    /**
     * Ctor.
     * @param unresolvedPropertyName is the event property name in unresolved form, ie. unvalidated against streams
     */
    public ExprIdentNode(String unresolvedPropertyName)
    {
        if (unresolvedPropertyName == null)
        {
            throw new IllegalArgumentException("Property name is null");
        }
        this.unresolvedPropertyName = unresolvedPropertyName;
        this.streamOrPropertyName = null;
    }

    /**
     * Ctor.
     * @param unresolvedPropertyName is the event property name in unresolved form, ie. unvalidated against streams
     * @param streamOrPropertyName is the stream name, or if not a valid stream name a possible nested property name
     * in one of the streams.
     */
    public ExprIdentNode(String unresolvedPropertyName, String streamOrPropertyName)
    {
        if (unresolvedPropertyName == null)
        {
            throw new IllegalArgumentException("Property name is null");
        }
        if (streamOrPropertyName == null)
        {
            throw new IllegalArgumentException("Stream (or property name) name is null");
        }
        this.unresolvedPropertyName = unresolvedPropertyName;
        this.streamOrPropertyName = streamOrPropertyName;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    /**
     * For unit testing, returns unresolved property name.
     * @return property name
     */
    public String getUnresolvedPropertyName()
    {
        return unresolvedPropertyName;
    }

    /**
     * For unit testing, returns stream or property name candidate.
     * @return stream name, or property name of a nested property of one of the streams
     */
    public String getStreamOrPropertyName()
    {
        return streamOrPropertyName;
    }

    /**
     * Set name.
     * @param streamOrPropertyName to use
     */
    public void setStreamOrPropertyName(String streamOrPropertyName) {
        this.streamOrPropertyName = streamOrPropertyName;
    }

    /**
     * Returns the unresolved property name in it's complete form, including
     * the stream name if there is one.
     * @return property name
     */
    public String getFullUnresolvedName()
    {
        if (streamOrPropertyName == null)
        {
            return unresolvedPropertyName;
        }
        else
        {
            return streamOrPropertyName + "." + unresolvedPropertyName;
        }
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        Pair<PropertyResolutionDescriptor, String> propertyInfoPair = getTypeFromStream(streamTypeService, unresolvedPropertyName, streamOrPropertyName);
        resolvedStreamName = propertyInfoPair.getSecond();
        streamNum = propertyInfoPair.getFirst().getStreamNum();
        propertyType = propertyInfoPair.getFirst().getPropertyType();
        resolvedPropertyName = propertyInfoPair.getFirst().getPropertyName();
        propertyGetter = propertyInfoPair.getFirst().getStreamEventType().getGetter(resolvedPropertyName);

        if (propertyGetter == null)
        {
            throw new ExprValidationException("Property getter returned was invalid for property '" + unresolvedPropertyName + "'");
        }
    }

    public Class getType()
    {
        if (resolvedPropertyName == null)
        {
            throw new IllegalStateException("Identifier node has not been validated");
        }
        return propertyType;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    /**
     * Returns stream id supplying the property value.
     * @return stream number
     */
    public int getStreamId()
    {
        if (streamNum == -1)
        {
            throw new IllegalStateException("Identifier node has not been validated");
        }
        return streamNum;
    }

    /**
     * Returns stream name as resolved by lookup of property in streams.
     * @return stream name
     */
    public String getResolvedStreamName()
    {
        if (resolvedStreamName == null)
        {
            throw new IllegalStateException("Identifier node has not been validated");
        }
        return resolvedStreamName;
    }

    /**
     * Return property name as resolved by lookup in streams.
     * @return property name
     */
    public String getResolvedPropertyName()
    {
        if (resolvedPropertyName == null)
        {
            throw new IllegalStateException("Identifier node has not been validated");
        }
        return resolvedPropertyName;
    }

    /**
     * Returns the root of the resolved property name, if any.
     * @return root
     */
    public String getResolvedPropertyNameRoot() {
        if (resolvedPropertyName == null)
        {
            throw new IllegalStateException("Identifier node has not been validated");
        }
        if (resolvedPropertyName.indexOf('[') != -1) {
            return resolvedPropertyName.substring(0, resolvedPropertyName.indexOf('['));
        }
        if (resolvedPropertyName.indexOf('(') != -1) {
            return resolvedPropertyName.substring(0, resolvedPropertyName.indexOf('('));
        }
        if (resolvedPropertyName.indexOf('.') != -1) {
            return resolvedPropertyName.substring(0, resolvedPropertyName.indexOf('.'));
        }
        return resolvedPropertyName;
    }

    /**
     * Determine stream id and property type given an unresolved property name and
     * a stream name that may also be part of the property name.
     * <p>
     * For example: select s0.p1 from...    p1 is the property name, s0 the stream name, however this could also be a nested property
     * @param streamTypeService - service for type infos
     * @param unresolvedPropertyName - property name
     * @param streamOrPropertyName - stream name, this can also be the first part of the property name
     * @return pair of stream number and property type
     * @throws ExprValidationPropertyException if no such property exists
     */
    protected static Pair<PropertyResolutionDescriptor, String> getTypeFromStream(StreamTypeService streamTypeService, String unresolvedPropertyName, String streamOrPropertyName)
        throws ExprValidationPropertyException
    {
        PropertyResolutionDescriptor propertyInfo;

        // no stream/property name supplied
        if (streamOrPropertyName == null)
        {
            try
            {
                propertyInfo = streamTypeService.resolveByPropertyName(unresolvedPropertyName);
            }
            catch (StreamTypesException ex)
            {
                String suggestion = getSuggestion(ex);
                if (suggestion != null)
                {
                    throw new ExprValidationPropertyException(ex.getMessage() + suggestion);
                }
                else
                {
                    throw new ExprValidationPropertyException(ex.getMessage());
                }
            }
            catch (PropertyAccessException ex)
            {
                throw new ExprValidationPropertyException(ex.getMessage());
            }

            // resolves without a stream name, return descriptor and null stream name
            return new Pair<PropertyResolutionDescriptor, String>(propertyInfo, propertyInfo.getStreamName());
        }

        // try to resolve the property name and stream name as it is (ie. stream name as a stream name)
        StreamTypesException typeExceptionOne;
        try
        {
            propertyInfo = streamTypeService.resolveByStreamAndPropName(streamOrPropertyName, unresolvedPropertyName);
            // resolves with a stream name, return descriptor and stream name
            return new Pair<PropertyResolutionDescriptor, String>(propertyInfo, streamOrPropertyName);
        }
        catch (StreamTypesException ex)
        {
            typeExceptionOne = ex;
        }

        // try to resolve the property name to a nested property 's0.p0'
        StreamTypesException typeExceptionTwo;
        String propertyNameCandidate = streamOrPropertyName + '.' + unresolvedPropertyName;
        try
        {
            propertyInfo = streamTypeService.resolveByPropertyName(propertyNameCandidate);
            // resolves without a stream name, return null for stream name
            return new Pair<PropertyResolutionDescriptor, String>(propertyInfo, null);
        }
        catch (StreamTypesException ex)
        {
            typeExceptionTwo = ex;
        }

        String suggestionOne = getSuggestion(typeExceptionOne);
        String suggestionTwo = getSuggestion(typeExceptionTwo);
        if (suggestionOne != null)
        {
            throw new ExprValidationPropertyException(typeExceptionOne.getMessage() + suggestionOne);
        }
        if (suggestionTwo != null)
        {
            throw new ExprValidationPropertyException(typeExceptionTwo.getMessage() + suggestionTwo);
        }

        // fail to resolve
        throw new ExprValidationPropertyException("Failed to resolve property '" + propertyNameCandidate + "' to a stream or nested property in a stream");
    }

    private static String getSuggestion(StreamTypesException ex)
    {
        if (ex == null)
        {
            return null;
        }
        if (ex.getOptionalSuggestion() == null)
        {
            return null;
        }
        if (ex.getOptionalSuggestion().getFirst() > LevenshteinDistance.ACCEPTABLE_DISTANCE)
        {
            return null;
        }
        return " (did you mean '" + ex.getOptionalSuggestion().getSecond() + "'?)";
    }

    public String toString()
    {
        return "unresolvedPropertyName=" + unresolvedPropertyName +
                " streamOrPropertyName=" + streamOrPropertyName +
                " resolvedPropertyName=" + resolvedPropertyName +
                " propertyInfo.pos=" + streamNum +
                " propertyInfo.type=" + propertyType;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        EventBean event = eventsPerStream[streamNum];
        if (event == null)
        {
            return null;
        }
        return propertyGetter.get(event);
    }

    /**
     * Returns true if the property exists, or false if not.
     * @param eventsPerStream each stream's events
     * @param isNewData if the stream represents insert or remove stream
     * @return true if the property exists, false if not
     */
    public boolean evaluatePropertyExists(EventBean[] eventsPerStream, boolean isNewData)
    {
        EventBean event = eventsPerStream[streamNum];
        if (event == null)
        {
            return false;
        }
        return propertyGetter.isExistsProperty(event);
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        if (streamOrPropertyName != null)
        {
            buffer.append(ASTFilterSpecHelper.unescapeDot(streamOrPropertyName)).append('.');
        }
        buffer.append(ASTFilterSpecHelper.unescapeDot(unresolvedPropertyName));

        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprIdentNode))
        {
            return false;
        }

        ExprIdentNode other = (ExprIdentNode) node;

        if (streamOrPropertyName != null ? !streamOrPropertyName.equals(other.streamOrPropertyName) : other.streamOrPropertyName != null)
            return false;
        if (unresolvedPropertyName != null ? !unresolvedPropertyName.equals(other.unresolvedPropertyName) : other.unresolvedPropertyName != null)
            return false;
        return true;
    }
}
