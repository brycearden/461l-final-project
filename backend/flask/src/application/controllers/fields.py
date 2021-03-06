from flask_restful import inputs, fields

class KeyField(fields.Raw):
    """Sending datastore ID via JSON"""
    def format(self, value):
        return value.id()

trip_fields = {
    'active': fields.Boolean,
    'added_by': KeyField,
    'created': fields.DateTime,
    'endloc': fields.String,
    'key': KeyField,
    'modified': fields.DateTime,
    'startloc': fields.String,
    'waypoint_ids': fields.List(KeyField),
    'user_ids': fields.List(KeyField)
}

user_fields = {
    'created': fields.DateTime,
    'distance': fields.Float,
    'isleader': fields.Boolean,
    'key': KeyField,
    'email': fields.String,
    'modified': fields.DateTime,
    'trip_ids': fields.List(KeyField),

}

waypoint_fields = {
    'created': fields.DateTime,
    'key': KeyField,
    'lat': fields.String,
    'lon': fields.String,
    'modified': fields.DateTime,
    'trip': KeyField,
}

trip_list_fields = {
    'trips': fields.List(fields.Nested(trip_fields)),
}

waypoint_list_fields = {
    'waypoints': fields.List(fields.Nested(waypoint_fields)),
}

user_list_fields = {
    'users': fields.List(fields.Nested(user_fields)),
}

