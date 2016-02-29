"""
User.py

This file defines what attributes a User of the Journey application should be
able to configure. Currently, we just save the username and the address, but
that will need to change whenever we have a better understanding of the things
that we want to put into the database. My guess is that we will need some
information about travel preferences, how far off of the route we can check,
and how often we can check along the route. We also may want to store some
additional information in there about the google maps api if that helps us
cache any additional information.
"""

from google.appengine.api import users
from google.appengine.ext import ndb

class UserModel(ndb.Model):
    """ Database attributes for a journey app user """

    @staticmethod
    def getSelf():
        """ returns the entire User database object """
        return UserModel.format(users.get_current_user())

    @staticmethod
    def format(user):
        """ formats a user Model into a dictionary for output

        :user: user object type that we want to format
        :returns: a dict with username and email for a given user

        """
        if user is None:
            return {}

        return {
            'username': user.nickname(),
            'user_id': user.user_id(),
            'email': user.email()
        }

