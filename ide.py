# NOTE: pass -d to this to print debugging info when the server crashes.
from flask import Flask, render_template, url_for, request
from subprocess import Popen, PIPE, check_call
import sys, os, string, glob

app = Flask(__name__)

def compileO():
    r = check_call(['gcc', 'o.c', '-DIDE', '-o', 'o-ide', '-lm'])
    if r != 0:
        print("O code could not be compile. Error: " + r)

@app.route('/', methods=['GET', 'POST'])
def index():
    url_for('static', filename='logo.ico')
    if request.method == 'POST':
        #Check files that start with 'o-ide*'
        files = glob.glob("o-ide*")
        print(files)
        #Check if C was compiled
        if len(files) < 1:
            print("Compiling O...")
            compileO()
        #Run code
        code = request.form['code']
        input = request.form['input'].replace('\r\n', '\n')
        print('Got code:', code, 'input:', input)
        print('Running O code...')
        p = Popen(['o-ide', '-e', code], stdout=PIPE, stderr=PIPE, stdin=PIPE, universal_newlines=True)
        output, error = p.communicate(input)
        #Output to IDE
        print('Output:', output, 'error:', error)
        if p.returncode:
            return render_template('error.html', code=code, input=input, error=error)
        else:
            return render_template('code.html', code=code, input=input, output=output, stack=error or '[]')
    else:
        return render_template('primary.html')

@app.route('/link/')
@app.route('/link/<link>')
def link(link='code="Error in linking code"o&input='):
    url_for('static', filename='logo.ico')
    print('Link:', link)
    return render_template('link.html', link=link)

if __name__ == '__main__':
    print('Compiling O...')
    compileO()
    print('Starting server...')
    app.run(debug='-d' in sys.argv[1:])