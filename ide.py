import os
from flask import Flask, render_template, url_for, request
from subprocess import call, check_output

app = Flask(__name__)

def insertFiles(file):
    file = file.replace("<#include>utilities.html</#include>", open("static/insert/utilities.html").read());
    file = file.replace("<#include>explanations.js</#include>", open("static/insert/explanations.js").read());
    file = file.replace("<#include>o.js</#include>", open("static/insert/o.js").read());
    return file

@app.route('/', methods=['GET', 'POST'])
def index():
    url_for('static', filename='logo.ico')
    index = ""
    if request.method == 'POST':
        code = request.form['code']
        input = request.form['input']
        print("Got code: " + code + " input: " + input)
        print("Running O code...")
        output = check_output("o % " + code)
        print("Output: " + output)
        index = open("static/code.html").read()
        index = insertFiles(index)
        index = index.replace("${CODE}", code)
        index = index.replace("${INPUT}", input)
        index = index.replace("${OUTPUT}", output)
    else:
        index = open("static/index.html").read()
        index = insertFiles(index)
    return index

@app.route("/link/")
@app.route("/link/<link>")
def link(link="code=%22Error+in+linking+code%22o&input="):
    url_for('static', filename='logo.ico')
    print("Link: " + link)
    return render_template("link.html", link=link);

if __name__ == "__main__":
    print("Compiling O...")
    call(["gcc", "o.c", "-o", "o"])
    print("Starting server...")
    app.run(host="0.0.0.0",port=80)