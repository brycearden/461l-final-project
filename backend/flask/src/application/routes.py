from flask import render_template
from application import app

from application.views.public.public_warmup import PublicWarmup
from application.views.public.public_index import PublicIndex
from flask.ext.cors import CORS
from flask_restful import reqparse, abort, Api, Resource

from controllers.AuthController import login, logout
from controllers.UserController import UserAPI
from controllers.TripController import TripAPI, CreateTripAPI
from controllers.WaypointController import WaypointAPI, CreateWaypointAPI
from controllers.TripWaypointController import TripWaypointAPI, TripWaypointListAPI, TripWaypointDeleteAPI
from controllers.UserTripController import UserTripAPI, UserTripListAPI, UserTripDeleteAPI

# Allow cross domain requests from localhost
CORS(app, resources={r"/api/*": {"origins": "http://localhost:1234"}})

# # API Endpoints
api = Api(app)

# Home page and login/logout
app.add_url_rule('/', 'public_index', view_func=PublicIndex.as_view('public_index'))
app.add_url_rule('/login', 'login', view_func=login)
app.add_url_rule('/logout', 'logout', view_func=logout)

# RESTful API
# TODO: figure out if email can be used as key without duplicates due to
# default values
# api.add_resource(UserAPI, '/api/users/<string:id>')
api.add_resource(UserAPI, '/api/user/<string:id>')
api.add_resource(UserTripListAPI, '/api/user/trip/list/<string:user_id>')
api.add_resource(UserTripAPI, '/api/user/trip/<string:user_id>')

api.add_resource(TripAPI, '/api/trip/<int:id>')
api.add_resource(CreateTripAPI, '/api/trip/new')
api.add_resource(TripWaypointListAPI, '/api/trip/waypoint/list/<int:trip_id>')
api.add_resource(TripWaypointAPI, '/api/trip/waypoint/<int:trip_id>')

api.add_resource(WaypointAPI, '/api/waypoint/<int:id>')
api.add_resource(CreateWaypointAPI, '/api/waypoint/new')

api.add_resource(TripWaypointDeleteAPI, '/api/trip/waypoint/remove/<int:trip_id>')
api.add_resource(UserTripDeleteAPI, '/api/user/trip/remove/<string:user_id>')


## URL dispatch rules
# App Engine warm up handler
# See http://code.google.com/appengine/docs/python/config/appconfig.html#Warming_Requests
def warmup():
    """App Engine warmup handler
    See http://code.google.com/appengine/docs/python/config/appconfig.html#Warming_Requests

    """
    return ''
app.add_url_rule('/_ah/warmup', 'public_warmup', view_func=PublicWarmup.as_view('public_warmup'))

@app.errorhandler(404)
def page_not_found(e):
    return render_template('404.html'), 404

# Handle 500 errors


@app.errorhandler(500)
def server_error(e):
    return render_template('500.html'), 500
