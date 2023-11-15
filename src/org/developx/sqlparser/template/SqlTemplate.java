package org.developx.sqlparser.template;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.developx.deletetable.DeleteFile;
import org.developx.deletetable.DeleteTable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SqlTemplate {

    // alias 를 key로 테이블 정보를 저장하고 있습니다.
    protected Map<String, String> tables = new HashMap();

    // sql에서 사용되는 table과 column 정보를 가지고 있습니다.
    protected Set<DeleteTable> sqlUsedData = new HashSet<>();

    private List<DeleteTable> deleteFile = DeleteFile.getInstance().getDeleteTable();

    public List<DeleteTable> run() {
        execute();
        return sqlUsedData.stream()
                .filter(deleteFile::contains)
                .collect(Collectors.toList());
    }

    protected abstract void execute();


    protected void resolveFromItem(FromItem fromItem) {

        if (fromItem == null) {
            return;
        }
        
        // 일반적인 테이블
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            String aliasName = table.getAlias() == null ? "" : table.getAlias().getName().toLowerCase();
            tables.put(aliasName, table.getName());
            sqlUsedData.add(DeleteTable.fromTable(table.getName()));
        }
        // inline 테이블
        else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            resolveSelectBody(subSelect.getSelectBody());
        }
        else {
            System.out.println("---> SqlTemplate(resolveFromItem) : " + fromItem.getClass());
        }
    }

    protected void resolveExpression(Expression expression) {

        if (expression == null) return;


        if (expression instanceof Column) {
            // 일반적인 컬럼
            Column column = (Column) expression;
            String aliasName = column.getTable() == null ? "" : column.getTable().getName().toLowerCase();
            // 주의: select 절에서 테이블에는 alias가 있는데 column에 alias가 없는 경우 대상을 찾지 못한다.
            if (tables.containsKey(aliasName)) {
                sqlUsedData.add(DeleteTable.fromColumn(tables.get(aliasName), column.getColumnName()));
            }
        }
        else if (expression instanceof Function) {
            // 함수 처리
            Function function = (Function) expression;
            if (function.getParameters() != null) {
                resolveExpressions(function.getParameters().getExpressions());
            }
        }
        else if (expression instanceof SubSelect) {
            // inline sql
            SubSelect subSelect = (SubSelect) expression;
            SelectBody selectBody = subSelect.getSelectBody();
            callPlainSelectProcess((PlainSelect) selectBody);
        }
        else if (expression instanceof InExpression) {
            // in 문법 처리
            InExpression inExpression = (InExpression) expression;
            resolveExpression(inExpression.getLeftExpression());
            resolveExpression(inExpression.getRightExpression());
            if (inExpression.getRightItemsList() != null) {
                resolveItemList(inExpression.getRightItemsList());
            }
        }
        else if (expression instanceof CastExpression) {
            CastExpression castExpression = (CastExpression) expression;
            resolveExpression(castExpression.getLeftExpression());
        }
        else if (expression instanceof CaseExpression) {
            // case, when, then 문법
            CaseExpression caseExpression = (CaseExpression) expression;
            resolveExpression(caseExpression.getSwitchExpression());
            resolveExpression(caseExpression.getElseExpression());
            List<WhenClause> whenClauses = caseExpression.getWhenClauses();
            for (WhenClause whenClause : whenClauses) {
                resolveExpression(whenClause.getWhenExpression());
                resolveExpression(whenClause.getThenExpression());
            }
        }
        else if (expression instanceof Parenthesis) {
            // 괄호 문법 (1=1)
            Parenthesis parenthesis = (Parenthesis) expression;
            resolveExpression(parenthesis.getExpression());
        }
        else if (expression instanceof StringValue || expression instanceof LongValue) {
            // StringValue 는 별도로 작업할 내용은 없음
        }
        else if (expression instanceof IsNullExpression) {
            // is null, is not null
            IsNullExpression isNullExpression = (IsNullExpression) expression;
            resolveExpression(isNullExpression.getLeftExpression());
        }
        else if (expression instanceof IsBooleanExpression) {
            // is boolean
            IsBooleanExpression isBooleanExpression = (IsBooleanExpression) expression;
            resolveExpression(isBooleanExpression.getLeftExpression());
        }
        else if (expression instanceof SignedExpression) {
            // 기호
            SignedExpression signedExpression = (SignedExpression) expression;
            resolveExpression(signedExpression.getExpression());
        }
        else if (expression instanceof BinaryExpression) {
            // OrExpression, AndExpression, GreaterThan 이하 등을 처리할 수 있네..
            // NotEqualsTo, EqualsTo
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            resolveExpression(binaryExpression.getLeftExpression());
            resolveExpression(binaryExpression.getRightExpression());
        }
        else if (expression instanceof AnalyticExpression) {
            // 테스트 필요 (쿼리가 없음)
            AnalyticExpression analyticExpression = (AnalyticExpression) expression;
            resolveExpression(analyticExpression.getExpression());
            resolveExpression(analyticExpression.getDefaultValue());
            if (analyticExpression.getPartitionExpressionList() != null) {
                resolveExpressions(analyticExpression.getPartitionExpressionList().getExpressions());
            }
            if (analyticExpression.getOrderByElements() != null) {
                List<OrderByElement> orderByElements = analyticExpression.getOrderByElements();
                for (OrderByElement orderByElement : orderByElements) {
                    resolveExpression(orderByElement.getExpression());
                }
            }
        } else {
            logNeedChecking("SqlTemplate", "resolveExpression", expression);
        }
    }

    private void resolveExpressions(List<Expression> expressions) {
        if (expressions != null) {
            for (Expression innerEx : expressions) {
                resolveExpression(innerEx);
            }
        }
    }

    // ItemList : in (여기 들어오는 데이터)
    private void resolveItemList(ItemsList itemsList) {
        if (itemsList instanceof SubSelect) {
            resolveExpression((SubSelect) itemsList);
        } else if (itemsList instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) itemsList;
            resolveExpressions(expressionList.getExpressions());
        } else {
            logNeedChecking("SqlTemplate", "resolveItemList", itemsList);
        }
    }



    protected void resolveSelectBody(SelectBody selectBody) {
        // 일반적인 Select 문 : PlainSelect
        if (selectBody instanceof PlainSelect) {
            callPlainSelectProcess((PlainSelect) selectBody);
        }
        // Union select 문 : SetOperationList
        else if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            for (SelectBody innerSelectBody : setOperationList.getSelects()) {
                callPlainSelectProcess((PlainSelect) innerSelectBody);
            }
        }
        else {
            System.out.println("---> SqlTemplate(resolveSelectBody) : " + selectBody.getClass());
        }
    }
    
    // inline select 문 처리
    protected void callPlainSelectProcess(PlainSelect selectBody) {

        HashMap<String, String> entryMap = new HashMap<>();
        for (Map.Entry<String, String> entry : tables.entrySet()) {
            entryMap.put(entry.getKey(), entry.getValue());
        }

        List<DeleteTable> ret = new PlainSelectProcessor(selectBody, entryMap).run();
        sqlUsedData.addAll(ret);
    }

    protected static <T> void logNeedChecking(String className, String methodName, T t) {
        if (t != null) {
            System.out.println(String.format("[확인필요] %s(%s) : %s", className, methodName, t.getClass()));
        }
    }
}
