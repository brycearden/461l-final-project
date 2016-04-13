"""
WaypointModel.py

The WaypointModel serves as a the database representation for what we want a Waypoint
objects to be able to store. We believe that a WaypointModel should include:

lat = Latitude of the GPS coordinate for the Waypoint
lon = Longitude of the GPS coordinate for the Waypoint
Whatever else we decide is helpful info from the Google Places API
"""

from google.appengine.ext import ndb
from BaseModel import *
from UserModel import *

class WayPointModel(ndb.Model):
    lat = ndb.StringProperty(required=True)
    lon = ndb.StringProperty(required=True)
    waypoint = KeyProperty(kind='TripModel')

