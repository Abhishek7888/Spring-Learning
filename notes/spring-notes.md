# Spring Notes

This file contains concise interview-focused notes on Spring Framework modules and core concepts including dependency injection, autowiring, bean lifecycle management, bean scopes, and stereotype annotations.

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

## 5. Bean Scopes (Detailed Interview Guide)

**What is Bean Scope?**
Bean scope defines the lifecycle and visibility of a bean in the Spring container. It determines when a bean is created, how many instances are created, and when it's destroyed.

### Types of Bean Scopes

#### 1. **singleton** (Default Scope - Most Common)

**Definition:**
Only one instance of the bean is created per Spring container and reused for all requests. The same instance is shared across the entire application.

**Characteristics:**
- Default scope (if not specified)
- Single instance per ApplicationContext
- Bean is created at container startup (eager initialization)
- Reused for every request
- Thread-safe (managed by Spring container)
- Long-lived (exists until container closes)

**Declaration:**
```java
// Annotation-based
@Component
@Scope("singleton")  // Explicitly specified (not required as it's default)
public class UserService {
    private String userId;
}

// Or simply (default)
@Component
public class UserService {
}

// XML-based
<bean id="userService" class="com.example.UserService" scope="singleton" />

// Or simply (default)
<bean id="userService" class="com.example.UserService" />
```

**Java Configuration:**
```java
@Configuration
public class AppConfig {
    @Bean
    @Scope("singleton")  // Explicitly (optional)
    public UserService userService() {
        return new UserService();
    }
}
```

**Important Points:**
- Instance is created once at startup
- Same instance shared across all requests
- Thread-safe (Spring manages synchronization)
- Stateful data shared across threads
- Performance efficient (no repeated instantiation)

**Example Problem:**
```java
@Component
@Scope("singleton")
public class RequestCounter {
    private int count = 0;  // Shared across all threads!
    
    public void increment() {
        count++;  // Race condition - not thread-safe!
    }
}
// Multiple threads accessing same count variable = data corruption
```

**When to use:**
- Stateless services (UserService, OrderService)
- Components with no mutable state
- Cache objects
- Utility services
- Data access objects (DAOs)

**Advantages:**
- Memory efficient (single instance)
- Performance efficient (no repeated creation)
- Fast lookups (same instance)
- Suitable for stateless components

**Disadvantages:**
- Not suitable for stateful components
- Thread-safety must be handled carefully
- Shared state risks
- Testing may require resetting state between tests

**Interview Example:**
```java
@Component
@Scope("singleton")
public class UserService {
    private UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // Stateless service - safe to be singleton
    public User getUserById(int id) {
        return userRepository.findById(id);
    }
}

// All requests share same UserService instance
// Thread-safe because no mutable state
```

---

#### 2. **prototype** (New Instance Every Time)

**Definition:**
A new instance of the bean is created every time it's requested from the container.

**Characteristics:**
- New instance created for each request
- Not shared between requests
- Created on-demand (lazy initialization)
- No singleton caching
- Container doesn't manage full lifecycle
- @PreDestroy NOT called
- Caller responsible for cleanup

**Declaration:**
```java
// Annotation-based
@Component
@Scope("prototype")
public class RequestContext {
    private String requestId;
    private LocalDateTime createdAt;
}

// XML-based
<bean id="requestContext" class="com.example.RequestContext" scope="prototype" />

// Java Configuration
@Configuration
public class AppConfig {
    @Bean
    @Scope("prototype")
    public RequestContext requestContext() {
        return new RequestContext();
    }
}
```

**Important Points:**
- New instance for every getBean() call
- Spring doesn't manage destruction (@PreDestroy NOT called)
- Caller must manage cleanup
- Each instance is independent
- No state sharing between instances
- Suitable for stateful objects

**Example:**
```java
// Prototype bean
@Component
@Scope("prototype")
public class UserSession {
    private String userId;
    private String sessionId = UUID.randomUUID().toString();
    private LocalDateTime loginTime = LocalDateTime.now();
    
    public UserSession() {
        System.out.println("New UserSession created: " + sessionId);
    }
    
    @PreDestroy
    public void cleanup() {
        // ⚠️ NOT called by Spring for prototype beans!
        System.out.println("Cleanup called");
    }
}

// Usage
@Component
public class SessionManager {
    @Autowired
    private ApplicationContext context;
    
    public UserSession createSession() {
        // Each call gets NEW instance
        UserSession session1 = context.getBean(UserSession.class);
        UserSession session2 = context.getBean(UserSession.class);
        
        // session1 != session2 (different objects)
        // session1.sessionId != session2.sessionId
        
        return session1;
    }
}
```

