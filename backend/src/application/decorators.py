"""
decorators.py

Decorators for URL handlers.

These are helper functions that I found online that allows us to require admin
or standard user credentials whenever we need to.
"""

from functools import wraps
from google.appengine.api import users
from flask import redirect, request, abort
import time, sys

def login_required(func):
    """ Requires standard login credentials """
    @wraps(func)
    def decorated_view(*args, **kwargs):
        if not users.get_current_user():
            return {'status' : 401, 'message' : 'must be signed in to access this endpoint'}, 401
        return func(*args, **kwargs)
    return decorated_view


def admin_required(func):
    """ Requires App Engine admin credentials """
    @wraps(func)
    def decorated_view(*args, **kwargs):
        if not users.get_current_user() or not users.is_current_user_admin():
            return {'status' : 401, 'message' : 'no permission'}, 401
        return func(*args, **kwargs)
    return decorated_view

def time_job(stream=sys.stdout):
    """ Prints out execution time of a function """
    def actual_time_job(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            start = time.time()
            ret = func(*args, **kwargs)
            end = time.time()
            elapsed = end - start
            stream.write("{} took {} seconds\n".format(func.__name__, elapsed))
        return wrapper
    return actual_time_job

