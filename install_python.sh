#!/bin/bash

# ============================================================
# 1) Install pyenv (https://realpython.com/intro-to-pyenv/)
# ============================================================

printf "===========================================================\n"
printf "\n"
printf "1/6 Fetching pyenv...\n"
printf "\n"
printf "===========================================================\n"
curl https://pyenv.run | bash

# ============================================================
# 2) Define environment variable PYENV_ROOT to point to the path where pyenv
# repo is cloned and add $PYENV_ROOT/bin to your $PATH for access to the pyenv
# command-line utility. (https://github.com/pyenv/pyenv)
# ============================================================

printf "===========================================================\n"
printf "\n"
printf "2/6 Defining environment variable PYENV_ROOT...\n"
printf "\n"
printf "===========================================================\n"

# echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.bash_profile
# echo 'export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.bash_profile
echo 'export PYENV_ROOT="$HOME/.pyenv"' >> $HOME/dotfiles/.zshrc
echo 'export PATH="$PYENV_ROOT/bin:$PATH"' >> $HOME/dotfiles/.zshrc

# ============================================================
# 3) Add pyenv init to your shell to enable shims and autocompletion.
# Please make sure eval "$(pyenv init -)" is placed toward the end of
# the shell configuration file since it manipulates PATH during the initialization.
# ============================================================

printf "===========================================================\n"
printf "\n"
printf "3/6 Adding pyenv init to your shell to enable shims and autocompletion...\n"
printf "\n"
printf "===========================================================\n"

# echo -e 'if command -v pyenv 1>/dev/null 2>&1; then\n  eval "$(pyenv init -)"\nfi' >> ~/.bash_profile
echo -e 'if command -v pyenv 1>/dev/null 2>&1; then\n  eval "$(pyenv init -)"\nfi' >> $HOME/dotfiles/.zshrc

# ============================================================
# Restart your shell so the path changes take effect. You can now begin using pyenv.
# ============================================================

. ~/.bashrc

# ============================================================
# 4) Install python dependencies (https://github.com/pyenv/pyenv/wiki)
# ============================================================

printf "===========================================================\n"
printf "\n"
printf "4/6 Installing python dependencies...\n"
printf "\n"
printf "===========================================================\n"

sudo apt-get update
sudo apt-get install --no-install-recommends make \
  build-essential libssl-dev zlib1g-dev libbz2-dev \
  libreadline-dev libsqlite3-dev wget curl llvm \
  libncurses5-dev xz-utils tk-dev libxml2-dev \
  libxmlsec1-dev libffi-dev liblzma-dev

# ============================================================
# 5) Install Python versions into $(pyenv root)/versions.
# ============================================================

printf "===========================================================\n"
printf "\n"
printf "5/6 Installing Python 3.6.1 ...\n"
printf "\n"
printf "===========================================================\n"

env PYTHON_CONFIGURE_OPTS="--enable-shared" pyenv install 3.6.1
export PYENV_VERSION=3.6.1

# ============================================================
# 6) Install project python dependencies
# ============================================================

printf "===========================================================\n"
printf "\n"
printf " 6/6 Installing project python dependencies...\n"
printf "\n"
printf "===========================================================\n"

pyenv virtualenv my-project-env
pip install -r requirements.txt
export PYTHONPATH="$PWD/src/main/io"
