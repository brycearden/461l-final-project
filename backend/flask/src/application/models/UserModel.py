from google.appengine.api import users
from google.appengine.ext import ndb

class UserModel(ndb.Model):
    """User Model"""

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
            'subscribed': False
        }
