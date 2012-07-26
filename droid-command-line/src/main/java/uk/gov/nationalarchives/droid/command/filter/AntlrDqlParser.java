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
package uk.gov.nationalarchives.droid.command.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * @author rflitcroft
 *
 */
public class AntlrDqlParser implements DqlFilterParser {

    private static final String SINGLE_QUOTE = "'";

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterCriterion parse(String dql) {
        
        try {
            CommonTree tree = AntlrUtils.parseDqlToAbstractSytntaxTree(dql);
            
            List<?> children = tree.getChildren();
            
            CommonTree field = (CommonTree) children.get(0);
            String dqlField = field.getText();
            
            CommonTree operator = (CommonTree) children.get(1);
            String dqlOperator = operator.getText();
            
            CommonTree values = (CommonTree) children.get(2);
            
            FilterCriterion criterion;
            
            final List<CommonTree> setValues = values.getChildren();
            if (setValues == null) {
                String dqlValue = values.getText();
                criterion = DqlCriterionFactory.newCriterion(dqlField, dqlOperator, 
                        fromDqlString(dqlValue));
            } else {
                Collection<String> dqlValues = new ArrayList<String>();
                for (CommonTree element : setValues) {
                    dqlValues.add(element.getText());
                }
                criterion = DqlCriterionFactory.newCriterion(dqlField, dqlOperator, dqlValues);
            }
            
            return criterion;
        } catch (RecognitionException e) {
            throw new DqlParseException(e);
        }

    }
    
    private static String fromDqlString(String dqlString) {
        return StringUtils.strip(dqlString, SINGLE_QUOTE).replace("\\'", SINGLE_QUOTE);
    }
    
}
