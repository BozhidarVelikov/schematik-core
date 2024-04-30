package org.schematik.api;

import io.javalin.http.Context;

public class TestController implements Controller {
    public TestController() {
        // Initialization code here
    }

    public void get(Context context) {
        context.result("Hello World");
    }

    public void getId(Context context) {
        context.result(context.pathParam("id"));
    }
}
