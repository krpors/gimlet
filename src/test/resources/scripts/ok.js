function script_register() {
    return {
        name: "Example script",
        description: "Example script description",
        author: "Herp A. Derp"
    };
}

/**
 * Show a dialog using Gimlet's Java classes. The code is commented
 * out, or else the unit test will fail regarding JavaFX's toolkit
 * initialization.
 */
function showdlg() {
    // var util = Java.type("cruft.wtf.gimlet.Utils")
    // util.showInfo("Hello", "World!");
}

/**
 * This is an example script which can function as a basic 'hello world'.
 */
function script_execute() {
    java.lang.System.out.println("This is the execution of an example script.");

    showdlg();

    return "OK"; // not necessary, but used to assert
}