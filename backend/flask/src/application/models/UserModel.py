from google.appengine.api import users
from google.appengine.ext import ndb

"""
The UserModel serves as a the database representation for what we want a User
object to be able to store. We have built this User object off of the google
user login so that a user can log in with their google account. The User object
will include:

trips - a list of Trip objects that are associated with the User
distance - the maximum distance that we can search off of a route for
isLeader - whether or not the User is the leader of a caravan
"""

from . import TripModel

# Information on how to create the oneToMany Relationship was taken from here:
# http://stackoverflow.com/questions/10077300/one-to-many-example-in-ndb
# Information on how to create the manyToMany Relationship was taken from here:
# http://stackoverflow.com/questions/24392270/many-to-many-relationship-in-ndb
class UserModel(ndb.Model):
    trips = ndb.KeyProperty(kind='TripModel', repeated=True)
    distance = ndb.FloatProperty()
    isleader = ndb.BooleanProperty(default=True)

    @staticmethod
    def getSelf():
        return UserModel.format(users.get_current_user())

    @staticmethod
    def format(user):
        if user is None:
            return {}

        return {
            'email': user.email(),
            'username': user.nickname(),
            'user_id': user.user_id(),
            'distance': user.distance,
            'isleader': user.isleader,
            'trips': user.trips,
        }
