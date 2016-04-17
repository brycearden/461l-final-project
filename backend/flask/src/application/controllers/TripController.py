"""
TripController.py

This file serves as the controller for the RESTful API that we use to satisfy
HTTP requests for information from our backend. It uses the Flask RESTful
framework to marshal objects with the correct syntax.

"""
from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from google.appengine.ext import ndb
from flask_restful import reqparse, marshal_with, Resource, inputs, fields
import logging
import datetime
from ..models.TripModel import TripModel
from fields import KeyField, trip_fields, user_fields, waypoint_fields

class TripAPI(Resource):

    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'active',
            type=bool,
            help='whether the current Trip is active or not',
            location='json',
        )
        parser.add_argument(
            'startloc',
            type=float,
            help='starting GPS coordinate of the trip',
            location='json'
        )
        parser.add_argument(
            'endloc',
            type=float,
            help='starting GPS coordinate of the trip',
            location='json'
        )
        return parser.parse_args()

    @marshal_with(trip_fields)
    def get(self, id):
        t = Trip.get_by_id(id)
        if not t:
            abort(404)
        return t

    @marshal_with(trip_fields)
    def put(self, id):
        t = Trip.get_by_id(id)
        if not t:
            abort(404)
        args = self.parse_args()

        # apply command args
        if args['active'] is not None:
            t.populate(active=args['active'])
        if args['startloc'] is not None:
            t.populate(startloc=args['startloc'])
        if args['endloc'] is not None:
            t.populate(endloc=args['endloc'])

        return t

    def delete(self, id):
        # TODO: update the delete functionality to remove key associations from lists
        t = Trip.get_by_id(id)
        if not t:
            abort(404)
        t.key.delete()
        return {
            "msg": "object {} has been deleted".format(id),
            "time": str(datetime.datetime.now()),
        }


class TripListAPI(Resource):
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'active',
            type=bool,
            help='whether the current Trip is active or not',
            location='json',
        )
        parser.add_argument(
            'startloc',
            type=float,
            help='starting GPS coordinate of the trip',
            location='json'
        )
        parser.add_argument(
            'endloc',
            type=float,
            help='starting GPS coordinate of the trip',
            location='json'
        )
        return parser.parse_args()

    @marshal_with(trip_fields)
    def post(self):
        args = self.parse_args()
        try:
            t = Trip()

            # apply command args
            if args['active'] is not None:
                t.active = args['active']
            if args['startloc'] is not None:
                t.startloc = args['startloc']
            if args['endloc'] is not None:
                t.endloc = args['endloc']
            t.put()
        except BaseException as e:
            abort(500, Error="Exception- {0}".format(e.message))
        return t

