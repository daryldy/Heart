Heart Observe
=============

Heart Observe is an Android app designed to easily track a person's blood pressure and pulse over time. It has graphing capabilities using the achartengine library.

Dependancies
------------

rst2html
++++++++

- from the python-docutils package

ActionBarSherlock
+++++++++++++++++

- Download from `ActionBarSherlock <http://actionbarsherlock.com>`_
- Setup as needed

SimonVT's android-numberpicker
++++++++++++++++++++++++++++++

- Download from `android-numberpicker <https://github.com/SimonVT/android-numberpicker>`_

Build Instructions
------------------

This assumes that you have setup the Android development environment. The instructions are for *ant* builds. If you use *eclipse*, modify as needed.

- Get the source:

.. code-block:: sh

  git clone https://github.com/daryldy/Heart.git

- Setup source for Android/Ant builds:

.. code-block:: sh

  android update project --path Heart

- Build and install on emulator or available device

  - Build assumes that the ActionBarSherlock and SimonVT's android-numberpicker are installed in the same directory as Heart. If they are not then edit project.properties as needed.


.. code-block:: sh

  cd Heart
  ant debug install
