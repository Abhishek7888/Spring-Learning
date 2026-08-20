# Spring Notes

This file contains concise interview-focused notes on Spring Framework modules and core concepts including dependency injection, autowiring, bean lifecycle management, and stereotype annotations.

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
- DI (Dependency Injection): A form of IoC where dependencies are provided (injected) into objects rather than the objects creating them. Types: constructor, setter, field (via reflection), and method.
- BeanFactory vs ApplicationContext:
  - BeanFactory: basic IoC container; lazy initialization.
  - ApplicationContext: superset that provides internationalization, event propagation, resource loading, and convenient configuration. Use ApplicationContext in most apps.
- Bean scope:
  - singleton (default): one shared instance per container
  - prototype: new instance every request to container
  - request/session/application/websocket: web-aware scopes
- Bean lifecycle:
  - Instantiation → Populate properties (DI) → BeanNameAware/BeanFactoryAware callbacks → Pre-initialization (BeanPostProcessors) → @PostConstruct / init-method → Ready → @PreDestroy / destroy-method

## 4. Bean Configuration Styles
- XML configuration (legacy but still asked): <bean id=... class=...> and <property name=.../>
- Java-based configuration (@Configuration and @Bean)
- Annotation-based component scanning (@Component, @Service, @Repository, @Controller) and stereotype annotations
- Externalized configuration with @Value and PropertySources

## 5. Dependency Injection Types (detailed)

### Constructor Injection
**What is it?**
- Dependencies are provided via constructor parameters at object creation time
- The object is fully initialized after construction
- Enables creation of immutable objects

**How it works (lifecycle point):**
- Container instantiates bean via constructor
- Container passes all dependencies as constructor arguments
- Container calls the constructor with resolved dependencies
- Post-initialization steps (@PostConstruct, BeanPostProcessors, init-method) run afterwards

**Advantages:**
- Immutable dependencies (thread-safe)
- Fail-fast: missing dependencies cause immediate startup failure
- Easier to test: can create instances with mock dependencies
- Complete initialization: object fully initialized after construction
- Dependency clarity: dependencies explicit in constructor signature
- Helps detect circular dependencies at startup

**Disadvantages:**
- Multiple constructors needed for flexibility (constructor overloading)
- Complex XML configuration for many dependencies
- Constructor parameter list can become large (inflation)
- Cannot change dependencies after object creation
- All dependencies must be resolved before bean creation

**Example XML Configuration:**
```xml
<!-- First define the dependency -->
<bean id="address" class="com.example.Address">
    <constructor-arg value="New York" />
</bean>

<!-- Then inject it into Employee -->
<bean id="employee" class="com.example.Employee">
    <constructor-arg type="int" value="1" />
    <constructor-arg type="java.lang.String" value="John" />
    <constructor-arg ref="address" />  <!-- Reference to address bean -->
</bean>
```

**Example Annotation-based:**
```java
@Component
public class Employee {
    private final String name;
    private final Address address;

    @Autowired  // Spring uses constructor injection (Spring 4.3+)
    public Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}
```

**When to use:**
- For mandatory dependencies
- When you want immutable components
- To ensure fail-fast behavior on startup
- When testing without container

### Setter Injection
**What is it?**
- Setter injection is when Spring injects dependencies by calling public setter methods on a bean after creating it via its no-arg constructor.

**How it works (lifecycle point):**
- Container instantiates bean (no-arg constructor or default instantiation)
- Container resolves dependencies (other beans or values)
- Container calls setter methods to inject resolved dependencies
- Post-initialization steps (@PostConstruct, BeanPostProcessors, init-method) run afterwards

**Examples:**
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
}
```

2) XML configuration
```xml
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

**Advantages:**
- Good for optional dependencies
- Avoids long constructors
- Can help resolve simple circular dependencies
- Setter names make dependencies clear

**Disadvantages:**
- Risk of partially-initialized beans
- Harder to enforce mandatory dependencies
- Objects are mutable (not thread-safe)

**When to use:**
- For optional or configurable dependencies
- When dependency is needed after object creation
- To help resolve circular dependencies
- When library/framework expects no-arg constructor

