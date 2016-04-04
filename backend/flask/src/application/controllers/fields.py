from flask_restful import inputs, fields

class KeyField(fields.Raw):
    """Sending datastore ID via JSON"""
    def format(self, value):
        return value.id()

user_fields = {
    'created': fields.DateTime,
    'distance': fields.Float,
    'isleader': fields.Boolean,
    'key': KeyField,
    'email': fields.String,
    'modified': fields.DateTime,
    'trips': fields.List(fields.Nested(KeyField)),
}

trip_fields = {
    'active': fields.Boolean,
    'added_by': fields.Nested(user_fields),
    'created': fields.DateTime,
    'endloc': fields.Float,
    'key': KeyField,
    'modified': fields.DateTime,
    'startloc': fields.Float,
    'waypoints': fields.List(fields.Nested(KeyField)),
}

waypoint_fields = {
    'created': fields.DateTime,
    'key': KeyField,
    'lat': fields.Float,
    'lon': fields.Float,
    'modified': fields.DateTime,
    'trip': KeyField,
}

