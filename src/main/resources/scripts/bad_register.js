function script_register() {
    return 2; // ERR: should return JSON object.
}

function script_execute() {
    java.lang.System.out.println("This is the execution of an example script.");
}