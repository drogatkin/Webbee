# Webbee
Web application building blocks Aldan3 based

# Getting started
Most of RAD tools provide tons of documentation to help people use them. Therefore considering that using tools without reading all documentation hardly possible, rapid aspect is very questionable. Webbee isn't an exception. However Webbee has certain benefits allowing starting a little faster. Therefore you won't find __Hello World__ example, instead of that you can create a new app in matter of seconds.

## Application anatomy
Every application defines one instance of Application Model object. Webbee provides access to this object from all other objects of the system. It provides from one side kind of singleton nature of the object, and from other side it isn't a singleton object. Next important component of the system is a front controller. It manages all external communications. Webbee defines only one servlet. All UI rendering is supported by MVC pattern with local or remote model piece. And finally a set of background services as on demand as scheduled can be created.

### Create application
Slip to build directory and run script as below, and then answer new application wizard questions. 

~~~
  >bee create<br/>
  Welcome to new project creation wizard<br/>
Enter projects root directory [../..]?<br/>
Enter projects name [test]?vega<br/>
Enter your organization name [acme]?drogatkin<br/>
Enter projects domain [com]?org<br/>
Database type used, one of h2, ora, my, hado [ora]?h2<br/>
~~~

Your application is ready now, you can switch to it directory and run -bee- script to build its .war. 

## Major object types
Webbee distinguishes the following fundamental objects

1. data
2. UI service
3. backend service

Use bee create command in an application forlder to create any of these objects.