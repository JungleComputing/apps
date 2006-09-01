This directory contains a collection of Ibis example programs, organized
into a number of sub-directories.

gmi             Programs using the gmi framework.
ibis            Programs using the Ibis communication layer directly.
io              Some benchmarks for the communication layer.
repmi           Programs using the repmi framework.
rmi             Programs using Java RMI as communication mechanism.
satin           Programs using the Satin divide-and-conquer framework.
extra           Unclassified example programs.

build.xml	Ant build file for building Ibis applications.
		"ant build" (or simply: "ant") will build all applications that
		are present in your Ibis installation. "ant clean" will remove
		what "ant build" made. You can also build individual
		applications by going to its directory and running "ant"
		there.

Note: To compile these Ibis applications, you need at least version 1.6.1
of ant. Ant is available from http://ant.apache.org.
You also need to set the JAVA_HOME environment variable to the root
directory of your Java installation, so if your Java lives in
"/usr/java/bin/java", set JAVA_HOME to "/usr/java".
You also need to set the IBIS_HOME environment variable to where
the Ibis tree lives on your system.

System-specific notes

Mac OS X

    Set the environment variable JAVA_HOME to "/Library/Java/Home".
    You are required to install the Java SDK.

Windows 2000, Windows XP

    Install a recent Java SDK, at least 1.4, and preferably 1.5, because
    1.4 seems to have some problems. This will get installed in
    for instance "c:\Program Files\Java\jdk1.5.0". You can set the
    JAVA_HOME environment variable to this path by going to the
    Control Panel, System, the "Advanced" tab, Environment variables,
    add it there and reboot your system.

Cygwin

    Set the environment variable JAVA_HOME to wherever it is installed
    on your windows system. You can do that as described above, or do
    it the "Unix" way. If you choose the Unix way, use quoting, so,
    for instance,

	export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.5.0"

	or

	export JAVA_HOME="C:\Program Files\Java\jdk1.5.0"
