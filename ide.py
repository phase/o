import os
from flask import Flask
from flask import render_template

app = Flask(__name__)

def insertFiles(file):
    file = file.replace("<#include>utilities.html</#include>", open("res/insert/utilities.html").read());
    file = file.replace("<#include>explanations.js</#include>", open("res/insert/explanations.js").read());
    file = file.replace("<#include>o.js</#include>", open("res/insert/o.js").read());
    return file

@app.route('/')
def index():
    index = open("res/index.html").read()
    index = insertFiles(index)
    return index

@app.route("/link/")
@app.route("/link/<link>")
def link(link="code=%22Error+in+linking+code%22o&input="):
    print("Link: " + link)
    return render_template("link.html", link=link);

if __name__ == "__main__":
    app.run(port=80)