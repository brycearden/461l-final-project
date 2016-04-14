from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from google.appengine.ext import ndb
from flask_restful import reqparse, marshal_with, Resource, inputs, fields
import logging
import datetime
from ..models.WaypointModel import *
from ..models.TrpModel import *
from fields import KeyField, waypoint_fields, trip_fields, user_fields

class TripWaypointAPI(Resource):
    """ REST API for the api/trip/waypoint URL

    functionality for interfacing with waypoints through a trip
    """
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'lat',
            type=float,
            help='latitute value of the Waypoint',
            location='json',
        )
        parser.add_argument(
            'lon',
            type=float,
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
        t = Trip.get_by_id(trip_id)
        w = Waypoint.get_by_id(args['waypoint_id'])

        # if the waypoint or trips don't exist, abort
        if not t or not w:
            abort(404)

        t.waypoints.append(w.key)
        return t

    def delete(self, trip_id):
        """ Removes an association from a Trip

        Does not delete the waypoint from the database
        """
        args = self.parse_args()
        t = Trip.get_by_id(trip_id)
        w = Waypoint.get_by_id(args['waypoint_id'])

        if not t or not w:
            abort(404)

        t.waypoints.remove(w.key)
        return t


class WaypointListAPI(Resource):
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'lat',
            type=float,
            help='latitute value of the Waypoint',
            location='json',
        )
        parser.add_argument(
            'lon',
            type=float,
            help='longitude value of the Waypoint',
            location='json'
        )
        return parser.parse_args()

    @marshal_with(trip_fields)
    def post(self):
        args = self.parse_args()
        try:
            w = Waypoint()

            if args['lat'] is not None:
                w.lat = args['lat']
            if args['lon'] is not None:
                w.lon = args['lon']
            w.put()
        except BaseException as e:
            abort(500, Error="Exception- {0}".format(e.message))
        return w

