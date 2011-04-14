package com.espertech.esper.filter;

import java.util.List;

/**
 * Holder object for a set of filters for one or more statements.
 */
public class FilterSet
{
    private List<FilterSetEntry> filters;

    /**
     * Ctor.
     * @param filters set of filters
     */
    public FilterSet(List<FilterSetEntry> filters)
    {
        this.filters = filters;
    }

    /**
     * Returns the filters.
     * @return filters
     */
    public List<FilterSetEntry> getFilters()
    {
        return filters;
    }
}
