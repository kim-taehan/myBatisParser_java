package org.developx.sqlparser.template;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.insert.Insert;

public class InsertProcessor extends NoSelectSqlTemplate {

    private final Insert insert;

    public InsertProcessor(Insert insert) {
        super();
        this.insert = insert;
    }

    @Override
    protected void execute() {

        resolveTable(insert.getTable());

        resolveColumns(insert.getTable(), insert.getColumns());

        itemList();

    }


    private void itemList() {
        ExpressionList itemsList = (ExpressionList) insert.getItemsList();
        if (itemsList.getExpressions() != null) {
            for (Expression expression : itemsList.getExpressions()) {
                resolveExpression(expression);
            }
        }
    }
}
