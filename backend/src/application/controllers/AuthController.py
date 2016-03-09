from google.appengine.api import users, mail
from google.appengine.runtime.apiproxy_errors import CapabilityDisabledError

from flask import request, render_template, flash, url_for, redirect, abort

from flask_cache import Cache

from application import app
from ..decorators import login_required, admin_required

# from ..models.PaletteModel import EntryModel, EmailSubscriptionModel
import datetime

# Flask-Cache (configured to use App Engine Memcache API)
cache = Cache(app)

def login():
    if not users.get_current_user():
        return redirect(users.create_login_url(request.url))
    return redirect("");

def logout():
    if users.get_current_user():
        return redirect(users.create_logout_url(request.url))
    return redirect("");

