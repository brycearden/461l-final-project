# 461L Final Project

## Project Workflow

Always start work by checking out your own branch. When you have finished what
you were working on, merge your own branch with dev. We will merge dev with
master whenever we think that something is finished. The idea is that master
should always be release ready code.

## Installation

For this project we will be using pip 7.1.2. Make sure you have the correct
version of pip because the newest version is a little buggy. Inorder to install
everything that you need for this project, you will need to start a virtual
environment by using the following command.

`sudo apt-get install virtualenv` `source flask/bin/activate`

Virtual environments are great because they help you manage your web
application dependencies without touching the binaries on your actual machine.
Virtualenv basically will create a private PATH variable just for this project
so that the web app will know which dependencies to download when you deploy
onto a remote server. You will know when your virtual environment is activated
when (env) appears next to your command prompt.

In order to download all of the requirements that you will need for our
project, use the following command.

`pip install -r requirements/common.txt`

## Adding a dependency to the project

In order to add a package to the project, use the pip Python package manager do
install whatever package you want. However, it is important that we keep the
requirements folder up to date. The easiest way to keep the requirements folder
up to date is to use the following command.

`pip install <package_name>`
`pip freeze > requirements/common.txt`

## Removing a dependency from the project

In order to remove a package from this project, it is important to also remove
all of the package dependencies. In order to do that, please use the following
command.

`pip-autoremove <package_name> -y`
`pip freeze > requirements/common.txt`

IMPORTANT: make sure you call `pip-autoremove` on the package named that you
used with `pip install`! If you don't, then all of the package dependencies
will not be deleted properly.

Please take the time to browse around some of the Flask source code. The
earlier that we can get comfortable around this code the easier our final
project will be. Here is a [link](http://blog.miguelgrinberg.com/post/the-flask-mega-tutorial-part-i-hello-world online)
to the best Flask tutorial that I could find.



