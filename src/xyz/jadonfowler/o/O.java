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
    public static final String VERSION = "1.1"; // Version of O
    public static O instance; // static instance used for other classes

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

    Stack stack = new Stack(64 * 1024);

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
            String s = scanner.nextLine();
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
    boolean arrayCreate = false;
    int bracketIndents = 0;
    boolean escapeCharacter = false;

    public String parse(char c) throws NumberFormatException, IOException {
        // System.out.println(c + ": " + stack.toString());
        for (Variable v : variables) {
            if (v.name == c) {
                if (variable) {
                    v.value = stack.peek();
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
                stack.push(readFile(stack.pop().toString()));
            }
            else if (c == 'o') {
                String path = stack.pop().toString();
                String output = stack.pop().toString();
                writeFile(path, output);
            }
        }
        else if (math) {
            // TODO More Math
            math = false;
            if (c == 'q') {
                stack.push(Math.sqrt((double) stack.pop()));
            }
            else if (c == '[') {
                stack.push(Math.floor((double) stack.pop()));
            }
            else if (c == ']') {
                stack.push(Math.ceil((double) stack.pop()));
            }
            else if (c == 's') {
                stack.push(Math.sin((double) stack.pop()));
            }
            else if (c == 'S') {
                stack.push(Math.asin((double) stack.pop()));
            }
            else if (c == 'c') {
                stack.push(Math.cos((double) stack.pop()));
            }
            else if (c == 'C') {
                stack.push(Math.acos((double) stack.pop()));
            }
            else if (c == 't') {
                stack.push(Math.tan((double) stack.pop()));
            }
            else if (c == 'T') {
                stack.push(Math.atan((double) stack.pop()));
            }
            else if (c == 'd') {
                double y = Math.pow((double) stack.pop(), 2);
                double x = Math.pow((double) stack.pop(), 2);
                stack.push(Math.sqrt(x + y));
            }
            else if (c == 'r') {
                double y = (double) stack.pop();
                double x = (double) stack.pop();
                if (y > x) {
                    for (double j = x; j <= y; j++) {
                        stack.push(j);
                    }
                }
                else if (x > y) {
                    for (double j = x; j >= y; j--) {
                        stack.push(j);
                    }
                }
            }
            else if (c == 'p') {
                stack.push(Math.PI);
            }
            else if (c == 'e') {
                stack.push(Math.E);
            }
            else if (c == 'l') {
                stack.push(299792458d);
            }
        }
        else if (character) {
            character = false;
            stack.push(String.valueOf(c));
        }
        else if (variable) {
            variables.add(new Variable(c, stack.peek()));
            variable = false;
        }
        else if (c == '{') {
            if (!codeBlock) {
                cb = new StringBuilder();
                codeBlock = true;
            }
            else {
                bracketIndents++;
                cb.append(c);
            }
        }
        else if (c == '}') {
            if (bracketIndents == 0) {
                codeBlock = false;
                stack.push(new CodeBlock(cb.toString()));
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
        else if (c == '\"') {
            if (string) {
                String p = sb.toString();
                sb = new StringBuilder();
                stack.push(p);
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
            stack.push((double) Integer.parseInt(String.valueOf(c), 36));
        }
        else if (c == '+') {
            if (stack.peek() instanceof ArrayList<?>) {
                double ans = 0;
                for (Object o : (ArrayList<Object>) stack.pop()) {
                    if (o instanceof Integer) {
                        ans += (int) o;
                    }
                    else if (o instanceof Double) {
                        ans += (double) o;
                    }
                }
                stack.push(ans);
                return "";
            }
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof CodeBlock && b instanceof CodeBlock) {
                String code = ((CodeBlock) a).code + ((CodeBlock) b).code;
                stack.push(new CodeBlock(code));
            }
            else if (a instanceof CodeBlock) {
                CodeBlock acb = (CodeBlock) a;
                acb.code += b.toString();
                stack.push(acb);
            }
            else if (b instanceof CodeBlock) {
                CodeBlock bcb = (CodeBlock) b;
                bcb.code += a.toString();
                stack.push(bcb);
            }
            else if (a instanceof String || b instanceof String) {
                String as = a.toString().replaceAll(".0$", "");
                String bs = b.toString().replaceAll(".0$", "");
                stack.push(as + bs);
            }
            else {
                stack.push(((double) a) + ((double) b));
            }
        }
        else if (c == '-') {
            if (stack.peek() instanceof ArrayList<?>) {
                double ans = 0;
                for (Object o : (ArrayList<Object>) stack.pop()) {
                    if (o instanceof Integer) {
                        ans -= (int) o;
                    }
                    else if (o instanceof Double) {
                        ans -= (double) o;
                    }
                }
                stack.push(ans);
                return "";
            }
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                String s = as.replace(bs, "");
                stack.push(s);
            }
            else {
                stack.push(((double) a) - ((double) b));
            }
        }
        else if (c == '*') {
            if (stack.peek() instanceof ArrayList<?>) {
                double ans = 1;
                for (Object o : (ArrayList<Object>) stack.pop()) {
                    if (o instanceof Integer) {
                        ans *= (int) o;
                    }
                    else if (o instanceof Double) {
                        ans *= (double) o;
                    }
                }
                stack.push(ans);
                return "";
            }
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String) {
                String as = a.toString();
                int bi = (int) Math.floor((double) b);
                for (int i = 0; i < bi; i++) {
                    stack.push(as);
                }
            }
            else if (b instanceof String) {
                String bs = b.toString();
                int ai = (int) Math.floor((double) a);
                for (int i = 0; i < ai; i++) {
                    stack.push(bs);
                }
            }
            else {
                stack.push(((double) a) * ((double) b));
            }
        }
        else if (c == '/') {
            if (stack.peek() instanceof ArrayList<?>) {
                double ans = 1;
                for (Object o : (ArrayList<Object>) stack.pop()) {
                    if (o instanceof Integer) {
                        ans /= (int) o;
                    }
                    else if (o instanceof Double) {
                        ans /= (double) o;
                    }
                }
                stack.push(ans);
                return "";
            }
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                if (bs.equals("")) {
                    for (char e : as.toCharArray()) {
                        stack.push(String.valueOf(e));
                    }
                }
                else {
                    for (String s : as.split(bs)) {
                        stack.push(s);
                    }
                }
            }
            else {
                stack.push(((double) a) / ((double) b));
            }
        }
        else if (c == '%') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String && b instanceof String) {
                String as = a.toString();
                String bs = b.toString();
                String so = stack.pop().toString();
                stack.push(so.replaceAll(as, bs));
            }
            else {
                stack.push(((double) a) % ((double) b));
            }
        }
        else if (c == ';') {
            stack.pop();
        }
        else if (c == '.') {
            stack.push(stack.peek());
        }
        else if (c == '\\') {
            Object x = stack.pop();
            Object y = stack.pop();
            stack.push(x);
            stack.push(y);
        }
        else if (c == '@') {
            Object x = stack.pop();
            Object y = stack.pop();
            Object z = stack.pop();
            stack.push(y);
            stack.push(x);
            stack.push(z);
        }
        else if (c == 'r') {
            stack.reverse();
        }
        else if (c == 'l') {
            stack.push(stack.length());
        }
        else if (c == 'f') {
            file = true;
        }
        else if (c == 'm') {
            math = true;
        }
        else if (c == 'o') {
            Object o = stack.pop();
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
            else {
                if (webIDE) return o.toString();
                else System.out.print(o.toString());
            }
        }
        else if (c == 'p') {
            Object o = stack.pop();
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
            final int port = (int) Math.floor(((double) stack.pop()));
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
                stack.push(inputs[inputPointer++]);
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                stack.push(scanner.nextLine());
                if (!repl) scanner.close();
            }
        }
        else if (c == 'j') {
            if (webIDE) {
                stack.push(Double.parseDouble(inputs[inputPointer++]));
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                double i = Double.parseDouble(s);
                stack.push(i);
                if (!repl) scanner.close();
            }
        }
        else if (c == 'Q') {
            if (webIDE) {
                String s = inputs[inputPointer++];
                try {
                    double d = Double.parseDouble(s);
                    variables.add(new Variable('Q', d));
                    stack.push(d);
                }
                catch (Exception e) {
                    variables.add(new Variable('Q', s));
                    stack.push(s);
                }
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                try {
                    double d = Double.parseDouble(s);
                    variables.add(new Variable('Q', d));
                    stack.push(d);
                }
                catch (Exception e) {
                    variables.add(new Variable('Q', s));
                    stack.push(s);
                }
                if (!repl) scanner.close();
            }
        }
        else if (c == 'z') {
            if (webIDE) {
                String s = inputs[inputPointer++];
                variables.add(new Variable('z', s));
                stack.push(s);
            }
            else {
                if (!repl) scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                variables.add(new Variable('z', s));
                stack.push(s);
                if (!repl) scanner.close();
            }
        }
        else if (c == 'J') {
            variables.add(new Variable('J', stack.peek()));
        }
        else if (c == 'K') {
            variables.add(new Variable('K', stack.peek()));
        }
        else if (c == 'G') {
            stack.push("abcdefghijklmnopqrstuvqxyz");
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
            String s = stack.pop().toString();
            try {
                stack.push((double) Double.parseDouble(s));
            }
            catch (NumberFormatException e) {
                stack.push((double) s.hashCode());
            }
        }
        else if (c == '=') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b instanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stack.push(bs.equals(as) ? 1d : 0d);
            }
            else {
                double bd = (double) b;
                double ad = (double) a;
                stack.push(ad == bd ? 1d : 0d);
            }
        }
        else if (c == '>') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b instanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stack.push(bs.contains(as) ? 1d : 0d);
            }
            else {
                double bd = (double) b;
                double ad = (double) a;
                stack.push(ad > bd ? 1d : 0d);
            }
        }
        else if (c == '<') {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a instanceof String || b instanceof String) {
                String bs = b.toString();
                String as = a.toString();
                stack.push(as.contains(bs) ? 1d : 0d);
            }
            else {
                double bd = (double) b;
                double ad = (double) a;
                stack.push(ad < bd ? 1d : 0d);
            }
        }
        else if (c == '[') {
            arrayCreate = true;
        }
        else if (c == ']') {
            arrayCreate = false;
            stack.pushArray();
        }
        else if (c == '&') {
            HashMap<Object, Object> dictionary = new HashMap<Object, Object>();
            for (int i = 0; i < stack.length(); i++) {
                Object b = stack.pop();
                if (b instanceof ArrayList || b instanceof HashMap) {
                    stack.push(b);
                    continue;
                }
                Object a = stack.pop();
                if (a instanceof ArrayList || a instanceof HashMap) {
                    stack.push(a);
                    stack.push(b);
                    continue;
                }
                dictionary.put(a, b);
            }
            stack.push(dictionary);
        }
        else if (c == '`') {
            Object b = stack.pop();
            if (b instanceof String) {
                String bs = b.toString();
                String rbs = new StringBuilder(bs).reverse().toString();
                stack.push(rbs);
            }
            else if (b instanceof Double) {
                String bs = b.toString();
                bs = bs.replace(".0", "");
                String rbs = new StringBuilder(bs).reverse().toString();
                stack.push(rbs);
            }
            else if (b instanceof ArrayList) {
                ArrayList<Object> bo = (ArrayList<Object>) b;
                Collections.reverse(bo);
                stack.push(bo);
            }
        }
        else if (c == 's') {
            Object a = stack.pop();
            if (a instanceof Integer) {
                stack.push(a.toString());
            }
            else if (a instanceof Double) {
                stack.push(a.toString().replaceAll(".0$", ""));
            }
            else if (a instanceof ArrayList) {
                ArrayList<Object> list = (ArrayList<Object>) a;
                ArrayList<Object> nList = new ArrayList<Object>();
                for (Object o : list) {
                    if (o instanceof Double) {
                        nList.add(a.toString().replaceAll(".0$", ""));
                    }
                    else {
                        nList.add(a.toString());
                    }
                }
                stack.push(nList);
            }
        }
        else if (c == 'k') {
            Object a = stack.pop();
            Object b = stack.peek();
            if (b instanceof HashMap<?, ?>) {
                HashMap<Object, Object> dictionary = (HashMap<Object, Object>) b;
                stack.push(b);
                stack.push(dictionary.get(a));
            }
            else if (b instanceof ArrayList<?>) {
                ArrayList<Object> list = (ArrayList<Object>) b;
                stack.push(b);
                stack.push(list.get((int) (double) a));
            }
            else if (b instanceof String) {
                String s = stack.pop().toString();
                int i = 0;
                if (a instanceof Integer) i = (int) a;
                else if (a instanceof Double) i = (int) ((double) a);
                stack.push(String.valueOf(s.toCharArray()[i]));
            }
        }
        else if (c == '~') {
            String s = stack.pop().toString();
            for (char g : s.toCharArray()) {
                parse(g);
            }
        }
        else if (c == '_') {
            Object a = stack.pop();
            if (a instanceof Double) {
                stack.push(-((double) a));
            }
            else if (a instanceof Integer) {
                stack.push(-((double) ((int) a)));
            }
            else if (a instanceof String) {
                stack.push(a.toString().toLowerCase());
            }
        }
        else if (c == '?') {
            Object f = stack.pop();
            Object t = stack.pop();
            Object s = stack.pop();
            if (isObjectTrue(s)) {
                return ((CodeBlock) t).run();
            }
            else {
                return ((CodeBlock) f).run();
            }
        }
        else if (c == 'd') {
            CodeBlock cb = ((CodeBlock) stack.pop());
            Object t = stack.pop();
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
            CodeBlock cb = ((CodeBlock) stack.pop());
            String s = "";
            while (isObjectTrue(stack.pop())) {
                s += cb.run();
            }
            return s;
        }
        else if (c == 'e') {
            Object o = stack.peek();
            if (o instanceof Double) {
                Object a = stack.pop();
                double ad = (double) a;
                stack.push(ad % 2 == 0 ? 1d : 0d);
            }
            else if (o instanceof String) {
                stack.push((double) o.toString().length());
            }
        }
        else if (c == ',') {
            Object a = stack.pop();
            if (a instanceof Double) {
                int ai = (int) ((double) a);
                for (int j = ai; j >= 0; j--) {
                    stack.push((double) j);
                }
            }
        }
        else if (c == '(') {
            stack.push(((double) stack.pop()) - 1);
        }
        else if (c == ')') {
            stack.push(((double) stack.pop()) + 1);
        }
        else if (c == '^') {
            double b = ((double) stack.pop());
            Object a = stack.pop();
            if (a instanceof Double) {
                stack.push(Math.pow((double) a, b));
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
                stack.push(newArrayList);
                return "";
            }
        }
        else if (c == 'b') {
            Object bo = stack.pop();
            Object no = stack.pop();
            int b = 10;
            int n = 1;
            if (bo instanceof Double) b = (int) Math.floor((double) bo);
            else if (bo instanceof Integer) b = (int) bo;
            if (no instanceof ArrayList) {
                ArrayList<Object> newArrayList = new ArrayList<Object>();
                for (Object o : (ArrayList<Object>) no) {
                    if (o instanceof Integer) {
                        if (b < 0) stack.push(toNegativeBase((int) o, b));
                        else stack.push(toBase((int) o, b));
                    }
                    else if (o instanceof Double) {
                        if (b < 0) stack.push(toNegativeBase((int) ((double) o), b));
                        else stack.push(toBase((int) ((double) o), b));
                    }
                }
                stack.push(newArrayList);
                return "";
            }
            if (no instanceof Double) n = (int) Math.floor((double) no);
            else if (no instanceof Integer) n = (int) no;
            if (b < 0) stack.push(toNegativeBase(n, b));
            else stack.push(toBase(n, b));
        }
        else if (c == 'u') {
            String s = stack.pop().toString();
            for (char x : s.toCharArray()) {
                stack.push((double) ((int) x));
            }
        }
        else if (c == 'c') {
            int i = 0;
            Object a = stack.pop();
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
                stack.push(toPush);
                return "";
            }
            stack.push(String.valueOf((char) i));
        }
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
            return isObjectTrue(O.instance.stack.pop());
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
}

