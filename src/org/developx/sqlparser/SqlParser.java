package org.developx.sqlparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.developx.deletetable.DeleteTable;
import org.developx.sqlparser.template.DeleteProcessor;
import org.developx.sqlparser.template.InsertProcessor;
import org.developx.sqlparser.template.SelectProcessor;
import org.developx.sqlparser.template.UpdateProcessor;

import java.util.ArrayList;
import java.util.List;

public class SqlParser {

    private final List ENPTY_LIST = new ArrayList();

    public List<DeleteTable> parser(String sqlText) throws JSQLParserException {

        Statement statement = CCJSqlParserUtil.parse(sqlText);
        if (statement instanceof Select) {
            return new SelectProcessor((Select) statement).run();
        }
        if (statement instanceof Insert) {
            return new InsertProcessor((Insert) statement).run();
        }
        if (statement instanceof Update) {
            return new UpdateProcessor((Update) statement).run();
        }
        if (statement instanceof Delete) {
            return new DeleteProcessor((Delete) statement).run();
        }
        else {
            System.out.println("---> SqlParser(parser) : " + statement.getClass());
        }
        return ENPTY_LIST;
    }
}
