package xyz.jadonfowler.o;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.math.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class O {
    public static final String VERSION = "1.2"; // Version of O
    public static O instance; // static instance used for other classes

    public O() {
        stacks[sid] = new Stack(stackSize);
    }

    public static void main(String[] a) throws IOException {
        instance = new O();
        if (a.length == 1) {
            File f = new File(a[0]);
            instance.runFile(f); // file input
        }
        else {
            instance.repl(); // REPL
        }
    }

    public static final int stackSize = 64 * 1024;
    int sid = 0;
    Stack[] stacks = new Stack[1024];

    public void runFile(File f) throws IOException {
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String line;
        scanner = new Scanner(System.in);
        while ((line = br.readLine()) != null) {
            for (char c : line.trim().toCharArray()) {
                try {
                    parse(c);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        br.close();
        fr.close();
    }

    Scanner scanner;
    boolean repl = false;
    boolean webIDE = false;
    String[] inputs;
    int inputPointer = 0;

    public void repl() {
        System.out.println("O REPL Version " + VERSION);
        repl = true;
        scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\nO v" + VERSION + " >> ");
            String s;
            try {
                s = scanner.nextLine();
            } catch (NoSuchElementException ex) {
                System.out.println("");
                return;
            }
            if (!s.trim().equalsIgnoreCase("")) {
                for (char c : s.toCharArray()) {
                    try {
                        parse(c);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Variables for parsing
    CopyOnWriteArrayList<Variable> variables = new CopyOnWriteArrayList<Variable>(); // list
                                                                                     // of
    // variables
    boolean codeBlock = false; // parse codeblock?
    StringBuilder cb = new StringBuilder(); // builder for codeblocks
    StringBuilder sb = new StringBuilder(); // builder for strings
    boolean string = false; // parse string?
    boolean file = false; // File mode
    boolean math = false; // Math mode
    boolean character = false; // parse character?
    boolean variable = false; // parse variable?
    int bracketIndents = 0;
    boolean escapeCharacter = false;

    public String parse(char c) throws Exception {
        for (Variable v : variables) {
            if (v.name == c && !codeBlock && !string && !file && !math && !character) {
                if (variable) {
                    v.value = stacks[sid].peek();
                    variable = false;
                }
                else if (v.value instanceof CodeBlock) {
                    return ((CodeBlock) v.value).run();
                }
                else {
                    v.push();
                }
                return "";
            }
        }
        if (file) { // File I/O
            file = false;
            if (c == 'i') {
                stacks[sid].push(readFile(stacks[sid].pop().toString()));
            }
            else if (c == 'o') {
                String path = stacks[sid].pop().toString();
                String output = stacks[sid].pop().toString();
                writeFile(path, output);
            }
        }
        else if (math) {
            // TODO More Math
            math = false;
            switch (c) {
                case 'q': stacks[sid].push(Math.sqrt((double) stacks[sid].pop()));
                          break;
                case '[': stacks[sid].push(Math.floor((double) stacks[sid].pop()));
                          break;
                case ']': stacks[sid].push(Math.ceil((double) stacks[sid].pop()));
                          break;
                case 's': stacks[sid].push(Math.sin((double) stacks[sid].pop()));
                          break;
                case 'S': stacks[sid].push(Math.asin((double) stacks[sid].pop()));
                          break;
                case 'c': stacks[sid].push(Math.cos((double) stacks[sid].pop()));
                          break;
                case 'C': stacks[sid].push(Math.acos((double) stacks[sid].pop()));
                          break;
                case 't': stacks[sid].push(Math.tan((double) stacks[sid].pop()));
                          break;
                case 'T': stacks[sid].push(Math.atan((double) stacks[sid].pop()));
                          break;
                case 'd': double d2 = Math.pow((double) stacks[sid].pop(), 2);
                          double d1 = Math.pow((double) stacks[sid].pop(), 2);
                          stacks[sid].push(Math.sqrt(d1 + d2));
                          break;
                case 'r': double r2 = (double) stacks[sid].pop();
                          double r1 = (double) stacks[sid].pop();
                          if (r2 > r1) {
                              for (double j = r1; j <= r2; j++) {
                                  stacks[sid].push(j);
                              }
                          }
                          else if (r1 > r2) {
                              for (double j = r1; j >= r2; j--) {
                                  stacks[sid].push(j);
                              }
                          }
                          break;
                case 'p': stacks[sid].push(Math.PI);
                          break;
                case 'e': stacks[sid].push(Math.E);
                          break;
                case 'l': stacks[sid].push(299792458d);
                          break;
            }
        }
        else if (character) {
            character = false;
            stacks[sid].push(String.valueOf(c));
        }
        else if (variable) {
            variables.add(new Variable(c, stacks[sid].peek()));
            variable = false;
        }
        else if (c == '{' && !string) {
            if (!codeBlock) {
                cb = new StringBuilder();
                codeBlock = true;
            }
            else {
                bracketIndents++;
                cb.append(c);
            }
        }
        else if (c == '}' && !string) {
            if (bracketIndents == 0) {
                codeBlock = false;
                stacks[sid].push(new CodeBlock(cb.toString()));
                cb = new StringBuilder();
            }
            else {
                bracketIndents--;
                cb.append(c);
            }
        }
        else if (codeBlock) {
            cb.append(c);
        }
        else if (c == ':') {
            variable = true;
        }
        else if (c == '\"' && !escapeCharacter) {
            if (string) {
                String p = sb.toString();
                sb = new StringBuilder();
                stacks[sid].push(p);
                string = false;
            }
            else {
                sb = new StringBuilder();
                string = true;
            }
        }
        else if (string) {
            if (escapeCharacter) {
                escapeCharacter = false;
                if (c == 'n') {
                    c = '\n';
                }
                else if (c == '\\') {
                    c = '\0';
                }
                sb.append(c);
            }
            else if (c == '\\') escapeCharacter = true;
            else {
                sb.append(c);
            }
        }
        else if (c == '\'') {
            character = true;
        }
        else if (String.valueOf(c).matches("[0-9A-FW-Z]")) {
            stacks[sid].push((double) Integer.parseInt(String.valueOf(c), 36));
        }
        else if (c == '+') {
            ArrayList<Object> array;
            if (stacks[sid].peek() instanceof ArrayList<?>) {
                array = (ArrayList<Object>) stacks[sid].pop();
                if (array.size() == 1) {
                    stacks[sid].push(array.get(0));
                }
                else if (array.size() == 0) {
                    return "";
                }
            }
            else {
                array = new ArrayList<Object>();
                Object b = stacks[sid].pop();
                Object a = stacks[sid].pop();
                array.add(a);
                array.add(b);
            }
            Object a = array.remove(0);
            while (array.size() > 0) {
                Object b = array.remove(0);
                if (a instanceof CodeBlock && b instanceof CodeBlock) {
                    String code = ((CodeBlock) a).code + ((CodeBlock) b).code;
                    a = new CodeBlock(code);
                }
                else if (a instanceof CodeBlock) {
                    CodeBlock acb = (CodeBlock) a;
                    acb.code += b.toString();
                    a = acb;
                }
                else if (b instanceof CodeBlock) {
                    CodeBlock bcb = (CodeBlock) b;
                    bcb.code += a.toString();
                    a = bcb;
                }
                else if (a instanceof String || b instanceof String) {
                    String as = a.toString().replaceAll(".0$", "");
                    String bs = b.toString().replaceAll(".0$", "");
                    a = as + bs;
                }
                else {
                    a = ((double) a) + ((double) b);
                }
            }
            stacks[sid].push(a);
        }
        else if (c == '-') {
            if (stacks[sid].peek() instanceof ArrayList<?>) {
                double ans = 0;
                boolean set = false;
                for (Object o : (ArrayList<Object>) stacks[sid].pop()) {
                    if (o instanceof Integer) {
                        if (!set) {
                            ans = (int) o;
                            set = true;
                        }
                        else ans -= (int) o;
                    }
                    else if (o instanceof Double) {
                        if (!set) {
                            ans = (double) o;
                            set = true;
                        }
                        else ans -= (double) o;
                    }
                }
                stacks[sid].push(ans);
                return "";
            }
            Object b = stacks[sid].pop();
            Object a = stacks[sid].pop();
            if (a instanceof String || b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                String s = as.replace(bs, "");
                stacks[sid].push(s);
            }
            else {
                stacks[sid].push(((double) a) - ((double) b));
            }
        }
        else if (c == '*') {
            if (stacks[sid].peek() instanceof ArrayList<?>) {
                double ans = 1;
                for (Object o : (ArrayList<Object>) stacks[sid].pop()) {
                    if (o instanceof Integer) {
                        ans *= (int) o;
                    }
                    else if (o instanceof Double) {
                        ans *= (double) o;
                    }
                }
                stacks[sid].push(ans);
                return "";
            }
            Object b = stacks[sid].pop();
            Object a = stacks[sid].pop();
            if (a instanceof String) {
                String as = a.toString();
                int bi = (int) Math.floor((double) b);
                for (int i = 0; i < bi; i++) {
                    stacks[sid].push(as);
                }
            }
            else if (b instanceof String) {
                String bs = b.toString();
                int ai = (int) Math.floor((double) a);
                for (int i = 0; i < ai; i++) {
                    stacks[sid].push(bs);
                }
            }
            else {
                stacks[sid].push(((double) a) * ((double) b));
            }
        }
        else if (c == '/') {
            if (stacks[sid].peek() instanceof ArrayList<?>) {
                double ans = 1;
                boolean set = false;
                for (Object o : (ArrayList<Object>) stacks[sid].pop()) {
                    if (o instanceof Integer) {
                        if (!set) {
                            ans = (int) o;
                            set = true;
                        }
                        else ans /= (int) o;
                    }
                    else if (o instanceof Double) {
                        if (!set) {
                            ans = (double) o;
                            set = true;
                        }
                        ans /= (double) o;
                    }
                }
                stacks[sid].push(ans);
                return "";
            }
            Object b = stacks[sid].pop();
            Object a = stacks[sid].pop();
            if (a instanceof String || b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                if (bs.equals("")) {
                    for (char e : as.toCharArray()) {
                        stacks[sid].push(String.valueOf(e));
                    }
                }
                else {
                    for (String s : as.split(bs)) {
                        stacks[sid].push(s);
                    }
                }
            }
            else {
                stacks[sid].push(((double) a) / ((double) b));
            }
        }
        else if (c == '%') {
            Object b = stacks[sid].pop();
            Object a = stacks[sid].pop();
            if (a instanceof String && b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                String so = stacks[sid].pop().toString();
                stacks[sid].push(so.replaceAll(as, bs));
            }
            else if (a instanceof ArrayList && b instanceof ArrayList) {
                ArrayList<Object> array = (ArrayList<Object>) a;
                array.addAll((ArrayList<Object>) b);
                stacks[sid].push(array);
            }
            else {
                stacks[sid].push(((double) a) % ((double) b));
            }
        }
        else if (c == ';') {
            stacks[sid].pop();
        }
        else if (c == '.') {
            stacks[sid].push(stacks[sid].peek());
        }
        else if (c == '\\') {
            Object x = stacks[sid].pop();
            Object y = stacks[sid].pop();
            stacks[sid].push(x);
            stacks[sid].push(y);
        }
        else if (c == '@') {
            Object x = stacks[sid].pop();
            Object y = stacks[sid].pop();
            Object z = stacks[sid].pop();
            stacks[sid].push(y);
            stacks[sid].push(x);
            stacks[sid].push(z);
        }
        else if (c == 'r') {
            stacks[sid].reverse();
        }
        else if (c == 'l') {
            stacks[sid].push(stacks[sid].length());
        }
        else if (c == 'f') {
            file = true;
        }
        else if (c == 'm') {
            math = true;
        }
        else if (c == 'o') {
            Object o = stacks[sid].pop();
            if (o instanceof Double) {
                double d = (double) o;
                if (d % 1 == 0) {
                    if (webIDE) return ((int) d) + "";
                    else System.out.print((int) d);
                }
                else {
                    if (webIDE) return (d) + "";
                    else System.out.print(d);
                }
            }
            else if (o instanceof Integer) {
                int i = (int) o;
                if (webIDE) return i + "";
                else System.out.print(i);
            }
            else {
                try {
                    if (webIDE) return o.toString();
                    else System.out.print(o.toString());
                }
                catch (NullPointerException e) {
                    if (webIDE) return o + "";
                    else System.out.print(o);
                }
            }
        }
        else if (c == 'p') {
            Object o = stacks[sid].pop();
            if (o instanceof Double) {
                double d = (double) o;
                if (d % 1 == 0) {
                    if (webIDE) return ((int) d) + "\n";
                    else System.out.println((int) d);
                }
                else {
                    if (webIDE) return (d) + "\n";
                    else System.out.println(d);
                }
            }
            else {
                if (webIDE) return o.toString() + "\n";
                else System.out.println(o.toString());
            }
        }
        else if (c == 'h') {
            // HTTP Server
            final int port = (int) Math.floor(((double) stacks[sid].pop()));
            final String path = stacks[sid].pop().toString();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        for (ServerSocket socket = new ServerSocket(port);;) {
                            Socket client = socket.accept();
                            PrintWriter out = new PrintWriter(client.getOutputStream());
                            out.print("HTTP/1.1 200 OK\r\n");
                            out.print("Content-Type: text/html\r\n");
                            out.print(readFile(path) + "\r\n");
                            System.out.println(readFile(path));
                            out.flush();
                            out.close();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else if (c == 'i') {
            if (webIDE) {
                stacks[sid].push(inputs[inputPointer++]);
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                stacks[sid].push(scanner.nextLine());
                if (!repl) scanner.close();
            }
        }
        else if (c == 'j') {
            if (webIDE) {
                stacks[sid].push(Double.parseDouble(inputs[inputPointer++]));
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                double i = Double.parseDouble(s);
                stacks[sid].push(i);
                if (!repl) scanner.close();
            }
        }
        else if (c == 'Q') {
            if (webIDE) {
                String s = inputs[inputPointer++];
                try {
                    double d = Double.parseDouble(s);
                    variables.add(new Variable('Q', d));
                    stacks[sid].push(d);
                }
                catch (Exception e) {
                    variables.add(new Variable('Q', s));
                    stacks[sid].push(s);
                }
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                try {
                    double d = Double.parseDouble(s);
                    variables.add(new Variable('Q', d));
                    stacks[sid].push(d);
                }
                catch (Exception e) {
                    variables.add(new Variable('Q', s));
                    stacks[sid].push(s);
                }
                if (!repl) scanner.close();
            }
        }
        else if (c == 'z') {
            if (webIDE) {
                String s = inputs[inputPointer++];
                variables.add(new Variable('z', s));
                stacks[sid].push(s);
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                variables.add(new Variable('z', s));
                stacks[sid].push(s);
                if (!repl) scanner.close();
            }
        }
        else if (c == 'J') {
            variables.add(new Variable('J', stacks[sid].peek()));
        }
        else if (c == 'K') {
            variables.add(new Variable('K', stacks[sid].peek()));
        }
        else if (c == 'G') {
            stacks[sid].push("abcdefghijklmnopqrstuvwxyz");
        }
        else if (c == 'H') {
            parse('[');
            parse('Q');
        }
        else if (c == 'I') {
            parse('[');
            parse('i');
        }
        else if (c == 'M') {
            parse('[');
            parse('i');
            parse('~');
        }
        else if (c == 'S') {
            parse(']');
            parse('s');
        }
        else if (c == '#') {
            String s = stacks[sid].pop().toString();
            try {
                stacks[sid].push((double) Double.parseDouble(s));
            }
            catch (NumberFormatException e) {
                stacks[sid].push((double) s.hashCode());
            }
        }
        else if (c == '=') {
            Object b = stacks[sid].pop();
            Object a = stacks[sid].pop();
            if (a instanceof String || b instanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stacks[sid].push(bs.equals(as) ? 1d : 0d);
            }
            else {
                double bd = (double) b;
                double ad = (double) a;
                stacks[sid].push(ad == bd ? 1d : 0d);
            }
        }
        else if (c == '>') {
            Object b = stacks[sid].pop();
            Object a = stacks[sid].pop();
            if (a instanceof String || b instanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stacks[sid].push(bs.contains(as) ? 1d : 0d);
            }
            else {
                double bd = (double) b;
                double ad = (double) a;
                stacks[sid].push(ad > bd ? 1d : 0d);
            }
        }
        else if (c == '<') {
            Object b = stacks[sid].pop();
            Object a = stacks[sid].pop();
            if (a instanceof String || b instanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stacks[sid].push(as.contains(bs) ? 1d : 0d);
            }
            else {
                double bd = (double) b;
                double ad = (double) a;
                stacks[sid].push(ad < bd ? 1d : 0d);
            }
        }
        else if (c == '[') {
            stacks[++sid] = new Stack(stackSize);
        }
        else if (c == ']') {
            sid--;
            stacks[sid].mergeDown();
        }
        else if (c == '&') {
            HashMap<Object, Object> dictionary = new HashMap<Object, Object>();
            for (int i = 0; i < stacks[sid].length(); i++) {
                Object b = stacks[sid].pop();
                if (b instanceof ArrayList || b instanceof HashMap) {
                    stacks[sid].push(b);
                    continue;
                }
                Object a = stacks[sid].pop();
                if (a instanceof ArrayList || a instanceof HashMap) {
                    stacks[sid].push(a);
                    stacks[sid].push(b);
                    continue;
                }
                dictionary.put(a, b);
            }
            stacks[sid].push(dictionary);
        }
        else if (c == '`') {
            Object b = stacks[sid].pop();
            if (b instanceof String) {
                String bs = b.toString();
                String rbs = new StringBuilder(bs).reverse().toString();
                stacks[sid].push(rbs);
            }
            else if (b instanceof Double) {
                String bs = b.toString();
                bs = bs.replaceAll(".0$", "");
                String rbs = new StringBuilder(bs).reverse().toString();
                stacks[sid].push(rbs);
            }
            else if (b instanceof ArrayList) {
                ArrayList<Object> bo = (ArrayList<Object>) b;
                Collections.reverse(bo);
                stacks[sid].push(bo);
            }
        }
        else if (c == 's') {
            Object a = stacks[sid].pop();
            if (a instanceof Integer) {
                stacks[sid].push(a.toString());
            }
            else if (a instanceof Double) {
                stacks[sid].push(a.toString().replaceAll(".0$", ""));
            }
            else if (a instanceof ArrayList) {
                ArrayList<Object> list = (ArrayList<Object>) a;
                ArrayList<Object> nList = new ArrayList<Object>();
                for (Object o : list) {
                    if (o instanceof Double) {
                        nList.add(o.toString().replaceAll(".0$", ""));
                    }
                    else {
                        nList.add(o.toString());
                    }
                }
                stacks[sid].push(nList);
            }
        }
        else if (c == 'k') {
            Object a = stacks[sid].pop();
            Object b = stacks[sid].peek();
            if (b instanceof HashMap<?, ?>) {
                HashMap<Object, Object> dictionary = (HashMap<Object, Object>) b;
                stacks[sid].push(b);
                stacks[sid].push(dictionary.get(a));
            }
            else if (b instanceof ArrayList<?>) {
                ArrayList<Object> list = (ArrayList<Object>) b;
                stacks[sid].push(b);
                stacks[sid].push(list.get((int) (double) a));
            }
            else if (b instanceof String) {
                String s = stacks[sid].pop().toString();
                int i = 0;
                if (a instanceof Integer) i = (int) a;
                else if (a instanceof Double) i = (int) ((double) a);
                stacks[sid].push(String.valueOf(s.toCharArray()[i]));
            }
        }
        else if (c == '~') {
            String s = stacks[sid].pop().toString();
            String r = "";
            for (char g : s.toCharArray()) {
                r += parse(g);
            }
            return r;
        }
        else if (c == '_') {
            Object a = stacks[sid].pop();
            if (a instanceof Double) {
                stacks[sid].push(-((double) a));
            }
            else if (a instanceof Integer) {
                stacks[sid].push(-((double) ((int) a)));
            }
            else if (a instanceof String) {
                stacks[sid].push(a.toString().toLowerCase());
            }
        }
        else if (c == '?') {
            Object f = stacks[sid].pop();
            Object t = stacks[sid].pop();
            Object s = stacks[sid].pop();
            if (isObjectTrue(s)) {
                return ((CodeBlock) t).run();
            }
            else {
                return ((CodeBlock) f).run();
            }
        }
        else if (c == 'd') {
            CodeBlock cb = ((CodeBlock) stacks[sid].pop());
            Object t = stacks[sid].pop();
            int f = 0;
            String s = "";
            if (t instanceof Double) f = (int) Math.floor((double) t);
            else if (t instanceof Integer) f = (int) t;
            for (int g = 0; g < f; g++) {
                boolean set = false;
                for (Variable v : variables) {
                    if (v.name == 'n') {
                        v.value = (double) g;
                        set = true;
                    }
                }
                if (!set) variables.add(new Variable('n', (double) g));
                s += cb.run();
            }
            for (Variable v : variables) {
                if (v.name == 'n') variables.remove(v);
            }
            return s;
        }
        else if (c == 'w') {
            CodeBlock cb = ((CodeBlock) stacks[sid].pop());
            String s = "";
            while (isObjectTrue(stacks[sid].pop())) {
                s += cb.run();
            }
            return s;
        }
        else if (c == 'e') {
            Object o = stacks[sid].peek();
            if (o instanceof Double) {
                Object a = stacks[sid].pop();
                double ad = (double) a;
                stacks[sid].push(ad % 2 == 0 ? 1d : 0d);
            }
            else if (o instanceof String) {
                stacks[sid].push((double) o.toString().length());
            }
        }
        else if (c == ',') {
            Object a = stacks[sid].pop();
            if (a instanceof Double) {
                int ai = (int) ((double) a);
                for (int j = ai; j >= 0; j--) {
                    stacks[sid].push((double) j);
                }
            }
        }
        else if (c == '(') {
            if (stacks[sid].peek() instanceof ArrayList){
                stacks[sid].reopenArray();
            }
            else stacks[sid].push(((double) stacks[sid].pop()) - 1);
        }
        else if (c == ')') {
            stacks[sid].push(((double) stacks[sid].pop()) + 1);
        }
        else if (c == '^') {
            double b = ((double) stacks[sid].pop());
            Object a = stacks[sid].pop();
            if (a instanceof Double) {
                stacks[sid].push(Math.pow((double) a, b));
            }
            else if (a instanceof ArrayList) {
                ArrayList<Object> newArrayList = new ArrayList<Object>();
                for (Object o : (ArrayList<Object>) a) {
                    if (o instanceof Integer) {
                        newArrayList.add(Math.pow(((int) o), b));
                    }
                    else if (o instanceof Double) {
                        newArrayList.add(Math.pow(((double) o), b));
                    }
                }
                stacks[sid].push(newArrayList);
                return "";
            }
        }
        else if (c == 'b') {
            Object bo = stacks[sid].pop();
            Object no = stacks[sid].pop();
            int b = 10;
            int n = 1;
            if (bo instanceof Double) b = (int) Math.floor((double) bo);
            else if (bo instanceof Integer) b = (int) bo;
            if (no instanceof ArrayList) {
                ArrayList<Object> newArrayList = new ArrayList<Object>();
                for (Object o : (ArrayList<Object>) no) {
                    if (o instanceof Integer) {
                        if (b < 0) stacks[sid].push(toNegativeBase((int) o, b));
                        else stacks[sid].push(toBase((int) o, b));
                    }
                    else if (o instanceof Double) {
                        if (b < 0) stacks[sid].push(toNegativeBase((int) ((double) o), b));
                        else stacks[sid].push(toBase((int) ((double) o), b));
                    }
                }
                stacks[sid].push(newArrayList);
                return "";
            }
            if (no instanceof Double) n = (int) Math.floor((double) no);
            else if (no instanceof Integer) n = (int) no;
            if (b < 0) stacks[sid].push(toNegativeBase(n, b));
            else stacks[sid].push(toBase(n, b));
        }
        else if (c == 'u') {
            String s = stacks[sid].pop().toString();
            for (char x : s.toCharArray()) {
                stacks[sid].push((double) ((int) x));
            }
        }
        else if (c == 'c') {
            int i = 0;
            Object a = stacks[sid].pop();
            if (a instanceof Integer) {
                i = (int) a;
            }
            else if (a instanceof Double) {
                i = (int) ((double) a);
            }
            else if (a instanceof ArrayList) {
                ArrayList<Object> aa = (ArrayList<Object>) a;
                String toPush = "";
                for (Object o : aa) {
                    if (o instanceof String) {
                        toPush += o.toString();
                    }
                    else if (o instanceof Integer) {
                        toPush += (char) ((int) o);
                    }
                    else if (o instanceof Double) {
                        toPush += (char) ((int) ((double) o));
                    }
                }
                stacks[sid].push(toPush);
                return "";
            }
            stacks[sid].push(String.valueOf((char) i));
        }
        else if (c == 'L') {
            int to = (int) (double) stacks[sid].pop();
            int from = (int) (double) stacks[sid].pop();
            String s = stacks[sid].pop().toString();
            int j = 0;
            ArrayList<Integer> g = new ArrayList<Integer>();
            for (char x : s.toCharArray()) {
                g.add((int) x);
            }
            int[] src = new int[g.size()];
            Object[] goa = g.toArray();
            for (int i = 0; i < goa.length; i++) {
                src[i] = (int) goa[i];
            }
            long[] con = convertBase(src, from, to);
            String p = "";
            for (long x : con) {
                p += (char) x;
            }
            stacks[sid].push(p);
        }
        else if (c == 'U') {
            stacks[sid].push(LZString.decompress(stacks[sid].pop().toString()));
        }
        else if (c == 'T') {
            stacks[sid].push(LZString.compress(stacks[sid].pop().toString()));
        }
        // System.out.println(c + ": " + stacks[sid].toString());
        return "";
    }

    public static boolean isObjectTrue(Object s) {
        if (s instanceof Double) {
            return ((double) s) > 0d;
        }
        else if (s instanceof Integer) {
            return ((int) s) > 0;
        }
        else if (s instanceof String) {
            return !((String) s).equals("");
        }
        else if (s instanceof CodeBlock) {
            ((CodeBlock) s).run();
            return isObjectTrue(O.instance.stacks[O.instance.sid].pop());
        }
        else if (s instanceof ArrayList) {
            return ((ArrayList<Object>) s).size() > 0;
        }
        else if (s instanceof HashMap) { return ((HashMap<Object, Object>) s).keySet().size() > 0; }
        return false;
    }

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void writeFile(String path, String output) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path, "UTF-8");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.print(output);
        writer.close();
    }

    /** converts integer n into a base b string */
    public static String toBase(int n, int base) {
        // special case
        if (n == 0) return "0";
        String digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String s = "";
        while (n > 0) {
            int d = n % base;
            s = digits.charAt(d) + s;
            n = n / base;
        }
        return s;
    }

    public static String toNegativeBase(int n, int b) {
        if (n == 0) return "0";
        String digits = "";
        while (n != 0) {
            int t_n = n;
            n = (int) (t_n / b);
            int remainder = (t_n % b);
            if (remainder < 0) {
                remainder += Math.abs(b);
                n++;
            }
            digits = remainder + digits;
        }
        return digits;
    }

    public static BigInteger toDecimal(int[] digits, int base) {
        BigInteger number = BigInteger.ZERO;
        long baseCounter = 0;
        for (int i = digits.length - 1; i >= 0; i--) {
            long l = (long) (Math.pow(base, baseCounter++) * digits[i]);
            number = number.add(BigInteger.valueOf(l));
        }
        return number;
    }

    public static long[] fromDecimal(BigInteger rd, int base) {
        ArrayList<Long> f = new ArrayList<Long>();
        while (rd != BigInteger.ZERO) {
            f.add(rd.mod(BigInteger.valueOf(base)).longValue());
            rd = rd.divide(BigInteger.valueOf(base));
        }
        Collections.reverse(f);
        Object[] foa = f.toArray();
        long[] fia = new long[foa.length];
        for (int i = 0; i < foa.length; i++) {
            if (foa[i] instanceof Long) fia[i] = (long) foa[i];
        }
        return fia;
    }

    /*
     * "Hello" "128"# "400"# L."400"# "128"# L\o' o.o
     */
    public static long[] convertBase(int src[], int from, int to) {
        BigInteger rd = toDecimal(src, from);
        // System.out.println("\nDecimal: " + rd);
        long[] rb = fromDecimal(rd, to);
        return rb;
    }
}

class Stack {
    Object[] stack;
    int i = -1;

    public Stack(int size) {
        stack = new Object[size];
    }

    public void mergeDown() {
        int aboveStackID = O.instance.sid + 1;
        ArrayList<Object> list = new ArrayList<Object>();
        for (Object o : O.instance.stacks[aboveStackID].stack) {
            if (o != null) list.add(o);
        }
        push(list);
    }

    public void reopenArray() {
        final ArrayList<Object> array = (ArrayList<Object>) pop();
        Stack s = O.instance.stacks[++O.instance.sid];
        s.stack = new Object[O.stackSize];
        s.i = -1;
        for (Object o : array){
            s.push(o);
        }
    }

    public void push(Object x) {
        if (i >= stack.length - 1)
            throw new ArrayIndexOutOfBoundsException("Can't push to full stack] " + x.toString());
        stack[++i] = x;
    }

    public Object pop() {
        if (i <= -1) throw new ArrayIndexOutOfBoundsException("Can't pop from empty stack!");
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

    public double length() {
        return i + 1d;
    }

    public String toString() {
        return Arrays.asList(stack).toString().replace(", null", "");
    }
}

class CodeBlock {
    public String code;

    public CodeBlock(String code) {
        this.code = code;
    }

    public String run() {
        String s = "";
        for (char c : code.toCharArray()) {
            try {
                s += O.instance.parse(c);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    public String toString() {
        return "{" + code + "}";
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
        O.instance.stacks[O.instance.sid].push(value);
    }
}
