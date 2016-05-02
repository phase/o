#!/usr/bin/env python
# NOTE: pass -d to this to print debugging info when the server crashes.
from flask import Flask, render_template, url_for, request
from subprocess import Popen, PIPE, check_call
import sys, os, string, glob, logging, pathlib

app = Flask(__name__)

app.logger.addHandler(logging.StreamHandler(sys.stdout))
app.logger.setLevel(logging.ERROR)

def compileO():
    r = check_call(['git', 'submodule', 'update', '--init', '--recursive'])
    if r != 0:
        print("Git submodules could not be initialized")
        raise RuntimeError("Could not initialize Git submodules")
    r = check_call(['make', 'ide'])
    print("o-ide: " + "".join(glob.glob("oide*")))
    if r != 0:
        print("o.c could not be compiled. Error: " + r)
        raise RuntimeError("Could not compile O interpreter")

@app.route('/', methods=['GET', 'POST'])
def index():
    url_for('static', filename='logo.ico')
    if request.method == 'POST':
        #Check files that start with 'o-ide*'
        files = glob.glob("oide*")
        print(files)
        #Check if C was compiled
        if len(files) < 1:
            print("Compiling O...")
            compileO()
        #Run code
        code = request.form['code']
        input = request.form['input'].replace('\r\n', '\n')
        if input is None: input = ""
        print('Got code:', code, 'input:', input)
        print('Running O code...')
        p = Popen(['./oide', '-e', code], stdout=PIPE, stderr=PIPE, stdin=PIPE, universal_newlines=True)
        output, error = p.communicate(input)
        #Output to IDE
        if p.returncode:
            print('Output:', output, 'error:', error)
            return render_template('error.html', code=code, input=input, error=error)
        else:
            print('Output:', output, 'stack:', error)
            return render_template('code.html', code=code, input=input, output=output, stack=error or '[]')
    else:
        return render_template('primary.html')

@app.route('/link/')
@app.route('/link/<code>/')
@app.route('/link/<code>/<input>')
def link(code="IkVycm9yJTIwbGlua2luZyUyMGNvZGUibw==", input=""):
    url_for('static', filename='logo.ico')
    print('Link:', code, input)
    return render_template('link.html', code=code, input=input)

if __name__ == '__main__':
    print('Compiling O...')
    compileO()
    print('Starting server...')
    app.run(port=80, debug='-d' in sys.argv[1:])