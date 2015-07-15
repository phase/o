import java.io.*;
import java.net.*;
import java.lang.*;
import java.math.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class Fifth {
    public static final String VERSION = "1"; // Version of Fifth

    public static Fifth instance; // static instance used for other classes

    public static void main(String[] a) throws IOException {
        if (a.length == 1) {
            File f = new File(a[0]);
            instance = new Fifth(f); // file input
        } else {
            instance = new Fifth(); // REPL
        }
    }

    Stack stack = new Stack(64 * 1024);

    public Fifth(File f) throws IOException {
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            for (char c : line.trim().toCharArray()) {
                try {
                    parse(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        br.close();
        fr.close();
    }

    public Fifth() {
        Scanner sn = new Scanner(System.in);
        while (true) {
            System.out.print("fifth" + VERSION + " >> ");
            for (char c : sn.nextLine().toCharArray()) {
                try {
                    parse(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Variables for parsing
    ArrayList<Variable> variables = new ArrayList<Variable>(); // list of
                                                               // variables
    boolean codeBlock = false; // parse codeblock?
    StringBuilder cb = new StringBuilder(); // builder for codeblocks
    StringBuilder sb = new StringBuilder(); // builder for strings
    boolean string = false; // parse string?
    boolean skip = false; // skip next command?
    boolean file = false; // File mode
    boolean character = false; // parse character?
    boolean variable = false; // parse variable?
    boolean arrayCreate = false;

    public void parse(char c) throws NumberFormatException, IOException {
        if (skip) {
            skip = false;
            return;
        }
        for (Variable v : variables) {
            if (v.name == c) {
                if (variable) {
                    v.value = stack.pop();
                    variable = false;
                } else if (v.value instanceof CodeBlock) {
                    ((CodeBlock)v.value).run();
                } else {
                    v.push();
                }
                return;
            }
        }

        if (file) { // File I/O
            file = false;
            if (c == 'i') {
                stack.push(readFile(stack.pop().toString()));
            } else if (c == 'o') {
                String path = stack.pop().toString();
                String output = stack.pop().toString();
                writeFile(path, output);
            }
        } else if (character) {
            character = false;
            stack.push(String.valueOf(c));
        } else if (variable) {
            variables.add(new Variable(c, stack.peek()));
            variable = false;
        } else if (c == '{') {
            cb = new StringBuilder();
            codeBlock = true;
        } else if (c == '}') {
            cb = null;
            codeBlock = false;
            stack.push(new CodeBlock(cb.toString()));
        } else if (codeBlock) {
            cb.append(c);
        } else if (c == ':') {
            variable = true;
        } else if (String.valueOf(c).matches("[0-9A-Z]")) {
            stack.push(Double.parseDouble(String.valueOf(c), 36));
        } else if (c == '\"') {
            if (string) {
                String p = sb.toString();
                sb = null;
                stack.push(p);
            } else {
                sb = new StringBuilder();
                string = true;
            }
        } else if (string) {
            sb.append(c);
        } else if (c == '\'') {
            character = true;
        } else if (c == '+') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof CodeBlock && b instanceof CodeBlock) {
                String code = ((CodeBlock)a).code + ((CodeBlock)b).code;
                stack.push(new CodeBlock(code));
            } else if (a instanceof CodeBlock) {
                CodeBlock acb = (CodeBlock) a;
                acb.code += b.toString();
                stack.push(acb);
            } else if (b instanceof CodeBlock) {
                CodeBlock bcb = (CodeBlock) b;
                bcb.code += a.toString();
                stack.push(bcb);
            } else if (a instanceof String || b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                stack.push(as + bs);
            } else {
                stack.push(((double) a) + ((double) b));
            }
        } else if (c == '-') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                String s = as.replaceAll(bs, "");
                stack.push(s);
            } else {
                stack.push(((double) a) - ((double) b));
            }
        } else if (c == '*') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String) {
                String as = a.toString();
                int bi =  Math.floor(b);
                for (int i = 0; i < bi; i++) {
                    stack.push(as);
                }
            } else if (b instanceof String) {
                String bs = b.toString();
                int ai = Math.floor(a);
                for (int i = 0; i < ai; i++) {
                    stack.push(bs);
                }
            } else {
                stack.push(((double) a) + ((double) b));
            }
        } else if (c == '/') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                for (String s : as.split(bs)) {
                    stack.push(s);
                }
            } else {
                stack.push(((double) a) / ((double) b));
            }
        } else if (c == ';') {
            stack.pop();
        } else if (c == '.') {
            stack.push(stack.peek());
        } else if (c == '\\') {
            Object x = stack.pop();
            Object y = stack.pop();
            stack.push(x);
            stack.push(y);
        } else if (c == '@') {
            Object x = stack.pop();
            Object y = stack.pop();
            Object z = stack.pop();
            stack.push(y);
            stack.push(x);
            stack.push(z);
        } else if (c == 'r') {
            stack.reverse();
        } else if (c == 'l') {
            stack.push(stack.length());
        } else if (c == 'f') {
            file = true;
        } else if (c == 'o') {
            System.out.println(stack.pop().toString());
        } else if (c == 'h') {
            // HTTP Server
            final int port = Math.floor(((double)stack.pop()));
            final String path = stack.pop().toString();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        for (ServerSocket socket = new ServerSocket(port);;) {
                            Socket client = socket.accept();
                            PrintWriter out = new PrintWriter(client.getOutputStream());
                            out.print("HTTP/1.1 200 OK\r\n");
                            out.print("Content-Type: text/html\r\n");
                            out.print(readFile(path) + "\r\n");
                            out.flush();
                            out.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else if (c == '!') {
            skip = true;
        } else if (c == '?') {
            Object s = stack.pop();
            if (s instanceof Double) {
                skip = Math.floor(((double) stack.pop())) == 0;
            } else if (s instanceof String) {
                skip = stack.pop().toString().isEmpty();
            }
        } else if (c == 'i') {
            Scanner sn = new Scanner(System.in);
            stack.push(sn.nextLine());
            sn.close();
        } else if (c == '#') {
            String s = stack.pop().toString();
            try {
                stack.push(Double.parseDouble(s));
            } catch (NumberFormatException e) {
                stack.push(s.hashCode());
            }
        } else if (c == '=') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b insanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stack.push(bs.equals(as) ? 1d : 0d);
            } else {
                double bd = (double) b;
                double ad = (double) a;
                stack.push(ad == bd ? 1d : 0d);
            }
        } else if (c == '>') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b insanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stack.push(bs.contains(as) ? 1d : 0d);
            } else {
                double bd = (double) b;
                double ad = (double) a;
                stack.push(ad > bd ? 1d : 0d);
            }
        } else if (c == '<') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b insanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stack.push(as.contains(bs) ? 1d : 0d);
            } else {
                double bd = (double) b;
                double ad = (double) a;
                stack.push(ad < bd ? 1d : 0d);
            }
        } else if (c == '[') {
            arrayCreate = true;
        } else if (c == ']') {
            arrayCreate = false;
            pushArray();
        }
    }

