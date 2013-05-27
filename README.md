Makrut: Resilient Client Libraries
=================================
[![Build Status](https://travis-ci.org/dclements/makrut.png)](https://travis-ci.org/dclements/makrut)

This is an tool for providing a straightforward framework for building clients that are resilient to failures in other components in a distributed system. 

This is an experiment in building a reliability framework using the tools provided by [Google's Guava](http://code.google.com/p/guava-libraries/), [Guice](http://code.google.com/p/google-guice/), and [codahale's metrics library](http://metrics.codahale.com). 

Work Remaining
--------------

### makrut-core

 * Provide more robust metrics, including high level success or failure.
 * Provide a module which will make it easier for those not using Guice. 
 * Improve documentation, in particular providing a sample project.
 * Set up publishing.
 * Allow for cross-cutting error detection and health checking. 
 * Profile current patterns and generate some baseline performance metrics.
 * Provide a little easier tunability for advanced use cases.

### makrut-db

 * Provide `batch` capability.
 * Make using transactions from the factory easier. 
 * Currently very rough interface, could use a little work on the exact pattern for usage, ordering, etc.  This is especially true for transactions. 
 * Better integration with Guice for nontrivial use cases.
 * Better support for SQLState evaluation for retry
 * Utilities, especially around `ResultSetHandler`.
