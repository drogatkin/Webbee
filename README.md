# Webbee
Web application building blocks Aldan3 based

# Getting started
Most of RAD tools provide tons of documentation to help people use them. Therefore considering that using tools without reading all documentation hardly possible, rapid aspect is very questionable. Webbee isn't an exception. However Webbee has certain benefits allowing starting a little faster. Therefore you won't find __Hello World__ example, instead of that you can create a new app in matter of seconds.

## Application anatomy


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