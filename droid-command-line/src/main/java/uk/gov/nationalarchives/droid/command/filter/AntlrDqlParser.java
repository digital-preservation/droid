/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
     * @throws IOException 
     * @throws  
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
