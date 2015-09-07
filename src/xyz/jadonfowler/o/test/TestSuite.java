package xyz.jadonfowler.o.test;

import xyz.jadonfowler.o.O;

public class TestSuite {

    private static O o;

    public static void main(String[] args) throws Exception {
        o = new O();
        O.instance = o;
        //checkParse("", "");
        checkParse("\"Hello World\"o", "Hello World");
        checkParse("12+o", "3");
        checkParse("[123]+o", "6");
        checkParse("[12] (4]*o", "8");
        checkParse("[3,]2^+o", "14");
    }

    public static void checkParse(String code, String expected) throws Exception {
        String result = "";
        for (char c : code.toCharArray())
            result += o.parse(c);
        if (!(result.equals(expected)))
            throw new Exception("'" + result + "' should be '" + expected + "' for '" + code + "'");
    }

}
