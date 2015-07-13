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
    Stack stack = new Stack(64 * 1024);

    public Fifth(File f) throws IOException{
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
    boolean skip = false;
    boolean file = false; // File mode

    public void parse(char c) throws NumberFormatException, IOException{
        if(skip){
            skip = false;
            return;
        }
        for(CodeBlock cb : codeBlocks){
            if(cb.name == c){
                cb.run();
                return;
            }
        }

        if(file){
            file = false;
            if(c == 'i'){
                stack.push(readFile(stack.pop().toString()));
            }
            else if(c == 'o'){
                String path = stack.pop().toString();
                String output = stack.pop().toString();
                writeFile(path, output);
            }
            return;
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

        else if(c == '+'){
            Object b = stack.pop();
            Object a = stack.pop();
            if(a instanceof String || b instanceof String){
                String as = a.toString();
                String bs = b.toString();
                stack.push(as + bs);
            }
            else {
                stack.push(((int)a) + ((int)b));
            }
        }
        else if(c == '-'){
            Object b = stack.pop();
            Object a = stack.pop();
            if(a instanceof String || b instanceof String){
                String as = a.toString();
                String bs = b.toString();
                String c = as.replaceAll(bs, "");
                stack.push(c);
            }
            else {
                stack.push(((int)a) - ((int)b));
            }
        }
        else if(c == '*'){
            Object b = stack.pop();
            Object a = stack.pop();
            if(a instanceof String){
                String as = a.toString();
                int bi = (int)b;
                for(int i = 0; i < bi; i++){
                    stack.push(as);
                }
            }
            else if(b instanceof String){
                String bs = a.toString();
                int ai = (int)b;
                for(int i = 0; i < ai; i++){
                    stack.push(bs);
                }
            }
            else {
                stack.push(((int)a) + ((int)b));
            }
        }
        else if(c == '/'){
            Object b = stack.pop();
            Object a = stack.pop();
            if(a instanceof String || b instanceof String){
                String as = a.toString();
                String bs = b.toString();
                for(String c : as.split(bs)){
                    stack.push(c);
                }
            }
            else {
                stack.push(((int)a) / ((int)b));
            }
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
        else if(c == 'f'){
            file = true;
        }
        else if(c == 'o'){
            System.out.println(stack.pop().toString());
        }
        else if(c == 'h'){
            //HTTP Server
            final int port = (int)stack.pop();
            final String path = stack.pop().toString();
            new Thread(new Runnable(){
                public void run(){
                    for(ServerSocket socket = new ServerScoket(port);;){
                        Socket client = socket.accept();
                        PrintWriter out = new PrintWriter(client.getOutputStream());
                        out.print("HTTP/1.1 200 OK\r\n");
                        out.print("Content-Type: text/html\r\n");
                        out.print(readFile(path) + "\r\n");
                        out.flush(); 
                        out.close();
                    }
                }
            }).start();
        }
        else if(c == '!'){
            skip = true;
        }
        else if(c == '?'){
            Object s = stack.pop();
            if(s instanceof Integer){
                skip = ((int)stack.pop()) == 0;
            }
            else if(s instanceof String){
                skip = stack.pop().toString().isEmpty();
            }
        }
    }

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void writeFile(String path, String output){
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.print(output);
        writer.close();
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
