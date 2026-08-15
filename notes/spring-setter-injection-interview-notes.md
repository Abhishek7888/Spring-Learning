# Spring Notes

This file contains concise interview-focused notes on Spring Framework modules and core concepts up to setter injection. Use this to revise quickly before interviews.

## 1. What is Spring?
- Spring is a lightweight Java framework that helps build enterprise applications.
- Key goals: simplify Java development, promote POJOs, enable Dependency Injection (DI), and provide modular infrastructure for transactions, AOP, data access, and web.

## 2. Main Spring Modules (high-level)
- Core Container
  - spring-core: fundamental utilities (Core, Reflection utils, Resource abstraction)
  - spring-beans: BeanFactory and BeanDefinition; handles bean creation and wiring
  - spring-context: ApplicationContext and higher-level features (events, i18n)
  - spring-context-support: integrations for community technologies (cache, mail)
- AOP (Aspect-Oriented Programming)
  - spring-aop: support for declaring aspects, advice (before/after/around), and pointcuts
  - integrates with AspectJ or proxy-based AOP
- Data Access / Integration
  - spring-jdbc: JDBC abstraction and exception translation
  - spring-tx: programmatic and declarative transaction management
  - spring-orm: integration with ORM frameworks (Hibernate, JPA)
  - spring-data (separate project): higher-level repository abstractions
- Web
  - spring-web: low-level web support, RestTemplate (legacy), WebUtils
  - spring-webmvc: Spring MVC (DispatcherServlet, Controllers, ViewResolvers)
  - spring-webflux: reactive-stack web framework (non-blocking)
- Testing
  - spring-test: testing support (ApplicationContext caching, MockMvc, test annotations)

Tip: In interviews, mention that Spring is modular — you include only what you need.

## 3. Core Concepts (brief, interview-ready)
- IoC (Inversion of Control): Objects do not configure or obtain their dependencies; the container (Spring) does.
- DI (Dependency Injection): A form of IoC where dependencies are provided (injected) into objects rather than the objects creating them. Types: constructor, setter, field (via reflection), and method injection.
- BeanFactory vs ApplicationContext:
  - BeanFactory: basic IoC container; lazy initialization.
  - ApplicationContext: superset that provides internationalization, event propagation, resource loading, and convenient configuration. Use ApplicationContext in most apps.
- Bean scope:
  - singleton (default): one shared instance per container
  - prototype: new instance every request to container
  - request/session/application/websocket: web-aware scopes
- Bean lifecycle:
  - Instantiation -> Populate properties (DI) -> BeanNameAware/BeanFactoryAware callbacks -> Pre-initialization (BeanPostProcessors) -> @PostConstruct / init-method -> Ready -> @PreDestroy / destroy-method on shutdown

## 4. Bean Configuration Styles
- XML configuration (legacy but still asked): <bean id=... class=...> and <property name=.../>
- Java-based configuration (@Configuration and @Bean)
- Annotation-based component scanning (@Component, @Service, @Repository, @Controller) and stereotype annotations
- Externalized configuration with @Value and PropertySources

## 5. Autowiring / Wiring Options
- By type, name, constructor
- Modes: explicit wiring (ref in XML, parameter in @Bean), or autowiring with @Autowired
- Autowiring in modern Spring uses constructor injection by default in recommended practices

## 6. Dependency Injection Types (short)
- Constructor Injection
  - Dependencies are provided via constructor parameters.
  - Pros: immutable dependencies, fail-fast, easier to test.
  - Cons: many parameters can make constructors verbose.
- Setter Injection
  - Dependencies are provided through setter methods after bean construction.
  - Pros: good for optional dependencies, avoids long constructors, can help with circular references.
  - Cons: risk of partially-initialized beans, harder to enforce mandatory dependencies.
- Field Injection (not recommended for production code / testing)
  - Uses reflection; convenient but hides dependencies and is harder to test.

## 7. Setter Injection — Detailed Notes (Interview focus)
### What is it?
- Setter injection is when Spring injects dependencies by calling public setter methods on a bean after creating it via its no-arg constructor.