/*
 else if (c == 'C') {
            
        }
*/

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void writeFile(String path, String output) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.print(output);
        writer.close();
    }
}

class Stack {
    Object[] stack;
    int i = -1;

    public Stack(int size) {
        stack = new Object[size];
    }

    public ArrayList<Object> tempArrayCreator = null;

    public void push(Object x) {
        if (Fifth.instance.arrayCreate) {
            if (tempArrayCreator = null) tempArrayCreater = new ArrayList<Object>();
            tempArrayCreater.add(x);
            return;
        }
        if (i >= stack.length - 1)
            throw new ArrayIndexOutOfBoundsException("Can't push to full stack: " + x.toString());
        stack[++i] = x;
    }

    public Object pop() {
        if (i <= -1)
            throw new ArrayIndexOutOfBoundsException("Can't pop from empty stack!");
        Object x = stack[i];
        stack[i] = null;
        i--;
        return x;
    }

    public Object peek() {
        return stack[i];
    }

    public void reverse() {
        for (int left = 0, right = i; left < right; left++, right--) {
            Object x = stack[left];
            stack[left] = stack[right];
            stack[right] = x;
        }
    }

    public int length() {
        return i + 1;
    }

    public void pushArray() {
        push(tempArrayCreator);
        tempArrayCreator = null;
    }

}

class CodeBlock {

    public String code;

    public CodeBlock(String code) {
        this.code = code;
    }

    public void run() {
        for (char c : code.toCharArray()) {
            try {
                Fifth.instance.parse(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class Variable {

    public char name;
    public Object value;

    public Variable(char name, Object value) {
        this.name = name;
        this.value = value;
    }

    public void push() {
        Fifth.instance.stack.push(value);
    }
}
