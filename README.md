## Schematik Framework Core

Create Next.js app without prompts:
`npx create-next-app schematik-test-app --javascript --no-eslint --no-tailwind --src-dir --app --import-alias "@/*"`

### Table of contents
1. [What is Schematik Framework?](#what-is-schematik-framework)
2. [Plugins](#plugins)
3. [Web APIs](#web-apis)
   1. [Endpoints](#endpoints)
   2. [Security](#security)
4. [Scheduled Tasks](#scheduled-tasks)

### What is Schematik Framework?
Schematik is a framework for web application development. It is designed in a way to speed up development, reducing 
costs both for the client and the developer. The core package contains four main modules: plugins, web APIs,
queries, and scheduled tasks.

### Plugins
Plugins are single instances of different classes that implement the `ISchematikPlugin`interface. The interface 
contains a single method: `register()`, which will run as soon as an instance of the plugin is created. Plugins are
defined in the file `plugins.config.xml` with the `<plugin>` tag. It has four properties:
1. name - Serves as an id for the plugin that will let you access the instance of the 
class in case you need to. To access the plugin instance, you have to call 
`PluginConfig.getPluginImplementation(String)` method, which will return the plugin instance or `null` if a plugin with
the specified name is not instantiated.
2. class - The implementation class of the plugin. It should implement the `ISchematikPlugin` interface.
3. enabled - Can be `true` or `false`. Determines whether to create an instance of the plugin or not.
4. env - An optional property. If present, an instance of the plugin class will be instantiated only if the current
`env` property in `application.properties` is set to the value of the plugin `env` property. If omitted, the plugin 
will always be instantiated.

<i>Note: If you have multiple plugin tags with the same name property, only the first enabled one with an appropriate 
env property will be registered. The other ones will be skipped.</i>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<plugins>
    <plugin
            name="Hibernate"
            class="org.schematik.data.hibernate.HibernatePlugin"
            enabled="true"
            env="dev"
    />
</plugins>
```

### Web APIs
Schematik uses the notion of Controllers. In order to register a Controller, you need to annotate a class with the 
`@Controller` annotation. Doing so will allow the framework to detect it when the application is started and will 
register the endpoints defined in the class. The Controller annotation contains the following properties:
1. endpoint - Defines the root route for all the defined endpoints in the class. The default value for the property is
the empty string.

#### Endpoints
Defining an endpoint is pretty simple as well. The only thing that needs to be done is to annotate a method with one of
the possible HTTP method types:
1. Get - Use the `@Get` annotation to define a Get request.
2. Post - Use the `@Post` annotation to define a Post request.
3. Put - Use the `@Put` annotation to define a Put request.
4. Delete - Use the `@Delete` annotation to define a Delete request.
5. Patch - Use the `@Patch` annotation to define a Patch request.
6. Head - Use the `@Head` annotation to define a Head request.
7. Options - Use the `@Options` annotation to define a Options request.

All the endpoints have the same properties:
1. endpoint - Defines the route that follows after the root route defined by the Controller. The default value is the 
empty string.
2. roleClass - Schematik uses Javalin to define endpoints, which allows to associate an endpoint with a list of roles
that can be used to authorize users when they make a request to it. The default value is `DefaultUserRole.class`.
3. roles - The list of roles that protect the endpoint. By default, the list is empty.

An endpoint's method can return one of three things:
1. In case the method returns an entity, the response will be a json serialized version of the entity.
2. If you need more control over the response, your method can return a `ResponseEntity<?>` instance. For the moment, 
the response entity allows you set the status code of the response <i>(1)</i>.
3. In any other case, the value returned by the `toString()` method of the returned object will be sent in the response 
body.

<i>(1) I am currently working on response headers as well, but I am open to other suggestions as well.</i>

#### Security
By default, Schematik provides three default user roles defined in the enum `DefaultUserRole`. You can use this class as
an example, or you can use them as they are in the best way you find that fits your needs. The roles are `USER`,
`ADMIN`, and `GUEST`. 

In order to implement authorization for your route, you should create a plugin class that implements
`IRestApiAuthenticationPlugin` and register it under the name `WebserviceAuthentication`:

```xml
<plugin 
    name="WebserviceAuthentication" 
    class="org.schematik.api.ExampleRestApiAuthenticationPlugin" 
    enabled="true"
/>
```
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

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scheduled-tasks>
   <task class="org.schematik.scheduler.test.MyScheduledJob">
      <schedule cron="*/5 * * * * *"/>
      <schedule period="12000" initialDelay="1000" fixedRate="true"/>
   </task>
</scheduled-tasks>
```
<i>Note: You don't have to use both schedules to schedule a task. They are given here just as an example.</i>

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
