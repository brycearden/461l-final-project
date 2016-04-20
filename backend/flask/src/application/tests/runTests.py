#!/usr/bin/env python


import glob
from subprocess import check_call, CalledProcessError
import contextlib

# nifty little context manager that lets me ignore all exceptions in one line
@contextlib.contextmanager
def ignored(*exceptions):
    try:
        yield
    except exceptions:
        pass

# port the app server is deployed on
DEV_APPSERVER = "http://localhost:8080"

for filename in glob.glob("*.yaml"):
    with ignored(CalledProcessError):
        check_call(["pyresttest", DEV_APPSERVER, filename])

