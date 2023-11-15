package org.developx.sqlparser.template;

import net.sf.jsqlparser.statement.delete.Delete;

public class DeleteProcessor extends NoSelectSqlTemplate {

    private final Delete delete;

    public DeleteProcessor(Delete delete) {
        super();
        this.delete = delete;
    }

    @Override
    protected void execute() {

        resolveTable(delete.getTable());

        where();

    }

    private void where() {
        if (delete.getWhere() != null) {
            resolveExpression(delete.getWhere());
        }
    }



}
