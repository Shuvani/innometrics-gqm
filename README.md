# GQM based metrics recommender module - backend
This system consumes the goals and questions of the programmers and automatically suggests focused set of metrics.

## Technologies:
* Maven
* Spring
* JUnit 5
* Swagger
* PostgreSQL
* NLP

## Structure

## Changes
There are two options
1) Add small changes and don't change production version
2) Release new version

### Small changes
1) create a new branch with name like "bugfix-[bug name]"
2) merge it with the master branch 
3) go to Actions page and wait until "Master-Testing" workflow will finish its work

### Make release
1) create a new branch with name like "release-v[version of release]"
2) check whether in src/main/resources/application.properties profiles=prod
3) merge with the master branch
4) go to Actions page and wait until "Release" workflow will finish its work