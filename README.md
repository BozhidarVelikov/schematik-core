## Schematik Framework Core

### Table of contents
1. [What is Schematik Framework?](#what-is-schematik-framework)
2. [Plugins](#plugins)
3. [Web APIs](#web-apis)
   1. [Authentication](#authentication)
4. [Scheduled Tasks](#scheduled-tasks)
5. [Examples](#examples)
   1. [Plugin Example](#plugin-example)
   2. [Webservice Example](#webservice-example)
   3. [Scheduled Task Example](#scheduled-task-example)

### What is Schematik Framework?
Schematik is a framework for web application development. It is designed in a way to 
speed up development, reducing costs both for the client and the developer. The core 
package contains three main modules: plugins, web APIs, and scheduled tasks.

### Plugins
Plugins are single instances of different classes that implement the `ISchematikPlugin`
interface. The interface contains a single method: `register()`, which will run as soon as
an instance of the plugin is created. Plugins are defined in the file `plugins.config.xml` with the `<plugin>`
tag. It has four properties:
1. name - Serves as an id for the plugin that will let you access the instance of the 
class in case you need to. To access the plugin instance, you have to call 
`PluginConfig.getPluginImplementation(String)` method, which will return `null` if a
plugin with the specified id is not instantiated.
2. class - The implementation class of the plugin. It should implement the `ISchematikPlugin`
interface.
3. enabled - Can be true or false. Determines whether to create an instance of the 
plugin or not.
4. env - An optional property. If present, an instance of the plugin class will be 
instantiated only if the current `env` property in `application.properties` is set to
the value of the plugin `env` property. If omitted, the plugin will always be instantiated.

<i>Note: If you have multiple plugin tags with the same name property, only the first enabled one
with an appropriate env property will be registered. The other ones will be skipped.</i>

### Web APIs
Creating an API endpoint is done by adding two things: a yaml file that describes the
endpoint and registering this yaml file in the configuration file `api.config.xml`.

The yaml file should be added to the `resources/api` folder. It should contain an OpenAPI
description of the endpoints.

Registering the webservice in the xml file is done using the `<webservice>` tag. It
has two properties: descriptor, which points to the yaml file from above, and class,
which specifies the class in which the implementation of the webservice is written.

#### Authentication
By default, the Schematik core package supports only basic authentication. It has
three default roles: `ANY`, `USER`, and `ADMIN`.

1. The `ANY` role allows everyone to access the endpoint. This is the same as leaving
the endpoint unsecured.
2. The `USER` role allows everyone that is authenticated to access the endpoints up to
this security level.
3. The `ADMIN` role also requires authentication and can access any endpoint.

The roles can be extended by creating a custom enum that implements RouteRole (similar
to the [DefaultRestApiRole]() enum).

Adding basic authentication to an endpoint is done with the custom parameters 
`x-schematik-security` and `x-schematik-roles`:

```yaml
paths:
  /hello/get:
    get:
      ...
      x-schematik-security:
        x-schematik-roles:
          - USER
          - ADMIN
```

The authentication itself is done by a plugin that implements the `IRestApiAuthenticationPlugin`
interface. A default implementation is provided with the class `DefaultRestApiAuthenticationPlugin`
and uses the default roles and users registered in the file `api/credentials.xml`.

### Scheduled Tasks
Scheduled tasks can be used for processes that need to run at a specific time. In order to
create a scheduled task, you need to implement the interface `IScheduledTask` and register the 
task in `scheduler.config.xml`.

The `IScheduledTask` interface contains three methods: `beforeJob()`, `doJob()`, and
`afterJob()`. The idea is that beforeJob() will be called to initialize any data needed
for the task, doJob() will contain the actual task logic, and afterJob() will contain
any clean-up code.

As mentioned above, the task should also be registered in `scheduler.config.xml`. This is
done with the `<task>` tag. The class tag contains a `class` property, pointing to the
class that implements the task. It also contains one or more `<schedule>` tags which
describe when to run the task. Scheduling the task can be done in two ways:
1. Using a cron expression.
2. Using the following three properties:
   1. `period` - The field specifies how often the task runs. The number is in milliseconds.
   This field is compulsory if not using a cron expression.
   2. `initialDelay` - The field specifies a delay in milliseconds before the first period
   of the task starts. This field is optional and a default value of 0 is used if not present.
   3. `fixedRate` - If set to true, the task will run multiple times. If set to false,
   the task won't be rescheduled after the first run. This field is optional and a default 
   value of true is used if not present.

### Examples
#### Plugin Example
You can see an example of a `plugins.config.xml` file at [the bottom of the
Webservice Example]() section. An example of a plugin implementation class is
[DefaultRestApiAuthenticationPlugin]().

#### Webservice Example
The example shows a webservice hello, which has two endpoints: `/hello/get`, which returns
"Hello world!" and `/hello/get/{id}`, which returns the passed id.

#### api.config.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<webservices>
    <webservice descriptor="hello.yml" class="org.schematik.api.TestController"/>
</webservices>
```
#### hello.yml
```yaml
openapi: 3.0.0
info:
  title: Test API Service
  version: 1.0.0
  description: A dummy example API
  contact:
    name: Bozhidar Velikov
    email: bojidarvelikov2@gmail.com
paths:
  /hello/get:
    get:
      summary: Get a greeting
      tags:
        - hello
      operationId: get
      security:
        - basicAuth: []
      responses:
        '200':
          description: Successful greeting
          content:
            text/plain:
              schema:
                type: string
        '401':
          $ref: '#components/responses/UnauthorizedError'
      x-schematik-security:
        x-schematik-roles:
          - USER
          - ADMIN
  /hello/get/{id}:
    get:
      summary: Get the passed id
      tags:
        - hello
      operationId: getId
      security:
        - basicAuth: []
      parameters:
        - in: path
          name: id
          schema:
            type: integer
          required: true
          description: The number that is going to be returned
      responses:
        '200':
          description: Successful operation
          content:
            text/plain:
              schema:
                type: string
        '401':
          $ref: '#components/responses/UnauthorizedError'
      x-schematik-security:
        x-schematik-roles:
          - ADMIN
components:
  responses:
    UnauthorizedError:
      description: Authentication information is missing or invalid
      headers:
        WWW_Authenticate:
          schema:
            type: string
  securitySchemes:
    basicAuth:
      type: http
      scheme: basic
security:
  - basicAuth: []
x-schematik-security:
  x-schematik-roles:
    - ANY
```
#### TestController.java
```java
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
```
#### credentials.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<users>
    <user>
        <username>admin</username>
        <password>admin</password>
        <roles>
            <role>ADMIN</role>
        </roles>
    </user>

    <user>
        <username>user</username>
        <password>pass</password>
        <roles>
            <role>USER</role>
        </roles>
    </user>
</users>
```
#### plugins.config.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<plugins>
    <plugin
            name="WebserviceAuthentication"
            class="org.schematik.api.DefaultRestApiAuthenticationPlugin"
            enabled="true"
            env="dev"
    />
</plugins>
```
#### Scheduled Task Example
The example shows a dummy task that logs the name of the method and prints the name of 
the thread on which the task is running.
#### scheduler.config.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<scheduled-tasks>
    <task class="org.schematik.scheduler.MyScheduledTask">
        <schedule cron="*/5 * * * * *"/>
        <schedule period="12000" initialDelay="1000" fixedRate="true"/>
    </task>
</scheduled-tasks>
```

#### MyScheduledTask.java
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyScheduledTask implements IScheduledTask {
    Logger logger = LoggerFactory.getLogger(MyScheduledTask.class);

    @Override
    public void beforeJob() {
        logger.info("beforeJob finished");
    }

    @Override
    public void doJob() {
        logger.info("Current task is running on thread [" + Thread.currentThread().getName() + "].");
        logger.info("doJob finished");
    }

    @Override
    public void afterJob() {
        logger.info("afterJob finished");
    }
}
```