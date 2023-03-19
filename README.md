# andre-mySpring-SpringMVC
Based on the original Spring and SpringMVC, I have used Annotation and Reflection to implment a simplified Spring Framework and use JAVA servlet to implement the Spring MVC Framework


# Relationship between Spring and Spring MVC
SpringMVC is a WEB layer framework that uses the spring controllers for url mapping, view Resolver and data format returned to the user, and supports the MVC development mode. In essence, the core of SpringMVC is still servlet from traditional JAVAWEB.

Spring framework on the other hand uses JAVA SE Annotation and Reflection to implement its dependecy injection(IOC), it also use proxy object and InvocationHandler() to implement AOP. Allowing decoupling and faster development. 

Therefore this project  will use JAVA Annotation and Reflection and servlet to implment our own SPRING and SPRINGMVC Framework. To help build a strong understanding on why these annotations and mechanism work at the source code level.

The two diagrams below will show how I will implment Spring and SpringMVC without using the related libraries：

# My Spring  Implementation
![SPRING SPRINGMVC - SPRING FRAMEWORK](https://user-images.githubusercontent.com/110853339/226154188-8b22c759-b119-47fc-aa87-b0b0d74163e3.jpeg)



# My SpringMVC Implementation
![SPRING SPRINGMVC - SPRING MVC FRAMEWORK](https://user-images.githubusercontent.com/110853339/226154466-66664d97-d9e1-45fa-9c86-3cf6dd1fdd4a.jpeg)





