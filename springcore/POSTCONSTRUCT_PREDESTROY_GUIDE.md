# @PostConstruct and @PreDestroy - Complete Guide for Interview

## Table of Contents
1. [Overview](#overview)
2. [@PostConstruct Annotation](#postconstruct-annotation)
3. [@PreDestroy Annotation](#predestroy-annotation)
4. [How They Work](#how-they-work)
5. [Execution Order](#execution-order)
6. [Practical Examples](#practical-examples)
7. [Common Use Cases](#common-use-cases)
8. [Best Practices](#best-practices)
9. [Interview Questions & Answers](#interview-questions--answers)

---

## Overview

`@PostConstruct` and `@PreDestroy` are lifecycle callback annotations that allow you to hook into the Spring bean lifecycle at specific points:
- **@PostConstruct**: Called after the bean is created and dependencies are injected
- **@PreDestroy**: Called before the bean is destroyed when the container shuts down

### Import Statement:
```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
```

### Key Points:
- Part of `javax.annotation` package (JSR-250)
- Introduced in Spring 2.5
- Work with any Spring bean
- Called automatically by Spring container
- Method can have any name (not like interface methods)
- Methods must be public and have no parameters
- Methods must not throw checked exceptions (can throw unchecked)

---

## @PostConstruct Annotation

### Definition
`@PostConstruct` marks a method that should be executed **after** the Spring container has instantiated the bean and injected all dependencies.

### Method Signature:
```java
@PostConstruct
public void methodName() {
    // Initialization code
}
```

### When is @PostConstruct Called?

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

### Example 1: Basic Usage

```java
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnection {
    private String url;
    private String username;
    private String password;
    private Connection connection;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ★ Called after all properties are set
    @PostConstruct
    public void initialize() {
        System.out.println("===== @PostConstruct =====");
        System.out.println("Initializing database connection...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        
        // Validation
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Database URL cannot be empty");
        }

        // Initialization logic
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Database connection established");
        } catch (SQLException e) {
            System.out.println("✗ Failed to connect to database: " + e.getMessage());
        }
    }

    public void query(String sql) {
        System.out.println("Executing query: " + sql);
    }
}
```

**Output:**
```
===== @PostConstruct =====
Initializing database connection...
URL: jdbc:mysql://localhost:3306/mydb
Username: root
✓ Database connection established
```

---

### Example 2: Resource Initialization

```java
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CacheService {
    private Map<String, Object> cache;
    private Timer cleanupTimer;

    @PostConstruct
    public void initializeCache() {
        System.out.println("@PostConstruct: Initializing cache service");
        
        // Initialize cache
        this.cache = new HashMap<>();
        
        // Start cleanup task
        this.cleanupTimer = new Timer();
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Cache cleanup task running...");
            }
        }, 0, 60000);  // Run every 60 seconds
        
        System.out.println("✓ Cache service initialized");
    }

    public void put(String key, Object value) {
        cache.put(key, value);
        System.out.println("Cached: " + key);
    }

    public Object get(String key) {
        return cache.get(key);
    }
}
```

---

### Example 3: Configuration Loading

```java
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.Properties;

@Component
public class ConfigurationLoader {
    
    @Value("${config.file.path:config.properties}")
    private String configFilePath;
    
    private Properties properties;

    @PostConstruct
    public void loadConfiguration() {
        System.out.println("@PostConstruct: Loading configuration from " + configFilePath);
        
        this.properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
            System.out.println("✓ Configuration loaded successfully");
            properties.forEach((key, value) -> 
                System.out.println("  " + key + " = " + value)
            );
        } catch (IOException e) {
            System.out.println("✗ Failed to load configuration: " + e.getMessage());
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
```

---

## @PreDestroy Annotation

### Definition
`@PreDestroy` marks a method that should be executed **before** the Spring container destroys the bean (when the application context is being closed).

### Method Signature:
```java
@PreDestroy
public void methodName() {
    // Cleanup code
}
```

### When is @PreDestroy Called?

```
1. Application Context Closing
        ↓
2. Application Context.close() called
        ↓
3. ★ @PreDestroy Method Called ← HERE
        ↓
4. DisposableBean.destroy() (if implemented)
        ↓
5. Bean Destroyed
```

### Example 1: Resource Cleanup

```java
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.io.*;

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
        System.out.println("Written: " + message);
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("===== @PreDestroy =====");
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
        }
        
        System.out.println("✓ Cleanup completed");
    }
}
```

---

### Example 2: Connection Pool Cleanup

```java
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.sql.*;
import java.util.*;

@Component
public class ConnectionPool {
    private List<Connection> connections;
    private Queue<Connection> availableConnections;

    public ConnectionPool() {
        this.connections = new ArrayList<>();
        this.availableConnections = new LinkedList<>();
        System.out.println("ConnectionPool constructed");
    }

    public Connection getConnection() {
        if (!availableConnections.isEmpty()) {
            return availableConnections.poll();
        }
        System.out.println("No available connections in pool");
        return null;
    }

    @PreDestroy
    public void closeConnections() {
        System.out.println("===== @PreDestroy =====");
        System.out.println("Closing " + connections.size() + " database connections");
        
        for (Connection conn : connections) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("✓ Connection closed");
                }
            } catch (SQLException e) {
                System.out.println("✗ Error closing connection: " + e.getMessage());
            }
        }
        
        connections.clear();
        availableConnections.clear();
        System.out.println("✓ All connections closed");
    }
}
```

---

### Example 3: Thread Shutdown

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;

@Service
public class BackgroundTaskExecutor {
    private ExecutorService executorService;

    @PostConstruct
    public void initializeExecutor() {
        System.out.println("@PostConstruct: Creating thread pool");
        this.executorService = Executors.newFixedThreadPool(5);
        System.out.println("✓ Thread pool created with 5 threads");
    }

    public void submitTask(Runnable task) {
        executorService.submit(task);
        System.out.println("Task submitted to thread pool");
    }

    @PreDestroy
    public void shutdownExecutor() {
        System.out.println("===== @PreDestroy =====");
        System.out.println("Shutting down thread pool...");
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("⚠ Executor did not terminate gracefully, forcing shutdown");
                executorService.shutdownNow();
            }
            System.out.println("✓ Thread pool shut down successfully");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.out.println("✗ Error during thread pool shutdown: " + e.getMessage());
        }
    }
}
```

---

## How They Work

### Step-by-Step Process:

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class BeanLifecycleDemo {
    
    // Step 1: Constructor
    public BeanLifecycleDemo() {
        System.out.println("Step 1: Constructor called");
    }
    
    // Step 2: Setter (Property Injection)
    public void setName(String name) {
        System.out.println("Step 2: Setter called - name = " + name);
    }
    
    // Step 3: PostConstruct
    @PostConstruct
    public void initialize() {
        System.out.println("Step 3: @PostConstruct called - Bean is ready for use");
    }
    
    // Step 4: Using the bean
    public void doWork() {
        System.out.println("Step 4: Bean is doing work");
    }
    
    // Step 5: PreDestroy
    @PreDestroy
    public void cleanup() {
        System.out.println("Step 5: @PreDestroy called - Cleaning up resources");
    }
}
```

**Output:**
```
Step 1: Constructor called
Step 2: Setter called - name = MyBean
Step 3: @PostConstruct called - Bean is ready for use
Step 4: Bean is doing work
Step 5: @PreDestroy called - Cleaning up resources
```

---

## Execution Order

### Complete Lifecycle Order:

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

---

## Practical Examples

### Example: Complete Application Configuration

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;

@Component
public class ApplicationConfiguration {
    
    private Properties appProperties;
    private FileReader fileReader;
    private Logger logger;
    
    @PostConstruct
    public void initialize() {
        System.out.println("=== APPLICATION CONFIGURATION INITIALIZATION ===");
        
        try {
            // Initialize logger
            this.logger = new Logger();
            logger.log("Initializing application configuration");
            
            // Load properties
            this.fileReader = new FileReader("application.properties");
            this.appProperties = new Properties();
            appProperties.load(fileReader);
            
            logger.log("✓ Properties loaded: " + appProperties.size() + " entries");
            
            // Validate required properties
            validateRequiredProperties();
            
            // Initialize connections
            initializeConnections();
            
            logger.log("✓ Application configuration initialized successfully");
            
        } catch (IOException e) {
            logger.error("Failed to initialize configuration: " + e.getMessage());
            throw new RuntimeException("Initialization failed", e);
        }
    }
    
    private void validateRequiredProperties() {
        String[] required = {"database.url", "database.user", "server.port"};
        for (String prop : required) {
            if (!appProperties.containsKey(prop)) {
                throw new IllegalArgumentException("Required property missing: " + prop);
            }
        }
        logger.log("✓ All required properties validated");
    }
    
    private void initializeConnections() {
        logger.log("Initializing database connections...");
        // Database initialization code
        logger.log("✓ Database connections initialized");
    }
    
    public String getProperty(String key) {
        return appProperties.getProperty(key);
    }
    
    @PreDestroy
    public void shutdown() {
        System.out.println("=== APPLICATION CONFIGURATION SHUTDOWN ===");
        
        try {
            logger.log("Starting application shutdown");
            
            // Close connections
            logger.log("Closing database connections...");
            // Close connections logic
            logger.log("✓ Database connections closed");
            
            // Save state
            logger.log("Saving application state...");
            // Save state logic
            logger.log("✓ Application state saved");
            
            // Close file reader
            if (fileReader != null) {
                fileReader.close();
                logger.log("✓ File reader closed");
            }
            
            logger.log("✓ Application shutdown completed successfully");
            
        } catch (IOException e) {
            logger.error("Error during shutdown: " + e.getMessage());
        }
    }
    
    // Simple Logger class for demonstration
    static class Logger {
        public void log(String message) {
            System.out.println("[LOG] " + message);
        }
        
        public void error(String message) {
            System.out.println("[ERROR] " + message);
        }
    }
}
```

---

## Common Use Cases

### 1. **Database Connection Initialization**
```java
@PostConstruct
public void initializeDatabase() {
    // Create connection pool
    // Load database configuration
    // Validate connection
}

@PreDestroy
public void closeDatabase() {
    // Close connections
    // Flush cache
    // Release resources
}
```

### 2. **Configuration Loading**
```java
@PostConstruct
public void loadConfig() {
    // Read configuration files
    // Parse XML/JSON config
    // Validate configuration
}
```

### 3. **Cache Initialization**
```java
@PostConstruct
public void initializeCache() {
    // Create cache instance
    // Load initial data
    // Start cleanup tasks
}

@PreDestroy
public void flushCache() {
    // Clear cache
    // Save cache to disk
    // Stop cleanup tasks
}
```

### 4. **Thread Pool Management**
```java
@PostConstruct
public void createThreadPool() {
    this.executor = Executors.newFixedThreadPool(10);
}

@PreDestroy
public void shutdownThreadPool() {
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
}
```

### 5. **Resource Cleanup**
```java
@PostConstruct
public void allocateResources() {
    // Open files
    // Allocate memory
    // Initialize streams
}

@PreDestroy
public void releaseResources() {
    // Close files
    // Close streams
    // Release memory
}
```

---

## Best Practices

### ✅ DO's

1. **Use @PostConstruct for initialization logic**
   ```java
   @PostConstruct
   public void initialize() {
       // Setup code that depends on injected dependencies
   }
   ```

2. **Keep methods simple and focused**
   ```java
   @PostConstruct
   public void initialize() {
       validateConfiguration();
       initializeConnections();
       loadData();
   }
   ```

3. **Handle exceptions gracefully**
   ```java
   @PostConstruct
   public void initialize() {
       try {
           setupResources();
       } catch (Exception e) {
           throw new RuntimeException("Initialization failed", e);
       }
   }
   ```

4. **Use @PreDestroy for cleanup**
   ```java
   @PreDestroy
   public void cleanup() {
       closeConnections();
       saveState();
       releaseResources();
   }
   ```

5. **Make methods public and no-arg**
   ```java
   @PostConstruct
   public void initialize() {  // ✓ Correct
   }
   
   @PostConstruct
   private void initialize() {  // ✗ Wrong - must be public
   }
   ```

### ❌ DON'Ts

1. **Don't use in constructor**
   ```java
   // ✗ Wrong - dependencies not injected yet
   public MyClass() {
       resourceService.initialize();
   }
   
   // ✓ Correct - use @PostConstruct
   @PostConstruct
   public void initialize() {
       resourceService.initialize();
   }
   ```

2. **Don't throw checked exceptions**
   ```java
   // ✗ Wrong
   @PostConstruct
   public void initialize() throws SQLException {
   }
   
   // ✓ Correct
   @PostConstruct
   public void initialize() {
       try {
           // code
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
   }
   ```

3. **Don't do heavy processing in lifecycle methods**
   ```java
   // ✗ Wrong - too heavy
   @PostConstruct
   public void initialize() {
       for (int i = 0; i < 1000000; i++) {
           expensiveOperation();
       }
   }
   
   // ✓ Better - do minimal setup
   @PostConstruct
   public void initialize() {
       executor.submit(() -> loadInitialData());
   }
   ```

---

## Interview Questions & Answers

### Q1: What is @PostConstruct and when is it called?

**Answer:**
`@PostConstruct` is a lifecycle annotation that marks a method to be invoked by Spring **after** the bean has been instantiated and all dependencies have been injected. It's called before the bean is put into use.

**Execution order:**
```
Constructor → Dependency Injection → @PostConstruct → Bean Ready
```

---

### Q2: What is @PreDestroy and when is it called?

**Answer:**
`@PreDestroy` is a lifecycle annotation that marks a method to be invoked by Spring **before** the bean is destroyed (when the application context is closed). It's used for cleanup and resource release.

**Execution order:**
```
Application Context Closing → @PreDestroy → Bean Destroyed
```

---

### Q3: Can we have multiple @PostConstruct methods in the same class?

**Answer:**
No, only one method can be marked with `@PostConstruct`. If multiple methods are marked, Spring will throw an exception.

```java
// ✗ Wrong - Only one method can be @PostConstruct
@Component
public class MyClass {
    @PostConstruct
    public void init1() {}
    
    @PostConstruct
    public void init2() {}  // This will cause error
}

// ✓ Correct - Call other methods from @PostConstruct
@Component
public class MyClass {
    @PostConstruct
    public void initialize() {
        init1();
        init2();
    }
    
    private void init1() {}
    private void init2() {}
}
```

---

### Q4: What is the difference between @PostConstruct and afterPropertiesSet()?

| Feature | @PostConstruct | afterPropertiesSet() |
|---------|----------------|----------------------|
| **Type** | Annotation | Interface method |
| **Import** | javax.annotation | org.springframework.beans.factory |
| **Coupling** | No Spring coupling | Tight Spring coupling |
| **Execution Order** | First (before afterPropertiesSet) | Second |
| **Method Name** | Any name | Must be afterPropertiesSet() |
| **Modern Usage** | Recommended | Legacy support |

---

### Q5: Can @PreDestroy methods throw checked exceptions?

**Answer:**
No, `@PreDestroy` methods must not throw checked exceptions. They can only throw unchecked exceptions (RuntimeException).

```java
// ✗ Wrong
@PreDestroy
public void cleanup() throws IOException {
    file.close();  // IOException is checked
}

// ✓ Correct
@PreDestroy
public void cleanup() {
    try {
        file.close();
    } catch (IOException e) {
        throw new RuntimeException("Cleanup failed", e);
    }
}
```

---

### Q6: Does @PostConstruct method need to be public?

**Answer:**
Yes, the method must be public. It cannot be private, protected, or package-private.

```java
// ✗ Wrong
@PostConstruct
private void initialize() {}

// ✓ Correct
@PostConstruct
public void initialize() {}
```

---

### Q7: Will @PreDestroy be called for prototype beans?

**Answer:**
No, `@PreDestroy` is **only called for singleton beans**. Prototype beans are not managed by Spring after creation, so their destruction methods are not called.

```java
@Bean
@Scope("prototype")
public MyClass myBean() {
    return new MyClass();
}
// @PreDestroy will NOT be called for prototype beans
```

---

### Q8: What if @PostConstruct throws an exception?

**Answer:**
If `@PostConstruct` throws an exception, the bean creation fails and the bean is not added to the application context. This typically causes the application to fail startup.

```java
@PostConstruct
public void initialize() {
    if (config == null) {
        throw new RuntimeException("Configuration not found");
        // Application startup fails here
    }
}
```

---

### Q9: Can we use both @PostConstruct and XML init-method?

**Answer:**
Yes, both will be called, but `@PostConstruct` is called first:

```
@PostConstruct method called first
     ↓
init-method from XML called second
```

---

### Q10: How does Spring know to call @PostConstruct methods?

**Answer:**
Spring uses `CommonAnnotationBeanPostProcessor` (a `BeanPostProcessor`) which scans all beans for `@PostConstruct` and `@PreDestroy` annotations and calls the marked methods at the appropriate lifecycle stages.

---

## Key Takeaways for Interview

✓ @PostConstruct is called after bean instantiation and dependency injection  
✓ @PreDestroy is called before bean destruction during context shutdown  
✓ Only one method can have @PostConstruct and one can have @PreDestroy  
✓ Methods must be public and take no parameters  
✓ Cannot throw checked exceptions  
✓ @PostConstruct is called before afterPropertiesSet()  
✓ @PreDestroy is only called for singleton beans, not prototype  
✓ Both are standard JSR-250 annotations (javax.annotation package)  
✓ Recommended for modern Spring applications  
✓ Used for initialization logic, validation, and resource cleanup  

---

## Complete Example: Real-World Scenario

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.concurrent.*;

@Service
public class ReportService {
    
    private Connection dbConnection;
    private ExecutorService executorService;
    private ConcurrentHashMap<String, ReportData> reportCache;
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void initialize() {
        System.out.println("===== ReportService Initialization =====");
        
        try {
            // 1. Create thread pool
            this.executorService = Executors.newFixedThreadPool(5);
            this.scheduler = Executors.newScheduledThreadPool(2);
            System.out.println("✓ Thread pools created");
            
            // 2. Initialize cache
            this.reportCache = new ConcurrentHashMap<>();
            System.out.println("✓ Cache initialized");
            
            // 3. Connect to database
            this.dbConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/reports", "root", "password");
            System.out.println("✓ Database connected");
            
            // 4. Start scheduler for cache cleanup
            scheduler.scheduleAtFixedRate(this::cleanupCache, 1, 5, TimeUnit.MINUTES);
            System.out.println("✓ Cache cleanup scheduler started");
            
            System.out.println("✓ ReportService initialized successfully");
            
        } catch (SQLException e) {
            throw new RuntimeException("Initialization failed", e);
        }
    }

    public void generateReport(String reportId) {
        System.out.println("Generating report: " + reportId);
        executorService.submit(() -> {
            // Report generation logic
            reportCache.put(reportId, new ReportData(reportId));
        });
    }

    private void cleanupCache() {
        System.out.println("Running cache cleanup...");
        reportCache.clear();
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("===== ReportService Shutdown =====");
        
        try {
            // 1. Stop scheduler
            scheduler.shutdown();
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            System.out.println("✓ Scheduler shut down");
            
            // 2. Stop thread pool
            executorService.shutdown();
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            System.out.println("✓ Thread pool shut down");
            
            // 3. Flush cache to database
            reportCache.forEach((id, data) -> saveReportToDatabase(id, data));
            System.out.println("✓ Cache flushed to database");
            
            // 4. Close database connection
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
            System.out.println("✓ Database connection closed");
            
            System.out.println("✓ ReportService shutdown completed");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("✗ Error during shutdown: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("✗ Database error during shutdown: " + e.getMessage());
        }
    }

    private void saveReportToDatabase(String id, ReportData data) {
        // Save report to database
    }

    static class ReportData {
        String id;
        ReportData(String id) { this.id = id; }
    }
}
```

---

## References

- [Spring Framework Official Documentation](https://spring.io/projects/spring-framework)
- [JSR-250: Common Annotations for the Java Platform](https://jcp.org/en/jsr/detail?id=250)
- [Bean Lifecycle Callbacks](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-lifecycle-callbacks)

