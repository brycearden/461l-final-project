"""
TripModel.py

The Trip serves as a the database representation for what we want a Trip
objects to be able to store. We believe that a Trip should include:

users - a list of User objects that are associated with the Trip
startloc - the ID of the Waypoint that represents the start of the Trip
endloc - the ID of the Waypoint that represents the end of the Trip
waypoints - a list of Waypoint objects that are associated with the Trip
active - whether or not the trip object is active
"""

from google.appengine.ext import ndb
from BaseModel import *
from UserModel import *
from WaypointModel import *

class TripModel(BaseModel):
    startloc = ndb.StringProperty()
    endloc = ndb.StringProperty()
    added_by = ndb.UserProperty()
    active = ndb.BooleanProperty(default=False)
    added_by = ndb.UserProperty()
    endloc = ndb.StringProperty()
    startloc = ndb.StringProperty()
    waypoint_ids = ndb.KeyProperty(kind='WaypointModel', repeated=True)
    user_ids = ndb.KeyProperty(kind='User', repeated=True)


    @property
    def users(self):
        return User.query().filter(User.trips == self.key)

    def add_user(self, user):
        self.user_ids.append(user.key)
        user.trip_ids.append(self.key)
        user.put()
        self.put()

    def remove_user(self, user):
        self.user_ids.remove(user.key)
        user.trip_ids.remove(self.key)
        user.put()
        self.put()
