/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.profile;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.RestrictionFactory;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Junction;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Restrictions;

/**
 * @author a-mpalmer
 *
 */
public final class SqlUtils {

    private SqlUtils() {
    }
    
    /**
     * Changes enumeration types to their ordinal value.
     * @param value The object to be transformed
     * @return Object the transformed object.
     */
    public static Object transformParameterToSQLValue(Object value) {
        return value instanceof Enum<?> ? ((Enum<?>) value).ordinal() : value;
    }
    
    /**
     * Transforms EJB queries into SQL syntax by replacing their
     * class names with table aliases.
     * 
     * @param ejbFragment The fragment of ejb to transform.
     * @param nodePrefix  The alias prefix of the profile resource node table.
     * @param formatPrefix The alias prefix of the format table.
     * @return String the EJB transformed to aliased tables and columns.
     */
    public static String transformEJBtoSQLFields(String ejbFragment,
            String nodePrefix, String formatPrefix) {
        return ejbFragment.replace("profileResourceNode.metaData.name", 
                        nodePrefix + ".name ")
                .replace("profileResourceNode.metaData.size", 
                        nodePrefix + ".file_size")
                .replace("profileResourceNode.metaData.extension", 
                        nodePrefix + ".extension")
                .replace("profileResourceNode.identificationCount",
                        nodePrefix + ".identification_count")
                .replace("profileResourceNode.metaData.lastModifiedDate",
                        nodePrefix + ".last_modified_date")
                .replace("profileResourceNode.metaData.resourceType",
                        nodePrefix + ".resource_type")        
                .replace("profileResourceNode.metaData.identificationMethod",
                        nodePrefix + ".identification_method")
                .replace("profileResourceNode.metaData.nodeStatus",
                        nodePrefix + ".node_status")
                .replace("format.mimeType",
                        formatPrefix + ".mime_type")
                .replace("format.name",
                        formatPrefix + ".name")
                .replace("format.puid",
                        formatPrefix + ".puid")
                .replace("extensionMismatch",
                        nodePrefix + ".extension_mismatch");
    }    
    
    /**
     * 
     * @param filter a filter to use to build the query.
     * @return QueryBuilder - the filter as an EJB QueryBuilder object.
     */
    public static QueryBuilder getQueryBuilder(Filter filter) {
        QueryBuilder queryBuilder = QueryBuilder
        .forAlias("profileResourceNode");
        queryBuilder.createAlias("format");
        
        if (filter.isNarrowed()) {
            for (FilterCriterion criterion : filter.getCriteria()) {
                queryBuilder.add(RestrictionFactory.forFilterCriterion(criterion));
            }
        } else {
            Junction disjunction = Restrictions.disjunction();
            for (FilterCriterion criterion : filter.getCriteria()) {
                disjunction.add(RestrictionFactory.forFilterCriterion(criterion));
            }
            queryBuilder.add(disjunction);
        }
        return queryBuilder;
    }
    
}
