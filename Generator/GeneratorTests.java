package plc.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

public class GeneratorTests {

    @Test
    void testSourcePrint() {
        Ast.Source ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Expression(new Ast.Expression.Function("print", Arrays.asList(
                        new Ast.Expression.Literal("Hello, World!")
                )))
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        print(\"Hello, World!\");",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testStatementPrint() {
        Ast.Statement ast = new Ast.Statement.Expression(new Ast.Expression.Function("print", Arrays.asList(
                new Ast.Expression.Literal("Hello, World!")
        )));
        String expected = "print(\"Hello, World!\");";
        test(ast, expected);
    }

    @Test
    void testExpressionPrint() {
        Ast.Expression ast = new Ast.Expression.Function("print", Arrays.asList(
                new Ast.Expression.Literal("Hello, World!")
        ));
        String expected = "print(\"Hello, World!\")";
        test(ast, expected);
    }

    @Test
    void testExpression() {
        Ast.Expression ast = new Ast.Expression.Binary("*",
                new Ast.Expression.Literal(new BigDecimal("3.14")),
                new Ast.Expression.Group(new Ast.Expression.Binary("*",
                        new Ast.Expression.Variable("r"),
                        new Ast.Expression.Variable("r")
                ))
        );
        String expected = "3.14 * (r * r)";
        test(ast, expected);
    }

    @Test
    void testDeclarationSmall() {
        Ast.Statement ast = new Ast.Statement.Declaration("str", "STRING",
                Optional.of(new Ast.Expression.Literal("hello")));
        String expected = "STRING str = \"hello\";";
        test(ast, expected);
    }

    @Test
    void testDeclarationBig() {
        Ast.Source ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("str", "STRING", Optional.of(new Ast.Expression.Literal("hello"))),
                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                        new Ast.Expression.Variable("str")
                ))),
                new Ast.Statement.Declaration("i", "INTEGER", Optional.empty()),
                new Ast.Statement.Assignment("i", new Ast.Expression.Literal(BigInteger.valueOf(5))),
                new Ast.Statement.Assignment("x", new Ast.Expression.Literal(BigInteger.TEN)),
                new Ast.Statement.Assignment("y", new Ast.Expression.Literal("why?")),
                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                        new Ast.Expression.Variable("x"),
                        new Ast.Expression.Variable("y")
                ))),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("i"),
                                new Ast.Expression.Variable("x")
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Variable("i")
                                )))
                        ),
                        Arrays.asList()
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        STRING str = \"hello\";",
                "        PRINT(str);",
                "        INTEGER i;",
                "        i = 5;",
                "        x = 10;",
                "        y = \"why?\";",
                "        PRINT(x, y);",
                "        if (i == x) {",
                "            PRINT(i);",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testIfSmall() {
        Ast.Statement ast = new Ast.Statement.If(
                new Ast.Expression.Binary("==",
                        new Ast.Expression.Variable("score"),
                        new Ast.Expression.Literal(BigInteger.valueOf(5))
                ),
                Arrays.asList(
                        new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                new Ast.Expression.Literal("five")
                        )))
                ),
                Arrays.asList()
        );
        String expected = String.join(System.lineSeparator(),
                "if (score == 5) {",
                "    PRINT(\"five\");",
                "}"
        );
        test(ast, expected);

    }

    @Test
    void testIfBig() {
        /*Ast input = Parser.parse(Lexer.lex("LET score : INTEGER;\nscore = score / 10;\nIF score == 10 THEN\n    " +
                "PRINT(\"A\");\nEND\nIF score == 9 THEN\n    " +
                "PRINT(\"A\");\nEND\nIF score == 8 THEN\n    " +
                "PRINT(\"B\");\nEND\nIF score == 7 THEN\n    " +
                "PRINT(\"C\");\nEND\nIF SCORE == 6 THEN\n    " +
                "PRINT(\"D\");END\nIF score != 10 THEN\n    " +
                "IF score != 9 THEN\n        " +
                "IF score != 8 THEN\n            " +
                "IF score != 7 THEN\n                " +
                "IF score != 6 THEN\n    " +
                "PRINT(\"E\");\n                " +
                "END\n            " +
                "END\n        " +
                "END\n    " +
                "END\nEND"));*/
        Ast.Source ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("score", "INTEGER", Optional.empty()),
                new Ast.Statement.Assignment("score", new Ast.Expression.Binary("/",
                        new Ast.Expression.Variable("score"),
                        new Ast.Expression.Literal(BigInteger.TEN)
                )),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("A")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(9))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("A")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(8))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("B")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(7))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("C")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(6))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("D")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        Arrays.asList(
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("score"),
                                                new Ast.Expression.Literal(BigInteger.valueOf(9))
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.If(
                                                        new Ast.Expression.Binary("!=",
                                                                new Ast.Expression.Variable("score"),
                                                                new Ast.Expression.Literal(BigInteger.valueOf(8))
                                                        ),
                                                        Arrays.asList(
                                                                new Ast.Statement.If(
                                                                        new Ast.Expression.Binary("!=",
                                                                                new Ast.Expression.Variable("score"),
                                                                                new Ast.Expression.Literal(BigInteger.valueOf(7))
                                                                        ),
                                                                        Arrays.asList(
                                                                                new Ast.Statement.If(
                                                                                        new Ast.Expression.Binary("!=",
                                                                                                new Ast.Expression.Variable("score"),
                                                                                                new Ast.Expression.Literal(BigInteger.valueOf(6))
                                                                                        ),
                                                                                        Arrays.asList(
                                                                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                                                                        new Ast.Expression.Literal("E")
                                                                                                )))
                                                                                        ),
                                                                                        Arrays.asList()
                                                                                )
                                                                        ),
                                                                        Arrays.asList()
                                                                )
                                                        ),
                                                        Arrays.asList()
                                                )
                                        ),
                                        Arrays.asList()
                                )
                        ),
                        Arrays.asList()
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        INTEGER score;",
                "        score = score / 10;",
                "        if (score == 10) {",
                "            PRINT(\"A\");",
                "        }",
                "        if (score == 9) {",
                "            PRINT(\"A\");",
                "        }",
                "        if (score == 8) {",
                "            PRINT(\"B\");",
                "        }",
                "        if (score == 7) {",
                "            PRINT(\"C\");",
                "        }",
                "        if (score == 6) {",
                "            PRINT(\"D\");",
                "        }",
                "        if (score != 10) {",
                "            if (score != 9) {",
                "                if (score != 8) {",
                "                    if (score != 7) {",
                "                        if (score != 6) {",
                "                            PRINT(\"E\");",
                "                        }",
                "                    }",
                "                }",
                "            }",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testWhileSmall() {
        Ast ast = new Ast.Statement.While(
                new Ast.Expression.Binary("!=",
                        new Ast.Expression.Variable("i"),
                        new Ast.Expression.Literal(BigInteger.ZERO)
                ),
                Arrays.asList(
                        new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                new Ast.Expression.Literal("bean")
                        ))),
                        new Ast.Statement.Assignment("i", new Ast.Expression.Binary("-",
                                new Ast.Expression.Variable("i"),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ))
                )
        );
        String expected = String.join(System.lineSeparator(),
                "while (i != 0) {",
                "    PRINT(\"bean\");",
                "    i = i - 1;",
                "}"
        );
        test(ast, expected);
    }

    @Test
    void testWhileBig() {
        Ast ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("i", "INTEGER", Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(2)))),
                new Ast.Statement.Declaration("n", "INTEGER", Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(5)))),
                new Ast.Statement.Declaration("zero", "INTEGER", Optional.empty()),
                new Ast.Statement.While(
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Variable("n"),
                                new Ast.Expression.Literal(BigInteger.ZERO)
                        ),
                        Arrays.asList(
                                new Ast.Statement.Assignment("zero", new Ast.Expression.Binary("*",
                                        new Ast.Expression.Binary("-",
                                                new Ast.Expression.Variable("n"),
                                                new Ast.Expression.Variable("i")
                                        ),
                                        new Ast.Expression.Group(new Ast.Expression.Binary("/",
                                                new Ast.Expression.Variable("n"),
                                                new Ast.Expression.Variable("i")
                                        ))
                                )),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(),
                                        Arrays.asList()
                                ),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("even")
                                                )))
                                        ),
                                        Arrays.asList()
                                ),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("odd")
                                                )))
                                        )
                                ),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("even")
                                                )))
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("odd")
                                                )))
                                        )
                                ),
                                new Ast.Statement.Assignment("n", new Ast.Expression.Binary("-",
                                        new Ast.Expression.Variable("n"),
                                        new Ast.Expression.Literal(BigInteger.ONE)
                                ))
                        )
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        INTEGER i = 2;",
                "        INTEGER n = 5;",
                "        INTEGER zero;",
                "        while (n != 0) {",
                "            zero = n - i * (n / i);",
                "            if (zero != 1) {}",
                "            if (zero != 1) {",
                "                PRINT(\"even\");",
                "            }",
                "            if (zero != 1) {} else {",
                "                PRINT(\"odd\");",
                "            }",
                "            if (zero != 1) {",
                "                PRINT(\"even\");",
                "            } else {",
                "                PRINT(\"odd\");",
                "            }",
                "            n = n - 1;",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testLiteralSmall() {
        Ast.Expression ast = new Ast.Expression.Literal("string");
        String expected = "\"string\"";
        test(ast, expected);
    }

    @Test
    void testLiteralBig() {
        Ast.Source ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("test", "BOOLEAN", Optional.of(new Ast.Expression.Literal(true))),
                new Ast.Statement.If(
                        new Ast.Expression.Variable("test"),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("success!")
                                )))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("failure?")
                                )))
                        )
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        BOOLEAN test = true;",
                "        if (test) {",
                "            PRINT(\"success!\");",
                "        } else {",
                "            PRINT(\"failure?\");",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testGroup() {
        Ast.Expression ast = new Ast.Expression.Group(new Ast.Expression.Binary("+",
                new Ast.Expression.Binary("*",
                        new Ast.Expression.Literal(BigInteger.valueOf(2)),
                        new Ast.Expression.Group(new Ast.Expression.Binary("-",
                                new Ast.Expression.Literal(BigInteger.valueOf(10)),
                                new Ast.Expression.Literal(BigInteger.valueOf(3))
                        ))),
                new Ast.Expression.Group(new Ast.Expression.Binary("/",
                        new Ast.Expression.Literal(BigInteger.valueOf(5)),
                        new Ast.Expression.Literal(BigInteger.valueOf(2))
                ))
        ));
        String expected = "(2 * (10 - 3) + (5 / 2))";
        test(ast, expected);
    }

    private static void test(Ast ast, String expected) {
        StringWriter writer = new StringWriter();
        new Generator(new PrintWriter(writer)).visit(ast);
        Assertions.assertEquals(expected, writer.toString());
    }


}
