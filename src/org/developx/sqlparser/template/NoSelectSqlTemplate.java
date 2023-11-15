package org.developx.sqlparser.template;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.developx.deletetable.DeleteTable;

import java.util.List;

public abstract class NoSelectSqlTemplate extends SqlTemplate {

    /**
     * insert, update, delete 의 경우 메인테이블 하나로 고정
     * alias + "" 형태로 두개를 저장합니다.
     * @param table
     */
    protected void resolveTable(Table table) {
        // 메인 테이블만 존재함
        String aliasName = table.getAlias() == null ? "" : table.getAlias().getName().toLowerCase();
        tables.put(aliasName, table.getName());
        tables.put("", table.getName());
        sqlUsedData.add(DeleteTable.fromTable(table.getName()));
    }

    protected void resolveColumns(Table table, List<Column> columns) {
        String tableName = table.getName();

        if(columns != null){
            for (Column column : columns) {
                sqlUsedData.add(DeleteTable.fromColumn(tableName, column.getColumnName()));
            }
        }

    }
}
