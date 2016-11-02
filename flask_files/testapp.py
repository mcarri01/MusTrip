import os
from flask import Flask
from flask import request


app = Flask(__name__)

@app.route("/add", methods=["POST"])
def add_nums():

	num1 = request.form["num1"]
	num2 = request.form["num2"]
	return str(int(num1) + int(num2))

@app.route("/multiple", methods=["POST"])
def multiple_nums():

	num1 = request.form["num1"]
	num2 = request.form["num2"]
	return str(int(num1) * int(num2))

@app.route("/divide", methods=["POST"])
def divide_nums():

	num1 = request.form["num1"]
	num2 = request.form["num2"]
	return str(int(num1) / int(num2))

if __name__ == "__main__":

    port = int(os.environ.get('port', 5000))
    app.run(host='0.0.0.0', port=port)