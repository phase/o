lines = []
with open('commands.md','rb') as f:
    for line in f:
        lines.append(line)
explanations = "var explanations = {\n"
for line in open("commands.md"):
    if line.startswith("* _"):
        operator = line.split("(`")[1].split("`)")[0].replace("\\", "\\\\").replace("` ` `", "`").replace("'", "\\'")
        explanation = line.split(": ")[1]
        explanations += "'" + operator + "':'" + explanation.rstrip("\n\r") + "',\n"
with open("static/explanations.js", "wb") as f:
    f.write(bytes(explanations + "\n};", "UTF-8"))