package xyz.jadonfowler.o;

import static spark.Spark.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WebIDE {
    public static void main(String[] a) {
        O instance = new O();
        O.instance = instance;
        instance.webIDE = true;
        get("/", (req, res) -> {
            return readFile("res/index.html");
        });
        post("/code", (req, res) -> {
            String code = req.queryParams("code");
            String s = "";
            for (char c : code.toCharArray()) {
                s += instance.parse(c);
            }
            return readFile("res/code.html").replace("${INPUT}", req.queryParams("code")).replace("${OUTPUT}", s);
        });
        get("/error", (request, response) -> {
            throw new Exception();
        });
        exception(Exception.class, (e, request, response) -> {
            e.printStackTrace();
            response.status(404);
            response.body("You borked my server! <a href=\"https://github.com/o\">GitHub</a>");
        });
    }

    public static String readFile(String path) throws IOException {
        FileReader fileReader = new FileReader(new File(path));
        BufferedReader br = new BufferedReader(fileReader);
        String all = "";
        String line = null;
        while ((line = br.readLine()) != null)
            all += line;
        br.close();
        fileReader.close();
        return all;
    }
}