**Circular dependency behavior:**
- Setter injection can allow circular references because Spring creates instances first and then injects properties. Example:
  - A has setB(B b)
  - B has setA(A a)
  - Spring can instantiate A and B and then inject each other's references
- Constructor injection cannot handle circular constructor dependencies (will fail with cyclic dependency error)

### Field Injection (not recommended)
- Uses reflection; convenient but hides dependencies and is harder to test.

### When NOT to use setter injection
- For mandatory dependencies where you want fail-fast behavior — prefer constructor injection
- When you want immutable components

### Common pitfalls and tips
- Avoid heavy logic in setters; use them only for assignment
- If an injected dependency is required, validate it early (e.g., in @PostConstruct) to avoid runtime NPEs
- Prefer constructor injection for required dependencies and setter injection for optional ones
- Use @Autowired(required=false) or Optional<T> for optional dependencies

## 6. Autowiring / Wiring Options

**What is Autowiring?**
Autowiring is a Spring feature that automatically injects bean dependencies without explicitly specifying them in XML configuration or using annotations. Instead of manually wiring dependencies using `<constructor-arg>` or `<property>`, Spring can automatically find and inject matching beans from the application context.

### Autowiring Modes (XML-based)

**1. no (Default - No Autowiring)**
No automatic injection. Dependencies must be explicitly wired.
```xml
<bean id="employee" class="com.example.Employee" autowire="no">
    <property name="address" ref="address" />
</bean>
```

**2. byName (Autowire by Property Name)**
Spring looks for a bean with the **same name** as the property and automatically injects it.
- Bean ID must match property name
- Requires setter methods for the properties
- Example:
```xml
<bean id="employee" class="com.example.Employee" autowire="byName" />
<!-- Property names MUST match bean IDs -->
<bean id="address" class="com.example.Address">
    <property name="city" value="New York" />
</bean>
<bean id="department" class="com.example.Department">
    <property name="name" value="IT" />
</bean>
```

**Pros:** Clear and explicit - property name tells you the bean name  
**Cons:** Tight coupling between property names and bean names

**3. byType (Autowire by Property Type)**
Spring looks for a bean with the **same type** as the property and automatically injects it.
- Bean IDs don't matter, type matching is used
- Requires setter methods for the properties
- Fails if multiple beans of same type exist
```xml
<bean id="employee" class="com.example.Employee" autowire="byType" />
<!-- Bean IDs can be anything - type matching is used -->
<bean id="addr" class="com.example.Address">
    <property name="city" value="New York" />
</bean>
<bean id="dept" class="com.example.Department">
    <property name="name" value="IT" />
</bean>
```

**Pros:** More flexible - bean IDs don't matter  
**Cons:** Fails if multiple beans of same type exist

**4. constructor (Autowire via Constructor)**
Spring autowires constructor parameters by **type** (similar to byType).
- Requires parameterized constructor
- Parameters resolved by type
```xml
<bean id="employee" class="com.example.Employee" autowire="constructor" />
<bean id="address" class="com.example.Address">
    <property name="city" value="New York" />
</bean>
<bean id="department" class="com.example.Department">
    <property name="name" value="IT" />
</bean>
```

**Pros:** Enables immutability with final fields  
**Cons:** Must have proper constructor

**5. autodetect (Deprecated)**
Spring 3.0+ deprecated. Automatically chooses between `constructor` and `byType`.

### Annotation-based Autowiring

**@Autowired - Type-based Injection**
- Most commonly used
- Can be applied on fields, constructors, or setters
- Works by type first, then by name
- Optional dependency support with `@Autowired(required=false)`

Examples:
```java
// Field Injection
@Component
public class Employee {
    @Autowired
    private Address address;
}

// Setter Injection
@Component
public class Employee {
    private Address address;
    
    @Autowired
    public void setAddress(Address address) {
        this.address = address;
    }
}

// Constructor Injection (recommended)
@Component
public class Employee {
    private final Address address;
    
    @Autowired
    public Employee(Address address) {
        this.address = address;
    }
}
```

