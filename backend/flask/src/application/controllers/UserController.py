from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from google.appengine.ext import ndb
from flask_restful import reqparse, marshal_with, Resource, inputs, fields
import logging
import datetime
from ..models.UserModel import *
from fields import KeyField, user_fields, waypoint_fields, trip_fields, user_list_fields


class UserAPI(Resource):
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'distance',
            type=float,
            help='number of miles off of route user is willing to look',
            location='json',
        )
        parser.add_argument(
            'isleader',
            type=bool,
            help='whether or not User is the leader of a Caravan',
            location='json',
        )
        parser.add_argument(
            'email',
            type=str,
            help='Users email',
            location='json',
        )
        return parser.parse_args()

    @marshal_with(user_fields)
    def post(self, id):
        args = self.parse_args()
        try:
            u = User()
            # apply command args
            if id is not None:
                u.email = id
            if args['distance'] is not None:
                u.distance = args['distance']
            #if args['email'] is not None:
                #u.email = args['email']
            if args['isleader'] is not None:
                u.isleader = args['isleader']

            u.key = ndb.Key(User, u.email)
            u.put()
        except BaseException as e:
            abort(500, Error="Exception- {0}".format(e.message))
        return u

    @marshal_with(user_fields)
    def get(self, id):
        args = self.parse_args()
        u = User.get_by_id(id)
        if not u:
            abort(404)
        #print "string user TRIPS: \n\n\n" + str(u.trips)
        #print "string user: \n\n\n" + str(u)
        return u

    @marshal_with(user_fields)
    def put(self, id):
        args = self.parse_args()
        u = User.get_by_id(id)
        if not u:
            abort(404)
        args = self.parse_args()

        #t = TripModel()
        #t.startLoc = "put trip!"
        #t.endLoc = "put trip!"
        #key = t.put()
        #print "put <4>"
        #u.trip_ids.append(key)

        # apply command args
        if args['distance'] is not None:
            u.populate(distance=args['distance'])
#        if args['email'] is not None: lets not let people change their email
#           u.populate(email=args['email'])
        if args['isleader'] is not None:
            u.populate(isleader=args['isleader'])

        u.put()
        return u

    def delete(self, id):
        # TODO: update the delete functionality to remove key associations from lists
        args = self.parse_args()
        u = User.get_by_id(id)
        if not u:
            abort(404)
        u.key.delete()
        return {
            "msg": "object {} has been deleted".format(id),
            "time": str(datetime.datetime.now()),
        }

