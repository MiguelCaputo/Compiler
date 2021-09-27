package plc.compiler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Ast> {

    public Scope scope;

    public Analyzer(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Ast visit(Ast.Source ast) throws AnalysisException {
        if (ast.getStatements().size() == 0)
            throw new AnalysisException("Empty source");
        List<Ast.Statement> statements = new ArrayList<>();
        for(Ast.Statement asts: ast.getStatements()) {
            statements.add(visit(asts));
        }
        return new Ast.Source(statements);
    }

    /**
     * Statically validates that visiting a statement returns a statement.
     */
    private Ast.Statement visit(Ast.Statement ast) throws AnalysisException {
        return (Ast.Statement) visit((Ast) ast);
    }

    @Override
    public Ast.Statement.Expression visit(Ast.Statement.Expression ast) throws AnalysisException {
        if (!(visit(ast.getExpression()) instanceof Ast.Expression.Function))
            throw new AnalysisException("Not a valid Expression Statement");
        return new Ast.Statement.Expression(visit(ast.getExpression()));
    }

    @Override
    public Ast.Statement.Declaration visit(Ast.Statement.Declaration ast) throws AnalysisException {
        String name = ast.getName();
        String type = ast.getType();
        scope.define(name, Stdlib.getType(type));
        if (Stdlib.getType(type) == Stdlib.Type.VOID)
                throw new AnalysisException("Type of Declaration is VOID");
        else if (ast.getValue().isPresent())
        {
            checkAssignable(visit(ast.getValue().get()).getType(), Stdlib.getType(type));
            return new Ast.Statement.Declaration(name, Stdlib.getType(type).getJvmName(), Optional.ofNullable(visit(ast.getValue().get())));
        }
        return new Ast.Statement.Declaration(name, Stdlib.getType(type).getJvmName(), ast.getValue());
    }

    @Override
    public Ast.Statement.Assignment visit(Ast.Statement.Assignment ast) throws AnalysisException {
        checkAssignable(visit(ast.getExpression()).getType() , scope.lookup(ast.getName()));
        return new Ast.Statement.Assignment(ast.getName(), visit(ast.getExpression()));
    }

    @Override
    public Ast.Statement.If visit(Ast.Statement.If ast) throws AnalysisException {
        if (visit(ast.getCondition()).getType() != Stdlib.Type.BOOLEAN)
            throw new AnalysisException("Condition is not a boolean for if");
        else if (ast.getThenStatements().size() == 0)
            throw new AnalysisException("THEN statement list is empty");
        List<Ast.Statement> thenList = new ArrayList<>();
        List<Ast.Statement> elseList = new ArrayList<>();
        scope = new Scope(scope);
        for (Ast.Statement then: ast.getThenStatements())
        {
            thenList.add(visit(then));
        }
        scope = scope.getParent();
        scope = new Scope(scope);
        for (Ast.Statement elseS: ast.getElseStatements())
        {
            elseList.add(visit(elseS));
        }
        scope = scope.getParent();
        return new Ast.Statement.If(visit(ast.getCondition()),thenList, elseList);
    }

    @Override
    public Ast.Statement.While visit(Ast.Statement.While ast) throws AnalysisException {
        if (visit(ast.getCondition()).getType() != Stdlib.Type.BOOLEAN)
            throw new AnalysisException("Condition is not a boolean for While");
        List<Ast.Statement> list = new ArrayList<>();
        scope = new Scope(scope);
        for (Ast.Statement asts: ast.getStatements())
        {
            list.add(visit(asts));
        }
        scope = scope.getParent();
        return new Ast.Statement.While(visit(ast.getCondition()),list);
    }

    /**
     * Statically validates that visiting an expression returns an expression.
     */
    private Ast.Expression visit(Ast.Expression ast) throws AnalysisException {
        return (Ast.Expression) visit((Ast) ast);
    }

    @Override
    public Ast.Expression.Literal visit(Ast.Expression.Literal ast) throws AnalysisException {
         if (ast.getValue() instanceof Boolean)
             return new Ast.Expression.Literal(Stdlib.Type.BOOLEAN, ast.getValue());
         else if (ast.getValue() instanceof BigInteger) {
             int newInteger;
             try {
                 newInteger = ((BigInteger) ast.getValue()).intValueExact();
             }
             catch (ArithmeticException e)
             {
                 throw new AnalysisException("Integer is out of range");
             }
             return new Ast.Expression.Literal(Stdlib.Type.INTEGER, newInteger);
         }
         else if (ast.getValue() instanceof BigDecimal)
         {
             double newDecimal = ((BigDecimal) ast.getValue()).doubleValue();
             if (newDecimal == Double.POSITIVE_INFINITY || newDecimal == Double.NEGATIVE_INFINITY)
                 throw new AnalysisException("Decimal is out of range");
             return new Ast.Expression.Literal(Stdlib.Type.DECIMAL, newDecimal);
         }
         else if (ast.getValue() instanceof String)
         {
             String newString = (String) ast.getValue();
             if (!newString.matches("^[A-Za-z0-9_!?.+\\-\\/* ]*$"))
                 throw new AnalysisException("String contains unsupported characters");
             return new Ast.Expression.Literal(Stdlib.Type.STRING, newString);
         }
        throw new AnalysisException("Not a valid literal");
    }

    @Override
    public Ast.Expression.Group visit(Ast.Expression.Group ast) throws AnalysisException {
        Ast.Expression expression = visit(ast.getExpression()); //visit first, then getType
        return new Ast.Expression.Group(expression.getType(), expression);
    }

    @Override
    public Ast.Expression.Binary visit(Ast.Expression.Binary ast) throws AnalysisException {
        String operator = ast.getOperator();
        Ast.Expression left = visit(ast.getLeft());
        Ast.Expression right = visit(ast.getRight());
        if (operator.compareTo("==") == 0 || operator.compareTo("!=") == 0)
        {
            if (right.getType() != Stdlib.Type.VOID && left.getType() != Stdlib.Type.VOID)
                return new Ast.Expression.Binary(Stdlib.Type.BOOLEAN, operator,left,right);
            else
                throw new AnalysisException("Not a valid equality binary expression");
        }
        else if (operator.compareTo("+") == 0)
        {
            if (right.getType() != Stdlib.Type.VOID && left.getType() != Stdlib.Type.VOID)
            {
                if (left.getType() == Stdlib.Type.STRING || right.getType() == Stdlib.Type.STRING)
                    return new Ast.Expression.Binary(Stdlib.Type.STRING, operator, left, right);
                else if (left.getType() == Stdlib.Type.INTEGER && right.getType() == Stdlib.Type.INTEGER)
                    return new Ast.Expression.Binary(Stdlib.Type.INTEGER, operator, left, right);
                else if (left.getType() == Stdlib.Type.DECIMAL || right.getType() == Stdlib.Type.DECIMAL)
                    return new Ast.Expression.Binary(Stdlib.Type.DECIMAL, operator, left, right);
                else
                    throw new AnalysisException("Not a valid binary addition");
            }
            else
                throw new AnalysisException("Not a valid binary addition");
        }
        else if (operator.compareTo("-") == 0 || operator.compareTo("*") == 0 || operator.compareTo("/") == 0 )
        {
            if ((right.getType() == Stdlib.Type.INTEGER || right.getType() == Stdlib.Type.DECIMAL)
                    && (left.getType() == Stdlib.Type.INTEGER || left.getType() == Stdlib.Type.DECIMAL))
            {
                if (left.getType() == Stdlib.Type.INTEGER && right.getType() == Stdlib.Type.INTEGER)
                    return new Ast.Expression.Binary(Stdlib.Type.INTEGER, operator, left, right);
                else if (left.getType() == Stdlib.Type.DECIMAL || right.getType() == Stdlib.Type.DECIMAL)
                    return new Ast.Expression.Binary(Stdlib.Type.DECIMAL, operator, left, right);
                else
                    throw new AnalysisException("Not a valid binary (-*/)");
            }
            else
                throw new AnalysisException("Not a valid binary (-*/)");
        }
        throw new AnalysisException("Not a valid binary");
    }

    @Override
    public Ast.Expression.Variable visit(Ast.Expression.Variable ast) throws AnalysisException {
        return new Ast.Expression.Variable(scope.lookup(ast.getName()), ast.getName());
    }

    @Override
    public Ast.Expression.Function visit(Ast.Expression.Function ast) throws AnalysisException {
        List<Ast.Expression> list = new ArrayList<>();
        int size = ast.getArguments().size();
        if (Stdlib.getFunction(ast.getName(), size).getParameterTypes().size() != size)
            throw new AnalysisException("Incorrect number of parameters for function");
        for (int i = 0; i < size; i++)
        {
            checkAssignable(visit(ast.getArguments().get(i)).getType(), Stdlib.getFunction(ast.getName(), ast.getArguments().size()).getParameterTypes().get(i));
            list.add(visit(ast.getArguments().get(i)));
        }
        return new Ast.Expression.Function(Stdlib.getFunction(ast.getName(), size).getReturnType(), Stdlib.getFunction(ast.getName(), size).getJvmName(), list);
    }

    public static void checkAssignable(Stdlib.Type type, Stdlib.Type target) throws AnalysisException {
        if (Objects.equals(type,target)) {}
        else if (type == Stdlib.Type.INTEGER && target == Stdlib.Type.DECIMAL) {}
        else if (type != Stdlib.Type.VOID && target == Stdlib.Type.ANY) {}
        else throw new AnalysisException("Type: " + type + " not assignable to type: " + target);
    }

}
