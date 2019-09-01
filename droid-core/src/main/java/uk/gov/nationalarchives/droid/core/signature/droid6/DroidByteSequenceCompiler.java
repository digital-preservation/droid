package uk.gov.nationalarchives.droid.core.signature.droid6;

import net.byteseek.parser.ParseException;
import net.byteseek.parser.StringParseReader;
import net.byteseek.parser.tree.ParseTree;

public class DroidByteSequenceCompiler {

    //TODO: compile byte sequence from list of subsequence ParseTrees.

    //TODO: BOF / EOF / VAR setting - ByteSequence defaults to VAR - reference parameter.
    public ByteSequence compileByteSequence(final String droidExpression) throws ParseException {
        final ByteSequence newByteSequence = new ByteSequence();
        compileByteSequence(newByteSequence, droidExpression);
        return newByteSequence;
    }

    //TODO: BOF / EOF / VAR setting - ByteSequence defaults to VAR - reference parameter.
    public void compileByteSequence(final ByteSequence sequence, final String droidExpression) throws ParseException {
        final StringParseReader reader = new StringParseReader(droidExpression);
        while (!reader.atEnd()) {
            final SubSequence newSequence = compileSubSequence(reader);
            if (newSequence == null) { // no new sub sequence (e.g. maybe some whitespace was left at the end).
                break;
            }
            sequence.addSubSequence(newSequence);
        }

        //TODO: error if no subsequences defined?
    }

    //TODO: compile from ParseTree rather than StringParseReader.

    public SubSequence compileSubSequence(final StringParseReader reader) throws ParseException {

        // Parse the subsequence:
        final ParseTree parsedSequence = DroidSubSequenceParser.parseSubSequence(reader);

        // Compile the parsed subsequence :
        if (parsedSequence != null) {

            // 1. Find the longest viable byte sequence to anchor the subsequence search:
            final int numNodes = parsedSequence.getNumChildren();
            int length = 0;
            int startPos = 0;
            int bestLength = 0;
            int bestStart  = 0;
            int bestEnd    = 0;
            for (int childIndex = 0; childIndex < numNodes; childIndex++) {
                ParseTree child = parsedSequence.getChild(childIndex);

                switch (child.getParseTreeType()) {

                    // Children that match a single byte position:
                    case BYTE: {
                        //TODO: if byte is inverted, then should we add it to end/start of anchoring sequence?
                        length++; // add one to the max length found.
                        break;
                    }

                    //TODO: vanilla pronom only uses bytes to determine max subsequence length.
                        //  this will produce signature definitions incompatible with droid versions earlier than 6.
                    case RANGE: {
                        //TODO: do we care about large ranges at the start/end of sequences?  This isn't optimal
                        length++;
                        break;
                    }

                    // Strings add the length of the string to the byte sequence:
                    case STRING: {
                        length += child.getTextValue().length(); // Add the string length.
                        break;
                    }

                    // Types which can't form part of an anchoring subsequence:
                    case ALTERNATIVES: case ANY: case REPEAT: case REPEAT_MIN_TO_MAX: case REPEAT_MIN_TO_MANY: {
                        // If we find a longer sequence that we had so far, use that:
                        if (length > bestLength) {
                            bestLength = length;
                            bestStart  = startPos;
                            bestEnd    = childIndex;
                        }

                        // Start looking for a longer suitable sequence:
                        length = 0;
                        startPos = childIndex + 1; // next subsequence to look for.
                        break;
                    }
                }
            }

            // Do a final check to see if the last nodes processed are the longest:
            if (length > bestLength) {
                bestLength = length;
                bestStart  = startPos;
                bestEnd    = numNodes;
            }

            // If we have no best length, then we have no anchoring subsequence - DROID can't process it.
            if (bestLength == 0) {
                throw new ParseException("No suitable anchoring sequence found in: " + reader.getString());
            }

            // 2. We have an anchoring subsequence - build the regular expression text for it:
            final SubSequence compiled = new SubSequence();
            compiled.setText(buildByteseekRegex(parsedSequence, bestStart, bestEnd));

            // 3. Create any left and right fragments needed on either side of the anchor text:


            return compiled;
        }
        return null;
    }

    private String buildByteseekRegex(ParseTree parsedSequence, int start, int end) throws ParseException {
        final StringBuilder regex = new StringBuilder();
        for (int childIndex = start; childIndex < end; childIndex++) {
            ParseTree node = parsedSequence.getChild(childIndex);
            switch (node.getParseTreeType()) {
                case BYTE: {
                    if (node.isValueInverted()) {
                        regex.append('^');
                    }
                    regex.append(String.format("%02x", node.getIntValue()));
                    break;
                }
                case STRING: {
                    regex.append('\'').append(node.getTextValue()).append('\'');
                    break;
                }
                case RANGE: {
                    if (node.isValueInverted()) {
                        regex.append('^');
                    }
                    ParseTree firstIntNode = node.getChild(0);
                    ParseTree secondIntNode = node.getChild(1);
                    regex.append(String.format("%02x", firstIntNode.getIntValue()));
                    regex.append('-');
                    regex.append(String.format("%02x", secondIntNode.getIntValue()));
                }
            }
        }
        return regex.toString();
    }


}
