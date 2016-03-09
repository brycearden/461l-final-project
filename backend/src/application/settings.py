"""
settings.py

This is the configuration file for the Flask application

Important: Place all of the keys in a secret_keys.py file which should never be
checked into the repository to save user information.
"""
from .secret_keys import CSRF_SECRET_KEY, SESSION_KEY

# from secret_keys import CSRF_SECRET_KEY, SESSION KEY

class Config(object):
    # TODO: create the secret keys in a secret_keys.py file and determine if we
    # are going to be using a cache or not
    SECRET_KEY = CSRF_SECRET_KEY
    CSRF_SESSION_KEY = SESSION_KEY
    # Flask cache settings
    CACHE_TYPE = 'gaememcached'


class Development(Config):
    DEBUG = True
    # sets up the flask debugging toolbar
    DEBUG_TB_PROFILER_ENABLED = True
    DEBUG_TB_INTERCEPT_REDIRECTS = False
    # may want to turn this on once we figure out what it is
    CSRF_ENABLED = True


class Testing(Config):
    TESTING = True
    DEBUG = True
    # may want to turn this on once we figure out what it is
    CSRF_ENABLED = True


class Production(Config):
    DEBUG = False
    # may want to turn this on once we figure out what it is
    CSRF_ENABLED = True

