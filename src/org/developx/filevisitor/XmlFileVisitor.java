package org.developx.filevisitor;

import net.sf.jsqlparser.JSQLParserException;
import org.developx.deletetable.DeleteTable;
import org.developx.sqlparser.SqlParser;
import org.developx.xmlparser.XmlParser;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XmlFileVisitor implements FileVisitor<Path> {

    private final XmlParser xmlParser = new XmlParser();
    private final SqlParser sqlParser = new SqlParser();

    private final List<ReportData> reportData;

    public XmlFileVisitor(List<ReportData> reportData) {
        this.reportData = reportData;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        xmlParser.parse(file)
                .ifPresent(element -> extracted(file, element));
        return FileVisitResult.CONTINUE;
    }

    private void extracted(Path file, Element mapperElement) {
        String namespace = getAttribute(mapperElement, "namespace");

        // 각 쿼리들의 순환하면서 sql 분석을 시작한다.
        for (Iterator i = mapperElement.elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            String sqlType = element.getName();


            // 쿼리 가져오기
            // 1. 주석제거
            // 2. 파라매터 #{colName} -> '' 로 변경
            String sqlText = xmlParser.parseSqlText(element);
            String id = getAttribute(element, "id");
            try {
                List<DeleteTable> deleteData = sqlParser.parser(sqlText);
                if (deleteData.size() > 0) {
                    reportData.add(createReport(file, namespace, sqlType, id, deleteData));
                }
            } catch (JSQLParserException e) {
                System.out.println("sql parser 오류발생: "+ file.getFileName() + " : " + namespace + " : " + sqlType + " - " + id);
            }
        }
    }

    private static ReportData createReport(Path file, String namespace, String sqlType, String id, List<DeleteTable> deleteData) {
        return new ReportData(
                file.getFileName().toString()
                , file.toFile().getAbsolutePath()
                , namespace
                , sqlType
                , id
                , deleteData
        );
    }

    private static String getAttribute(Element element, String key) {
        Attribute attribute = element.attribute(key);
        return attribute == null ? "" : attribute.getValue();
    }


    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
