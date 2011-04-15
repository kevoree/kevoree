package com.espertech.esper.client.annotation;

import com.espertech.esper.epl.annotation.AnnotationException;

import java.lang.annotation.Annotation;

/**
 * Enumeration of hint values. Since hints may be a comma-separate list in a single @Hint annotation
 * they are listed as enumeration values here.
 */
public enum HintEnum
{
    /**
     * For use with match_recognize, iterate-only matching.
     */
    ITERATE_ONLY("ITERATE_ONLY", false, false),

    /**
     * For use with group-by, disabled reclaim groups.
     */
    DISABLE_RECLAIM_GROUP("DISABLE_RECLAIM_GROUP", false, false),

    /**
     * For use with group-by and std:groupwin, reclaim groups for unbound streams based on time. The number of seconds after which a groups is reclaimed if inactive.
     */
    RECLAIM_GROUP_AGED("RECLAIM_GROUP_AGED", true, true),

    /**
     * For use with group-by and std:groupwin, reclaim groups for unbound streams based on time, this number is the frequency in seconds at which a sweep occurs for aged
     * groups, if not provided then the sweep frequency is the same number as the age.
     */
    RECLAIM_GROUP_FREQ("RECLAIM_GROUP_FREQ", true, true),

    /**
     * For use with create-named-window statements only, to indicate that statements that subquery the named window
     * use named window data structures (unless the subquery statement specifies below DISBABLE hint and as listed below).
     * <p>
     * By default and if this hint is not specified or subqueries specify a stream filter on a named window,
     * subqueries use statement-local data structures representing named window contents (table, index).
     * Such data structure is maintained by consuming the named window insert and remove stream.
     */
    ENABLE_WINDOW_SUBQUERY_INDEXSHARE("ENABLE_WINDOW_SUBQUERY_INDEXSHARE", false, false),

    /**
     * If ENABLE_WINDOW_SUBQUERY_INDEXSHARE is not specified for a named window (the default) then this instruction is ignored.
     * <p>
     * For use with statements that subquery a named window and that benefit from a statement-local data structure representing named window contents (table, index),
     * maintained through consuming the named window insert and remove stream.
     * <p>
     */
    DISABLE_WINDOW_SUBQUERY_INDEXSHARE("DISABLE_WINDOW_SUBQUERY_INDEXSHARE", false, false),

    /**
     * For use with subqueries and on-select, on-merge, on-update and on-delete to specify the query engine neither
     * build an implicit index nor use an existing index, always performing a full table scan.
     */
    SET_NOINDEX("SET_NOINDEX", false, false);

    private final String value;
    private final boolean acceptsParameters;
    private final boolean requiresParameters;

    private HintEnum(String value, boolean acceptsParameters, boolean requiresParameters)
    {
        this.value = value.toUpperCase();
        this.acceptsParameters = acceptsParameters;
        if (acceptsParameters)
        {
            this.requiresParameters = true;
        }
        else
        {
            this.requiresParameters = requiresParameters;
        }

    }

    /**
     * Returns the constant.
     * @return constant
     */
    public String getValue()
    {
        return value;
    }

    /**
     * True if the hint accepts params.
     * @return indicator
     */
    public boolean isAcceptsParameters()
    {
        return acceptsParameters;
    }

    /**
     * True if the hint requires params.
     * @return indicator
     */
    public boolean isRequiresParameters()
    {
        return requiresParameters;
    }

