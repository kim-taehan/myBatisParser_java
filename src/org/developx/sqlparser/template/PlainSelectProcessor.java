package org.developx.sqlparser.template;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.*;

import java.util.HashMap;
import java.util.List;

public class PlainSelectProcessor extends SqlTemplate {

    private final PlainSelect plainSelect;

    public PlainSelectProcessor(PlainSelect plainSelect) {
        super();
        this.plainSelect = plainSelect;
    }

    public PlainSelectProcessor(PlainSelect plainSelect, HashMap<String, String> tables) {
        super();
        this.plainSelect = plainSelect;
        this.tables = tables;
    }

    @Override
    protected void execute() {

        from();

        selectItems();

        where();

        groupBy();

        having();

        orderBy();



    }

    private void from() {
        // 메인 테이블
        resolveFromItem(plainSelect.getFromItem());

        // 조인되는 테이블
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                resolveFromItem(join.getRightItem());
                resolveExpression(join.getOnExpression());
            }
        }
    }

    private void selectItems() {
        if (plainSelect.getSelectItems() == null) return;


        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem item = (SelectExpressionItem) selectItem;
                resolveExpression(item.getExpression());
            } else if (selectItem instanceof AllTableColumns || selectItem instanceof AllColumns) {
                // 아스타의 경우 처리불가하다
            } else {
                logNeedChecking("PlainSelectProcessor", "selectItems", selectItem);
            }

        }
    }

    private void where() {
        if (plainSelect.getWhere() != null) {
            resolveExpression(plainSelect.getWhere());
        }
    }

    private void groupBy() {
        // group by
        if (plainSelect.getGroupBy() != null) {
            ExpressionList groupByExpressionList = plainSelect.getGroupBy().getGroupByExpressionList();
            for (Expression expression : groupByExpressionList.getExpressions()) {
                resolveExpression(expression);
            }
        }
    }

    private void having() {
        // group by
        if (plainSelect.getHaving() != null) {
            resolveExpression(plainSelect.getHaving());
        }
    }

    private void orderBy() {
        if (plainSelect.getOrderByElements() != null) {
            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
            for (OrderByElement orderByElement : orderByElements) {
                resolveExpression(orderByElement.getExpression());
            }
        }
    }

}
