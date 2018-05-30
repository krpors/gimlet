function script_register() {
    return {
        name: "Example script",
        description: "Example script description",
        author: "Herp A. Derp"
    };
}

function script_execute() {
    java.lang.System.out.println("This is the execution of an example script.");
    return "OK"; // not necessary, but used to assert
}