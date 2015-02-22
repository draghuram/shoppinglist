
===============
 Shopping List
===============

Introduction
============

This document describes a simple shopping list web application. A demo
instance can be found at https://shoppinglist-demo.appspot.com. 

Design
======

There are three entities in the app.

Store
    Describes a store where items can be purchased. 

Category
    Each item can belong to a single category (such as `Vegetables`).  

Item
    This is the main entity. An item can optionally belong to a single
    category and can have a list of stores associated with it. 

    Items can be added to the shopping list and when they are
    purchased, they can be removed from the list. 

The application comprises of a back-end that implements REST APIs and
a front-end that makes use of these APIs. 

For a complete description of REST APIs, please see the accompanying
api document (``apidoc.rst``). 

Implementation Details
======================

- REST APIs are implemented in Python running on `Google App Engine`_
  (using the native ``webapp2`` framework). All the entities belong to
  a single entity group. This is required to provide strong
  consistency. 

- UI uses `Angular JS`_ framework. 

- Styling is done using bootstrap_. 

- There is no user account support in the app in the sense that data
  is not associated with users. For this reason, the app is
  setup to be accessible only to admins of the appengine application.  

- To deploy the app, do ``scripts/app_update.sh``. 

- To test locally, do ``scripts/dev_setup.sh`` first. This will remove
  the login requirement making local testing easier. 

Hosting your own instance
=========================

Since this is a `Google App Engine`_ project, it is very simple to
host your own instance. Here are the steps you need to follow in this
regard: 

- You can start by cloning this repo locally. How ever, a
  much better option is to `fork this repo
  <https://help.github.com/articles/fork-a-repo/>`_.  Forking allows
  you to submit pull requests in future.

- Download and install `Google App Engine`_ SDK from
  https://cloud.google.com/appengine/downloads. 

- Register your own app at https://appengine.google.com/. You just
  need to choose an available name and accept defaults for everything
  else. 

- Once you have the code locally and an appengine application-id (say,
  "mylist") ready, open the file ``appengine/production.app.yaml`` and
  replace ``shoppinglist42`` in the first line with your
  application-id. So the first line now looks like so::
    
    application: mylist

- Run ``./scripts/app_update.sh`` which uploads the application to App
  Engine (you need to provide credentials). The script uses
  ``appcfg.py`` from App Engine SDK.  

- Now, you should be able to use your very own instance. If you
  want to let someone else access your instance, just add
  that person as admin to the App Engine project. 

- Have fun!

Future
======

Though the app is perfectly useful in its present form, several
enhancements are possible. 

- APIs should return error ID and description in case of any
  problems. More over, UI should show errors in a better
  way. Currently, we just display the whole request and response. 

- Referential integrity for "Store" and "Category" with respect to
  "Item". 

- Introduce user accounts. Support sharing of data between multiple
  accounts. This is useful for multiple members of a family to share
  the list.  

- It should be possible to add pictures to items. 

Testing
=======

All the REST APIs have corresponding tests. The tests are written
in `Groovy`_ and use `TestNG`_ framework. To run::

    $ ./scripts/dev_setup.sh
    # This removes login requirement for the app.

    $ cd tests
    $ ./apitests

Make sure that there is no pre-existing data before running the
tests. There is definite scope for improvement here in that tests
should work even if there is some data to begin with. 

Testing guidelines
------------------

- To test list APIs, test empty list as well as list with at least two
  items.  

- update tests should test modification as well as deletion (where
  ever allowed). 

License
=======

This project (except ``NavbarController`` in ``controllers.js``) is
licensed under Apache License 2.0. Please check LICENSE.txt for the
actual license.  

``NavbarController`` has been adapted from AngularJSDemos_ project by
`Dan Wahlin`_. 

Acknowledgments
================

The community at stackoverflow_ is truly amazing. Almost every
question I had and every problem I ran into was resolved with a quick
search which almost invariably ended up at stackoverflow_ site. 

I would like to thank `Dan Wahlin`_ for an excellent introduction to
Angular JS framework (available at http://youtu.be/i9MHigUZKEM). I
also used his ``NavbarController`` available from AngularJSDemos_.

`Google App Engine`_ provides a very nice cloud back-end and
``dev_appserver`` from its SDK makes local testing a breeze. And of
course, it is always a great pleasure to write code in Python.   

I would like to thank the people who are behind bootstrap_ project.
It allowed me to apply "decent" styling to the application and that is
saying something :-). Also, I would like to thank the author
of `glyphicons`_ for making them available free of cost to bootstrap_
users.  

.. _Google App Engine: https://cloud.google.com/appengine/docs
.. _Angular JS: https://angularjs.org/
.. _bootstrap: http://getbootstrap.com/
.. _stackoverflow: http://stackoverflow.com/
.. _Dan Wahlin: https://weblogs.asp.net/dwahlin
.. _Groovy: http://groovy-lang.org/
.. _TestNG: http://testng.org
.. _AngularJSDemos: https://github.com/gurjeet/AngularJSDemos
.. _glyphicons: http://glyphicons.com/
