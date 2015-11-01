package xyz.jadonfowler.o;

class OCBindings {
    public native String parse(Character c);
    public native void cl();
    public native void setInputs(String[] inputs);
    public native void setInputPointer(Integer p);
    public native String getCurrentStackContents();
    
    static { System.loadLibrary("o2-j"); }
}