**@Qualifier - Specify Exact Bean**
When multiple beans of same type exist, use `@Qualifier` to specify which one.
```java
@Component
public class Employee {
    @Autowired
    @Qualifier("primaryAddress")
    private Address address;
}
```

**@Resource (JSR-250)**
Similar to `@Autowired` but searches by **name first**, then type.
```java
@Component
public class Employee {
    @Resource(name = "address")
    private Address address;
}
```

**@Inject (JSR-330)**
Standard Java annotation (similar to `@Autowired`).
```java
@Component
public class Employee {
    @Inject
    private Address address;
}
```

### Comparison: XML vs Annotations Autowiring

| Feature | XML Autowiring | Annotation Autowiring |
|---------|----------------|----------------------|
| **Configuration** | Centralized in XML | Distributed in classes |
| **Verbosity** | More verbose | Less verbose |
| **Discoverability** | Requires reading XML | Visible in code |
| **Performance** | Same | Same |
| **Flexibility** | Very flexible | Less flexible |
| **Best for** | Large enterprise apps | Modern microservices |
| **Learning Curve** | Steeper | Easier |

### Best Practices for Autowiring
1. Use Constructor Injection for required dependencies
2. Use `@Qualifier` when multiple beans of same type exist
3. Enable component scanning explicitly
4. Use `@Autowired(required = false)` for optional dependencies
5. Prefer annotations over XML for modern Spring applications

## 7. @PostConstruct and @PreDestroy (Bean Lifecycle Callbacks)

**Overview:**
`@PostConstruct` and `@PreDestroy` are lifecycle callback annotations that allow you to hook into the Spring bean lifecycle at specific points:
- **@PostConstruct**: Called after the bean is created and dependencies are injected
- **@PreDestroy**: Called before the bean is destroyed when the container shuts down

**Import Statement:**
```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
```

**Key Points:**
- Part of `javax.annotation` package (JSR-250)
- Introduced in Spring 2.5
- Work with any Spring bean
- Called automatically by Spring container
- Method can have any name
- Methods must be public and have no parameters
- Methods must not throw checked exceptions (can throw unchecked)

### @PostConstruct Annotation

**Definition:**
`@PostConstruct` marks a method that should be executed **after** the Spring container has instantiated the bean and injected all dependencies.

**When is @PostConstruct Called?**
```
1. Bean Class Loaded
    ↓
2. Constructor Invoked (Instantiation)
    ↓
3. Dependencies Injected (Property/Setter/Constructor Injection)
    ↓
4. ★ @PostConstruct Method Called ← HERE
    ↓
5. BeanPostProcessor.postProcessAfterInitialization()
    ↓
6. Bean Ready for Use
```

**Common Use Cases:**
1. Initialize resources (database connections, thread pools)
2. Load configuration files
3. Validate bean properties
4. Start background tasks
5. Initialize caches

**Example:**
```java
@Component
public class DatabaseConnection {
    @Value("${db.url}")
    private String url;
    
    @Value("${db.user}")
    private String username;
    
    private Connection connection;

    @PostConstruct
    public void initialize() {
        System.out.println("Initializing database connection...");
        
        // Validation
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Database URL cannot be empty");
        }

        // Initialization logic
        try {
            this.connection = DriverManager.getConnection(url, username, "password");
            System.out.println("✓ Database connection established");
        } catch (SQLException e) {
            System.out.println("✗ Failed to connect: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
```

### @PreDestroy Annotation

**Definition:**
`@PreDestroy` marks a method that should be executed **before** the Spring container destroys the bean (when the application context is being closed).

**When is @PreDestroy Called?**
```
1. Application Context Closing
    ↓
2. ApplicationContext.close() called
    ↓
3. ★ @PreDestroy Method Called ← HERE
    ↓
4. DisposableBean.destroy() (if implemented)
    ↓
5. Bean Destroyed
```

**Common Use Cases:**
1. Close resource connections (database, files)
2. Cleanup thread pools
3. Release memory
4. Save state before shutdown
5. Cancel scheduled tasks

