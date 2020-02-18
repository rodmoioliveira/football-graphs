#!/bin/bash
# sudo apt install python3-pip
# sudo apt install python3-venv
# pip install virtualenv
# virtualenv my-project-env
pyenv virtualenv my-project-env
pyenv my-project-env
# source my-project-env/bin/activate
pip install -r req.txt
export PYTHONPATH="$PWD/src/main/io"
