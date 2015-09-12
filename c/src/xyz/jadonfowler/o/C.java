package xyz.jadonfowler.o;

class C {
    public native String parse(Character c);

    static { System.loadLibrary("o2-j"); }
}
