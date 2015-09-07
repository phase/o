package xyz.jadonfowler.o.test;

import static org.junit.Assert.assertEquals;

import xyz.jadonfowler.o.O;
import org.junit.Test;
import org.junit.Before;

public class ParserTest {

    private O o;

    @Before
    public void setUp() {
        o = new O();
        O.instance = o;
    }

    @Test
    public void testOParsing() {
        //assertEquals("", parse(""), "");
        assertEquals("12+o -> 3", parse("12+o"), "3");
        assertEquals("\"Hello World\"o -> Hello World", parse("\"Hello World\"o"), "Hello World");
        assertEquals("[123]+o -> 6", parse("[123]+o"), "6");
        assertEquals("[12] (4]*o -> 8", parse("[12] (4]*o"), "8");
        assertEquals("[3,]2^+o -> 14", parse("[3,]2^+o"), "14");
    }

    public String parse(String s) {
        String result = "";
        for (char c : s.toCharArray()) {
            try {
                result += o.parse(c);
            } catch(Exception e) {}
        }
        return result;
    }

}