**Example:**
```java
@Component
public class FileHandler {
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    public FileHandler() throws IOException {
        this.fileWriter = new FileWriter("output.txt");
        this.bufferedWriter = new BufferedWriter(fileWriter);
    }

    public void write(String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Closing file resources...");
        
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
                System.out.println("✓ BufferedWriter closed");
            }
            if (fileWriter != null) {
                fileWriter.close();
                System.out.println("✓ FileWriter closed");
            }
        } catch (IOException e) {
            System.out.println("✗ Error closing resources: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
```

### Complete Bean Lifecycle Execution Order

```
CREATION PHASE:
1. Bean Class Loaded
2. Constructor Called
3. Property Injection (Setter/Field Injection)
4. Aware Interfaces (if implemented)
5. BeanPostProcessor.postProcessBeforeInitialization()
6. ★ @PostConstruct Called ← HERE
7. InitializingBean.afterPropertiesSet() (if implemented)
8. init-method from XML/Java config (if specified)
9. BeanPostProcessor.postProcessAfterInitialization()

USAGE PHASE:
10. Bean is ready for use
11. Application uses the bean

DESTRUCTION PHASE:
12. Application Context closing
13. ★ @PreDestroy Called ← HERE
14. DisposableBean.destroy() (if implemented)
15. destroy-method from XML/Java config (if specified)
16. Bean is destroyed
```

### Best Practices for @PostConstruct and @PreDestroy

**✅ DO's:**
1. Use @PostConstruct for initialization logic that depends on injected dependencies
2. Keep methods simple and focused
3. Handle exceptions gracefully (wrap checked exceptions in RuntimeException)
4. Use @PreDestroy for cleanup of resources
5. Make methods public and no-arg

**❌ DON'Ts:**
1. Don't use in constructor (dependencies not injected yet)
2. Don't throw checked exceptions
3. Don't do heavy processing in lifecycle methods
4. Don't mix too much logic in these methods

### Interview Q&A on @PostConstruct and @PreDestroy

**Q1: Can we have multiple @PostConstruct methods?**
A: No, only one method can be marked with @PostConstruct. If multiple are marked, Spring throws an exception. Instead, call other methods from @PostConstruct.

**Q2: Will @PreDestroy be called for prototype beans?**
A: No, @PreDestroy is only called for singleton beans. Prototype beans are not managed by Spring after creation.

**Q3: What's the difference between @PostConstruct and afterPropertiesSet()?**
A: Both are called after initialization, but @PostConstruct is called first. @PostConstruct is recommended (standard JSR-250 annotation) while afterPropertiesSet() is Spring-specific and legacy.

**Q4: Can @PostConstruct throw checked exceptions?**
A: No, methods must not throw checked exceptions. Wrap them in RuntimeException if needed.

**Q5: Does the @PostConstruct method name matter?**
A: No, it can have any name. Unlike interface methods, only the annotation matters.

## 8. Stereotype Annotations (Component Scanning)

**What are Stereotype Annotations?**
Stereotype annotations are special annotations that mark a class as a Spring bean candidate for component scanning. They tell Spring to automatically detect and register the class as a bean in the ApplicationContext without explicit XML or Java configuration.

**Key Concept:**
Instead of manually defining beans in XML or using @Bean, stereotype annotations allow you to declare beans directly in the class definition. Spring discovers these during component scanning.

### How Component Scanning Works
1. Spring scans the specified package and sub-packages
2. Finds classes annotated with stereotype annotations
3. Instantiates beans automatically
4. Registers them in the ApplicationContext
5. Performs dependency injection

**Enable Component Scanning:**
```java
// In Java Configuration
@Configuration
@ComponentScan(basePackages = "com.example")  // Specify package to scan
public class AppConfig {
    // Other bean definitions
}
```

```xml
<!-- In XML Configuration -->
<context:component-scan base-package="com.example" />
```

### Main Stereotype Annotations

#### 1. **@Component** (Most Generic)
- Generic stereotype annotation for any Spring-managed component
- Direct child class used for non-specific components
- Spring will automatically detect and register as a bean

