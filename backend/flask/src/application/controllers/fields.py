from flask_restful import inputs, fields

class KeyField(fields.Raw):
    """Sending datastore ID via JSON"""
    def format(self, value):
        return value.id()

