import java.util.*;
import java.io.*;

public class Fifth {
    public static final String VERSION = "1";

    public static Fifth instance;

    public static void main(String[] a) throws IOException{
        if(a.length == 1){
            File f = new File(a[0]);
            instance = new Fifth(f);
        } else {
            instance = new Fifth();
        }
    }

    File file;
    Stack stack;

    public Fifth(File f) throws IOException{
        this.stack = new Stack(64 * 1024);
        this.file = f;
        FileReader fr = new FileReader(this.file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while((line = br.readLine()) != null){
            for(char c : line.trim().toCharArray()){
                parse(c);
            }
        }
        br.close();
        fr.close();
    }

    public Fifth(){
        while(true){
            System.out.print("fifth"+VERSION"+ >> ");
            Scanner sn = new Scanner(System.in);
            for(char c : sn.nextLine()){
                parse(c);
            }
        }
    }

    public void parse(char c){
        
    }

}

class Stack {
    Object[] stack;
    int i = -1;

    public Stack(int size){
        stack = new Object[size];
    }

    public void push(Object x){
        if(i >= stack.length -1) throw new ArrayIndexOutOfBoundsException("Can't push to full stack: " + x.toString());
        stack[++i] = x;
    }

    public Object pop(){
        if (i <= -1) throw new ArrayIndexOutOfBoundsException("Can't pop from empty stack! ");
        Object x = stack[i];
        stack[i] = 0;
        i--;
        return x;
    }

    public void reverse(){
        for (int left = 0, right = i; left < right; left++, right--) {
            double x = stack[left];
            stack[left] = stack[right];
            stack[right] = x;
        }
    }

    public int length(){
        return i+1;
    }

}

interface Operator {
    public void run();
}
