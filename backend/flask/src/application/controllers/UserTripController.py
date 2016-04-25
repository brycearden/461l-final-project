from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from google.appengine.ext import ndb
from flask_restful import reqparse, marshal_with, Resource, inputs, fields
import logging
import datetime
from ..models.UserModel import *
from ..models.TripModel import *
from fields import KeyField, waypoint_fields, trip_fields, user_fields, user_list_fields, trip_list_fields

class UserTripAPI(Resource):
    """REST API for the api/user/trip waypoint URL

    functionality for interfaceing with trips through a user
    """
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'trip_id',
            type=int,
            default=None,
            help='possible Key for a Trip object',
            location='json',
        )
        return parser.parse_args()

    @marshal_with(user_fields)
    def put(self, user_id):
        """ Associated an existing Trip with a User

        Does not create a new Trip object
        """
        args = self.parse_args()
        #print "HERE WE ARE"
        #print "In put and this trip_id " + str(args)
        u = User.get_by_id(user_id)
        t = TripModel.get_by_id(args['trip_id'])
        # if the waypoint or trips don't exist, abort
        if u is None or t is None:
            abort(404)

        print "got to here and this is t: " + str(t) + "this is u: " + str(u)
        t.add_user(u)
        return u


class UserTripListAPI(Resource):
    """Returns the List of Trips associated with a User"""

    @marshal_with(trip_list_fields)
    def get(self, user_id):
        data = list()
        u = User.get_by_id(user_id)
        if u is None:
            abort(404)

        # get the list of objects associated with the User
        for key in u.trip_ids:
            t = TripModel.get_by_id(key.id())
            if t is not None:
                data.append(t)
        return {'trips': data}, 200

class UserTripDeleteAPI(Resource):
    """ Removes a Trip Relationship from a User without deleting User

    Unfortunately, the REST API for a delete ignores all objects that are
    passed in the message body. Therefore we are going to use another PUT
    method to remove a relationship from the objects.
    """

    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'trip_id',
            type=int,
            default=None,
            help='possible Key for a Trip object',
            location='json',
        )
        return parser.parse_args()

    @marshal_with(user_fields)
    def put(self, user_id):
        args = self.parse_args()

        if not args.trip_id:
            abort(404)

        t = TripModel.get_by_id(args.trip_id)
        u = User.get_by_id(user_id)

        if not u:
            abort(404)

        u.trip_ids.remove(t.key)
        u.put()
        return {
            "msg": "object {} has been deleted".format(user_id),
            "time": str(datetime.datetime.now()),
        }

