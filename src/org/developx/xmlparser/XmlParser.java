package org.developx.xmlparser;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * mybatis xml 파일을 읽어서 Document 로 돌려줍니다.
 */
public class XmlParser {

    private static final String XML_PARSER_URL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    private static final String XML_EXT = ".xml";
    private static final String ENCODING = "UTF-8";
    private static final String MAPPER = "mapper";
    private static final String REMOVE_COMMENT_REGEX = "/\\*(?:.|[\\n\\r])*?\\*/|(--.*)";
    private static final String UPDATE_PARAM_REGEX = "#\\{(?:.|[\\n\\r])*?\\}";


    private final SAXReader reader;

    public XmlParser() {
        // private 하여 다른 클래스에 생성하지 못하게 한다.
        // 확실하게 표현 해주는 것도 좋은 방식이다.
        this.reader = new SAXReader(false);
        reader.setEncoding(ENCODING);
        try {
            reader.setFeature(XML_PARSER_URL, false);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<Element> parse(Path path) {
        if(this.isXmlFile(path)){
            Document document = this.parseDocument(path);
            Element rootElement = document.getRootElement();
            if (rootElement.getName().equals(MAPPER)) {
                return Optional.of(rootElement);
            }
        }
        return Optional.empty();
    }

    private boolean isXmlFile(Path file) {
        return file.getFileName().toString().endsWith(XML_EXT);
    }

    // xml 파일을 document 로 파싱하기
    private Document parseDocument(Path path) {
        try {
            return reader.read(path.toFile());
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * mybatis에 노드에 입력된 SQL 전체를 가져온다 (if, where 등에 포함된 쿼리도 같이 조회)
     * 1. 주석제거
     * 2. 파라매터 #{colName} -> '' 로 변경
     *
     * @param element : XML 노드에 포함된 SQL 전체
     * @return
     */
    private static Set<String> NO_TARGET_ATTR = Set.of("selectKey");
    public String parseSqlText(Element element) {
        // 단순 getText() 의 경우 if 문등의 내부 데이터를 가져오지 못함
        StringBuilder sb = new StringBuilder();
        for (Iterator i = element.nodeIterator(); i.hasNext(); ) {
            Node node = (Node) i.next();
            // selectKey 인 내용은 사용하지 않는다.
            if (node.getName() == null || !NO_TARGET_ATTR.contains(node.getName())) {
                sb.append(node.getText());
            }
        }
        return sb.toString()
                .replaceAll(REMOVE_COMMENT_REGEX, "")  // 주석제거
                .replaceAll(UPDATE_PARAM_REGEX, "''"); // #{columnName} -> '' 로 치환
    }

}
