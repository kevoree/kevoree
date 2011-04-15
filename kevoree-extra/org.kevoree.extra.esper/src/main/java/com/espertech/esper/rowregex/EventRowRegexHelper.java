package com.espertech.esper.rowregex;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.expression.ExprNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Helper for match recognize.
 */
public class EventRowRegexHelper
{
    private static final Log log = LogFactory.getLog(EventRowRegexHelper.class);

    /**
     * Inspect variables recursively.
     * @param parent parent regex expression node
     * @param isMultiple if the variable in the stack is multiple of single
     * @param variablesSingle single variables list
     * @param variablesMultiple group variables list
     */
    protected static void recursiveInspectVariables(RowRegexExprNode parent, boolean isMultiple, Set<String> variablesSingle, Set<String> variablesMultiple)
    {
        if (parent instanceof RowRegexExprNodeNested)
        {
            RowRegexExprNodeNested nested = (RowRegexExprNodeNested) parent;
            for (RowRegexExprNode child : parent.getChildNodes())
            {
                recursiveInspectVariables(child, nested.getType().isMultipleMatches() || isMultiple, variablesSingle, variablesMultiple);
            }
        }
        else if (parent instanceof RowRegexExprNodeAlteration)
        {
            for (RowRegexExprNode childAlteration : parent.getChildNodes())
            {
                LinkedHashSet<String> singles = new LinkedHashSet<String>();
                LinkedHashSet<String> multiples = new LinkedHashSet<String>();

                recursiveInspectVariables(childAlteration, isMultiple, singles, multiples);

                variablesMultiple.addAll(multiples);
                variablesSingle.addAll(singles);
            }
            variablesSingle.removeAll(variablesMultiple);
        }
        else if (parent instanceof RowRegexExprNodeAtom)
        {
            RowRegexExprNodeAtom atom = (RowRegexExprNodeAtom) parent;
            String name = atom.getTag();
            if (variablesMultiple.contains(name))
            {
                return;
            }
            if (variablesSingle.contains(name))
            {
                variablesSingle.remove(name);
                variablesMultiple.add(name);
                return;
            }
            if (atom.getType().isMultipleMatches())
            {
                variablesMultiple.add(name);
                return;
            }
            if (isMultiple)
            {
                variablesMultiple.add(name);
            }
            else
            {
                variablesSingle.add(name);
            }
        }
        else
        {
            for (RowRegexExprNode child : parent.getChildNodes())
            {
                recursiveInspectVariables(child, isMultiple, variablesSingle, variablesMultiple);
            }
        }
    }

    /**
     * Build a list of start states from the parent node.
     * @param parent to build start state for
     * @param variableDefinitions each variable and its expressions
     * @param variableStreams variable name and its stream number
     * @return strand of regex state nodes
     */
    protected static RegexNFAStrandResult recursiveBuildStartStates(RowRegexExprNode parent,
                                               Map<String, ExprNode> variableDefinitions,
                                               Map<String, Pair<Integer, Boolean>> variableStreams
                                               )
    {
        Stack<Integer> nodeNumStack = new Stack<Integer>();

        RegexNFAStrand strand = recursiveBuildStatesInternal(parent,
                                               variableDefinitions,
                                               variableStreams,
                                               nodeNumStack);

        // add end state
        RegexNFAStateEnd end = new RegexNFAStateEnd();
        for (RegexNFAStateBase endStates : strand.getEndStates())
        {
            endStates.addState(end);
        }

        // assign node num as a counter
        int nodeNumberFlat = 0;
        for (RegexNFAStateBase base : strand.getAllStates())
        {
            base.setNodeNumFlat(nodeNumberFlat++);
        }

        return new RegexNFAStrandResult(new ArrayList<RegexNFAState>(strand.getStartStates()), strand.getAllStates());
    }

