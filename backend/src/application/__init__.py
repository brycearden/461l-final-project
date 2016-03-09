"""
Initialize Flask app

"""
import os
from flask import Flask
from flask_debugtoolbar import DebugToolbarExtension
from werkzeug.debug import DebuggedApplication
from .settings import Testing, Development, Production

app = Flask('application')

if os.getenv('FLASK_CONF') == 'TEST':
    app.config.from_object(Testing)

elif 'SERVER_SOFTWARE' in os.environ and os.environ['SERVER_SOFTWARE'].startswith('Dev'):
    # Development settings
    app.config.from_object(Development)
    # Flask-DebugToolbar
    toolbar = DebugToolbarExtension(app)

    # Google app engine mini profiler
    # https://github.com/kamens/gae_mini_profiler
    app.wsgi_app = DebuggedApplication(app.wsgi_app, evalex=True)

    # from gae_mini_profiler import profiler, templatetags

    # @app.context_processor
    # def inject_profiler():
    #     return dict(profiler_includes=templatetags.profiler_includes())
    # app.wsgi_app = profiler.ProfilerWSGIMiddleware(app.wsgi_app)
else:
    app.config.from_object(Production)

# Enable jinja2 loop controls extension
app.jinja_env.add_extension('jinja2.ext.loopcontrols')

# Pull in URL dispatch routes
import routes
