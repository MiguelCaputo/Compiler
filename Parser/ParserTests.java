package plc.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * You know the drill...
 */
final class ParserTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testSource(String test, List<Token> tokens, Ast.Source expected) {
        test(tokens, expected, Parser::parseSource);
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("Zero Statements",
                        Arrays.asList(),
                        new Ast.Source(Arrays.asList())
                ),
                Arguments.of("Multiple Statements",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "stmt1", -1),
                                new Token(Token.Type.OPERATOR, ";", -1),
                                new Token(Token.Type.IDENTIFIER, "stmt2", -1),
                                new Token(Token.Type.OPERATOR, ";", -1),
                                new Token(Token.Type.IDENTIFIER, "stmt3", -1),
                                new Token(Token.Type.OPERATOR, ";", -1)
                        ),
                        new Ast.Source(Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Variable("stmt1")),
                                new Ast.Statement.Expression(new Ast.Expression.Variable("stmt2")),
                                new Ast.Statement.Expression(new Ast.Expression.Variable("stmt3"))
                        ))
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testExpressionStatement(String test, List<Token> tokens, Ast.Statement.Expression expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testExpressionStatement() {
        return Stream.of(
                Arguments.of("Function Expression",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, "(", -1),
                                new Token(Token.Type.OPERATOR, ")", -1),
                                new Token(Token.Type.OPERATOR, ";", -1)
                        ),
                        new Ast.Statement.Expression(new Ast.Expression.Function("name", Arrays.asList()))
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testDeclarationStatement(String test, List<Token> tokens, Ast.Statement.Declaration expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Definition",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "LET", -1),
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, ":", -1),
                                new Token(Token.Type.IDENTIFIER, "TYPE", -1),
                                new Token(Token.Type.OPERATOR, ";", -1)
                        ),
                        new Ast.Statement.Declaration("name", "TYPE", Optional.empty())
                ),
                Arguments.of("Initialization",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "LET", -1),
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, ":", -1),
                                new Token(Token.Type.IDENTIFIER, "TYPE", -1),
                                new Token(Token.Type.OPERATOR, "=", -1),
                                new Token(Token.Type.IDENTIFIER, "expr", -1),
                                new Token(Token.Type.OPERATOR, ";", -1)
                        ),
                        new Ast.Statement.Declaration("name", "TYPE", Optional.of(new Ast.Expression.Variable("expr")))
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testAssignmentStatement(String test, List<Token> tokens, Ast.Statement.Assignment expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testAssignmentStatement() {
        return Stream.of(
                Arguments.of("Assignment",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, "=", -1),
                                new Token(Token.Type.IDENTIFIER, "expr", -1),
                                new Token(Token.Type.OPERATOR, ";", -1)
                        ),
                        new Ast.Statement.Assignment("name", new Ast.Expression.Variable("expr"))
                )
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testIfStatement(String test, List<Token> tokens, Ast.Statement.If expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    static Stream<Arguments> testIfStatement() {
        return Stream.of(
                Arguments.of("Then",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "IF", -1),
                                new Token(Token.Type.IDENTIFIER, "expr", -1),
                                new Token(Token.Type.IDENTIFIER, "THEN", -1),
                                new Token(Token.Type.IDENTIFIER, "stmt", -1),
                                new Token(Token.Type.OPERATOR, ";", -1),
                                new Token(Token.Type.IDENTIFIER, "END", -1)
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Variable("expr"),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Variable("stmt"))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Else",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "IF", -1),
                                new Token(Token.Type.IDENTIFIER, "expr", -1),
                                new Token(Token.Type.IDENTIFIER, "THEN", -1),
                                new Token(Token.Type.IDENTIFIER, "stmt1", -1),
                                new Token(Token.Type.OPERATOR, ";", -1),
                                new Token(Token.Type.IDENTIFIER, "ELSE", -1),
                                new Token(Token.Type.IDENTIFIER, "stmt2", -1),
                                new Token(Token.Type.OPERATOR, ";", -1),
                                new Token(Token.Type.IDENTIFIER, "END", -1)
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Variable("expr"),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Variable("stmt1"))),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Variable("stmt2")))
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testWhileStatement(String test, List<Token> tokens, Ast.Statement.While expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    static Stream<Arguments> testWhileStatement() {
        return Stream.of(
                Arguments.of("While",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "WHILE", -1),
                                new Token(Token.Type.IDENTIFIER, "expr", -1),
                                new Token(Token.Type.IDENTIFIER, "DO", -1),
                                new Token(Token.Type.IDENTIFIER, "stmt", -1),
                                new Token(Token.Type.OPERATOR, ";", -1),
                                new Token(Token.Type.IDENTIFIER, "END", -1)
                        ),
                        new Ast.Statement.While(
                                new Ast.Expression.Variable("expr"),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Variable("stmt")))
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testLiteralExpression(String test, List<Token> tokens, Ast.Expression.Literal expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Boolean Literal",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "TRUE", -1)),
                        new Ast.Expression.Literal(Boolean.TRUE)
                ),
                Arguments.of("Integer Literal",
                        Arrays.asList(new Token(Token.Type.INTEGER, "1", -1)),
                        new Ast.Expression.Literal(new BigInteger("1"))
                ),
                Arguments.of("Decimal Literal",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "2.0", -1)),
                        new Ast.Expression.Literal(new BigDecimal("2.0"))
                ),
                Arguments.of("String Literal",
                        Arrays.asList(new Token(Token.Type.STRING, "\"string\"", -1)),
                        new Ast.Expression.Literal("string")
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testGroupExpression(String test, List<Token> tokens, Ast.Expression.Group expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                Arguments.of("Grouped Variable",
                        Arrays.asList(
                                new Token(Token.Type.OPERATOR, "(", -1),
                                new Token(Token.Type.IDENTIFIER, "expr", -1),
                                new Token(Token.Type.OPERATOR, ")", -1)
                        ),
                        new Ast.Expression.Group(new Ast.Expression.Variable("expr"))
                ),
                Arguments.of("Grouped Binary",
                        Arrays.asList(
                                new Token(Token.Type.OPERATOR, "(", -1),
                                new Token(Token.Type.IDENTIFIER, "expr1", -1),
                                new Token(Token.Type.OPERATOR, "+", -1),
                                new Token(Token.Type.IDENTIFIER, "expr2", -1),
                                new Token(Token.Type.OPERATOR, ")", -1)
                        ),
                        new Ast.Expression.Group(new Ast.Expression.Binary("+",
                                new Ast.Expression.Variable("expr1"),
                                new Ast.Expression.Variable("expr2")
                        ))
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testBinaryExpression(String test, List<Token> tokens, Ast.Expression.Binary expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("Binary Equality",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", -1),
                                new Token(Token.Type.OPERATOR, "==", -1),
                                new Token(Token.Type.IDENTIFIER, "expr2", -1)
                        ),
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("expr1"),
                                new Ast.Expression.Variable("expr2")
                        )
                ),
                Arguments.of("Binary Addition",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", -1),
                                new Token(Token.Type.OPERATOR, "+", -1),
                                new Token(Token.Type.IDENTIFIER, "expr2", -1)
                        ),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Variable("expr1"),
                                new Ast.Expression.Variable("expr2")
                        )
                ),
                Arguments.of("Binary Multiplication",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", -1),
                                new Token(Token.Type.OPERATOR, "*", -1),
                                new Token(Token.Type.IDENTIFIER, "expr2", -1)
                        ),
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Variable("expr1"),
                                new Ast.Expression.Variable("expr2")
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testVariableExpression(String test, List<Token> tokens, Ast.Expression.Variable expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testVariableExpression() {
        return Stream.of(
                Arguments.of("Variable",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "name", -1)),
                        new Ast.Expression.Variable("name")
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testFunctionExpression(String test, List<Token> tokens, Ast.Expression.Function expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Zero Arguments",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, "(", -1),
                                new Token(Token.Type.OPERATOR, ")", -1)
                        ),
                        new Ast.Expression.Function("name", Arrays.asList())
                ),
                Arguments.of("Multiple Arguments",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, "(", -1),
                                new Token(Token.Type.IDENTIFIER, "expr1", -1),
                                new Token(Token.Type.OPERATOR, ",", -1),
                                new Token(Token.Type.IDENTIFIER, "expr2", -1),
                                new Token(Token.Type.OPERATOR, ",", -1),
                                new Token(Token.Type.IDENTIFIER, "expr3", -1),
                                new Token(Token.Type.OPERATOR, ")", -1)
                        ),
                        new Ast.Expression.Function("name", Arrays.asList(
                                new Ast.Expression.Variable("expr1"),
                                new Ast.Expression.Variable("expr2"),
                                new Ast.Expression.Variable("expr3")
                        ))
                ),
                Arguments.of("Complex Argument",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, "(", -1),
                                new Token(Token.Type.IDENTIFIER, "expr1", -1),
                                new Token(Token.Type.OPERATOR, "+", -1),
                                new Token(Token.Type.IDENTIFIER, "expr2", -1),
                                new Token(Token.Type.OPERATOR, ")", -1)
                        ),
                        new Ast.Expression.Function("name", Arrays.asList(
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Variable("expr1"),
                                        new Ast.Expression.Variable("expr2")
                                )
                        ))
                )
        );
    }

    @Test
    void testExample1() {
        /* LET first: INTEGER = 1;
         * WHILE first != 10 DO
         *   PRINT(first);
         *   first = first + 1;
         * END
         */
        List<Token> input = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "LET", -1),
                new Token(Token.Type.IDENTIFIER, "first", -1),
                new Token(Token.Type.OPERATOR, ":", -1),
                new Token(Token.Type.IDENTIFIER, "INTEGER", -1),
                new Token(Token.Type.OPERATOR, "=", -1),
                new Token(Token.Type.INTEGER, "1", -1),
                new Token(Token.Type.OPERATOR, ";", -1),

                new Token(Token.Type.IDENTIFIER, "WHILE", -1),
                new Token(Token.Type.IDENTIFIER, "first", -1),
                new Token(Token.Type.OPERATOR, "!=", -1),
                new Token(Token.Type.INTEGER, "10", -1),
                new Token(Token.Type.IDENTIFIER, "DO", -1),

                new Token(Token.Type.IDENTIFIER, "PRINT", -1),
                new Token(Token.Type.OPERATOR, "(", -1),
                new Token(Token.Type.IDENTIFIER, "first", -1),
                new Token(Token.Type.OPERATOR, ")", -1),
                new Token(Token.Type.OPERATOR, ";", -1),

                new Token(Token.Type.IDENTIFIER, "first", -1),
                new Token(Token.Type.OPERATOR, "=", -1),
                new Token(Token.Type.IDENTIFIER, "first", -1),
                new Token(Token.Type.OPERATOR, "+", -1),
                new Token(Token.Type.INTEGER, "1", -1),
                new Token(Token.Type.OPERATOR, ";", -1),

                new Token(Token.Type.IDENTIFIER, "END", -1)
        );
        Ast.Source expected = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("first", "INTEGER",
                        Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(1)))),
                new Ast.Statement.While(
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Variable("first"),
                                new Ast.Expression.Literal(BigInteger.valueOf(10))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(
                                        new Ast.Expression.Function("PRINT", Arrays.asList(
                                                new Ast.Expression.Variable("first"))
                                        )
                                ),
                                new Ast.Statement.Assignment("first",
                                        new Ast.Expression.Binary("+",
                                                new Ast.Expression.Variable("first"),
                                                new Ast.Expression.Literal(BigInteger.valueOf(1))
                                        )
                                )
                        )
                )
        ));
        test(input, expected, Parser::parseSource);
    }

    /**
     * Standard test function. If expected is null, a ParseException is expected
     * to be thrown (not used in the provided tests).
     */
    private static <T extends Ast> void test(List<Token> tokens, T expected, Function<Parser, T> function) {
        Parser parser = new Parser(tokens);
        if (expected != null) {
            Assertions.assertEquals(expected, function.apply(parser));
        } else {
            Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        }
    }

}
