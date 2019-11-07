package uk.gov.nationalarchives.droid.core.signature.droid6;

import java.util.ArrayList;
import java.util.List;

import net.byteseek.parser.ParseException;
import net.byteseek.parser.StringParseReader;
import net.byteseek.parser.tree.ParseTree;
import net.byteseek.parser.tree.ParseTreeType;
import net.byteseek.parser.tree.node.StringNode;
import net.byteseek.parser.tree.node.BaseNode;
import net.byteseek.parser.tree.node.ByteNode;
import net.byteseek.parser.tree.node.ChildrenNode;
import net.byteseek.parser.tree.node.IntNode;

public class DroidSubSequenceParser {
    
    public static ParseTree parseSubSequence(final String droidExpression) throws ParseException {
        return parseSubSequence(new StringParseReader(droidExpression));
    }
    
    public static ParseTree parseSubSequence(final StringParseReader reader) throws ParseException {
        final List<ParseTree> subSequenceNodes = new ArrayList<ParseTree>();
        int currentChar;
        SUBSEQUENCE: while ((currentChar = reader.read()) >= 0) {
            switch (currentChar) {

                // * Wildcard - end of subsequence.
                case '*': {
                    break SUBSEQUENCE;
                }

                // Whitespace (ignore)
                case ' ': case '\n': case '\r': case '\t': {
                    break;
                }

                // Hex byte:
                case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': {
                    subSequenceNodes.add(ByteNode.valueOf(reader.readHexByte(currentChar)));
                    break;
                }

                // Any byte ??
                case '?': {
                    subSequenceNodes.add(parseAnyNode(reader));
                    break;
                }

                // Inverted or ranged bytes:
                case '[': {
                    subSequenceNodes.add(parseByteSet(reader));
                    break;
                }

                // Wildcard Gaps {n}, {n-m} and {n-*}
                case '{': {
                    subSequenceNodes.add(parseGapNode(reader));
                    break;
                }

                // Alternative sequences (a|b)
                case '(': {
                    subSequenceNodes.add(parseAlternatives(reader));
                    break;
                }

                // Open string (used in container syntax, so support here):
                case '\'': {
                    subSequenceNodes.add(parseString(reader));
                    break;
                }
                default:
                    //TODO should we throw a ParseException exception here or just ignore?

            }

        }

        // If we have some nodes, return them as a sequence node
        if (!subSequenceNodes.isEmpty()) {
            return new ChildrenNode(ParseTreeType.SEQUENCE, subSequenceNodes);

        }
        return null; // no nodes found - return null.
    }

    private static ParseTree parseAlternatives(final StringParseReader reader) throws ParseException {
        List<ParseTree> alternatives = new ArrayList<ParseTree>();
        List<ParseTree> sequence = new ArrayList<ParseTree>();

        int currentChar;
        ALTERNATIVES: while ((currentChar = reader.read()) >= 0) {

            switch (currentChar) {

                // Closes alternatives - stop processing.
                case ')': {
                    break ALTERNATIVES;
                }

                // Ignore whitespace (not part of normal DROID syntax, but we use it in container sigs):
                case ' ': case '\n': case '\r': case '\t': {
                    break;
                }

                // Starts a new alternative
                case '|': {
                    if (sequence.size() == 0) { // no sequence defined before alternative:
                        throw createParseException("No sequence defined before alternative |", reader);
                    }
                    alternatives.add(new ChildrenNode(ParseTreeType.SEQUENCE, sequence));
                    sequence = new ArrayList<ParseTree>(); // start a new sequence list.
                    break;
                }

                // String type: supported in container signatures, so support it here.
                case '\'': {
                    sequence.add(parseString(reader));
                    break;
                }

                // Must be a hex byte - add it to the alternative sequence.  Will throw an error if not.
                default: {
                    sequence.add(ByteNode.valueOf(reader.readHexByte(currentChar)));
                }
            }
        }

        // If we've closed the alternatives properly and we have some, return them:
        if (currentChar == ')' && alternatives.size() > 0) {
            return new ChildrenNode(ParseTreeType.ALTERNATIVES, alternatives);
        }
        throw createParseException("Alternatives (a|b) syntax incorrect", reader);
    }

    private static ParseTree parseByteSet(final StringParseReader reader) throws ParseException {

        // Check whether values are inverted using ! after the open [
        boolean inverted = false;
        if (reader.peekAhead() == '!') {
            inverted = true;
            reader.read(); // consume the ! character.
        }

        // Must be a hex byte following the open [ or [!
        byte firstByte = reader.readHexByte();

        // Get the next character:
        int nextChar = reader.read();

        // If we're closing the set now, it's a byte or inverted byte:
        if (nextChar == ']') {
            return new ByteNode(firstByte, inverted);
        }

        // If we're not closing the set now, it must be a range of bytes:
        if (nextChar == ':') {
            byte secondByte = reader.readHexByte();

            // The next character must close the range after the second byte value:
            if (reader.read() == ']') {
                return new ChildrenNode(ParseTreeType.RANGE, inverted, ByteNode.valueOf(firstByte), ByteNode.valueOf(secondByte));
            }
        }

        throw createParseException("[] syntax incorrect", reader);
    }

    private static ParseTree parseString(final StringParseReader reader) throws ParseException {
        return new StringNode(reader.readString('\''));
    }

    private static ParseTree parseAnyNode(final StringParseReader reader) throws ParseException {
        if (reader.read() == '?') {
            return BaseNode.ANY_NODE;
        }
        // must be two ?? together.  Invalid syntax if there isn't a second ?
        throw createParseException("? not followed by another ?", reader);
    }

    private static ParseTree parseGapNode(final StringParseReader reader) throws ParseException {
        final int firstGapNumber = reader.readInt();
        int nextChar = reader.read();

        // End of the gap?
        if (nextChar == '}') {
            // only a fixed gap exists - return a repeated ANY node:
            return new ChildrenNode(ParseTreeType.REPEAT, new IntNode(firstGapNumber), BaseNode.ANY_NODE);
        }

        // If not the end, then a second value exists?
        if (nextChar == '-') {

            // If the value is a wildcard *:
            if (reader.peekAhead() == '*') {
                reader.read(); // consume the *
                if (reader.read() == '}') { // And is closed by a }
                    // Return a min to many node:
                    return new ChildrenNode(ParseTreeType.REPEAT_MIN_TO_MANY, new IntNode(firstGapNumber), BaseNode.ANY_NODE);
                }
            } else { // The value must be a second number:
                final int secondGapNumber = reader.readInt();
                if (reader.read() == '}') { // number is closed with }
                    return new ChildrenNode(ParseTreeType.REPEAT_MIN_TO_MAX,
                            new IntNode(firstGapNumber), new IntNode(secondGapNumber), BaseNode.ANY_NODE);
                }

            }
        }

        // Syntax isn't right.  We drop through to this if any of the above syntax fails to parse.
        throw createParseException("{} wildcard matching syntax incorrect", reader);
    }

    private static ParseException createParseException(String message, StringParseReader reader) throws ParseException {
        return new ParseException(message + ". Position: " + reader.getPosition() + " in: " + reader.getString());
    }

}
