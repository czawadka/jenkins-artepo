Concept
======

Artepo is artifacts repository plugin for Jenkins. Main purpose is to copy build artifacts between promotions of given
build (see [Promoted Builds Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Promoted+Builds+Plugin) for more on concept
of build promotions).


Scenario
--------
The development team creates build. The build is internally tested and if everything works OK it is promoted for
  client's acceptance environment. Then client's acceptance environment is updated with tested build. Next client
  perform tests on acceptance environment and if everything is going smoothly it will mark build as production ready.
  Eventually production environment could be updated with production ready build.

So there is a chain of build copies:
* BUILD - version for internal tests
* ACC - version for acceptance environment
* PRD - version for production environment


How to achieve that
-------------------
BUILD copy can be created using Artepo Copy as Post Build step.

ACC copy can be created using promotion "ACC" which uses Artepo Copy as promotion action.

PRD copy can be created using promotion "PRD" which uses Artepo Copy as promotion action.


Known issues
============
* Not visible repository configuration in promotion after config screen load. Workaround: click repository type
  to show repository configuration.


TODO
====
* svn: instantly check configuration validity (doCheck)
* svn: allow to change repo url without crashing
* add GIT repository (?)
