package org.developx.sqlparser.template;

import net.sf.jsqlparser.statement.select.Select;

public class SelectProcessor extends SqlTemplate {

    private final Select select;

    public SelectProcessor(Select select) {
        super();
        this.select = select;
    }

    @Override
    protected void execute() {
        resolveSelectBody(select.getSelectBody());
    }




}