**Usage:**
```java
@Component
public class MyComponent {
    // This is a generic Spring bean
}

// With custom bean name
@Component("customBeanName")
public class MyComponent {
    // Bean will be registered as "customBeanName"
}

// Default bean name (lowercase first letter of class name)
@Component  // Registered as "myComponent"
public class MyComponent {
}
```

**When to use:**
- General-purpose Spring components
- Classes that don't fit into Service, Repository, or Controller categories
- Utility classes that need to be managed by Spring

#### 2. **@Service** (Business Logic)
- Specialization of @Component for business logic layer
- Marks a class as a Service provider
- Semantically indicates the class contains business logic
- Functionally identical to @Component but conveys intent

**Usage:**
```java
@Service
public class UserService {
    // Business logic methods
    public User getUserById(int id) {
        // Implementation
    }
    
    public void saveUser(User user) {
        // Implementation
    }
}

// With custom bean name
@Service("userServiceImpl")
public class UserService {
}
```

**Best Practices:**
- Use @Service for classes containing business logic
- Typically injected with @Autowired in controllers
- Should contain service methods (use cases)

**Common Interview Question:**
```
Q: What is @Service used for?
A: @Service marks a class as a service layer component containing business logic. 
   It's a specialization of @Component that helps organize code by layer and 
   makes the architecture clearer. Functionally, it's the same as @Component.
```

#### 3. **@Repository** (Data Access)
- Specialization of @Component for data access/persistence layer
- Marks a class as a Repository (DAO - Data Access Object)
- Provides benefits:
  - Platform-independent persistence exception translation
  - Converts database-specific exceptions to Spring DataAccessException
  - Makes error handling consistent across database technologies

**Usage:**
```java
@Repository
public class UserRepository {
    // Database operations
    public User findById(int id) {
        // SQL query logic
    }
    
    public void save(User user) {
        // Insert/Update logic
    }
    
    public void delete(int id) {
        // Delete logic
    }
}

// With custom bean name
@Repository("userDAO")
public class UserRepository {
}
```

**Exception Translation:**
```java
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public User findById(int id) {
        try {
            // Database operations
            return jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE id = ?", 
                new UserRowMapper(), 
                id
            );
        } catch (DataAccessException e) {
            // Spring automatically translates database exceptions
            // to DataAccessException (unchecked)
            throw e;
        }
    }
}
```

**When to use:**
- Classes that interact with database
- DAO (Data Access Object) implementations
- Repository pattern implementations
- JPA/Hibernate repositories

**Best Practices:**
- Use @Repository for data access logic
- Extends CrudRepository or JpaRepository for Spring Data repositories
- Methods should perform CRUD operations
- Exception translation is automatic

#### 4. **@Controller** (Web Layer - Request Handling)
- Specialization of @Component for web/presentation layer
- Marks a class as a Controller that handles HTTP requests
- Used with @RequestMapping and request handler methods
- Returns a view name or model
- Often used with traditional Spring MVC applications

**Usage:**
```java
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @RequestMapping("/users/{id}")
    public String getUser(@PathVariable int id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "userDetail";  // View name
    }
    
    @RequestMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "userList";  // View name
    }
}
```

**Key Points:**
- Returns view names (String) that get resolved to actual views (JSP, Thymeleaf, etc.)
- Populates Model with data for the view
- Uses ViewResolver to resolve view names to actual view files

#### 5. **@RestController** (Web Layer - REST API)
- Specialization of @Controller for RESTful web services
- Equivalent to @Controller + @ResponseBody on all methods
- Automatically serializes return value to JSON/XML
- Returns response body directly (not a view name)
- Modern REST API development

**Usage:**
```java
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    
    @Autowired
    private UserService userService;
    
    // Returns JSON response
    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return userService.getUserById(id);  // Auto-converted to JSON
    }
    
    @GetMapping
    public List<User> listUsers() {
        return userService.getAllUsers();  // Auto-converted to JSON array
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);  // Auto-converted to JSON
    }
    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }
}
```

