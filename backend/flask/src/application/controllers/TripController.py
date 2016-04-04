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
from ..models.TripModel import Trip
from fields import KeyField, trip_fields, user_fields, waypoint_fields

class TripAPI(Resource):

    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'active',
            type=bool,
            default=True,
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
        t = ndb.Key(Trip, id).get()
        if not t:
            abort(404)
        # print "THIS IS THE TRIP: {0}".format(u)
        return t

    @marshal_with(trip_fields)
    def put(self, id):
        # TODO: put is still not working
        t = ndb.Key(Trip, id).get()
        if not t:
            abort(404)
        args = self.parse_args()
        for key, val in args.items():
            if val is not None:
                t[key] = val
        t.put()
        return t

    def delete(self, id):
        t = ndb.Key(Trip, id).get()
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
            default=True,
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
        print "we are in the tripList post method"
        args = self.parse_args()
        try:
            print args
            t = Trip(**args)
            key = t.put()
        except BaseException as e:
            abort(500, Error="Exception- {0}".format(e.message))
        return t
        # return {
        #     "msg": "object {} has been created".format(id),
        #     "time": str(datetime.datetime.now()),
        # }

