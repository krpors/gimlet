module gimlet {
    requires java.base;
    requires java.scripting;
    requires java.xml;
    requires java.xml.bind;
    requires java.sql;
    requires java.sql.rowset;

    requires guava;
    requires logback.classic;
    requires logback.core;
    requires slf4j.api;
    requires com.sun.xml.bind;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    // Third party libs require us to export certain packages for introspection.
    exports cruft.wtf.gimlet;
    exports cruft.wtf.gimlet.ui;
    exports cruft.wtf.gimlet.util;

    // Some opens directives for Guava as well.
    opens cruft.wtf.gimlet;
    opens cruft.wtf.gimlet.conf to java.xml.bind;
}