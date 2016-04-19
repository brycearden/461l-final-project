from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from google.appengine.ext import ndb
from flask_restful import reqparse, marshal_with, Resource, inputs, fields
import logging
import datetime
from ..models.WaypointModel import *
from ..models.TripModel import *
from fields import KeyField, waypoint_fields, trip_fields, user_fields, waypoint_list_fields

class TripWaypointAPI(Resource):
    """ REST API for the api/trip/waypoint URL

    functionality for interfacing with waypoints through a trip
    """
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'lat',
            type=str,
            help='latitute value of the Waypoint',
            location='json',
        )
        parser.add_argument(
            'lon',
            type=str,
            help='longitude value of the Waypoint',
            location='json',
        )
        parser.add_argument(
            'waypoint_id',
            type=int,
            help='possible key for a waypoint object',
            default=None,
            location='json',
        )
        return parser.parse_args()

    @marshal_with(trip_fields)
    def put(self, trip_id):
        """ Associated an existing Waypoint witha a trip

        Does not create a new Waypoint object
        """
        args = self.parse_args()
        t = TripModel.get_by_id(trip_id)
        w = WaypointModel.get_by_id(args['waypoint_id'])

        # if the waypoint or trips don't exist, abort
        if not t or not w:
            abort(404)

        t.waypoints.append(w.key)
        t.put()
        return t

    def delete(self, trip_id):
        """ Removes an association from a Trip

        Does not delete the waypoint from the database
        """
        args = self.parse_args()
        t = TripModel.get_by_id(trip_id)
        w = WaypointModel.get_by_id(args['waypoint_id'])

        if not t or not w:
            abort(404)

        t.waypoints.remove(w.key)
        t.put()
        return t

class TripWaypointListAPI(Resource):
    """REST API for the api/trip/waypoint/list URL

    returns a list of all waypoints associated with a trip
    """
    @marshal_with(waypoint_list_fields)
    def get(self, trip_id):
        data = []
        t = trip.get_by_id(trip_id)

        if t is None:
            abort(404)

        # get the list of objects associated with the User
        for key in t.waypoints:
            w = WaypointModel.get(key)
            if w is not None:
                data.append(w)

        return {
            'waypoints': data
        }

