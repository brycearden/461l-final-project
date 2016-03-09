import os, sys

sys.path.insert(1, os.path.join(os.path.abspath('.'), 'lib'))
sys.path.append("/home/brycearden/lib/google_appengine")
print "running the application"
import application
print "successfully made the import!"