**When to use:**
- Stateful objects (each needs its own state)
- Request-scoped data (but use @RequestScope instead)
- Objects that hold mutable state
- User sessions
- Form backing objects
- When each client needs independent instance

**Advantages:**
- Independent instances
- No state sharing
- Suitable for stateful objects
- Thread-safe (each thread gets own instance)

**Disadvantages:**
- Memory overhead (multiple instances)
- No Spring lifecycle management
- Caller must manage cleanup
- @PreDestroy not called
- Performance impact from repeated creation

**Important Note:**
```java
@Component
@Scope("prototype")
public class ProtoBean {
    @PreDestroy
    public void cleanup() {
        // ❌ NOT CALLED for prototype scope!
    }
}

@Component
@Scope("singleton")
public class SingletonBean {
    @PreDestroy
    public void cleanup() {
        // ✅ CALLED for singleton scope
    }
}
```

---

#### 3. **request** (Web-Aware - Request Scope)

**Definition:**
A new bean instance is created for each HTTP request in a web application. The instance lives for the duration of the HTTP request.

**Characteristics:**
- New instance per HTTP request
- Created when request arrives
- Destroyed when request completes
- Bound to ServletRequest
- Only available in web applications
- Spring manages full lifecycle (@PostConstruct and @PreDestroy called)

**Declaration:**
```java
// Using @Scope annotation
@Component
@Scope("request")
public class HttpRequestLogger {
    private String requestId;
    private LocalDateTime startTime;
    
    @PostConstruct
    public void init() {
        this.requestId = UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Request " + requestId + " completed");
    }
}

// Using WebApplicationContext.SCOPE_REQUEST (alternative)
@Component
@Scope(WebApplicationContext.SCOPE_REQUEST)
public class HttpRequestLogger {
}

// XML-based
<bean id="httpRequestLogger" class="com.example.HttpRequestLogger" scope="request" />
```

**Java Configuration:**
```java
@Configuration
public class WebConfig {
    @Bean
    @Scope("request")
    public HttpRequestLogger httpRequestLogger() {
        return new HttpRequestLogger();
    }
}
```

**Example Use Case:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private HttpRequestLogger requestLogger;  // Different instance per request
    
    @Autowired
    private UserService userService;  // Same singleton instance
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        // requestLogger is unique to this request
        System.out.println("Request Logger ID: " + requestLogger.getRequestId());
        return userService.getUserById(id);
    }
}
```

**When to use:**
- Logging request details
- Storing request-specific data
- Tracking request headers
- User request context
- Request counters
- Request/response tracking

**Advantages:**
- Automatic lifecycle management
- Fresh instance per request
- No state sharing between requests
- Thread-safe within request context
- @PreDestroy called automatically

**Disadvantages:**
- Only in web applications
- Memory overhead for each request
- Cannot be injected into singleton beans directly

---

#### 4. **session** (Web-Aware - Session Scope)

**Definition:**
A bean instance is created once per HTTP session and reused for all requests in that session. Destroyed when session expires.

**Characteristics:**
- One instance per user session
- Created when user first accesses application
- Bound to HttpSession
- Shared across requests in same session
- Destroyed when session expires/invalidates
- Only in web applications
- Spring manages full lifecycle

**Declaration:**
```java
// Using @Scope annotation
@Component
@Scope("session")
public class UserSessionData {
    private String userId;
    private String userName;
    private LocalDateTime loginTime;
    
    @PostConstruct
    public void init() {
        this.loginTime = LocalDateTime.now();
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Session expired for user: " + userName);
    }
}

