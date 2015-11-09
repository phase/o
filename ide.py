import os
from flask import Flask
from flask import render_template
from flask import url_for

app = Flask(__name__)

def insertFiles(file):
    file = file.replace("<#include>utilities.html</#include>", open("static/insert/utilities.html").read());
    file = file.replace("<#include>explanations.js</#include>", open("static/insert/explanations.js").read());
    file = file.replace("<#include>o.js</#include>", open("static/insert/o.js").read());
    return file

@app.route('/')
def index():
    url_for('static', filename='logo.ico')
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
    app.run(port=80)