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


from TripModel import TripModel
from BaseModel import *

# Information on how to create the oneToMany Relationship was taken from here:
# http://stackoverflow.com/questions/10077300/one-to-many-example-in-ndb
# Information on how to create the manyToMany Relationship was taken from here:
# http://stackoverflow.com/questions/24392270/many-to-many-relationship-in-ndb
class User(BaseModel):
    email = ndb.StringProperty(default="abc123@yahoo.com")
    trip_ids = ndb.KeyProperty(kind='TripModel', repeated=True)
    distance = ndb.FloatProperty(default=5.0)
    isleader = ndb.BooleanProperty(default=True)

