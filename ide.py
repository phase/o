from flask import Flask, render_template, url_for, request
from subprocess import check_output
import os, string

app = Flask(__name__)

def include_string(path, string):
    with open(os.path.join('static/insert', path)) as f:
        return string.replace('<#include>%s</#include>' % path, f.read())

def insertFiles(string):
    for path in 'utilities.html', 'explanations.js', 'o.js':
        string = include_string(path, string)
    return string

@app.route('/', methods=['GET', 'POST'])
def index():
    url_for('static', filename='logo.ico')
    index = ''
    if request.method == 'POST':
        code = request.form['code']
        input = request.form['input']
        print('Got code:', code, 'input:', input)
        print('Running O code...')
        output = check_output(['./o', '-e', code])
        print('Output:', output)
        with open('static/code.html') as f:
            index = f.read()
        index = insertFiles(index)
        index = string.Template(index).substitute(CODE=code, INPUT=input, OUTPUT=output)
    else:
        with open('static/index.html') as f:
            index = f.read()
        index = insertFiles(index)
    return index

@app.route('/link/')
@app.route('/link/<link>')
def link(link='code=%22Error+in+linking+code%22o&input='):
    url_for('static', filename='logo.ico')
    print('Link:', link)
    return render_template('link.html', link=link);

if __name__ == '__main__':
    print('Compiling O...')
    check_output(['gcc', 'o.c', '-o', 'o', '-lm'])
    print('Starting server...')
    app.run(host='0.0.0.0',port=80)
