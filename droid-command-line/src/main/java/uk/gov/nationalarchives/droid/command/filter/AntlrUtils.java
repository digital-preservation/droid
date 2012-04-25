/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

import uk.gov.nationalarchives.droid.dql.DqlLexer;
import uk.gov.nationalarchives.droid.dql.DqlParser;

/**
 * Utility classs for antlr.
 * @author rflitcroft
 *
 */
public final class AntlrUtils {

    private AntlrUtils() { }
    
    /**
     * Parses a DQL string into an syntax tree.
     * @param dql the DQl to parse
     * @return an AST
     * @throws RecognitionException if eth DQl could not be parsed.
     */
    static CommonTree parseDqlToAbstractSytntaxTree(String dql) throws RecognitionException {
        
        ANTLRStringStream input = new ANTLRStringStream(dql);
        DqlLexer lexer = new DqlLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        DqlParser parser = new DqlParser(tokens) {
            @Override
            public void emitErrorMessage(String msg) {
                throw new DqlParseException(msg);
            }
        };
        
        final TreeAdaptor adaptor = new CommonTreeAdaptor() {
            @Override
            public Object create(Token payload) {
                return new CommonTree(payload);
            }
        };
        
        parser.setTreeAdaptor(adaptor);
        DqlParser.criterion_return ret = parser.criterion();
        CommonTree tree = (CommonTree) ret.getTree();
        
        return tree;

    }
}
