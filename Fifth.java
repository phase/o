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
                try{
                    parse(c);
                }catch(Exception e){e.printStackTrace();}
            }
        }
        br.close();
        fr.close();
    }

    public Fifth(){
        Scanner sn = new Scanner(System.in);
        while(true){
            System.out.print("fifth" + VERSION + " >> ");
            for(char c : sn.nextLine().toCharArray()){
                try{
                    parse(c);
                }catch(Exception e){e.printStackTrace();}
            }
        }
    }

    //Variables for parsing
    ArrayList<CodeBlock> codeBlocks = new ArrayList<CodeBlock>();
    boolean codeBlock = false;
    boolean blockCreate = false;
    StringBuilder cb = null;
    StringBuilder sb = null;
    boolean string = false;

    public void parse(char c) throws NumberFormatException{
        for(CodeBlock cb : codeBlocks){
            if(cb.name == c){
                cb.run();
                return;
            }
        }

        
        if(c == '{'){
            cb = new StringBuilder();
            codeBlock = true;
        }
        else if(c == '}'){
            cb = null;
            codeBlock = false;
            blockCreate = true;
        }
        else if(blockCreate){
            codeBlocks.add(new CodeBlock(c, cb.toString()));
            blockCreate = false;
        }
        else if(codeBlock){
            cb.append(c);
        }
        else if(String.valueOf(c).matches("[0-9A-Z]")){
            stack.push(Integer.parseInt(c, 36)
        }
        else if(c == '"'){
            if(string){
                String p = sb.toString();
                sb = null;
                stack.push(p);
            }else{
                sb = new StringBuilder();
                string = true;
            }
        }
        else if(string){
            sb.append(c);
        }
        else if(c == ';'){
            stack.pop();
        }
        else if(c == '.'){
            Object x = stack.pop();
            stack.push(x);
            stack.push(x);
        }
        else if(c == '\\'){
            Object x = stack.pop();
            Object y = stack.pop();
            stack.push(x);
            stack.push(y);
        }
        else if(c == '@'){
            Object x = stack.pop();
            Object y = stack.pop();
            Object z = stack.pop();
            stack.push(y);
            stack.push(x);
            stack.push(z);
        }
        else if(c == 'r'){
            stack.reverse();
        }
        else if(c == 'l'){
            stack.push(stack.length());
        }
        else if(c == 'o'){
            System.out.println(stack.pop().toString());
        }
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

class CodeBlock {

    public String code;
    public char name;

    public CodeBlock(String code, char name) {
        this.code = code;
        this.name = name;
    }

    public void run() {
        for (char c : code.toCharArray()) {
            try {
                Fifth.instance.parse(c, true);
            }catch(Exception e){e.printStackTrace();}
        }
    }
}
