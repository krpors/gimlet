module gimlet {
    requires java.base;
    requires java.scripting;
    requires java.xml;
    requires java.sql;
    requires java.sql.rowset;

    requires com.google.common;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires jakarta.activation;
    requires jakarta.xml.bind;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    // Third party libs require us to export certain packages for introspection.
    exports cruft.wtf.gimlet;
    exports cruft.wtf.gimlet.ui;
    exports cruft.wtf.gimlet.util;

    // Some opens directives for Guava as well.
    opens cruft.wtf.gimlet;
    opens cruft.wtf.gimlet.conf to jakarta.xml.bind;


}