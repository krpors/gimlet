function script_register() {
    return {
        name: "Example script",
        description: "Example script description",
        author: "Herp A. Derp"
    };
}

function script_execute() {
    // The test binds 'someVar' to 123.
    return someVar * 8;
}