**Differences from @Controller:**
- @Controller returns view names (String)
- @RestController returns response body (JSON/XML)
- @RestController = @Controller + @ResponseBody

**When to use:**
- Building REST APIs
- Returning JSON/XML data
- Modern web services
- Microservices architecture

### Comparison: Stereotype Annotations

| Annotation | Layer | Purpose | Return Type | Use Case |
|-----------|-------|---------|-------------|----------|
| **@Component** | Generic | Generic Spring bean | - | General-purpose components |
| **@Service** | Business Logic | Service/Business logic layer | Service methods | Business operations |
| **@Repository** | Data Access | DAO/Database layer | Entity objects | CRUD operations |
| **@Controller** | Presentation | MVC request handler | View name (String) | Traditional web apps |
| **@RestController** | Presentation | REST API handler | Response body (JSON) | REST APIs |

### Hierarchy & Inheritance

```
@Component (Generic)
    ↓
@Service, @Repository, @Controller, @RestController
    ↓
    └─ @RestController extends @Controller
```

- @Service, @Repository, @Controller are all meta-annotated with @Component
- You can create custom stereotype annotations by meta-annotating @Component
- All stereotype annotations enable automatic component scanning

### Naming Beans with Stereotype Annotations

```java
// Default naming (lowercase first letter of class name)
@Service
public class UserService { }  // Bean name: "userService"

@Repository
public class UserRepository { }  // Bean name: "userRepository"

// Custom naming
@Service("customUserService")
public class UserService { }  // Bean name: "customUserService"

@Repository("userDAO")
public class UserRepository { }  // Bean name: "userDAO"
```

### Common Usage Pattern (Three-Tier Architecture)

```java
// 1. Controller Layer (Presentation)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;  // Inject service
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return userService.getUserById(id);
    }
}

// 2. Service Layer (Business Logic)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;  // Inject repository
    
    public User getUserById(int id) {
        return userRepository.findById(id);
    }
}

// 3. Repository Layer (Data Access)
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public User findById(int id) {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new UserRowMapper(),
            id
        );
    }
}
```

### Interview Questions on Stereotype Annotations

**Q1: What is the difference between @Component and @Service?**
A: Both are functionally identical and enable component scanning. @Service is a specialization that semantically indicates business logic. Use @Component for generic components and @Service for business logic layer.

**Q2: When would you use @Repository?**
A: Use @Repository for data access/persistence layer classes (DAOs). It provides exception translation, converting database-specific exceptions to Spring's DataAccessException.

**Q3: What is the difference between @Controller and @RestController?**
A: @Controller returns view names (for traditional MVC), while @RestController returns response body (JSON/XML) for REST APIs. @RestController is @Controller + @ResponseBody.

**Q4: Can you create custom stereotype annotations?**
A: Yes, by meta-annotating with @Component:
```java
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyCustomStereotype {
}
```

**Q5: What is component scanning and how does it work?**
A: Component scanning is the process where Spring automatically detects classes marked with stereotype annotations in specified packages and registers them as beans. Enable it with @ComponentScan or <context:component-scan>.

**Q6: How does Spring know which beans to create?**
A: Spring uses component scanning to:
1. Find all classes with stereotype annotations (@Component, @Service, etc.)
2. Instantiate them
3. Register as beans in ApplicationContext
4. Perform dependency injection

**Q7: What is the default bean name when using @Service?**
A: The default bean name is the class name with the first letter in lowercase. For example, UserService becomes "userService".

**Q8: Can you use multiple stereotype annotations on same class?**
A: You should use only one. Use the most specific one:
- @Service for business logic
- @Repository for data access
- @Controller/@RestController for web layer
- @Component for generic components

### Best Practices for Stereotype Annotations

✅ **DO's:**
1. Use @Service for business logic classes
2. Use @Repository for data access classes
3. Use @RestController for REST APIs
4. Use @Controller for traditional MVC views
5. Organize code into layers (presentation, business, data)
6. Enable component scanning with @ComponentScan
7. Use @Qualifier when multiple beans of same type exist

