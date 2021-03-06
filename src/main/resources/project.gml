<?xml version="1.0" encoding="UTF-8" ?>
<gimlet-project>
    <name>LegacySystem_X</name>
    <description>This project contains drilldown queries for project x.</description>
    <aliases>
        <name>Alias name</name>
        <description>Alias description</description>
        <alias>
            <name>Test env</name>
            <description>Something something pride and accomplishment</description>
            <url>jdbc:hsqldb:hsql://localhost/</url>
            <driver-class>org.hsqldb.jdbc.JDBCDriver</driver-class>
            <user>SA</user>
            <password></password>
        </alias>
        <alias>
            <name>Acceptance env</name>
            <description>Something something pride and accomplishment</description>
            <url>jdbc:oracle:thin:@acc-host:1521:SOMESID</url>
            <user>derpington</user>
            <password>boatymcboatface</password>
        </alias>
    </aliases>

    <queries>
        <!-- Queries is repeated. A file can contain multiple queries, which can contain itself.
             In other words, it's a tree of queries. -->
        <query>
            <name>Select all customers</name>
            <description>This query selects all the customers</description>
            <content>select * from customer;</content>
            <queries>
                <query>
                    <name>Find the invoices for the customer.</name>
                    <content>select * from invoice where customerid = :ID</content>
                    <queries>
                        <query>
                            <name>Get items belonging to invoice</name>
                            <content>select * from item where invoiceid = :ID</content>
                            <queries>
                                <query>
                                    <name>Get product item</name>
                                    <content>select * from product where id = :PRODUCTID</content>
                                    <queries/>
                                </query>
                            </queries>
                        </query>
                        <query>
                            <name>Get items and their product names for invoice</name>
                            <content>select * from item inner join product on (product.id = item.productid) where
                                item.invoiceid = :ID
                            </content>
                            <queries/>
                        </query>
                    </queries>
                </query>
            </queries>
        </query>
    </queries>

</gimlet-project>


