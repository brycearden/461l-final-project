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

class WaypointModel(BaseModel):
    lat = ndb.StringProperty(required=True, default=None)
    lon = ndb.StringProperty(required=True, default=None)
    trip = ndb.KeyProperty(kind='TripModel')

