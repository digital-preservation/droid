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