// Using WebApplicationContext.SCOPE_SESSION
@Component
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserSessionData {
}
```

**Example Use Case:**
```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @Autowired
    private UserSessionData sessionData;  // One per user session
    
    @GetMapping
    public UserProfile getProfile() {
        // sessionData is unique to this user's session
        return new UserProfile(sessionData.getUserId());
    }
    
    @PostMapping("/login")
    public void login(@RequestBody LoginRequest request) {
        // Store in session
        sessionData.setUserId(request.getUserId());
        sessionData.setUserName(request.getUserName());
    }
}
```

**When to use:**
- User session data
- Shopping cart (e-commerce)
- User preferences
- User login information
- Session-specific state
- Multi-request user data

**Advantages:**
- Automatic lifecycle management
- Shared safely across requests in same session
- Automatic cleanup on session expiration
- Natural fit for web applications
- Thread-safe within session context

**Disadvantages:**
- Only in web applications
- Memory overhead (one per user)
- Session data persists
- Not suitable for large data volumes

**Important Note:**
```java
@Component  // singleton by default
public class UserService {
    @Autowired
    private UserSessionData sessionData;  // Problem! Injecting session into singleton
    // ⚠️ Spring creates proxy to handle this
}
```

---

#### 5. **application** (Web-Aware - Application Scope)

**Definition:**
A single bean instance is created per ServletContext (entire web application). Shared across all users and sessions.

**Characteristics:**
- One instance per ServletContext
- Shared across entire application
- Similar to singleton but web-specific
- Persists for application lifetime
- Spring manages full lifecycle
- Only in web applications

**Declaration:**
```java
// Using @Scope annotation
@Component
@Scope("application")
public class ApplicationConfig {
    private String appVersion = "1.0.0";
    private LocalDateTime startupTime;
    
    @PostConstruct
    public void init() {
        this.startupTime = LocalDateTime.now();
    }
}