❌ **DON'Ts:**
1. Don't mix multiple stereotype annotations on same class
2. Don't use @Component when a specific annotation (Service, Repository, etc.) applies
3. Don't rely on default component scanning without explicit @ComponentScan
4. Don't mix @Controller and @RestController
5. Don't put business logic in Controller classes
6. Don't put database queries outside Repository classes

## 9. Interview Q&A (expanded: modules + DI + autowiring + lifecycle + stereotypes)

**Q1: Name the main Spring modules and one responsibility of each.**
A: Core (utilities), Beans (bean factory / wiring), Context (ApplicationContext and higher-level services), AOP (aspects/advice), JDBC/ORM (data access), Web/MVC (web layer), Test (testing support).

**Q2: What is the difference between BeanFactory and ApplicationContext?**
A: BeanFactory is the basic IoC container with lazy init. ApplicationContext builds on it, providing features like internationalization, event propagation, resource loading, and bean post-processing.

**Q3: What is Dependency Injection and what are common types?**
A: DI is providing dependencies from the container rather than the object creating them. Types: constructor, setter, field, method.

**Q4: What is the difference between Constructor Injection and Setter Injection?**
A: Constructor injection uses constructor parameters; setter injection uses setter methods. Constructor injection is preferred for required dependencies and immutability; setter injection for optional ones.

**Q5: Can setter injection help with circular dependencies?**
A: Yes — because Spring can instantiate beans first and later set dependencies via setters. Constructor injection cannot resolve circular constructor dependencies.

**Q6: What is autowiring?**
A: Autowiring is Spring automatically finding and injecting matching beans without explicit configuration. Modes: byName, byType, constructor, autodetect (deprecated).

**Q7: What autowiring mode should I use?**
A: `byType` is most common and flexible. Use `byName` when bean IDs are meaningful. Use `constructor` with annotations (@Autowired on constructor) in modern Spring.

**Q8: What is @PostConstruct and when is it called?**
A: It marks a method called after bean instantiation and dependency injection, before the bean is ready for use. Used for initialization logic.

**Q9: What is @PreDestroy and when is it called?**
A: It marks a method called before bean destruction when the application context closes. Used for resource cleanup and shutdown logic.

**Q10: Can @PreDestroy be used with prototype beans?**
A: No, @PreDestroy is only called for singleton beans. Spring doesn't manage the lifecycle of prototype beans.

**Q11: What are stereotype annotations?**
A: Annotations that mark classes as Spring beans for component scanning: @Component (generic), @Service (business logic), @Repository (data access), @Controller (MVC), @RestController (REST APIs).

**Q12: What is the difference between @Service and @Component?**
A: Both enable component scanning. @Service semantically indicates business logic layer, while @Component is for generic components. Use specific annotations for better code organization.

**Q13: When should you use @Repository?**
A: For data access/persistence layer classes that interact with database. It provides exception translation from database-specific exceptions to Spring's DataAccessException.

**Q14: What is component scanning?**
A: Process where Spring automatically detects classes with stereotype annotations in specified packages and registers them as beans. Enable with @ComponentScan or <context:component-scan>.

**Q15: How is @RestController different from @Controller?**
A: @Controller returns view names (for traditional MVC rendering), while @RestController returns response body (JSON/XML) for REST APIs. @RestController = @Controller + @ResponseBody.

## 10. Quick revision checklist
- ✓ List Spring modules and their responsibilities
- ✓ Explain IoC vs DI concisely
- ✓ Describe BeanFactory vs ApplicationContext
- ✓ Know bean scopes and lifecycle steps
- ✓ Explain all three DI types with examples and pros/cons
- ✓ Explain autowiring modes and when to use each
- ✓ Understand @PostConstruct and @PreDestroy lifecycle hooks
- ✓ Know when to use each stereotype annotation
- ✓ Understand component scanning and how it works
- ✓ Recognize three-tier architecture pattern
- ✓ Know best practices for organizing code into layers

---

For more details on any topic, refer to the comprehensive guides in the springcore/ directory for deeper examples and advanced patterns.
