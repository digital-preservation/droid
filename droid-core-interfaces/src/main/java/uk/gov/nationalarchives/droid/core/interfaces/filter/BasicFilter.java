package uk.gov.nationalarchives.droid.core.interfaces.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicFilter implements Filter {

    private List<FilterCriterion> criteria;
    private boolean enabled;
    private boolean narrowed;

    public BasicFilter(List<FilterCriterion> criteria, boolean isNarrowed) {
        this(criteria, isNarrowed, true);
    }

    public BasicFilter(List<FilterCriterion> criteria, boolean isNarrowed, boolean isEnabled) {
        this.criteria = criteria == null? Collections.EMPTY_LIST : criteria;
        this.narrowed = narrowed;
        this.enabled = enabled;
    }

    @Override
    public List<FilterCriterion> getCriteria() {
        return criteria;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isNarrowed() {
        return narrowed;
    }

    @Override
    public boolean hasCriteria() {
        return criteria.size() > 0;
    }

    @Override
    public void setNarrowed(boolean isNarrowed) {
        this.narrowed = isNarrowed;
    }

    @Override
    public FilterCriterion getFilterCriterion(int index) {
        return criteria.get(index);
    }

    @Override
    public int getNumberOfFilterCriterion() {
        return criteria.size();
    }

    /**
     * <b>Shallow Copy:</b>
     * BasicFilter clone only returns a shallow copy.  The FilterCriterion will be the same
     * objects as those in this object, although it will have its own list of them.
     * {@inheritDoc}
     */
    @Override
    public Filter clone() {
        return new BasicFilter(new ArrayList<>(criteria), narrowed, enabled);
    }
}
