package com.espertech.esper.util;

import com.espertech.esper.collection.MultiKeyUntyped;

import java.util.Comparator;
import java.text.Collator;
import java.io.Serializable;

/**
 * A comparator on multikeys with string values and using the Collator for comparing.
 * The multikeys must contain the same number of values.
 */
public final class MultiKeyCollatingComparator implements Comparator<MultiKeyUntyped>, MetaDefItem, Serializable
{
    private final boolean[] isDescendingValues;
    private final boolean[] stringTypedValue;
    private transient Collator collator = null;
    private static final long serialVersionUID = -1166857207888935691L;

    /**
     * Ctor.
     * @param isDescendingValues - each value is true if the corresponding (same index)
     *        entry in the multi-keys is to be sorted in descending order. The multikeys
     *        to be compared must have the same number of values as this array.
     * @param stringTypeValues true for each string-typed column
     */
    public MultiKeyCollatingComparator(boolean[] isDescendingValues, boolean[] stringTypeValues)
    {
        this.isDescendingValues = isDescendingValues;
        this.stringTypedValue = stringTypeValues;
        this.collator = Collator.getInstance();
    }

    public final int compare(MultiKeyUntyped firstValues, MultiKeyUntyped secondValues)
    {
        if(firstValues.size() != isDescendingValues.length || secondValues.size() != isDescendingValues.length)
        {
            throw new IllegalArgumentException("Incompatible size MultiKey sizes for comparison");
        }

        for (int i = 0; i < firstValues.size(); i++)
        {
            Object valueOne = firstValues.get(i);
            Object valueTwo = secondValues.get(i);
            boolean isDescending = isDescendingValues[i];

            if (!stringTypedValue[i])
            {
                int comparisonResult = MultiKeyComparator.compareValues(valueOne, valueTwo, isDescending);
                if (comparisonResult != 0)
                {
                    return comparisonResult;
                }
            }
            else
            {
                int comparisonResult = MultiKeyComparator.compareValuesCollated(valueOne, valueTwo, isDescending, collator);
                if (comparisonResult != 0)
                {
                    return comparisonResult;
                }
            }
        }

        // Make the comparator compatible with equals
        if(!firstValues.equals(secondValues))
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
