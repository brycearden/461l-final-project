"""
Controller function for the UserModel
"""
from flask_restful import reqparse, Resource

class UserSelf(Resource):
    def get(self):
        return UserModel.getSelf()