class Stack {
    Object[] stack;
    int i = -1;

    public Stack(int size) {
        stack = new Object[size];
    }

    public ArrayList<Object> tempArrayCreator = null;

    public void push(Object x) {
        if (O.instance.arrayCreate) {
            if (tempArrayCreator == null) tempArrayCreator = new ArrayList<Object>();
            tempArrayCreator.add(x);
            return;
        }
        if (i >= stack.length - 1)
            throw new ArrayIndexOutOfBoundsException("Can't push to full stack: " + x.toString());
        stack[++i] = x;
    }

    public Object pop() {
        if (O.instance.arrayCreate) {
            final Object re = tempArrayCreator.get(tempArrayCreator.size() - 1);
            tempArrayCreator.remove(tempArrayCreator.size() - 1);
            return re;
        }
        if (i <= -1) throw new ArrayIndexOutOfBoundsException("Can't pop from empty stack!");
        Object x = stack[i];
        stack[i] = null;
        i--;
        return x;
    }

    public Object peek() {
        if (O.instance.arrayCreate) return tempArrayCreator.get(tempArrayCreator.size() - 1);
        return stack[i];
    }

    public void reverse() {
        if (O.instance.arrayCreate) {
            Collections.reverse(tempArrayCreator);
        }
        else {
            for (int left = 0, right = i; left < right; left++, right--) {
                Object x = stack[left];
                stack[left] = stack[right];
                stack[right] = x;
            }
        }
    }

    public double length() {
        if (O.instance.arrayCreate) return tempArrayCreator.size();
        return i + 1d;
    }

    public void pushArray() {
        push(tempArrayCreator);
        tempArrayCreator = null;
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
        O.instance.stack.push(value);
    }
}