### How it works (lifecycle point)
- Container instantiates bean (no-arg constructor or default instantiation)
- Container resolves dependencies (other beans or values)
- Container calls setter methods to inject resolved dependencies
- Post-initialization steps (@PostConstruct, BeanPostProcessors, init-method) run afterwards

### Examples
1) Plain POJO
```java
public class Employee {
    private String name;
    private Department department;

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    // getters, business methods
}
```

2) XML configuration
```xml
<bean id="department" class="com.example.Department"/>

<bean id="employee" class="com.example.Employee">
    <property name="name" value="Alice"/>
    <property name="department" ref="department"/>
</bean>
```

3) Annotation-based
```java
@Component
public class Employee {
    private String name;
    private Department department;

    @Value("${employee.name:DefaultName}")
    public void setName(String name) {
        this.name = name;
    }

    @Autowired
    public void setDepartment(Department department) {
        this.department = department;
    }
}
```
Notes:
- `@Autowired` can be applied on setter methods.
- `@Value` injects literals or properties into setters.
- `@Autowired(required = false)` can mark the dependency optional.

### When to use setter injection
- When dependency is optional or configurable after object creation.
- When a bean needs a no-arg constructor (libraries, frameworks, or for proxies).
- To help resolve simple circular dependencies (bean A depends on B and vice versa).

### When not to use it
- For mandatory dependencies where you want a fail-fast behavior — prefer constructor injection.
- When you want immutable components.

### Circular dependency behavior
- Setter injection can allow circular references because Spring creates instances first and then injects properties. Example:
  - A has setB(B b)
  - B has setA(A a)
  Spring can instantiate A and B and then inject each other's references.
- Constructor injection cannot handle circular constructor dependencies (will fail with a cyclic dependency error).

### Common pitfalls and tips
- Avoid heavy logic in setters; use them only for assignment.
- If an injected dependency is required, validate it early (e.g., in @PostConstruct) to avoid runtime NPEs.
- Prefer constructor injection for required dependencies and setter injection for optional ones.
- Use @Autowired(required=false) or Optional<T> for optional dependencies.

## 8. Interview Q&A (expanded: modules + setter injection)
1) Q: Name the main Spring modules and one responsibility of each.
   A: Core (utilities), Beans (bean factory / wiring), Context (ApplicationContext and higher-level services), AOP (aspects/advice), JDBC/ORM (data access), Web/MVC (web layer), Test (testing support).

2) Q: What is the difference between BeanFactory and ApplicationContext?
   A: BeanFactory is the basic IoC container with lazy init. ApplicationContext builds on it, providing features like internationalization, event propagation, resource loading, and bean post-processing.

3) Q: What is Dependency Injection and what are common types?
   A: DI is providing dependencies from the container rather than the object creating them. Types: constructor, setter, field, method.

4) Q: What is setter injection and how does it differ from constructor injection?
   A: Setter injection uses setter methods after construction; constructor injection provides dependencies at construction time. Constructor injection is preferred for required dependencies; setter for optional ones.

5) Q: Can setter injection help with circular dependencies?
   A: Yes — because Spring can instantiate beans first and later set dependencies via setters. Constructor injection cannot resolve circular constructor dependencies.

6) Q: How do you declare a setter-injected dependency using annotations?
   A: Annotate the setter with `@Autowired`. Use `@Value` for injecting property values into setters.

7) Q: Name a disadvantage of setter injection.
   A: Beans may be partially-initialized or accidentally left without required dependencies, leading to runtime errors.

8) Q: What bean scopes are commonly used in Spring?
   A: singleton, prototype, and web scopes like request and session.

## 9. Quick revision checklist
- Be able to list Spring modules and mention why modularity helps.
- Explain IoC vs DI concisely.
- Describe BeanFactory vs ApplicationContext.
- Know bean scopes and lifecycle steps.
- Explain setter injection — code example, pros/cons, when to use, circular dependency behavior.

---

If you want, I can:
- Add small runnable examples (Maven module) demonstrating constructor vs setter injection.
- Add a one-page printable cheat sheet that you can memorize.
- Break this file into separate topic files (modules.md, core-concepts.md, injection-types.md).

Happy to make those changes next — tell me which one to add.