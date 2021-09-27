package plc.compiler;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the tokens and returns the parsed AST.
     */
    public static Ast parse(List<Token> tokens) throws ParseException {
        return new Parser(tokens).parseSource();
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        List<Ast.Statement> trees = new ArrayList<>();
        while(tokens.has(0))
        {
            trees.add(parseStatement());
        }
        return new plc.compiler.Ast.Source(trees);
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, assignment, if, or while
     * statement, then it is an expression statement. See these methods for
     * clarification on what starts each type of statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        if(match("LET"))
            return parseDeclarationStatement();
        else if(match(Token.Type.IDENTIFIER, "="))
            return parseAssignmentStatement();
        else if(match("IF"))
            return parseIfStatement();
        else if(match("WHILE"))
            return parseWhileStatement();
        else
            return parseExpressionStatement();
    }

    /**
     * Parses the {@code expression-statement} rule. This method is called if
     * the next tokens do not start another statement type, as explained in the
     * javadocs of {@link #parseStatement()}.
     */
    public Ast.Statement.Expression parseExpressionStatement() throws ParseException {
        Ast.Expression expression = parseExpression();
        if (match(";"))
            return new Ast.Statement.Expression(expression);
        throw new ParseException("Not a valid Expression Statement", 0);
    }

    /**
     * Parses the {@code declaration-statement} rule. This method should only be
     * called if the next tokens start a declaration statement, aka {@code let}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        String name;
        String type;
        Optional<Ast.Expression> expression = Optional.empty();
        if (match(Token.Type.IDENTIFIER, ":", Token.Type.IDENTIFIER))
        {
            name = tokens.get(-3).getLiteral();
            type = tokens.get(-1).getLiteral();
            if (match(";"))
                return new Ast.Statement.Declaration(name, type , expression);
            else if (match("=")) {
                expression = Optional.ofNullable(parseExpression());
                if (match(";"))
                    return new Ast.Statement.Declaration(name, type, expression);
                else
                    throw new ParseException("Not a valid Declaration", 0);

            }
        }
        throw new ParseException("Not a valid Declaration", 0);
    }

    /**
     * Parses the {@code assignment-statement} rule. This method should only be
     * called if the next tokens start an assignment statement, aka both an
     * {@code identifier} followed by {@code =}.
     */
    public Ast.Statement.Assignment parseAssignmentStatement() throws ParseException {
        String name;
        Ast.Expression expression;
        name = tokens.get(-2).getLiteral();
        expression = parseExpression();
        if (match(";"))
            return new Ast.Statement.Assignment(name, expression);
        throw new ParseException("Not a valid Assignment", 0);
    }

    /**
     * Parses the {@code if-statement} rule. This method should only be called
     * if the next tokens start an if statement, aka {@code if}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        Ast.Expression expression = parseExpression();
        List<Ast.Statement> then = new ArrayList();
        List<Ast.Statement> elseL = new ArrayList();
        if (match("THEN"))
        {
            if (match("END")) {
                return new Ast.Statement.If(expression, then, elseL);
            }
            while(!peek("ELSE") && !peek("END")  && tokens.has(0))
            {
                then.add(parseStatement());
            }
            if (match("ELSE"))
            {
                while(!peek("END") && tokens.has(0))
                {
                    elseL.add(parseStatement());
                }
            }
            if (match("END"))
                return new Ast.Statement.If(expression, then,elseL);
        }
        throw new ParseException("IF format incomplete", 0);
    }

    /**
     * Parses the {@code while-statement} rule. This method should only be
     * called if the next tokens start a while statement, aka {@code while}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        Ast.Expression condition = parseExpression();
        List<Ast.Statement> statements = new ArrayList();
        if (match("DO"))
        {
            if(match("END"))
                return new Ast.Statement.While(condition, statements);
            else {
                while(!peek("END")  && tokens.has(0))
                {
                    statements.add(parseStatement());
                }
            }
            if (match("END"))
                return new Ast.Statement.While(condition, statements);
        }
        throw new ParseException("While format incomplete", 0);
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseEqualityExpression();
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseEqualityExpression() throws ParseException {
        Ast.Expression left = parseAdditiveExpression();
        Ast.Expression right = null;
        String operator = "";
        while (match("==") || match("!=")) {
            operator = tokens.get(-1).getLiteral();
            right = parseAdditiveExpression();
            left = new Ast.Expression.Binary(operator, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        Ast.Expression left = parseMultiplicativeExpression();
        Ast.Expression right = null;
        String operator = "";
        while (match("+") || match("-")) {
            operator = tokens.get(-1).getLiteral();
            right = parseMultiplicativeExpression();
            left = new Ast.Expression.Binary(operator, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
        Ast.Expression left = parsePrimaryExpression();
        Ast.Expression right = null;
        String operator = "";
        while (match("*") || match("/")) {
            operator = tokens.get(-1).getLiteral();
            right = parsePrimaryExpression();
            left = new Ast.Expression.Binary(operator, left, right);
        }
        return left;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {
        if (match(Token.Type.IDENTIFIER, "("))
            return parseFunction();
        else if (peek("TRUE") || peek("FALSE") || peek(Token.Type.INTEGER) || peek(Token.Type.DECIMAL) || peek(Token.Type.STRING))
            return parseLiteral();
        else if (match(Token.Type.IDENTIFIER))
            return new Ast.Expression.Variable(tokens.get(-1).getLiteral());
        else if (match("("))
        {
            Ast.Expression expression = parseExpression();
            if (match(")"))
                return new Ast.Expression.Group(expression);
            throw new ParseException("Not a valid Group", 0);
        }
        throw new ParseException("Not a valid Expression", 0);
    }

    public Ast.Expression parseLiteral() throws ParseException {
        String value = tokens.get(0).getLiteral();
        if (match("TRUE") || match("FALSE"))
            return new Ast.Expression.Literal(Boolean.parseBoolean(value));
        else if (match(Token.Type.INTEGER)) {
            return new Ast.Expression.Literal(new BigInteger(value));
        }
        else if (match(Token.Type.DECIMAL)) {
            return new Ast.Expression.Literal(new BigDecimal(value));
        }
        else if (match(Token.Type.STRING))
        {
            value = value.substring(1 , value.length() - 1);
            return new Ast.Expression.Literal(value);
        }
        throw new ParseException("Not a valid Literal", 0);
    }

    public Ast.Expression parseFunction() throws ParseException {
        String name = tokens.get(-2).getLiteral();
        List<Ast.Expression> expressions = new ArrayList();
        while (tokens.has(0) && !peek(")"))
        {
            expressions.add(parseExpression());
            if (peek(",",")"))
                throw new ParseException("Missing argument", 0);
            else if (!match(","))
                break;
        }
        if(match(")"))
            return new Ast.Expression.Function(name, expressions);
        throw new ParseException("Not a valid Function", 0);
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) {
                return false;
            } else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            } else {
                throw new AssertionError();
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
