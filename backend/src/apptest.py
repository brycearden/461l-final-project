#!user/bin/python
import os, sys, unittest2, warnings

# ignore complaints about imports
warnings.filterwarnings('ignore', category=UserWarning)

USAGE = """
Path to your sdk must be the first arg. To run type:

$ apptest.py path/to/appengine

In order to run properly, make sure to set the Environment Varialbe FLASK_CONF
to TEST. By using this environment variable, you can add custom testing
configurations in src/application/settings.py.

"""

def main(sdk_path, test_path):
    sys.path.insert(0, sdk_path)
    import dev_appserver
    dev_appserver.fix_sys_path()
    sys.path.insert(1, os.path.join(os.path.abspath('.'), 'lib'))
    test_suite = unittest2.loader.TestLoader().discover(test_path)
    unittest2.TextTestRunner(verbosity=2).run(suite)

if __name__ == "__main__":
    # See: http://code.google.com/appengine/docs/python/tools/localunittesting.html
    try:
        SDK_PATH = sys.argv[1] # ...or hardcoded path
        # Path the to the tests folder
        TEST_PATH = os.path.join(os.path.dirname(os.path.abspath(__name__)), 'tests')
    except Exception as e:
        # your probably forgot to path in the path argument
        print USAGE

