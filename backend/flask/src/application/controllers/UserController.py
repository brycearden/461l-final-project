from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError
from flask_restful import reqparse, Resource
import logging

from ..models.UserModel import UserModel


# UserSelf
# gets information about the current user
class UserSelf(Resource):
    def get(self):
        return UserModel.getSelf();

    # @login_required
    # def put(self):
    #     parser = reqparse.RequestParser()
    #     parser.add_argument('subscribed', type=bool)
    #     args = parser.parse_args()

    #     sub = EmailSubscriptionModel.query(EmailSubscriptionModel.user == users.get_current_user()).get()
    #     sub.subscribed = args['subscribed']

    #     try:
    #         sub.put()
    #         return self.format(users.get_current_user(), sub)
    #     except CapabilityDisabledError:
    #         return {'status' : 500, 'message' : 'can\'t access database'}, 500


