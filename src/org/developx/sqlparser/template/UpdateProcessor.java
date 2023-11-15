package org.developx.sqlparser.template;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.update.Update;

public class UpdateProcessor extends NoSelectSqlTemplate {

    private final Update update;

    public UpdateProcessor(Update update) {
        super();
        this.update = update;
    }

    @Override
    protected void execute() {

        resolveTable(update.getTable());

        resolveColumns(update.getTable(), update.getColumns());

        expressions();

        where();

    }

    private void expressions() {
        if (update.getExpressions() != null) {
            for (Expression expression : update.getExpressions()) {
                resolveExpression(expression);
            }
        }
    }

    private void where() {
        if (update.getWhere() != null) {
            resolveExpression(update.getWhere());
        }
    }
}
