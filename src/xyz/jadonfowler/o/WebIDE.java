package xyz.jadonfowler.o;

import static spark.Spark.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebIDE {
    public static void main(String[] a) {
        try {
            int port = Integer.valueOf(System.getenv("PORT"));
            System.out.println("Binding to port " + port);
            port(port);
        }
        catch (Exception e) {
            int port = 80;
            System.out.println("Binding to port " + port);
            port(port);
        }
        get("/", (req, res) -> {
            return readFile("res/index.html");
        });
        post("/", (req, res) -> {
            O instance = new O();
            O.instance = instance;
            instance.webIDE = true;
            instance.stack = new Stack(32 * 1024);
            instance.variables = new CopyOnWriteArrayList<Variable>();
            String code = req.queryParams("code");
            String input = req.queryParams("input");
            instance.inputs = input.split("\n");
            instance.inputPointer = 0;
            String s = "";
            for (char c : code.toCharArray()) {
                s += instance.parse(c);
            }
            s = s.replace("\n", "<br/>");
            String f = readFile("res/code.html");
            f = f.replace("${INPUT}", input);
            f = f.replace("${CODE}", code);
            f = f.replace("${OUTPUT}", s);
            f = f.replace("${STACK}", instance.stack.toString());
            return f;
        });
        get("/link/:code/*", (req, res) -> {
            return readFile("res/link.html");
        });
        get("/link/:code", (req, res) -> {
            return readFile("res/link.html");
        });
        exception(Exception.class, (e, req, res) -> {
            String code = req.queryParams("code");
            String input = req.queryParams("input");
            String error = e.getMessage() + "<br>";
            error += e.getStackTrace()[0].toString();
            String f = "";
            try {
                f = readFile("res/error.html");
            }
            catch (Exception e1) {}
            f = f.replace("${INPUT}", input);
            f = f.replace("${CODE}", code);
            f = f.replace("${ERROR}", error);
            res.body(f);
        });
    }

    public static String readFile(String path) throws IOException {
        FileReader fileReader = new FileReader(new File(path));
        BufferedReader br = new BufferedReader(fileReader);
        String all = "";
        String line = null;
        while ((line = br.readLine()) != null)
            all += line + "\n";
        br.close();
        fileReader.close();
        return all;
    }
}