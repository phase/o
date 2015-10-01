package xyz.jadonfowler.o;

class C {
    public native String parse(Character c);
    public native void cl();

    static { System.loadLibrary("o2-j"); }
}
