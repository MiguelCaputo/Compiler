package plc.compiler;

import java.util.ArrayList;
import java.util.List;

public final class Lexer {

    final CharStream chars;

    Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Lexes the input and returns the list of tokens.
     */
    public static List<Token> lex(String input) throws ParseException {
        return new Lexer(input).lex();
    }

    /**
     * Repeatedly lexes the next token using {@link #lexToken()} until the end
     * of the input is reached, returning the list of tokens lexed. This should
     * also handle skipping whitespace.
     */
    List<Token> lex() throws ParseException {
        List<plc.compiler.Token> list = new ArrayList<>();
        while (chars.has(0)) {
            if (!peek("[ \n\r\t]")) {
                list.add(lexToken());
                if(peek("[ \n\r\t]")) {
                    chars.advance();
                }
            }
            else
                chars.advance();
        }
        return list;
    }

    Token lexToken() throws ParseException {
        chars.skip();
        if (peek("[A-Za-z_]"))
            return lexIdentifier();
        else if (peek("[0-9]"))
            return lexNumber();
        else if (peek("\""))
            return lexString();
        else if (peek("."))
            return lexOperator();
        else
            throw new plc.compiler.ParseException("Not a valid Token", chars.index);
    }

    /**
     * Lexes an IDENTIFIER token. Unlike the previous project, fewer characters
     * are allowed in identifiers.
     */
    Token lexIdentifier() throws ParseException {
        if(match("[A-Za-z_]"))
        {
            while(match("[A-Za-z0-9_]"))
            {
                continue;
            }
            return chars.emit(plc.compiler.Token.Type.IDENTIFIER);
        }
        throw new plc.compiler.ParseException("Not a valid Identifier", chars.index);
    }

    Token lexNumber() throws ParseException {
        boolean isDot = false;
        if (peek("[0-9]")) {
            while (peek("[0-9]") || peek("\\.")) {
                if (peek("\\.")) {
                    if (isDot)
                        return chars.emit(plc.compiler.Token.Type.DECIMAL);
                    else if(!peek("\\.","[0-9]"))
                        return chars.emit(plc.compiler.Token.Type.INTEGER);
                    match("\\.");
                    isDot = true;
                }
                else if (match("[0-9]")) {}
            }
            if (isDot)
                return chars.emit(plc.compiler.Token.Type.DECIMAL);
            return chars.emit(plc.compiler.Token.Type.INTEGER);
        }
        throw new plc.compiler.ParseException("Not a valid Number", chars.index);
    }

    Token lexString() throws plc.compiler.ParseException {
        int index = chars.index;
        if (!match("\""))
            throw new ParseException("String not starting with \"", index);
        while (!peek("[\"]")) {
            if (match((".")) || match("[ \n\t\r]")) {}
            else
                throw new plc.compiler.ParseException("Not a valid String", index);
        }
        if (match("\"")) {
            return chars.emit(Token.Type.STRING);
        }
        throw new ParseException("Not a valid String", index);
    }

    Token lexOperator() throws ParseException {
        if (match("=","=") || match("!","="))
            return chars.emit(plc.compiler.Token.Type.OPERATOR);
        match(".");
        return chars.emit(plc.compiler.Token.Type.OPERATOR);
    }

    boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }
        return true;
    }

    boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                chars.advance();
            }
        }
        return peek;
    }

     public static final class CharStream {

        final String input;
        int index = 0;
        int length = 0;

        CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip(); //we've saved the starting point already
            return new Token(type, input.substring(start, index), start);
        }

    }
}