    /**
     * Check if the hint is present in the annotations provided.
     * @param annotations the annotations to inspect
     * @return indicator
     */
    public Hint getHint(Annotation[] annotations)
    {
        if (annotations == null)
        {
            return null;
        }

        for (Annotation annotation : annotations)
        {
            if (!(annotation instanceof Hint))
            {
                continue;
            }

            Hint hintAnnotation = (Hint) annotation;
            String hintVal = hintAnnotation.value();
            if (hintVal == null)
            {
                continue;
            }
            hintVal = hintVal.trim().toUpperCase();

            if (hintVal.equals(value))
            {
                return hintAnnotation;
            }

            if (hintVal.indexOf('=') != -1)
            {
                String hintName = hintVal.substring(0, hintVal.indexOf('='));
                if (hintName.trim().toUpperCase().equals(value))
                {
                    return hintAnnotation;
                }
            }

            String[] hints = hintVal.split(",");
            for (String hint : hints)
            {
                hintVal = hint.trim().toUpperCase();
                if (hintVal.equals(value))
                {
                    return hintAnnotation;
                }

                if (hintVal.indexOf('=') != -1)
                {
                    String hintName = hintVal.substring(0, hintVal.indexOf('='));
                    if (hintName.trim().toUpperCase().equals(value))
                    {
                        return hintAnnotation;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns null or value assigned.
     * @param hintAnnotation annotation
     * @return value
     */
    public String getHintAssignedValue(Hint hintAnnotation)
    {
        if (!acceptsParameters)
        {
            return null;
        }
        
        String hintValMixed = hintAnnotation.value().trim();
        String hintVal = hintValMixed.toUpperCase();

        if (hintVal.indexOf(",") == -1)
        {
            if (hintVal.indexOf('=') == -1)
            {
                return null;
            }

            String hintName = hintVal.substring(0, hintVal.indexOf('='));
            if (!hintName.equals(value))
            {
                return null;
            }
            return hintValMixed.substring(hintVal.indexOf('=') + 1, hintVal.length());
        }

        String[] hints = hintValMixed.split(",");
        for (String hint : hints)
        {
            int indexOfEquals = hint.indexOf('=');
            if (indexOfEquals == -1)
            {
                continue;
            }

            hintVal = hint.substring(0, indexOfEquals).trim().toUpperCase();
            if (!hintVal.equals(value))
            {
                continue;
            }

            String value = hint.substring(indexOfEquals + 1).trim();
            if (value.length() == 0)
            {
                return null;
            }
            return value;
        }
        return null;
    }

    /**
     * Validate a hint annotation ensuring it contains only recognized hints.
     * @param annotation to validate
     * @throws AnnotationException if an invalid text was found
     */
    public static void validate(Annotation annotation) throws AnnotationException
    {
        if (!(annotation instanceof Hint))
        {
            return;
        }

        Hint hint = (Hint) annotation;

        for (HintEnum val : HintEnum.values())
        {
            if (val.getValue().equals(hint.value().trim().toUpperCase()))
            {
                validateParameters(val, hint.value().trim());
                return;
            }
        }

        String[] hints = hint.value().split(",");
        for (int i = 0; i < hints.length; i++)
        {
            String hintVal = hints[i].trim().toUpperCase();
            HintEnum found = null;
            
            for (HintEnum val : HintEnum.values())
            {
                if (val.getValue().equals(hintVal))
                {
                    found = val;
                    break;
                }

                if (hintVal.indexOf('=') != -1)
                {
                    String hintName = hintVal.substring(0, hintVal.indexOf('='));
                    if (val.getValue().equals(hintName.trim().toUpperCase()))
                    {
                        found = val;
                        break;
                    }
                }
            }

            if (found == null)
            {
                String hintName = hints[i].trim();
                if (hintName.indexOf('=') != -1)
                {
                    hintName = hintName.substring(0, hintName.indexOf('='));
                }
                throw new AnnotationException("Hint annotation value '" + hintName.trim() + "' is not one of the known values");
            }
            else
            {
                validateParameters(found, hintVal);                
            }
        }
    }

    private static void validateParameters(HintEnum val, String hintVal) throws AnnotationException
    {
        if (val.isRequiresParameters())
        {
            if (hintVal.indexOf('=') == -1)
            {
                throw new AnnotationException("Hint '" + val + "' requires a parameter value");
            }
        }
        if (!val.isAcceptsParameters())
        {
            if (hintVal.indexOf('=') != -1)
            {
                throw new AnnotationException("Hint '" + val + "' does not accept a parameter value");
            }
        }
    }
}