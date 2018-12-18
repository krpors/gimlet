module gimlet {
    requires java.base;
    requires guava;
    requires java.scripting;
    requires java.xml;
    requires java.xml.bind;
    requires java.sql;
    requires java.sql.rowset;

    requires logback.classic;
    requires logback.core;
    requires slf4j.api;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    requires error.prone.annotations;
    requires jsr305;
}