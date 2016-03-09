from flask import render_template

from application import app

from application.views.public.public_warmup import PublicWarmup
from application.views.public.public_index import PublicIndex
from application.views.public.public_say_hello import PublicSayHello

from application.views.admin.admin_list_examples import AdminListExamples
from application.views.admin.admin_list_examples_cached import AdminListExamplesCached
from application.views.admin.admin_secret import AdminSecret
from application.views.admin.admin_edit_example import AdminEditExample
from application.views.admin.admin_delete_example import AdminDeleteExample


# URL dispatch rules

# App Engine warm up handler
# See http://code.google.com/appengine/docs/python/config/appconfig.html#Warming_Requests
app.add_url_rule('/_ah/warmup', 'public_warmup', view_func=PublicWarmup.as_view('public_warmup'))

app.add_url_rule('/', 'public_index', view_func=PublicIndex.as_view('public_index'))

app.add_url_rule('/hello/<username>', 'public_say_hello', view_func=PublicSayHello.as_view('public_say_hello'))

app.add_url_rule('/examples', 'list_examples', view_func=AdminListExamples.as_view('list_examples'), methods=['GET', 'POST'])

app.add_url_rule('/examples/cached', 'cached_examples', view_func=AdminListExamplesCached.as_view('cached_examples'))

app.add_url_rule('/admin_only', 'admin_only', view_func=AdminSecret.as_view('admin_only'))

app.add_url_rule('/examples/<int:example_id>/edit', 'edit_example', view_func=AdminEditExample.as_view('edit_example'), methods=['GET', 'POST'])

app.add_url_rule('/examples/<int:example_id>/delete', 'delete_example', view_func=AdminDeleteExample.as_view('delete_example'), methods=['POST'])

# Error handlers

# Handle 404 errors
# """
# routes.py

# URL dispatch route mappings and error handlers

# """

# from flask import render_template
# from flask.ext.cors import CORS
# from flask_restful import reqparse, abort, Api, Resource
# from application import app

# from controllers.AuthController import login, logout
# from controllers.UserController import UserSelf

# # Allow cross domain requests from localhost
# CORS(app, resources={r"/api/*": {"origins": "http://localhost:1234"}})

# # API Endpoints
# api = Api(app)
# api.add_resource(UserSelf, '/api/users/self')

# # Login page
# app.add_url_rule('/login', 'login', view_func=login)
# app.add_url_rule('/logout', 'logout', view_func=logout)

# ## URL dispatch rules
# # App Engine warm up handler
# # See http://code.google.com/appengine/docs/python/config/appconfig.html#Warming_Requests
# def warmup():
#     """App Engine warmup handler
#     See http://code.google.com/appengine/docs/python/config/appconfig.html#Warming_Requests

#     """
#     return ''
# app.add_url_rule('/api/_ah/warmup', 'warmup', view_func=warmup)

@app.errorhandler(404)
def page_not_found(e):
    return render_template('404.html'), 404

# Handle 500 errors


@app.errorhandler(500)
def server_error(e):
    return render_template('500.html'), 500
