package plc.compiler;

import java.io.PrintWriter;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        print("public final class Main {");
        newline(0);
        newline(++indent);
        print("public static void main(String[] args) {");
        if (ast.getStatements().size() != 0)
        {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++) {
                visit(ast.getStatements().get(i));
                if (i != ast.getStatements().size() - 1)
                    newline(indent);
            }
            newline(1);
        }
        print("}");
        newline(0);
        newline(0);
        print("}");
        newline(0);
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        print(ast.getType(), " ", ast.getName());
        if (ast.getValue().isPresent()) {
            print(" = ", ast.getValue().get());
        }
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        print(ast.getName(), " = ");
        visit(ast.getExpression());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        print("if (");
        visit(ast.getCondition());
        print(") {");
        if (ast.getThenStatements().size() != 0)
        {
            newline(++indent);
            for (int i = 0; i < ast.getThenStatements().size(); i++) {
                visit(ast.getThenStatements().get(i));
                if (i != ast.getThenStatements().size() - 1)
                    newline(indent);
            }
            newline(--indent);
        }
        print("}");
        if (ast.getElseStatements().size() != 0)
        {
            print(" else {");
            newline(++indent);
            for (int i = 0; i < ast.getElseStatements().size(); i++) {
                visit(ast.getElseStatements().get(i));
                if (i != ast.getElseStatements().size() - 1)
                    newline(indent);
            }
            newline(--indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        print("while (");
        visit(ast.getCondition());
        print(") {");
        if (ast.getStatements().size() != 0)
        {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++) {
                visit(ast.getStatements().get(i));
                if (i != ast.getStatements().size() - 1)
                    newline(indent);
            }
            newline(--indent);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if (ast.getValue() instanceof String)
            print("\"", ast.getValue(), "\"");
        else print(ast.getValue());
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        print("(");
        visit(ast.getExpression());
        print(")");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        visit(ast.getLeft());
        print(" ", ast.getOperator(), " ");
        visit(ast.getRight());
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Variable ast) {
        print(ast.getName());
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        print(ast.getName(),"(");
        for (int i = 0; i < ast.getArguments().size(); i++)
        {
            visit(ast.getArguments().get(i));
            if (i != ast.getArguments().size() - 1)
                print(", ");
        }
        print(")");
        return null;
    }

}
