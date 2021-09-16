package cruft.wtf.gimlet.jdbc;

import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ParseResultTest {

    @Test
    public void parseComplete() throws IOException {
        String actual = new String(ParseResultTest.class.getResourceAsStream("/sql/complete-actual.sql").readAllBytes());
        String expected = new String(ParseResultTest.class.getResourceAsStream("/sql/complete-expected.sql").readAllBytes());

        ParseResult prs = ParseResult.parse(actual);

        assertEquals(expected, prs.getSql());

        assertEquals(6, prs.getParameters().size());
        assertEquals(5, prs.getUniqueParameters().size());

        assertEquals(ParseResult.Type.NONE, prs.getParameters().get(0).getDataType());
        assertEquals("id", prs.getParameters().get(0).getName());

        assertEquals(ParseResult.Type.STRING, prs.getParameters().get(1).getDataType());
        assertEquals("name", prs.getParameters().get(1).getName());

        assertEquals(ParseResult.Type.DATE, prs.getParameters().get(2).getDataType());
        assertEquals("other", prs.getParameters().get(2).getName());

        assertEquals(ParseResult.Type.DATETIME, prs.getParameters().get(3).getDataType());
        assertEquals("bla", prs.getParameters().get(3).getName());

        assertEquals(ParseResult.Type.NUMBER, prs.getParameters().get(4).getDataType());
        assertEquals("num", prs.getParameters().get(4).getName());

        assertEquals(ParseResult.Type.NONE, prs.getParameters().get(5).getDataType());
        assertEquals("id", prs.getParameters().get(5).getName());
    }

    @Test
    public void parseWithDataTypes() {
        ParseResult prs = ParseResult.parse("select * from yoda where id = :id[DATE] and z = :asdas[DATETIME]");
        assertEquals("select * from yoda where id = ? and z = ?", prs.getSql());
        assertEquals(2, prs.getParameters().size());

        assertEquals(ParseResult.Type.DATE, prs.getParameters().get(0).getDataType());
        assertEquals("id", prs.getParameters().get(0).getName());

        assertEquals(ParseResult.Type.DATETIME, prs.getParameters().get(1).getDataType());
        assertEquals("asdas", prs.getParameters().get(1).getName());
    }

    @Test
    public void parseNoDataTypes() {
        ParseResult prs = ParseResult.parse("select * from herro where id = :id and num = :num");
        assertEquals("select * from herro where id = ? and num = ?", prs.getSql());
        assertEquals(2, prs.getParameters().size());

        assertEquals("id", prs.getParameters().get(0).getName());
        assertEquals(ParseResult.Type.NONE, prs.getParameters().get(0).getDataType());

        assertEquals("num", prs.getParameters().get(1).getName());
        assertEquals(ParseResult.Type.NONE, prs.getParameters().get(1).getDataType());
    }

    @Test
    public void parsePostgreSQLCasting() {
        ParseResult prs = ParseResult.parse("SELECT '100'::INTEGER, '01-OCT-2015'::DATE;");
        assertEquals("SELECT '100'::INTEGER, '01-OCT-2015'::DATE;", prs.getSql());
        assertEquals(0, prs.getParameters().size());
    }


    @Test
    public void parseMixed() {
        ParseResult prs = ParseResult.parse("select * from herro where id = :id and num = :num[NUMBER]");
        assertEquals("select * from herro where id = ? and num = ?", prs.getSql());
        assertEquals(2, prs.getParameters().size());

        assertEquals("id", prs.getParameters().get(0).getName());
        assertEquals(ParseResult.Type.NONE, prs.getParameters().get(0).getDataType());

        assertEquals("num", prs.getParameters().get(1).getName());
        assertEquals(ParseResult.Type.NUMBER, prs.getParameters().get(1).getDataType());
    }

    @Test
    public void uniqueParameters() {
        ParseResult prs = ParseResult.parse("select * from herro where id = :id[DATE] and stuff = :id[DATE] and a = :clazz[NUMBER]");
        Set<ParseResult.Param> uniques = prs.getUniqueParameters();
        System.out.println(uniques);
    }


}
