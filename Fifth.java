import java.io.*;
import java.net.*;
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
	ArrayList<CodeBlock> codeBlocks = new ArrayList<CodeBlock>(); // list of
																	// codeblocks
	ArrayList<Variable> variables = new ArrayList<Variable>(); // list of
																// variables
	boolean codeBlock = false; // parse codeblock?
	boolean blockCreate = false; // create codeblock?
	StringBuilder cb = new StringBuilder(); // builder for codeblocks
	StringBuilder sb = new StringBuilder(); // builder for strings
	boolean string = false; // parse string?
	boolean skip = false; // skip next command?
	boolean file = false; // File mode
	boolean character = false; // parse character?
	boolean variable = false; // parse variable?

	public void parse(char c) throws NumberFormatException, IOException {
		if (skip) {
			skip = false;
			return;
		}
		for (CodeBlock cb : codeBlocks) {
			if (cb.name == c) {
				cb.run();
				return;
			}
		}
		for (Variable v : variables) {
			if (v.name == c) {
				if (variable) {
					v.value = stack.pop();
					variable = false;
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
			return;
		}
		if (variable) {
			variables.add(new Variable(c, stack.pop()));
			variable = false;
		}
		if (blockCreate) {
			codeBlocks.add(new CodeBlock(c, cb.toString()));
			blockCreate = false;
		} else if (codeBlock) {
			cb.append(c);
		} else if (c == '{') {
			cb = new StringBuilder();
			codeBlock = true;
		} else if (c == '}') {
			cb = null;
			codeBlock = false;
			blockCreate = true;
		} else if (c == ':') {
			variable = true;
		} else if (String.valueOf(c).matches("[0-9A-Z]")) {
			stack.push(Integer.parseInt(String.valueOf(c), 36));
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
		} else if (character) {
			character = false;
			stack.push(String.valueOf(c));
		} else if (c == '+') {
			Object b = stack.pop();
			Object a = stack.pop();
			if (a instanceof String || b instanceof String) {
				String as = a.toString();
				String bs = b.toString();
				stack.push(as + bs);
			} else {
				stack.push(((int) a) + ((int) b));
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
				stack.push(((int) a) - ((int) b));
			}
		} else if (c == '*') {
			Object b = stack.pop();
			Object a = stack.pop();
			if (a instanceof String) {
				String as = a.toString();
				int bi = (int) b;
				for (int i = 0; i < bi; i++) {
					stack.push(as);
				}
			} else if (b instanceof String) {
				String bs = a.toString();
				int ai = (int) b;
				for (int i = 0; i < ai; i++) {
					stack.push(bs);
				}
			} else {
				stack.push(((int) a) + ((int) b));
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
				stack.push(((int) a) / ((int) b));
			}
		} else if (c == ';') {
			stack.pop();
		} else if (c == '.') {
			Object x = stack.pop();
			stack.push(x);
			stack.push(x);
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
			final int port = (int) stack.pop();
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
			if (s instanceof Integer) {
				skip = ((int) stack.pop()) == 0;
			} else if (s instanceof String) {
				skip = stack.pop().toString().isEmpty();
			}
		}
	}

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

	public void push(Object x) {
		if (i >= stack.length - 1)
			throw new ArrayIndexOutOfBoundsException("Can't push to full stack: " + x.toString());
		stack[++i] = x;
	}

	public Object pop() {
		if (i <= -1)
			throw new ArrayIndexOutOfBoundsException("Can't pop from empty stack! ");
		Object x = stack[i];
		stack[i] = 0;
		i--;
		return x;
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

}

class CodeBlock {

	public String code;
	public char name;

	public CodeBlock(char name, String code) {
		this.code = code;
		this.name = name;
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
