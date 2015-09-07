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
        o.webIDE = true;
        O.instance = o;
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

    @Test
    public void testMath() {
        assertEquals("Addition", parse("12+o"), "3");
        assertEquals("Subtraction", parse("53-o"), "2");
        assertEquals("Multiplication", parse("34*5*o"), "60");
        assertEquals("Division", parse("63/o"), "2");
        assertEquals("Pow", parse("32^o"), "9");
    }

    @Test
    public void testArrays() {
        assertEquals("Array Folding: Adding", parse("[123]+o"), "6");
        assertEquals("Array Dispersement: Pow", parse("[3,]2^+o"), "14");
        assertEquals("Array Reopening", parse("[12] (4]*o"), "8");
    }

}
