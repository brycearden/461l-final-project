"""
WaypointController.py

This file serves as the controller for the RESTful API that we use to satisfy
HTTP requests for information from our backend. It uses the Flask RESTful
framework to marshal objects with the correct syntax.

"""

from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from google.appengine.ext import ndb
from flask_restful import reqparse, marshal_with, Resource, inputs, fields
import logging
import datetime
from ..models.WaypointModel import *
from fields import KeyField, waypoint_fields, trip_fields, user_fields

class WaypointAPI(Resource):
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
            location='json'
        )
        return parser.parse_args()

    @marshal_with(waypoint_fields)
    def get(self, id):
        w = WaypointModel.get_by_id(id)
        if not w:
            abort(404)
        return w

    @marshal_with(waypoint_fields)
    def put(self, id):
        w = WaypointModel.get_by_id(id)
        if not w:
            abort(404)
        args = self.parse_args()

        # apply command args
        if args['lat'] is not None:
            w.populate(lat=args['lat'])
        if args['lon'] is not None:
            w.populate(lon=args['lon'])
        return w

    def delete(self, id):
        # TODO: update the delete functionality to remove key associations from lists
        w = WaypointModel.get_by_id(id)
        ret = {
            "msg": "object {} has been deleted".format(id),
            "time": str(datetime.datetime.now()),
        }

        if not w:
            abort(404)

        if not w.trip:
            w.key.delete()
            return ret
        # after we get the waypoint, make sure the Trip Model that contains us
        # has been updated
        t = Trip.get(w.trip)
        if not t:
            abort(404)
        # remove our key from an associated trip, then delete obj
        t.waypoints.remove(w.key)
        w.key.delete()
        return ret

class CreateWaypointAPI(Resource):
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'lat',
            type=str,
            default=None,
            help='latitute value of the Waypoint',
            location='json',
        )
        parser.add_argument(
            'lon',
            type=str,
            default=None,
            help='longitude value of the Waypoint',
            location='json'
        )
        return parser.parse_args()

    @marshal_with(waypoint_fields)
    def post(self):
        args = self.parse_args()
        try:
            w = WaypointModel()

            if args['lat'] is not None:
                w.populate(lat=args['lat'])
            if args['lon'] is not None:
                w.populate(lon=args['lon'])
            w.put()
        except BaseException as e:
            abort(500, Error="Exception- {0}".format(e.message))
        return w

