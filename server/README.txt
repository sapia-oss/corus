THIS PROJECT IS MADE PUBLIC UNDER  THE SAPIA-OSS
LICENSE. SEE LICENSE.txt THAT COMES  PACKAGED IN 
THE ROOT OF THE DISTRIBUTION OR GO TO 

http://www.sapia-oss.org/license.html 



-------------------------------------------------------------

                         C O R U S

-------------------------------------------------------------



CREDITS
=======

This product includes software developed  by the  
Apache Software Foundation  (http://www.apache.org/).

Other third-party software:

- JDom (www.jdom.org)
- Jug  (http://www.doomdark.org/doomdark/proj/jug/)

And, of course, a bunch of libs from Sapia ;-)


INSTALLATION
============

1. Download

Download the distribution from SourceForge 
(http://www.sourceforge.net/projects/sapia). The download
link appears in the "Files" section; the latest "corus"
release should be downloaded.


2. Extract

Extract the distribution under a directory of your choice
on the target computer.

3. Set-up Environment

The library itself can be found under the ./dist directory
of the extraction directory. The project requires the libraries
under the ./lib directory as dependencies.

In addition:

set CORUS_HOME as an environment variable; append CORUS_HOME/bin
to your PATH env. variable.


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


TO START THE CORUS SERVER
==========================

Type: corus

You should see help appear on the screen; read it and
go...

TO START THE CORUS COMMAND-LINE CLIENT
=======================================

Type: coruscli

Do as shown in the help to connect to the corus server you
have started.

To PLAY WITH THE DEMOS
======================

Corus comes with a distribution that is ready for deploy-
ment into a Corus server. The demo distribution (demoDist.jar)
is generated under the 'dist' directory. 

The corresponding sources are under:

java/src/org/sapia/corus/examples


The corus.xml deployment descriptor is under:

etc/demoApp/META-INF

The demoDist.jar file contains the compiled sources, as well
as the files under etc/demoApp.

To see Corus in action, go to the corus installation directory
(CORUS_HOME) and proceed to the following:

1- Start a corus server: corus -d test

2- Start a corus client: coruscli -h localhost -p 33000

3- From the client, type: deploy dist/demoDist.jar 
   (for this to work as is, you must have started the cli
    from under your CORUS_HOME - the deploy command either
    takes an absolute path, or a path relative to the location
    from where you have started your client).

4- Type "ls" in the client to see the currently deployed 
   distributions.

Now, we are ready to start VMs. The demo distribution comes
with two applications: one that does nothing (used to illustrate
and test corus interactions), and one that requests a restart
at a predefined interval. These apps are respectively named: 
"noopApp" and "restartApp" in the corus.xml file.

We will start the "noopApp":

5- In the client, type: exec -d demo -v 1.0 -n noopApp -p test

6- Go to the corus server window; check its activity - you should
   see that it is starting a VM. Eventually, you will see log
   messages indicating that the VM is polling the corus server.

7- In the client, type: ps. You should see information about
   the currently running VM. In the "Dyn. PID" column, you
   see the identifier that is assigned to the VM by Corus.

8- In the client, type: status. You should see status information
   about the currently running VM.

9- In the client, type: kill -i <identifier>
   (where <identifier> is the above-mentioned VM identifier).

10- Go to the corus server's window and wait a few seconds; you 
   should see the "kill" activity appearing. Eventually, you
   should see that the process is effectively killed.

That's it, you have started and stopped a VM with Corus. Play
around a bit, type "man" in the client to have a list of avai-
lable commands. Read their descriptions; they will give you
an idea about Corus's capabilities.

Have fun.