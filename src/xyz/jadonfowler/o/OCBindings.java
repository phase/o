package xyz.jadonfowler.o;

class OCBindings {
    public native String parse(Character c);
    public native void cl();

    static { System.loadLibrary("o2-j"); }
}