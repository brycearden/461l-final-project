# -*- coding: utf-8 -*-

from flask.views import View

from flask import redirect, url_for
from flask import render_template


class PublicIndex(View):

    def dispatch_request(self):
        # recomment this line if you want to be able to create objects from a
        # UI on the website
        # return redirect(url_for('list_examples'))
        return render_template('index.html')
