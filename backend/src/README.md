## Python Flask Skeleton for Google App Engine

A skeleton for building Python applications on Google App Engine with the
[Flask micro framework](http://flask.pocoo.org).

See our other [Google Cloud Platform github
repos](https://github.com/GoogleCloudPlatform) for sample applications and
scaffolding for other python frameworks and use cases.

## Run Locally
1. Install virtualenv, which is a virtual environment setup environment so that
   all project dependencies can be installed without messing with your own PATH
   variables. To install virtualenv run:

    ```
    sudo apt-get install virtualenv
    ```
2. To run the virtual env associated with this project, run the following
   command. You will now if you are running the virtual environemnt if there is
   (venv) next to your command line.
    ```
    source venv/bin/activate
    ```

3. Install the [App Engine Python SDK](https://developers.google.com/appengine/downloads).
See the README file for directions. You'll need python 2.7 and [pip 1.4 or later](http://www.pip-installer.org/en/latest/installing.html) installed too.

4. Clone this repo with

   ```
   git clone https://github.com/GoogleCloudPlatform/appengine-python-flask-skeleton.git
   ```
5. Install dependencies in the project's lib directory.
   Note: App Engine can only import libraries from inside your project directory.

   ```
   cd appengine-python-flask-skeleton
   pip install -r requirements.txt -t lib
   ```
5. Run this project locally from the command line:

   ```
   dev_appserver.py .
   ```

Visit the application [http://localhost:8080](http://localhost:8080)

See [the development server documentation](https://developers.google.com/appengine/docs/python/tools/devserver)
for options when running dev_appserver.

## Deploy
To deploy the application:

1. Use the [Admin Console](https://appengine.google.com) to create a
   project/app id. (App id and project id are identical)
1. [Deploy the
   application](https://developers.google.com/appengine/docs/python/tools/uploadinganapp) with

   ```
   appcfg.py -A <your-project-id> --oauth2 update .
   ```
1. Congratulations!  Your application is now live at your-app-id.appspot.com

## Next Steps
This project has some TODO markers spread out to help us figure out what to
implement next. That being said, here are some important next steps.

1. Figure out how we are actually supposed to run this project. I have been
   able to get a lot of work done by following the directory structure of [this
   project](https://github.com/rsyvarth/Schemify).

2. Look into any other features that we want to add and data structures that we
   want to store in the user model. The user model information can be found in
   the /application/models folder.

3. Decide if there is any other information that we would like to store besides
   a User Object. Might be useful to store some other pertinent information
   about the trip.

4. Look into how we want to store a location online so that we can implement
   the caravan features.

5. Decide if we want to actually have a front end for this webserver or not. I
   don't think that it is necessary, but if we have time and can finish other
   parts of this application quickly it would be an easy way to expand this
   project.

6. Talk with Kevin and start figuring out what kind of features that we want
   our API to provide to the Android application. We will be implementing a
   simple REST framework to pass information back and forth between the Android
   application and the webserver, but we need to think about how we want to
   deliver that information. Maybe having functions that return individual
   Users, a list of Users, and other data structures that might be helpful for
   us on the Android side of things.

7. This kind of goes along with 6, but we need to figure out how we want the
   routes to work as well. Like what the URL structure of a query to the
   backend from the Android application will look like. It might be helpful to
   talk to Kevin about this since he has a better idea about how he wants to be
   querying the backend.

8. Figure out if we need a relational database or just some place to store
   simple objects. A relational database is a little more complicated to setup,
   because you have to deal with all the different type of relationships
   (OneToMany, ManyToMany, etc..), but it would provide use with a little more
   functionality on the backend. For example, relational databases allow us to
   have a Destination model that contains information about a Trip. Then a User
   model could possibly point to Many Trips. This would be a ManyToMany
   relationship. Personally, I don't think that we should worry about setting
   up a relational database until we figure out that we need it.

9. Just do some research on the google app engine stuff and figure out if I am
   properly using the google app engine ndb API. If we need a relational
   database we will have to change the application/models folder.

10. Look into the app.yaml file and figure out what it does/why it is
    necessary. I am pretty sure it is necessary to use with Google App Engine
    but I don't know if we need to incorporate it into the Flask framework or
    not.

### Relational Databases and Datastore
To add persistence to your models, use
[NDB](https://developers.google.com/appengine/docs/python/ndb/) for
scale.  Consider
[CloudSQL](https://developers.google.com/appengine/docs/python/cloud-sql)
if you need a relational database.

### Installing Libraries
See the [Third party
libraries](https://developers.google.com/appengine/docs/python/tools/libraries27)
page for libraries that are already included in the SDK.  To include SDK
libraries, add them in your app.yaml file. Other than libraries included in
the SDK, only pure python libraries may be added to an App Engine project.

### Feedback
Star this repo if you found it useful. Use the github issue tracker to give
feedback on this repo.

## Contributing changes
See [CONTRIB.md](CONTRIB.md)

## Licensing
See [LICENSE](LICENSE)

## Author
Logan Henriquez and Johan Euphrosine
