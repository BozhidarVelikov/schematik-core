package org.schematik.api.test;

import org.schematik.api.annotation.Controller;
import org.schematik.api.annotation.parameter.PathParam;
import org.schematik.api.annotation.request.Get;

@Controller(endpoint = "/hello")
public class TestController {
    public TestController() {
        // Initialization code here
    }

    @Get
    public String get() {
        return "Hello World";
    }

    @Get(endpoint = "/{id}")
    public int getId(@PathParam int id) {
        return id;
    }
}
