package cruft.wtf.gimlet.jdbc;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParseResultTest {

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
    public void parseMixed() {
        ParseResult prs = ParseResult.parse("select * from herro where id = :id and num = :num[NUMBER]");
        assertEquals("select * from herro where id = ? and num = ?", prs.getSql());
        assertEquals(2, prs.getParameters().size());

        assertEquals("id", prs.getParameters().get(0).getName());
        assertEquals(ParseResult.Type.NONE, prs.getParameters().get(0).getDataType());

        assertEquals("num", prs.getParameters().get(1).getName());
        assertEquals(ParseResult.Type.NUMBER, prs.getParameters().get(1).getDataType());
    }


}
