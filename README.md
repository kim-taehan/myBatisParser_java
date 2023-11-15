# MyBatis SQL 분석 도구 
- 특정 경로에 존재하는 xml로 구성된 MyBatis mapper 파일들을 분석하여 SQL에서 사용되는 테이블과 컬럼들을 추출한다.


## 사용법 
- main 클래스 수행시에 args 1번째 인자로 MyBatis mapper들이 존재하는 위치를 지정한다.
- jar, java 실행경로에 delete.dat 파일을 두고 이를 통해 삭제된 컬럼, 삭제된 테이블 정보를 입력한다
```text
TABLE|delete_table1||
COLUMN|delete_table1|delete_col1|
COLUMN|delete_table2|delete_col2|
COLUMN|delete_table2|delete_col3|
```
- java -jar myBatisParser.jar D:\\000_source\\study\\myBatisParser\\samplesql

## 개발된 내용
- select, insert, update, delete Sql에 대한 분석을 수행한다.
- select 에서 사용된는 from, where, group by, having, order by의 컬럼을 전체 점검
- inline 조인의 경우 외부에 table alias 까지 같이 확인할 수 있다. 
- union, case, in, function 등의 문법에서 내부에 컬럼을 확인한다.
- mybatis 문법인 if/where 등은 쿼리는 검사하지만, "selectKey", "import" 등의 문법의 내용은 확인하지 않는다.
- insert, update, delete에서 테이블에 alias을 열고, 컬럼에 alias 없이 사용해도 가능 (select 문은 불가)

### 추가 확인이 필요한 내용 (중요)
- alias로 예약어를 사용하는 경우 처리 불가 (예제: at)
- select 절에서 테이블에는 alias가 있는데 column에 alias가 없는 경우 대상을 찾지 못한다. 
- inline 쿼리에서 아스타로 조회된 컬럼은 외부에서 제거된 컬럼 사용시 확인할 수 없다.
- select, insert, update, delete Sql에 대해서만 분석이 가능하다 

3. 
## 각 패키지 설명 

### org.devlopx.filevisitor
> java 에서 제공하는 filevisitor를 통해 사용자가 지정한 경로의(하위 파일 포함) xml 파일을 확인한다.
```java
@Override
public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

    xmlParser.parse(file)
            .ifPresent(element -> extracted(file, element));
    return FileVisitResult.CONTINUE;
}
```

### org.devlopx.xmlparser
#### 1. xml 파일을 읽어서 Dom4j 라이브러리를 통해서 파싱 작업을 수행한다
```java
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
```

####  2. 분석 된 데이터를 텍스트로 변환하는 작업
- 주석 제거, 아규먼트 자동변환 작업도 수행됨
```java
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
```


### org.devlopx.deletetable
- delete.dat 파일을 읽어서 삭제된 테이블과 컬럼정보를 미리 저장해 둔다. 

### org.devlopx.sqlparser
- `org.devlopx.xmlparser`에서 제공해주는 sql text 데이터를 통해 SQL 파서작업을 수행하고
- `org.devlopx.deletetable`에서 제공해는데 제거된 테이블과 컬럼을 맵핑하여 최종 결과를 반환한다

```java
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
```

### org.devlopx.csv
- `org.devlopx.sqlparser`에서 제공한 데이터를 CSV 파일로 변환하여 저장하는 기능