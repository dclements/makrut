Makrut: Resilient Client Libraries
=================================
[![Build Status](https://travis-ci.org/dclements/makrut.png)](https://travis-ci.org/dclements/makrut)

This is an tool for providing a straightforward framework for building clients that are resilient to failures in other components in a distributed system. 

This is an experiment in building a reliability framework using the tools provided by [Google's Guava](http://code.google.com/p/guava-libraries/), [Guice](http://code.google.com/p/google-guice/), and [codahale's metrics library](http://metrics.codahale.com). 

Work Remaining
--------------

 * Provide more robust metrics, including high level success or failure.
 * Provide a module which will make it easier for those not using Guice.  
 * Provide basic skeletons of commands for quickly building clients. 
 * Improve documentation, in particular providing a sample project.
 * Set up some form of publishing.
 * Allow for cross-cutting error detection and health checking. 
 * Profile current patterns and generate some baseline performance metrics.
