# coding: utf-8

from google.appengine.ext import ndb
from ModelX import *

class BaseModel(ndb.Model, BaseX):
    """
    Abstract super class for all models
    Properties:
        key, id, parent,
        created, modified, version
    """
    created = ndb.DateTimeProperty(auto_now_add=True)
    modified = ndb.DateTimeProperty(auto_now=True, indexed=False)
    # version = ndb.IntegerProperty(default=CURRENT_VERSION_TIMESTAMP, indexed=False)

