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
1. Save project as.
1. Drag/drop queries to a new parent.
1. Drag/drop (or reorder by button or context menu) of aliases.
1. Query wizard or something: start by a root query, execute sample query, then hit next
to configure the next query, until finished, then a query tree is a result.
1. ~~Tab coloring so difference in environment can be made more explicit.~~
1. Ask for password checkbox.


# Other things

Mimic network latency: https://wiki.linuxfoundation.org/networking/netem

