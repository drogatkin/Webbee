# Webbee
A Web application building blocks using Aldan3

## How it differs from other frameworks?
The WebBee gives an opportunity to use a server or a client side rendering including a hybrid approach.
It mostly utilizes native system rendering capabilities than artificially created ones as in the React.
It's an extremely light weight and flexible.

## Getting started
Most of RAD tools provide tons of documentation to help people use them. Therefore considering that using tools without reading all documentation
 hardly possible, the rapid aspect is very questionable. Webbee isn't an exception. However Webbee has certain benefits allowing starting a little faster.
  Therefore you won't find __Hello World__ example, instead of that you can create a new app in a matter of seconds.

### Application anatomy
Every application defines one instance of an Application Model object. Webbee provides access to this object from all other objects of the system.
 It provides from one side kind of singleton nature of the object, and from other side it isn't a singleton object. Next important component of the system
  is the front controller. It manages all external communications. Webbee defines only one servlet. All UI rendering is supported by MVC pattern
   with a local or remote model piece. And finally a set of background services as on demand or scheduled can be created. Webbee considers any web
    application as some composition of building blocks holding certain functionality. The following blocks are considered:

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
12. Around dozen more and the set keeps growing

### Create application
Switch to the build directory and run the script as below, and then answer new application wizard questions. 

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

### Major object types
Webbee distinguishes the following fundamental objects

1. data
2. UI service
3. backend service

Use `bee create` command in an application folder to create any of these objects.

## First time run

It is recommended to build Webbee instead of getting binaries. [7Bee](https://github.com/drogatkin/7Bee),
 [Aldan3 JDO](https://github.com/drogatkin/aldan3-jdo), and [Aldan3](https://github.com/drogatkin/aldan3) are prerequisites.
  [TJWS](https://github.com/drogatkin/TJWS2) is optional, however it is recommended instead of Tomcat.
   Make some Webbee suite directory and then checkout all mentioned projects there. It will be easy to reference each other just using relative path.

Switch to  Webbee build directory and copy __env.xml.mas__ to __env.xml__. Edit __env.xml__ as below

``` xml
 <!-- useful for quick run without app server like Tomcat -->
  <variable name="TJWS_HOME" type="path">../../TJWS2/1.x</variable>

  <!-- Aldan3 library home  -->
  <variable name="ALDAN3_HOME" type="path">../../aldan3</variable>
  ```
  
  Internet access is required to pull other dependencies from Maven repository. However the local copy of the dependencies can be used in case of 
  offline usage. Note that Internet access is required only for filling the dependencies cache.
  
  Use `bee` to build Webbee. Now you are ready to use `bee create` to start building your first project

### My library - my first Webbee project
  
  Consider creating a simple project to manage a personal library. 
  
 ```
[dmitriy@fedora-box build]$ bee create
Welcome to a new project creation wizard
Tips: use only a single word in lower case letters for answers
Enter Webbee suite root directory [../..]?
Enter project's name [test]?library
Enter your organization name [acme]?webbee
Enter project's domain [com]?org
Provide database type used, one of h2, ora, my, hado, none [ora]?h2
Does the application require user authentication [y]?n
[dmitriy@fedora-box build]$ 
```

Now your project is ready and you can even build it and deploy. Switch to *../../library* directory. However before of a building and deploying, 
let add some functionality. Since it is a library, an object as *book* sounds reasonable. To create the object issue `bee create` again, 
but in the project directory.

```
[dmitriy@fedora-box library]$ bee create
Welcome to Webbee component creation wizard
Enter type of component d - for JDO, u - for web, s - for service [d]? d
Enter name of component? book
[dmitriy@fedora-box library]$
```
Well, perhaps just creation of data object without a way to fill it with some data isn't much fun. Let's create a simple form around it.
 Again issue `bee create`, and select UI component in this time.

```
[dmitriy@fedora-box library]$ bee create
Welcome to Webbee component creation wizard
Enter type of component d - for JDO, u - for web, s - for service [d]? u
Enter name of component? book
Enter UX block type f - Form, t - Tabular, q - SQLTabular, s - Schema setup? f
Enter based on JDO name if any [DataObject]? book
[dmitriy@fedora-box library]$
```

Note there is no conflict between JDO name and UI component name, however you have to specify _book_ as an object name for __book__ form.
 Now you can deploy 
your first Webbee web application. Execute `bee deploy`, simple, isn't it? 
if your TJWS isn't running yet, then launch it in app server mode (`bee runapp`).

Reach your application pointing a browser to `http://fedora-box:8080/library/`

You need to initialize your H2 database schema first, therefore click __Prepare schema__ first, check content of the schema initialization form and submit. 
Now you are ready to test your first form _book_, issue URL `http://fedora-box:8080/library/webbee/Book`. you can fill out the form and submit it to get
 your first book record created. 

## What is next

You already have your application created in matter of one minute, however it doesn't do all functionality you may plan for it. 
Adding the functionality isn't so complicated. Start with adding new fields in your book JDO. 




   