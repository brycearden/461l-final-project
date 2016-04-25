#!/usr/bin/env python

import glob
from subprocess import check_call, CalledProcessError
import contextlib
import argparse

# nifty little context manager that lets me ignore all exceptions in one line
@contextlib.contextmanager
def ignored(*exceptions):
    try:
        yield
    except exceptions:
        pass

# port the app server is deployed on
# DEV_APPSERVER = "http://localhost:8080"
DEV_APPSERVER = "http://journey-1236.appspot.com"

parser = argparse.ArgumentParser(description='runs tests for all file args')
parser.add_argument('filenames',
                    metavar='Filenames',
                    type=str,
                    default=None,
                    nargs='*',
                    help='Filename(s) to run the REST tests for. If you do not \
                    pass in any filenames, then you will run all of the .yaml \
                    files in the current directory',
                    )
args = parser.parse_args()
if not args.filenames:
    flist = glob.glob("*.yaml")
else:
    flist = args.filenames

for filename in flist:
    print "STARTING TESTS FOR {}".format(filename)
    with ignored(CalledProcessError):
        check_call(["pyresttest", DEV_APPSERVER, filename])

