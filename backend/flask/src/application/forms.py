"""
forms.py

Web forms based on Flask-WTForms

See: http://flask.pocoo.org/docs/patterns/wtforms/
     http://wtforms.simplecodes.com/

"""

from flask.ext.wtf import Form
from wtforms.ext.appengine.ndb import model_form
from wtforms.fields import TextField, TextAreaField
from wtforms import validators

from application.models.ExampleModel import ExampleModel


class ClassicExampleForm(Form):
    example_name = TextField('Name', validators=[validators.Required()])
    example_description = TextAreaField('Description', validators=[validators.Required()])

# App Engine ndb model form example
ExampleForm = model_form(ExampleModel, Form, field_args={
    'example_name': dict(validators=[validators.Required()]),
    'example_description': dict(validators=[validators.Required()]),
})
