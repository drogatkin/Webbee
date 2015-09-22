# Webbee
Web application building blocks Aldan3 based

# Getting started
Most of RAD tools provide tons of documentation to help people use them. Therefore considering that using tools without reading all documentation hardly possible, rapid aspect is very questionable. Webbee isn't an exception. However Webbee has certain benefits allowing starting a little faster. Therefore you won't find __Hello World__ example, instead of that you can create a new app in matter of seconds.

## Application anatomy
Every application defines one instance of Application Model object. Webbee provides access to this object from all other objects of the system. It provides from one side kind of singleton nature of the object, and from other side it isn't a singleton object. Next important component of the system is a front controller. It manages all external communications. Webbee defines only one servlet. All UI rendering is supported by MVC pattern with local or remote model piece. And finally a set of background services as on demand as scheduled can be created. Webbee considers any web application as some composition of building blocks holding certain functionality. The following blocks are considered:

1. Form
2. Tabular view
3. Menu
4. Fast access menu
5. Grid
6. Gadget
7. Chat
8. Portal
9. Tree navigator
10. Bar code
12. Around dozen more and set keeps growing

### Create application
Slip to a build directory and run script as below, and then answer new application wizard questions. 

```
>bee create
  Welcome to new project creation wizard
Enter projects root directory [../..]?
Enter projects name [test]?vega
Enter your organization name [acme]?drogatkin
Enter projects domain [com]?org
Database type used, one of h2, ora, my, hado [ora]?h2
```

Your application is ready now, you can switch to its directory and run `bee` to build a .war. 

## Major object types
Webbee distinguishes the following fundamental objects

1. data
2. UI service
3. backend service

Use `bee create` command in an application folder to create any of these objects.

## First time run

It is recommended to build Webbee instead of getting binaries. [7Bee](https://github.com/drogatkin/7Bee), [Aldan3 JDO](https://github.com/drogatkin/aldan3-jdo), and [Aldan3](https://github.com/drogatkin/aldan3) are prerequisites. [TJWS](https://github.com/drogatkin/TJWS2) is optional, however it is recommended instead of Tomcat. Make some Webbee suite directory and then checkout all mentioned projects there. It will be easy to reference each other just using relative path.

Slip in Webbee build directory and copy __env.xml.mas__ to __env.xml__. Edit __env.xml__ as below

``` xml
 <!-- useful for quick run without app server like Tomcat -->
  <variable name="TJWS_HOME" type="path">../../TJWS2/1.x</variable>

  <!-- Aldan3 library home  -->
  <variable name="ALDAN3_HOME" type="path">../../aldan3</variable>
  ```
  
  Internet access is required to pull other dependencies from Maven repository. However local copy of the dependencies can be used in case of offline usage. Note that Internet access is required only for filling dependencies cache.
  
  Use `bee` to build Webbee. Now you are ready to use `bee create` to start building your first project
  
  ### My library - my first Webbee project
  
  Consider creating a simple project to manage a personal library. 
  
   