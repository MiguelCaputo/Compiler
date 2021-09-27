package plc.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Tests have been provided for a few selective parts of the Ast, and are not
 * exhaustive. You should add additional tests for the remaining parts and make
 * sure to handle all of the cases defined in the specification which have not
 * been tested here.
 */
public final class AnalyzerTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void testDeclarationStatement(String test, Ast.Statement.Declaration ast, Ast.Statement.Declaration expected) {
        Analyzer analyzer = test(ast, expected, Collections.emptyMap());
        if (expected != null) {
            Assertions.assertEquals(expected.getType(), analyzer.scope.lookup(ast.getName()).getJvmName());
        }
    }

    public static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Declare Boolean",
                        new Ast.Statement.Declaration("x", "BOOLEAN", Optional.empty()),
                        new Ast.Statement.Declaration("x", "boolean", Optional.empty())
                ),
                Arguments.of("Define String",
                        new Ast.Statement.Declaration("y", "STRING",
                                Optional.of(new Ast.Expression.Literal("string"))),
                        new Ast.Statement.Declaration("y", "String",
                                Optional.of(new Ast.Expression.Literal(Stdlib.Type.STRING, "string")))
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void testIfStatement(String test, Ast.Statement.If ast, Ast.Statement.If expected) {
        test(ast, expected, Collections.emptyMap());
    }

    public static Stream<Arguments> testIfStatement() {
        return Stream.of(
                Arguments.of("Valid Condition",
                        new Ast.Statement.If(
                                new Ast.Expression.Literal(Boolean.TRUE),
                                Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                new Ast.Expression.Literal("string")
                                        )))
                                ),
                                Arrays.asList()
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Literal(Stdlib.Type.BOOLEAN, Boolean.TRUE),
                                Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Function(Stdlib.Type.VOID, "System.out.println", Arrays.asList(
                                                new Ast.Expression.Literal(Stdlib.Type.STRING, "string")
                                        )))
                                ),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Invalid Condition",
                        new Ast.Statement.If(
                                new Ast.Expression.Literal("false"),
                                Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                new Ast.Expression.Literal("string")
                                        )))
                                ),
                                Arrays.asList()
                        ),
                        null
                ),
                Arguments.of("Invalid Statement",
                        new Ast.Statement.If(
                                new Ast.Expression.Literal(Boolean.TRUE),
                                Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Literal("string"))
                                ),
                                Arrays.asList()
                        ),
                        null
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void testLiteralExpression(String test, Ast.Expression.Literal ast, Ast.Expression.Literal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    public static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Integer Valid",
                        new Ast.Expression.Literal(BigInteger.TEN),
                        new Ast.Expression.Literal(Stdlib.Type.INTEGER, 10)
                ),
                Arguments.of("Integer Invalid",
                        new Ast.Expression.Literal(BigInteger.valueOf(123456789123456789L)),
                        null
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void testBinaryExpression(String test, Ast.Expression.Binary ast, Ast.Expression.Binary expected) {
        test(ast, expected, Collections.emptyMap());
    }

    public static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("Equals",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal(Boolean.FALSE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        new Ast.Expression.Binary(Stdlib.Type.BOOLEAN, "==",
                                new Ast.Expression.Literal(Stdlib.Type.BOOLEAN, Boolean.FALSE),
                                new Ast.Expression.Literal(Stdlib.Type.DECIMAL, 10.0)
                        )
                ),
                Arguments.of("String Concatenation",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal("b")
                        ),
                        new Ast.Expression.Binary(Stdlib.Type.STRING, "+",
                                new Ast.Expression.Literal(Stdlib.Type.INTEGER, 1),
                                new Ast.Expression.Literal(Stdlib.Type.STRING, "b")
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void testFunctionExpression(String test, Ast.Expression.Function ast, Ast.Expression.Function expected) {
        test(ast, expected, Collections.emptyMap());
    }

    public static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Print One Argument",
                        new Ast.Expression.Function("PRINT", Arrays.asList(
                                new Ast.Expression.Literal("string")
                        )),
                        new Ast.Expression.Function(Stdlib.Type.VOID, "System.out.println", Arrays.asList(
                                new Ast.Expression.Literal(Stdlib.Type.STRING, "string")
                        ))
                ),
                Arguments.of("Print Multiple Arguments",
                        new Ast.Expression.Function("PRINT", Arrays.asList(
                                new Ast.Expression.Literal("a"),
                                new Ast.Expression.Literal("b"),
                                new Ast.Expression.Literal("c")
                        )),
                        null
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void testCheckAssignable(String test, Stdlib.Type type, Stdlib.Type target, boolean success) {
        if (success) {
            Assertions.assertDoesNotThrow(() -> Analyzer.checkAssignable(type, target));
        } else {
            Assertions.assertThrows(AnalysisException.class, () -> Analyzer.checkAssignable(type, target));
        }
    }

    public static Stream<Arguments> testCheckAssignable() {
        return Stream.of(
                Arguments.of("Same Types", Stdlib.Type.BOOLEAN, Stdlib.Type.BOOLEAN, true),
                Arguments.of("Different Types", Stdlib.Type.BOOLEAN, Stdlib.Type.STRING, false),
                Arguments.of("Integer to Decimal", Stdlib.Type.INTEGER, Stdlib.Type.DECIMAL, true),
                Arguments.of("Decimal to Integer", Stdlib.Type.DECIMAL, Stdlib.Type.INTEGER, false),
                Arguments.of("String to Any", Stdlib.Type.STRING, Stdlib.Type.ANY, true),
                Arguments.of("Void to Any", Stdlib.Type.VOID, Stdlib.Type.ANY, false)
        );
    }

    private static <T extends Ast> Analyzer test(T ast, T expected, Map<String, Stdlib.Type> map) {
        Analyzer analyzer = new Analyzer(new Scope(null));
        map.forEach(analyzer.scope::define);
        if (expected != null) {
            Assertions.assertEquals(expected, analyzer.visit(ast));
        } else {
            Assertions.assertThrows(AnalysisException.class, () -> analyzer.visit(ast));
        }
        return analyzer;
    }

}
