<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gimlet-project>
    <name>Example Gimlet project</name>
    <description>This is an example Gimlet project, aimed towards an HSQL in-memory database.</description>
    <aliases>
        <alias>
            <name>In memory HSQL database</name>
            <description>This is an in-memory HSQL database</description>
            <color>#CCCC33</color>
            <colorDisabled>false</colorDisabled>
            <url>jdbc:hsqldb:mem:example;shutdown=true</url>
            <driver-class>org.hsqldb.jdbc.JDBCDriver</driver-class>
            <user>SA</user>
            <password></password>
            <askForPassword>false</askForPassword>
            <readOnly>false</readOnly>
            <jdbcProperties/>
            <query>select * from customer</query>
        </alias>
    </aliases>
    <queries>
        <query>
            <name>Select all customers</name>
            <description>Starts by selecting all the customers</description>
            <color>#B31A1A</color>
            <colorDisabled>true</colorDisabled>
            <content>select * from customer</content>
            <queries>
                <query>
                    <name>Invoices for customer</name>
                    <description>Selects the invoices for the selected customer</description>
                    <color>#FFFFFF</color>
                    <colorDisabled>true</colorDisabled>
                    <content>select * from invoice where customerid = :ID</content>
                    <queries>
                        <query>
                            <name>Items with invoice</name>
                            <description>Gets all items for the invoice</description>
                            <color>#FFFFFF</color>
                            <colorDisabled>true</colorDisabled>
                            <content>select * from item where invoiceid = :ID</content>
                            <queries>
<query>
    <name>Product details</name>
    <description>Finds product details</description>
    <color>#99CC99</color>
    <colorDisabled>true</colorDisabled>
    <content>select * from product where ID = :PRODUCTID</content>
    <queries/>
    <referencedQueries/>
</query>
                            </queries>
                            <referencedQueries/>
                        </query>
                    </queries>
                    <referencedQueries/>
                </query>
            </queries>
            <referencedQueries/>
        </query>
        <query>
            <name>Customer by name</name>
            <description>Selects a customer by name</description>
            <color>#FFFF66</color>
            <colorDisabled>true</colorDisabled>
            <content>select * from customer where 
firstname like :firstname
and 
lastname like :lastname;</content>
            <queries/>
            <referencedQueries/>
        </query>
    </queries>
</gimlet-project>
