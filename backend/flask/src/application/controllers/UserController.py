from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from google.appengine.ext import ndb
from flask_restful import reqparse, marshal_with, Resource, inputs, fields
import logging
import datetime
from ..models.UserModel import *
from fields import KeyField, user_fields


# UserSelf
#post gets information about the current user
class UserAPI(Resource):

    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'distance',
            type=float,
            default=5.0,
            help='number of miles off of route user is willing to look',
            location='json',
        )
        parser.add_argument(
            'isleader',
            type=bool,
            default=False,
            help='whether or not User is the leader of a Caravan',
            location='json',
        )
        return parser.parse_args()

    @marshal_with(user_fields)
    def get(self, id):
        u = ndb.Key(User, id).get()
        if not u:
            abort(404)
        print "THIS IS THE USER: {0}".format(u)
        return u

    @marshal_with(user_fields)
    def put(self, id):
        u = ndb.Key(User, id).get()
        if not u:
            abort(404)
        args = parser.parse_args()
        for key, val in args.items():
            if val is not None:
                u[key] = val
        return u

    def delete(self, id):
        u = ndb.Key(User, id).get()
        if not u:
            abort(404)
        u.key.delete()
        return {
            "msg": "object {} has been deleted".format(id),
            "time": str(datetime.datetime.now()),
        }

class UserListAPI(Resource):
    def parse_args(self):
        parser = reqparse.RequestParser()
        parser.add_argument(
            'distance',
            type=float,
            default=5.0,
            help='number of miles off of route user is willing to look',
            location='json',
        )
        parser.add_argument(
            'isleader',
            type=bool,
            default=False,
            help='whether or not User is the leader of a Caravan',
            location='json',
        )
        return parser.parse_args()

    @marshal_with(user_fields)
    def post(self):
        args = self.parse_args()
        print "we are in the list post function!"
        try:
            u = User(**args)
            key = u.put()
        except BaseException as e:
            abort(500, Error="Exception- {0}".format(e.message))
        return u

    @marshal_with(user_fields)
    def get(self):
        # TODO: return a list of all Users
        pass

