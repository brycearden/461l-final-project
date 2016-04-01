"""
TripModel.py

The TripModel serves as a the database representation for what we want a Trip
objects to be able to store. We believe that a TripModel should include:

users - a list of User objects that are associated with the Trip
startloc - the ID of the Waypoint that represents the start of the Trip
endloc - the ID of the Waypoint that represents the end of the Trip
waypoints - a list of Waypoint objects that are associated with the Trip
active - whether or not the trip object is active
"""

from google.appengine.ext import ndb
from . import UserModel, WayPointModel

class TripModel(ndb.Model):
    startloc = ndb.FloatProperty()
    endloc = ndb.FloatProperty()
    added_by = ndb.UserProperty()
    timestamp = ndb.DateTimeProperty(auto_now_add=True)
    waypoints = ndb.KeyProperty(kind='WaypointModel', repeated=True)
    active = ndb.BooleanProperty(default=False)

    @property
    def users(self):
        return UserModel.query().filter(UserModel.trips == self.key)

    def add_user(self, user):
        user.trips.append(self.key)
        user.put()

    @staticmethod
    def format(trip):
        if trip is None:
            return {}

        return {
            'startloc': trip.startloc,
            'endloc': trip.endloc,
            'added_by': trip.added_by,
            'timestamp': trip.timestamp,
            'active': trip.active,
            'waypoints:': [key.urlsafe() for key in trip.waypoints],
        }
