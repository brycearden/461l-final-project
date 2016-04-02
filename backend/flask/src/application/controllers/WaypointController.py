"""
WaypointController.py

This file serves as the controller for the RESTful API that we use to satisfy
HTTP requests for information from our backend. It uses the Flask RESTful
framework to marshal objects with the correct syntax.

"""

from flask_restful import reqparse, marshal_with, Resource, inputs, fields

waypoint_fields = {
    'lat': fields.Float,
    'long': fields.Float,
    # TODO: figure out how to serialize keys with Flask RESTful I think they
    # are called NestedLists or something like that
}

class Waypoint(Resource):
    def __init__(self):
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument(
            'urlParam', type=str, required=True,
            help='This is an example param from HTTP',
            location='json',
        )
        super(Waypoint, self).__init__()

    @marshal_with(waypoint_fields)
    def get(self, id):
        print "We are in the get function of Waypoint"

    @marshal_with(waypoint_fields)
    def post(self, id):
        print "We are in the post function of Waypoint"

    @marshal_with(waypoint_fields)
    def put(self, id):
        print "We are in the update function of Waypoint"

    @marshal_with(waypoint_fields)
    def delete(self, id):
        print "We are in the delete function of Waypoint"


class WaypointList(Resource):
    def __init__(self):
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument(
            'urlParam', type=str, required=True,
            help='This is an example param from HTTP',
            location='json',
        )
        super(WaypointList, self).__init__()

    @marshal_with(waypoint_fields)
    def get(self, id):
        print "We are in the get function of WaypointList"

    @marshal_with(waypoint_fields)
    def post(self, id):
        print "We are in the post function of WaypointList"

    @marshal_with(waypoint_fields)
    def put(self, id):
        print "We are in the update function of WaypointList"

    @marshal_with(waypoint_fields)
    def delete(self, id):
        print "We are in the delete function of WaypointList"

