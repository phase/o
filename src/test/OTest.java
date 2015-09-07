package xyz.jadonfowler.o;

import org.junit.Assert;
import org.junit.Test;

public class OTest {
    private String parse(String s) {
        O parser = new O();
        StringBuilder result = new StringBuilder("");
        for (char c : s.toCharArray()) {
            try {
                result.append(parser.parse(c));
            } catch (Exception name) {
            }
        }
        return result.toString();
    }

    @Test
    public void basic() {
        Assert.assertEquals(2, 2);
    }

    @Test
    public void basicParse() {
        String res = this.parse("12+p");
        Assert.assertEquals("3", res);
    }
}
