THIS PROJECT IS MADE PUBLIC UNDER  THE SAPIA-OSS
LICENSE. SEE LICENSE.txt THAT COMES  PACKAGED IN 
THE ROOT OF THE DISTRIBUTION OR GO TO 

http://www.sapia-oss.org/license.html 


SAPIA CORUS INTEROP
====================

Sapia Corus Interop is the module responsible of the interoperability of Corus.
See the Corus home page (http://www.sapia-oss.org/projects/corus/home.html)
for more information.


FILES
=====

./LICENSE.txt                Sapia license file

./dist/corus_iop.jar         The sapia Corus Interop jar files (classes only)

./dist/corus_iop_src.jar     The sapia Corus Interop jar files (classes and source)
 
./docs/index.html            Corus Interop documentation home page

./docs/api/index.html        Corus Interop Javadoc home page

./lib/*.jar                  Core and third party libraries used by Corus Interop


CREDITS
=======

This product includes software developed  by the  
Apache Software Foundation  (http://www.apache.org/).


DEPENDENCIES
============

Log4j 1.2.5              (http://jakarta.apache.org/log4j/docs/index.html)

Piccolo XML parser 1.0.3 (http://piccolo.sourceforge.net/)

Junit 3.7.               (http://www.junit.org/index.htm)

JDOM 1.0 beta 9          (http://www.jdom.org)


SUPPORT
=======

Mailing List: sapia-corus_users@lists.sourceforge.net

Email: info@sapia-oss.org


INSTALLATION
============

1. Download

Download the distribution from SourceForge 
(http://www.sourceforge.net/projects/corus). The download
link appears in the "Files" section; the latest "corus interop"
release should be downloaded.


2. Extract

Extract the distribution under a directory of your choice
on the target computer.


3. Set-up Environment

The library itself can be found under the ./dist directory
of the extraction directory. The project requires the libraries
under the ./lib directory as dependencies, so put this project's 
jar file, as well as the jars that appear in the ./lib directory, 
in your application's classpath.


BUILD
=====

You must have Ant installed (see http://ant.apache.org).

In this directory, type the "ant" command, followed by the
name of the target you want to execute (example: to compile
the sources, type "ant compile".

The following targets are available:

- init

 Creates the directories (under this directory) necessary
 to build this project.

 THIS TARGET MUST BE CALLED THE FIRST TIME THIS PROJECT 
 IS BUILT.

- compile

 Compiles the sources.

- test

 Runs the unit tests.

- dist
 
 Creates the library for this project (in the ./dist
 directory).

