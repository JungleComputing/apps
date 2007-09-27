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

Linux, Solaris, other Unix systems
    Install a recent Java SDK, at least 1.4, and set the JAVA_HOME
    environment variable to the location where it is installed,
    for example
        export JAVA_HOME=/usr/local/java/jdk1.4
    or
        set JAVA_HOME=/usr/local/java/jdk1.4
    for CSH users.
    It is probably best to add this to your .bash_profile, .profile,
    or .cshrc file (whichever gets executed when you log in to your
    system).

Mac OS X
    Set the environment variable JAVA_HOME to "/Library/Java/Home".
    You are required to install the Java SDK. See the Linux notes on
    how to set environment variables.

Windows 2000, Windows XP
    Install a recent Java SDK, at least 1.4, and preferably 1.5, because
    1.4 seems to have some problems. This will get installed in
    for instance "c:\Program Files\Java\jdk1.5.0". You can set the
    JAVA_HOME environment variable to this path by going to the
    Control Panel, System, the "Advanced" tab, Environment variables,
    add it there and reboot your system. IBIS_HOME can be set
    similarly.

Cygwin
    See the notes on Windows 2000, Windows XP.

This product includes software developed by the
Ant-Contrib project (http://sourceforge.net/projects/ant-contrib).
See "notices/LICENSE.ant-contrib.txt" for the ant-contrib copyright notice.
