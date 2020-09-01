# GUSP. Glue between GUava and SPring
Guava services are usefull in many ways. They are especially usefull for reducing threaded boilerplate code.
GUSP attempts to reduce boilerplate required to make them managed by spring.

## Install
Get latest version from central.
Maven:
```pom
<dependency>
  <groupId>io.github.alopukhov.gusp</groupId>
  <artifactId>gusp</artifactId>
  <version>1.0.1</version>
</dependency>
```
Gradle:
```groovy
implementation 'io.github.alopukhov.gusp:gusp:1.0.1'
```
Gusp requires at least Spring 4.1.1, guava 15.0 and Java 8 to work.

## Use
Enable annotation support (if required) with simple annotation:
```java
@SpringBootApplication
@EnableGusp
public class AcmeApp { ... }
```
or create required postprocessors manually:
```java
@Configuration
public class MyAwesomeConfig {
  @Bean
  public static WithSmartLifecyclePostprocessor withSmartLifeCyclePostprocessor() {
    return new WithSmartLifecyclePostprocessor();
  }
}
```

Annotate your service beans with ```@WithSmartLifecycle``` annotation.
This is as simple as:
```java
@Component
@WithSmartLifecycle
public class AwesomeService extends AbstractExecutionThreadService { ... }
```
or
```java
@Configuration
public class MyAwesomeConfig {
  @WithSmartLifecycle
  @Bean
  public Service myAwesomeGuavaService() { ... }
}
```

It will create ```ServiceSmartLifecycle``` support bean (of course you can create it yourself). 
This bean is responsible for starting and stopping your service.

Following configurations are available
|Propery|Default|Description|
|---|---|---|
|beanName||name for generate SmarLifecycle bean. If empty - bean name will be generated based on annotated bean name|
|autoStartup|true|If set to true service will be started automatically by spring|
|asyncStart|false|If set to true support bean will issue only startAsync() call|
|phase|0|Phase for SmartLifecycle|
|stopOnDestroy|false|If set to true support bean is responsible for stopping annotated service during it's destroy call (see [Spring Lifecycle doc](https://docs.spring.io/spring/docs/5.2.8.RELEASE/javadoc-api/org/springframework/context/Lifecycle.html#stop--) for cases when stop will not be called)|
