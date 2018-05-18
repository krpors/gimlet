# Gimlet

GImlet is an attempt to make a useful query application. Users can create their
own predefined (parameterized) queries to run against a database. Following the
result set of that query, you can 'drill down' further into other tables by
effectively executing a sub-query, based on the resultset of the previous query.
In a sense, it's a lazy query evaluator.

#### Screenshots

Screenshots speak louder when it comes to user interfaces:

![Get an overview of all database objects.](https://i.imgur.com/AbcRMKh.png)
👆 Loading database objects from a database. Red rows are the primary keys. Tabs can be
given an explicit color as a hint of what database/environment you are connected
to.

![Plain SQL](https://i.imgur.com/2A2nnbi.png)
👆 Plain SQL can be entered in the SQL tab. Selected rows can be copied into the
clipboard.

![Drilldown tab](https://i.imgur.com/II4ThcY.png)
👆 Execute a configured child query, based on results from the previous (parent) query.

![Query configuration](https://i.imgur.com/hPkhugT.png)
👆 This screenshot shows the relationship between named parameters (`:ID`) and how the
query is completed using the result from the second row in the table (`13`). The actual
query which is executed by Gimlet will be

```sql
select * from item where invoiceid = 13
```

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

Create `.tar.gz` and `.zip` distribution:

    mvn clean package assembly:single

In the `target` directory a file will be created, called `gimlet-$VERSION.tar.gz`.
The contents are as follows:

    gimlet-$VERSION
		README.md       <-- this file you're reading
		LICENSE         <-- license file
		/bin/
			gimlet.sh   <-- shell script for Linux
			gimlet.bat  <-- batch file for Windows
			gimletw.bat <-- batch file for Windows, without console
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

# Running from Maven

To directly run from within Maven, just run:

    mvn exec:java

# JDBC driver installation

The only thing required is that the necessary JARs are to be put on the classpath.
If you have a driver JAR, for example `ojdbc6.jar`, just drop it in the `./lib/`
folder. When Gimlet is run the next time (using the run scripts), all JARs are
added to the classpath, thus loading the JDBC driver automatically.
In the Alias editor dialog, the combobox *should* then list all available drivers.

# Parameter formatting

There are multiple ways to configure parameters in a formatted query, and can be
of influence for user input. Depending on the configuration, a `PreparedStatement`
receives a different type.

Parameter format | Parsing notes | UI element
---------------- | ------------- | ----------
`:ID` | Basic parameter input. Result will be given as a `java.lang.String` | Text field
`:ID[STRING]` | Same as `:ID`, but more explicit. Rather redundant. | Text field
`:ID[NUMBER]` | The given parameter will be given as a `java.lang.Number` into the statement. | Number text field
`:ID[DATE]` | A `java.sql.Date` is used. | Date picker
`:ID[DATETIME]` | A `java.sql.Timestamp` is used. | Date/time picker

For example, if a query is configured as follows:

```sql
select * from some_table
where
    id         = :someId[NUMBER]
and start_date > :startDate[DATE]
and click_time < :clicketyTime[DATETIME]
and user       = :userName[STRING]
```

and the query is directly executed, you are prompted with this screen:

![image](http://i.imgur.com/bdV4fYQ.png)

# TODOs and ideas

A list of things to be done, or some ideas. Not in any particular order.

#### Milestones for 1.1 release

1. Recent queries: save them in different file.
1. Multiple select, and multiple sub-query execution. For instance, select 4 rows, right
   click and then select a sub query. This will result in 4 new tabs with results.

#### Nice to haves

1. Add some kind of list of known JDBC drivers + connection strings. This eases up the
   initial setup of a JDBC connection.
1. Re-use (make a reference to) a query structure so you can make sort of
duplicates, where you make a reference to another query structure for the
subqueries.
1. Help file/screen.

1. Query wizard or something: start by a root query, execute sample query, then hit next
to configure the next query, until finished, then a query tree is a result.
1. Easier column selector (hide all/show all)
1. Syntax highlighter?

#### Code cleanups

1. Fix the way the `QueryTree` and the backed `Query` object tree work?
1. Moar Javadoc.

# Notes

These are some notes about things used to help development of certain features.

[Mimic network latency](https://wiki.linuxfoundation.org/networking/netem) using NetEm (`man netem`),
using the `tc` utility. This can be used to test connections such as longer roundtrips, connection
timeouts, interruptions, etc.

[Icons](https://materialdesignicons.com/) used can be found here.

Mimic network cutoffs by using `socat` as a TCP proxy between Gimlet and
the target database host, for example

    socat -x -v tcp4-listen:1521,bind=127.0.0.1,reuseaddr,fork tcp4:targetmachine:1521 2> lol.txt

Then establish a connection to `localhost:1521` and interrupt using `^C`.