// Using WebApplicationContext.SCOPE_APPLICATION
@Component
@Scope(WebApplicationContext.SCOPE_APPLICATION)
public class ApplicationConfig {
}
```

**When to use:**
- Application-wide configuration
- Global counters
- Application version info
- Startup time
- Application metadata
- Shared across all users

**Advantages:**
- Single instance for entire app
- Shared across all users
- Memory efficient for app-level data
- Automatic lifecycle management

**Disadvantages:**
- Only in web applications
- Thread-safety concerns
- Data shared across all users
- Limited use cases

---

#### 6. **websocket** (Web-Aware - WebSocket Scope)

**Definition:**
A bean instance is created per WebSocket session and lives for the lifetime of the WebSocket connection.

**Characteristics:**
- One instance per WebSocket connection
- Lives for duration of WebSocket session
- Suitable for real-time communication
- Spring manages full lifecycle
- Only with Spring WebSocket support

**When to use:**
- WebSocket session data
- Real-time messaging
- Persistent connections
- Live chat applications
- Real-time dashboards

---

### Bean Scope Comparison Table

| Scope | Instance Count | Lifetime | Created | Destroyed | Use Case | Thread-Safe |
|-------|---|---|---|---|---|---|
| **singleton** | 1 per container | Application | Startup | On shutdown | Stateless services | ✓ (managed) |
| **prototype** | New each time | Per request | On-demand | Not by Spring | Stateful objects | ✓ (isolated) |
| **request** | 1 per HTTP request | HTTP request | Request arrives | Request ends | Request-specific data | ✓ (request scope) |
| **session** | 1 per user session | HTTP session | Session starts | Session expires | User session data | ✓ (session scope) |
| **application** | 1 per app | Application | Startup | On shutdown | App-wide config | ✓ (managed) |
| **websocket** | 1 per connection | WebSocket session | Connection opens | Connection closes | Real-time data | ✓ (connection scope) |

### Interview Questions on Bean Scopes

**Q1: What is the default bean scope in Spring?**
A: Singleton is the default scope. One instance is created per ApplicationContext and reused for all requests.

**Q2: When should you use prototype scope?**
A: Use prototype scope for stateful objects where each client needs independent state. Examples: form backing objects, user sessions, request-specific data.

**Q3: Will @PreDestroy be called for prototype beans?**
A: No, @PreDestroy is NOT called for prototype beans. Spring doesn't manage the full lifecycle. Caller must manage cleanup.

**Q4: What is the difference between singleton and request scope?**
A: Singleton creates one instance for the entire application (reused everywhere). Request scope creates a new instance for each HTTP request and destroys it after the request.

**Q5: Can you inject a session-scoped bean into a singleton?**
A: Not directly, but Spring creates a proxy to handle this. The proxy resolves the actual session bean at runtime.

**Q6: What is the difference between request and session scope?**
A: Request scope lives for one HTTP request. Session scope lives for the entire user session (multiple requests).

**Q7: When do you use application scope?**
A: Application scope is rarely used. Similar to singleton but web-specific. Use for application-wide configuration or metadata shared across all users.

**Q8: Why is prototype scope less common than singleton?**
A: Prototype has overhead (new instance each time) and Spring doesn't manage lifecycle (@PreDestroy not called). Singleton is more efficient for stateless services.

**Q9: Can multiple requests use the same singleton bean safely?**
A: Yes, if the bean is stateless (no mutable fields). Spring manages thread-safety through container synchronization.

**Q10: What happens if you inject prototype into singleton?**
A: The prototype bean is created once during singleton initialization and reused. To get a new prototype each time, use ObjectProvider or ApplicationContext.getBean().

---

### Best Practices for Bean Scopes

✅ **DO's:**
1. Use singleton for stateless services (default)
2. Use prototype for stateful objects
3. Use request/session for web-specific data
4. Be aware of thread-safety in singleton beans
5. Use ObjectProvider for dynamic prototype injection into singleton

❌ **DON'Ts:**
1. Don't use singleton for stateful objects
2. Don't assume @PreDestroy is called for prototype
3. Don't put mutable state in singleton beans without synchronization
4. Don't expect prototype beans to be garbage collected immediately
5. Don't use prototype scope for performance (it's slower)

---

## 6. Dependency Injection Types (detailed)

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

## 7. Autowiring / Wiring Options

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

## 8. @PostConstruct and @PreDestroy (Bean Lifecycle Callbacks)

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

## 9. Stereotype Annotations (Component Scanning)

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

**When to use:**
- Classes that interact with database
- DAO (Data Access Object) implementations
- Repository pattern implementations
- JPA/Hibernate repositories

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

### Comparison: Stereotype Annotations

| Annotation | Layer | Purpose | Return Type | Use Case |
|-----------|-------|---------|-------------|----------|
| **@Component** | Generic | Generic Spring bean | - | General-purpose components |
| **@Service** | Business Logic | Service/Business logic layer | Service methods | Business operations |
| **@Repository** | Data Access | DAO/Database layer | Entity objects | CRUD operations |
| **@Controller** | Presentation | MVC request handler | View name (String) | Traditional web apps |
| **@RestController** | Presentation | REST API handler | Response body (JSON) | REST APIs |

## 10. Interview Q&A (Final Comprehensive)

**Q1: What are the different bean scopes available in Spring?**
A: singleton (default, one instance per container), prototype (new instance each time), request (per HTTP request), session (per user session), application (per ServletContext), websocket (per WebSocket connection).

**Q2: What happens if you inject a prototype bean into a singleton bean?**
A: Spring creates a proxy to handle it. The prototype bean is created once during singleton initialization. To get a new instance each time, use ObjectProvider or ApplicationContext.getBean().

**Q3: When is @PreDestroy NOT called?**
A: @PreDestroy is NOT called for prototype-scoped beans since Spring doesn't manage their lifecycle after creation.

**Q4: Singleton vs Prototype - which is better for performance?**
A: Singleton is better for performance as it creates the instance once. Prototype has overhead from creating new instances each time.

**Q5: Can you safely store mutable state in a singleton bean?**
A: Not safely without proper synchronization. Singleton beans are shared across threads, so mutable state leads to race conditions. Keep singleton beans stateless.

**Q6: What is the default bean scope in Spring?**
A: Singleton. One instance per ApplicationContext, reused for all requests.

**Q7: How long does a request-scoped bean live?**
A: Only for the duration of the HTTP request. Created when request arrives, destroyed when response is sent.

**Q8: What is the difference between request scope and session scope?**
A: Request scope lives for one HTTP request. Session scope lives for the entire user session (multiple requests).

**Q9: Can prototype beans use @PreDestroy?**
A: Yes, but @PreDestroy won't be called automatically by Spring. Caller must manage cleanup.

**Q10: What bean scope should you use for shopping cart data?**
A: Session scope, so each user has their own shopping cart that persists across requests.

---

## 11. Quick revision checklist
- ✓ List all 6 bean scopes and their lifetimes
- ✓ Understand singleton vs prototype differences
- ✓ Know when @PreDestroy is/isn't called
- ✓ Explain request, session, and application scopes
- ✓ Understand thread-safety in singleton beans
- ✓ Know how to inject prototypes into singletons
- ✓ List Spring modules and their responsibilities
- ✓ Explain IoC vs DI concisely
- ✓ Describe BeanFactory vs ApplicationContext
- ✓ Explain all three DI types with pros/cons
- ✓ Explain autowiring modes
- ✓ Understand @PostConstruct and @PreDestroy lifecycle hooks
- ✓ Know when to use each stereotype annotation
- ✓ Understand component scanning

---

For more details on any topic, refer to the comprehensive guides in the springcore/ directory for deeper examples and advanced patterns.
