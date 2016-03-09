"""
routes.py

URL routing file for the flask framework

"""
from flask import render_template
from flask.ext.cors import CORS
from flask_restful import reqparse, abort, Api, Resource
from application import app

from controllers.UserController import UserSelf
from controllers.AuthController import login, logout

# Allow cross domain requests from localhost
CORS(app, resource={r"/api/*": {"origins" : "http://localhost:1234"}})

# API endpoints
api = Api(app)
api.add_resource(UserSelf, '/api/users/self')

# login page
app.add_url_rule('/login', 'login', view_func=login)
app.add_url_rule('/logout', 'logout', view_func=logout)

# App Engine warm up handler
# See http://code.google.com/appengine/docs/python/config/appconfig.html#Warming_Requests
def warmup():
    """App Engine warmup handler
    See http://code.google.com/appengine/docs/python/config/appconfig.html#Warming_Requests
    """
    return ''

app.add_url_rule('/api/_ah/warmup', 'warmup', view_func=warmup)

