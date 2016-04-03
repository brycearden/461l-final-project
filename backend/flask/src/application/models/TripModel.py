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
    startloc = ndb.FloatProperty()
    endloc = ndb.FloatProperty()
    added_by = ndb.UserProperty()
    waypoints = ndb.KeyProperty(kind='Waypoint', repeated=True)
    active = ndb.BooleanProperty(default=False)

    @property
    def users(self):
        return User.query().filter(User.trips == self.key)

    def add_user(self, user):
        user.trips.append(self.key)
        user.put()

