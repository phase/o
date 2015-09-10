package xyz.jadonfowler.o;

class OC {
    public native Character parse(Character c);

    static { System.loadLibrary("o2-j"); }
}
