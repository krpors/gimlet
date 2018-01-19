# Gimlet

GImlet is an attempt to make a useful query application. Users can create their
own predefined (parameterized) queries to run against a database. Following the
result set of that query, you can 'drill down' further into other tables by
effectively executing a sub-query, based on the resultset of the previous query.
In a sense, it's a lazy query evaluator.

## But why not just use JOINs?

Joins are useful of course, but fail to display data properly when you have to
find specific information when there is more than 1 join. You could get double
data and things. Also, when there are N:M relations between tables, a drill-down
functionality might come in handy. This tool attempts to make that drill down
configurable.

## Example

Given two tables:

    CUSTOMER
    -------------------------
    ID    NAME    LASTNAME
    1     A       Einstein
    2     T       Raadt, de
    3     G       Rossum, van
    4     P       Fry


    INVOICE
    -------------------------
    ID    CUSTOMER_ID
    1     1
    2     1
    3     1
    4     3
    5     3
    6     4

In Gimlet we can now define the following query tree:

1. Select all customers: `select * from CUSTOMER`
   1. Select invoices for customer: `select * from INVOICE where CUSTOMER_ID = :ID`

The `:ID` is crucial here: this is input from the column `ID` from the previous query.

# Compiling and packaging

Java 8 is required. Maven must be used to build the sources and optionally build a
distribution (work in progress):

Just compile:

    mvn clean install

Create .tar.gz distribution:

    mvn clean package assembly:single

In the `target` directory a file will be created, called `gimlet-1.0-SNAPSHOT.tar.gz`.
The contents are as follows:

    gimlet-1.0-SNAPSHOT
		README.md       <-- this file you're reading
		/bin/
			gimlet.sh   <-- shell script for Linux
			gimlet.bat  <-- batch file for Windows
		/lib/
			error_prone_annotations-2.0.18.jar
			hsqldb-2.4.0.jar
			animal-sniffer-annotations-1.14.jar
			j2objc-annotations-1.1.jar
			gimlet-1.0-SNAPSHOT.jar
			jsr305-1.3.9.jar
			slf4j-api-1.7.25.jar
			logback-core-1.2.3.jar
			guava-22.0.jar
			logback-classic-1.2.3.jar
			... drop any JDBC driver JARs here ...

# JDBC driver installation

The only thing required is that the necessary JARs are to be put on the classpath.
If you have a driver JAR, for example `ojdbc6.jar`, just drop it in the `./lib/`
folder. When Gimlet is run the next time (using the run scripts), all JARs are
added to the classpath, thus loading the JDBC driver automatically.
In the Alias editor dialog, the combobox *should* then list all available drivers.

# TODOs and ideas

A list of things to be done, or some ideas. Not in any particular order.

1. Fix the way the `QueryTree` and the backed `Query` object tree works?
1. Re-use (make a reference to) a query structure so you can make sort of
duplicates, where you make a reference to another query structure for the
subqueries.
1. Add some kind of list of known JDBC drivers + connection strings.
1. Splash screen with some funky graphic.
1. Help file/screen.
1. New project.
1. ~~Save project as.~~
1. ~~Drag/drop queries to a new parent.~~
1. Drag/drop (or reorder by button or context menu) of aliases.
1. Query wizard or something: start by a root query, execute sample query, then hit next
to configure the next query, until finished, then a query tree is a result.
1. ~~Tab coloring so difference in environment can be made more explicit.~~
1. Ask for password checkbox.
1. "Rotated table" view. Handy with many columns.
1. Easier column selector (hide all/show all)
1. Query/alias editor window doesn't resize the textarea when resized, so fix that.
1. Syntax highlighter?
1. Preferences window.
    1. Preference option: column content truncate size


# Other things

Mimic network latency: https://wiki.linuxfoundation.org/networking/netem

