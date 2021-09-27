package plc.compiler;

import java.util.List;
import java.util.Optional;

/**
 * Contains classes for the Abstract Syntax Tree (AST), which stores a
 * structural representation of the program.
 */
public class Ast {

    public static final class Source extends Ast {

        private final List<Statement> statements;

        public Source(List<Statement> statements) {
            this.statements = statements;
        }

        public List<Statement> getStatements() {
            return statements;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Source && statements.equals(((Source) obj).statements);
        }

        @Override
        public String toString() {
            return "Source{" +
                    "statements=" + statements +
                    '}';
        }

    }

    public static class Statement extends Ast {

        public static final class Expression extends Statement {

            private final Ast.Expression expression;

            public Expression(Ast.Expression expression) {
                this.expression = expression;
            }

            public Ast.Expression getExpr() {
                return expression;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Statement.Expression && expression.equals(((Statement.Expression) obj).expression);
            }

            @Override
            public String toString() {
                return "Expression{" +
                        "expression=" + expression +
                        '}';
            }

        }

        public static final class Declaration extends Statement {

            private final String name;
            private final String type;
            private final Optional<Ast.Expression> value;

            public Declaration(String name, String type, Optional<Ast.Expression> value) {
                this.name = name;
                this.type = type;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public String getType() {
                return type;
            }

            public Optional<Ast.Expression> getValue() {
                return value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Declaration &&
                        name.equals(((Declaration) obj).name) &&
                        type.equals(((Declaration) obj).type) &&
                        value.equals(((Declaration) obj).value);
            }

            @Override
            public String toString() {
                return "Declaration{" +
                        "name='" + name + '\'' +
                        ", type='" + type + '\'' +
                        ", value=" + value +
                        '}';
            }

        }

        public static final class Assignment extends Statement {

            private final String name;
            private final Ast.Expression expression;

            public Assignment(String name, Ast.Expression expression) {
                this.name = name;
                this.expression = expression;
            }

            public String getName() {
                return name;
            }

            public Ast.Expression getExpression() {
                return expression;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Assignment &&
                        name.equals(((Assignment) obj).name) &&
                        expression.equals(((Assignment) obj).expression);
            }

            @Override
            public String toString() {
                return "Assignment{" +
                        "name='" + name + '\'' +
                        ", expression=" + expression +
                        '}';
            }

        }

        public static final class If extends Statement {

            private final Ast.Expression condition;
            private final List<Statement> thenStatements;
            private final List<Statement> elseStatements;

            public If(Ast.Expression condition, List<Statement> thenStatements, List<Statement> elseStatements) {
                this.condition = condition;
                this.thenStatements = thenStatements;
                this.elseStatements = elseStatements;
            }

            public Ast.Expression getCondition() {
                return condition;
            }

            public List<Statement> getThenStatements() {
                return thenStatements;
            }

            public List<Statement> getElseStatements() {
                return elseStatements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof If &&
                        condition.equals(((If) obj).condition) &&
                        thenStatements.equals(((If) obj).thenStatements) &&
                        elseStatements.equals(((If) obj).elseStatements);
            }

            @Override
            public String toString() {
                return "If{" +
                        "condition=" + condition +
                        ", thenStatements=" + thenStatements +
                        ", elseStatements=" + elseStatements +
                        '}';
            }

        }

        public static final class While extends Statement {

            private final Ast.Expression condition;
            private final List<Statement> statements;

            public While(Ast.Expression condition, List<Statement> statements) {
                this.condition = condition;
                this.statements = statements;
            }

            public Ast.Expression getCondition() {
                return condition;
            }

            public List<Statement> getStatements() {
                return statements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof While &&
                        condition.equals(((While) obj).condition) &&
                        statements.equals(((While) obj).statements);
            }

            @Override
            public String toString() {
                return "While{" +
                        "condition=" + condition +
                        ", statements=" + statements +
                        '}';
            }

        }

    }

    public static class Expression extends Ast {

        public static final class Literal extends Expression {

            private final Object value;

            public Literal(Object value) {
                this.value = value;
            }

            public Object getValue() {
                return value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Literal && value.equals(((Literal) obj).value);
            }

            @Override
            public String toString() {
                return "Literal{" +
                        "value=" + value +
                        '}';
            }

        }

        public static final class Group extends Expression {

            private final Expression expression;

            public Group(Expression expression) {
                this.expression = expression;
            }

            public Expression getExpr() {
                return expression;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Group && expression.equals(((Group) obj).expression);
            }

            @Override
            public String toString() {
                return "Group{" +
                        "expression=" + expression +
                        '}';
            }

        }

        public static final class Binary extends Expression {

            private final String operator;
            private final Expression left;
            private final Expression right;

            public Binary(String operator, Expression left, Expression right) {
                this.operator = operator;
                this.left = left;
                this.right = right;
            }

            public String getOperator() {
                return operator;
            }

            public Expression getLeft() {
                return left;
            }

            public Expression getRight() {
                return right;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Binary &&
                        operator.equals(((Binary) obj).operator) &&
                        left.equals(((Binary) obj).left) &&
                        right.equals(((Binary) obj).right);
            }

            @Override
            public String toString() {
                return "Binary{" +
                        "operator='" + operator + '\'' +
                        ", left=" + left +
                        ", right=" + right +
                        '}';
            }

        }

        public static final class Variable extends Expression {

            private final String name;

            public Variable(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Variable && name.equals(((Variable) obj).name);
            }

            @Override
            public String toString() {
                return "Variable{" +
                        "name='" + name + '\'' +
                        '}';
            }

        }

        public static final class Function extends Expression {

            private final String name;
            private final List<Expression> arguments;

            public Function(String name, List<Expression> arguments) {
                this.name = name;
                this.arguments = arguments;
            }

            public String getName() {
                return name;
            }

            public List<Expression> getArguments() {
                return arguments;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Function &&
                        name.equals(((Function) obj).name) &&
                        arguments.equals(((Function) obj).arguments);
            }

            @Override
            public String toString() {
                return "Function{" +
                        "name='" + name + '\'' +
                        ", arguments=" + arguments +
                        '}';
            }

        }

    }

}
