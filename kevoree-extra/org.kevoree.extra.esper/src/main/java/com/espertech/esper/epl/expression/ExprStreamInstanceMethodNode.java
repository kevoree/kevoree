/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.schedule.TimeProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * Represents an invocation of a instance method on an event of a given stream in the expression tree.
 */
public class ExprStreamInstanceMethodNode extends ExprNode implements ExprEvaluator, ExprNodeInnerNodeProvider
{
    private static final Log log = LogFactory.getLog(ExprNode.class);
    private static final long serialVersionUID = 3422689488586035557L;

	private final String streamName;
	private final List<ExprChainedSpec> chainSpec;

    private int streamNum = -1;
    private transient ExprDotEval[] evaluators;

    /**
	 * Ctor.
	 * @param streamName - the declaring class for the method that this node will invoke
	 */
	public ExprStreamInstanceMethodNode(String streamName, List<ExprChainedSpec> chainSpec)
	{
		if(streamName == null)
		{
			throw new NullPointerException("Stream name is null");
		}
		if((chainSpec == null) || (chainSpec.isEmpty()))
		{
			throw new NullPointerException("chain name is null or empty");
		}

		this.streamName = streamName;
		this.chainSpec = chainSpec;
	}

    @Override
    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    @Override
    public Map<String, Object> getEventType() {
        return null;
    }

	/**
     * Returns the stream name.
	 * @return the stream that provides events that provide the instance method
	 */
	public String getStreamName() {
		return streamName;
	}

    /**
     * Returns stream id supplying the property value.
     * @return stream number
     */
    public int getStreamId()
    {
        if (streamNum == -1)
        {
            throw new IllegalStateException("Stream underlying node has not been validated");
        }
        return streamNum;
    }

    public String toExpressionString()
	{
        StringBuilder buffer = new StringBuilder();
		buffer.append(streamName);
        ExprNodeUtility.toExpressionString(chainSpec, buffer);
		return buffer.toString();
	}

	public boolean equalsNode(ExprNode node)
	{
		if(!(node instanceof ExprStreamInstanceMethodNode))
		{
			return false;
		}

        ExprStreamInstanceMethodNode other = (ExprStreamInstanceMethodNode) node;
        if (!streamName.equals(other.streamName)) {
            return false;
		}
        if (other.chainSpec.size() != this.chainSpec.size()) {
            return false;
        }
        for (int i = 0; i < chainSpec.size(); i++) {
            if (!(this.chainSpec.get(i).equals(other.chainSpec.get(i)))) {
                return false;
            }
        }
        return true;
	}

	public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
	{
        String[] streams = streamTypeService.getStreamNames();
        for (int i = 0; i < streams.length; i++)
        {
            if ((streams[i] != null) && (streams[i].equals(streamName)))
            {
                streamNum = i;
                break;
            }
        }

        if (streamNum == -1)
        {
            throw new ExprValidationException("Stream by name '" + streamName + "' could not be found among all streams");
        }

        EventType eventType = streamTypeService.getEventTypes()[streamNum];
        Class type = eventType.getUnderlyingType();

        evaluators = ExprDotNodeUtility.getChainEvaluators(type, chainSpec, methodResolutionService, false);
	}

	public Class getType()
	{
        if (evaluators == null)
        {
            throw new IllegalStateException("Stream underlying node has not been validated");
        }
        return evaluators[evaluators.length - 1].getResultType();
	}

	public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
	{
        // get underlying event
        EventBean event = eventsPerStream[streamNum];
        if (event == null)
        {
            return null;
        }
        Object inner = event.getUnderlying();

        if (inner == null) {
            return null;
        }

        for (ExprDotEval methodEval : evaluators) {
            inner = methodEval.evaluate(inner, eventsPerStream, isNewData, exprEvaluatorContext);
            if (inner == null) {
                break;
            }
        }
        return inner;
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
