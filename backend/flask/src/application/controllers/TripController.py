"""
TripController.py

This file serves as the controller for the RESTful API that we use to satisfy
HTTP requests for information from our backend. It uses the Flask RESTful
framework to marshal objects with the correct syntax.

"""

from flask_restful import reqparse, marshal_with, Resource, inputs, fields

trip_fields = {
    'startloc': fields.Float,
    'endloc': fields.Float,
    'timestamp': fields.DateTime,
    'active': fields.Boolean,
    # TODO: figure out how to serialize keys with Flask RESTful I think they
    # are called NestedLists or something like that
}

class Trip(Resource):
    def __init__(self):
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument(
            'urlParam', type=str, required=True,
            help='This is an example param from HTTP',
            location='json',
        )
        super(Trip, self).__init__()

    @marshal_with(trip_fields)
    def get(self, id):
        print "We are in the get function of Trip"

    @marshal_with(trip_fields)
    def post(self, id):
        print "We are in the post function of Trip"

    @marshal_with(trip_fields)
    def put(self, id):
        print "We are in the update function of Trip"

    @marshal_with(trip_fields)
    def delete(self, id):
        print "We are in the delete function of Trip"


class TripList(Resorce):
    def __init__(self):
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument(
            'urlParam', type=str, required=True,
            help='This is an example param from HTTP',
            location='json',
        )
        super(TripList, self).__init__()

    @marshal_with(trip_fields)
    def get(self, id):
        print "We are in the get function of TripList"

    @marshal_with(trip_fields)
    def post(self, id):
        print "We are in the post function of TripList"

    @marshal_with(trip_fields)
    def put(self, id):
        print "We are in the update function of TripList"

    @marshal_with(trip_fields)
    def delete(self, id):
        print "We are in the delete function of TripList"