    private static RegexNFAStrand recursiveBuildStatesInternal(RowRegexExprNode node,
                                               Map<String, ExprNode> variableDefinitions,
                                               Map<String, Pair<Integer, Boolean>> variableStreams,
                                               Stack<Integer> nodeNumStack
                                               )
    {
        if (node instanceof RowRegexExprNodeAlteration)
        {
            int nodeNum = 0;

            List<RegexNFAStateBase> cumulativeStartStates = new ArrayList<RegexNFAStateBase>();
            List<RegexNFAStateBase> cumulativeStates = new ArrayList<RegexNFAStateBase>();
            List<RegexNFAStateBase> cumulativeEndStates = new ArrayList<RegexNFAStateBase>();

            boolean isPassthrough = false;
            for (RowRegexExprNode child : node.getChildNodes())
            {
                nodeNumStack.push(nodeNum);
                RegexNFAStrand strand = recursiveBuildStatesInternal(child,
                                               variableDefinitions,
                                               variableStreams,
                                               nodeNumStack);
                nodeNumStack.pop();

                cumulativeStartStates.addAll(strand.getStartStates());
                cumulativeStates.addAll(strand.getAllStates());
                cumulativeEndStates.addAll(strand.getEndStates());
                if (strand.isPassthrough())
                {
                    isPassthrough = true;
                }

                nodeNum++;
            }

            return new RegexNFAStrand(cumulativeStartStates, cumulativeEndStates, cumulativeStates, isPassthrough);
        }
        else if (node instanceof RowRegexExprNodeConcatenation)
        {
            int nodeNum = 0;

            boolean isPassthrough = true;
            List<RegexNFAStateBase> cumulativeStates = new ArrayList<RegexNFAStateBase>();
            RegexNFAStrand[] strands = new RegexNFAStrand[node.getChildNodes().size()];

            for (RowRegexExprNode child : node.getChildNodes())
            {
                nodeNumStack.push(nodeNum);
                strands[nodeNum] = recursiveBuildStatesInternal(child,
                                               variableDefinitions,
                                               variableStreams,
                                               nodeNumStack);
                nodeNumStack.pop();

                cumulativeStates.addAll(strands[nodeNum].getAllStates());
                if (!strands[nodeNum].isPassthrough())
                {
                    isPassthrough = false;
                }

                nodeNum++;
            }

            // determine start states: all states until the first non-passthrough start state
            List<RegexNFAStateBase> startStates = new ArrayList<RegexNFAStateBase>();
            for (int i = 0; i < strands.length; i++)
            {
                startStates.addAll(strands[i].getStartStates());
                if (!strands[i].isPassthrough())
                {
                    break;
                }
            }

            // determine end states: all states from the back until the last non-passthrough end state
            List<RegexNFAStateBase> endStates = new ArrayList<RegexNFAStateBase>();
            for (int i = strands.length - 1; i >= 0; i--)
            {
                endStates.addAll(strands[i].getEndStates());
                if (!strands[i].isPassthrough())
                {
                    break;
                }
            }

            // hook up the end state of each strand with the start states of each next strand
            for (int i = strands.length - 1; i >= 1; i--)
            {
                RegexNFAStrand current = strands[i];
                for (int j = i - 1; j >= 0; j--)
                {
                    RegexNFAStrand prior = strands[j];

                    for (RegexNFAStateBase endState : prior.getEndStates())
                    {
                        for (RegexNFAStateBase startState : current.getStartStates())
                        {
                            endState.addState(startState);
                        }
                    }

                    if (!prior.isPassthrough())
                    {
                        break;
                    }
                }
            }

            return new RegexNFAStrand(startStates, endStates, cumulativeStates, isPassthrough);
        }
        else if (node instanceof RowRegexExprNodeNested)
        {
            RowRegexExprNodeNested nested = (RowRegexExprNodeNested) node;
            nodeNumStack.push(0);
            RegexNFAStrand strand = recursiveBuildStatesInternal(node.getChildNodes().get(0),
                                           variableDefinitions,
                                           variableStreams,
                                           nodeNumStack);
            nodeNumStack.pop();

            boolean isPassthrough = strand.isPassthrough() || nested.getType().isOptional();

            // if this is a repeating node then pipe back each end state to each begin state
            if (nested.getType().isMultipleMatches())
            {
                for (RegexNFAStateBase endstate : strand.getEndStates())
                {
                    for (RegexNFAStateBase startstate : strand.getStartStates())
                    {
                        if (!endstate.getNextStates().contains(startstate))
                        {
                            endstate.getNextStates().add(startstate);
                        }
                    }
                }
            }
            return new RegexNFAStrand(strand.getStartStates(), strand.getEndStates(), strand.getAllStates(), isPassthrough);
        }
        else
        {
            RowRegexExprNodeAtom atom = (RowRegexExprNodeAtom) node;

            // assign stream number for single-variables for most direct expression eval; multiple-variable gets -1
            int streamNum = variableStreams.get(atom.getTag()).getFirst();
            boolean multiple = variableStreams.get(atom.getTag()).getSecond();
            ExprNode expressionDef = variableDefinitions.get(atom.getTag());

            RegexNFAStateBase nextState;
            if ((atom.getType() == RegexNFATypeEnum.ZERO_TO_MANY) || (atom.getType() == RegexNFATypeEnum.ZERO_TO_MANY_RELUCTANT))
            {
                nextState = new RegexNFAStateZeroToMany(toString(nodeNumStack), atom.getTag(), streamNum, multiple, atom.getType().isGreedy(), expressionDef);
            }
            else if ((atom.getType() == RegexNFATypeEnum.ONE_TO_MANY) || (atom.getType() == RegexNFATypeEnum.ONE_TO_MANY_RELUCTANT))
            {
                nextState = new RegexNFAStateOneToMany(toString(nodeNumStack), atom.getTag(), streamNum, multiple, atom.getType().isGreedy(), expressionDef);
            }
            else if ((atom.getType() == RegexNFATypeEnum.ONE_OPTIONAL) || (atom.getType() == RegexNFATypeEnum.ONE_OPTIONAL_RELUCTANT))
            {
                nextState = new RegexNFAStateOneOptional(toString(nodeNumStack), atom.getTag(), streamNum, multiple, atom.getType().isGreedy(), expressionDef);
            }
            else if (expressionDef == null)
            {
                nextState = new RegexNFAStateAnyOne(toString(nodeNumStack), atom.getTag(), streamNum, multiple);
            }
            else
            {
                nextState = new RegexNFAStateFilter(toString(nodeNumStack), atom.getTag(), streamNum, multiple, expressionDef);
            }

            return new RegexNFAStrand(Collections.singletonList(nextState), Collections.singletonList(nextState),
                    Collections.singletonList(nextState), atom.getType().isOptional());            
        }
    }

    private static String toString(Stack<Integer> nodeNumStack) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (Integer atom : nodeNumStack)
        {
            builder.append(delimiter);
            builder.append(Integer.toString(atom));
            delimiter = ".";
        }
        return builder.toString();
    }
}
