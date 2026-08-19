# Spring Autowiring - Complete Guide for Interview

## Table of Contents
1. [What is Autowiring?](#what-is-autowiring)
2. [Autowiring Modes](#autowiring-modes)
3. [XML-based Autowiring](#xml-based-autowiring)
4. [Annotation-based Autowiring](#annotation-based-autowiring)
5. [Comparison: XML vs Annotations](#comparison-xml-vs-annotations)
6. [Practical Examples](#practical-examples)
7. [Common Issues & Solutions](#common-issues--solutions)
8. [Best Practices](#best-practices)
9. [Interview Questions & Answers](#interview-questions--answers)

---

## What is Autowiring?

**Autowiring** is a Spring feature that automatically injects bean dependencies without explicitly specifying them in XML configuration or using annotations.

### Key Concept:
Instead of manually wiring dependencies using `<constructor-arg>` or `<property>`, Spring can automatically find and inject matching beans from the application context.

### Example Comparison:

**Manual Wiring (Without Autowiring):**
```xml
<bean id="employee" class="com.example.Employee">
    <property name="address" ref="address" />
    <property name="department" ref="department" />
</bean>

<bean id="address" class="com.example.Address" />
<bean id="department" class="com.example.Department" />
```

**Autowiring (Automatic):**
```xml
<bean id="employee" class="com.example.Employee" autowire="byType" />
<bean id="address" class="com.example.Address" />
<bean id="department" class="com.example.Department" />
<!-- Spring automatically injects matching beans -->
```

---

## Autowiring Modes

Spring supports 5 different autowiring modes:

### 1. **no** (Default - No Autowiring)

No automatic injection. Dependencies must be explicitly wired.

```xml
<bean id="employee" class="com.example.Employee" autowire="no">
    <property name="address" ref="address" />
</bean>
```

---

### 2. **byName** (Autowire by Property Name)

Spring looks for a bean with the **same name** as the property and automatically injects it.

#### Java Class:
```java
public class Employee {
    private Address address;  // Property name: 'address'
    private Department department;  // Property name: 'department'

    // Setter methods
    public void setAddress(Address address) {
        this.address = address;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void display() {
        System.out.println("Employee: " + address + ", " + department);
    }
}
```

#### XML Configuration:
```xml
<!-- Autowiring byName -->
<bean id="employee" class="com.example.Employee" autowire="byName" />

<!-- Bean names MUST match property names -->
<bean id="address" class="com.example.Address">
    <property name="city" value="New York" />
</bean>

<bean id="department" class="com.example.Department">
    <property name="name" value="IT" />
</bean>
```

#### How it Works:
1. Spring finds the `employee` bean with `autowire="byName"`
2. Looks at all properties: `address`, `department`
3. Searches for beans with matching names: `address`, `department`
4. Finds them and automatically injects

#### Output:
```
Employee: Address{city='New York'}, Department{name='IT'}
```

**Advantage:** Clear and explicit - property name tells you the bean name  
**Disadvantage:** Tight coupling between property names and bean names

---

### 3. **byType** (Autowire by Property Type)

Spring looks for a bean with the **same type** as the property and automatically injects it.

#### Java Class:
```java
public class Employee {
    private Address address;  // Type: Address
    private Department department;  // Type: Department

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void display() {
        System.out.println("Employee: " + address + ", " + department);
    }
}
```

#### XML Configuration:
```xml
<!-- Autowiring byType -->
<bean id="employee" class="com.example.Employee" autowire="byType" />

<!-- Bean IDs can be anything - type matching is used -->
<bean id="addr" class="com.example.Address">
    <property name="city" value="New York" />
</bean>

<bean id="dept" class="com.example.Department">
    <property name="name" value="IT" />
</bean>
```

#### How it Works:
1. Spring finds the `employee` bean with `autowire="byType"`
2. Looks at property types: `Address`, `Department`
3. Searches for beans of type `Address` and `Department`
4. Finds them (regardless of bean ID) and automatically injects

#### Output:
```
Employee: Address{city='New York'}, Department{name='IT'}
```

**Advantage:** More flexible - bean IDs don't matter  
**Disadvantage:** Fails if multiple beans of same type exist

---

### 4. **constructor** (Autowire via Constructor)

Spring autowires constructor parameters by **type** (similar to byType).

#### Java Class:
```java
public class Employee {
    private Address address;
    private Department department;

    // Constructor with parameters
    public Employee(Address address, Department department) {
        this.address = address;
        this.department = department;
        System.out.println("Employee created via constructor");
    }

    public void display() {
        System.out.println("Employee: " + address + ", " + department);
    }
}
```

#### XML Configuration:
```xml
<!-- Autowiring via constructor -->
<bean id="employee" class="com.example.Employee" autowire="constructor" />

<!-- Beans to be injected via constructor -->
<bean id="address" class="com.example.Address">
    <property name="city" value="New York" />
</bean>

<bean id="department" class="com.example.Department">
    <property name="name" value="IT" />
</bean>
```

#### How it Works:
1. Spring finds the `employee` bean with `autowire="constructor"`
2. Analyzes constructor parameters: `Address`, `Department`
3. Searches for beans matching these types
4. Calls constructor with matching beans

#### Output:
```
Employee created via constructor
Employee: Address{city='New York'}, Department{name='IT'}
```

**Advantage:** Enables immutability with final fields  
**Disadvantage:** Must have proper constructor

---

### 5. **autodetect** (Spring 3.0+ deprecated)

Spring automatically chooses between `constructor` and `byType` based on presence of default constructor.

```xml
<bean id="employee" class="com.example.Employee" autowire="autodetect" />
```

**Note:** This mode is deprecated in Spring 3.0+

---

## XML-based Autowiring

### Syntax:
```xml
<bean id="beanId" class="com.example.ClassName" autowire="mode" />
```

### Example 1: Complete Employee-Department Setup (byName)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Autowiring byName -->
    <bean id="employee" class="com.example.Employee" autowire="byName" />
    
    <!-- Property names MUST match autowire="byName" bean IDs -->
    <bean id="address" class="com.example.Address">
        <property name="street" value="123 Main St" />
        <property name="city" value="New York" />
        <property name="zip" value="10001" />
    </bean>

    <bean id="department" class="com.example.Department">
        <property name="name" value="Information Technology" />
        <property name="budget" value="100000" />
    </bean>

</beans>
```

---

### Example 2: Constructor Autowiring

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Autowiring via constructor -->
    <bean id="employee" class="com.example.Employee" autowire="constructor" />
    
    <!-- Type matching for constructor parameters -->
    <bean id="address" class="com.example.Address">
        <property name="city" value="New York" />
    </bean>

    <bean id="department" class="com.example.Department">
        <property name="name" value="IT" />
    </bean>

</beans>
```

---

### Example 3: Mixing Manual and Autowiring

```xml
<bean id="employee" class="com.example.Employee" autowire="byType">
    <!-- Some properties can still be explicitly configured -->
    <property name="name" value="John Doe" />
    <property name="salary" value="50000" />
    <!-- Other properties are autowired -->
</bean>
```

---

## Annotation-based Autowiring

### Common Annotations:

| Annotation | Location | Behavior |
|-----------|----------|----------|
| `@Autowired` | Property, Constructor, Setter | Type-based injection |
| `@Resource` | Property, Setter | Name or type-based |
| `@Inject` | Property, Constructor, Setter | Type-based (JSR-330) |
| `@Qualifier` | With @Autowired | Specify exact bean |

---

### 1. **@Autowired - Type-based Injection**

#### Java Class:
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Employee {
    private String name = "John";

    // Field Injection
    @Autowired
    private Address address;

    @Autowired
    private Department department;

    public void display() {
        System.out.println("Employee: " + name);
        System.out.println("Address: " + address);
        System.out.println("Department: " + department);
    }
}
```

#### Java Configuration:
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@ComponentScan("com.example")
public class AppConfig {
    // Beans will be auto-discovered via @Component
}
```

#### Output:
```
Employee: John
Address: Address{city='New York'}
Department: Department{name='IT'}
```

---

### 2. **@Autowired on Setter**

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Employee {
    private Address address;
    private Department department;

    // Setter Injection
    @Autowired
    public void setAddress(Address address) {
        this.address = address;
        System.out.println("Address injected via setter");
    }

    @Autowired
    public void setDepartment(Department department) {
        this.department = department;
        System.out.println("Department injected via setter");
    }

    public void display() {
        System.out.println("Employee -> " + address + ", " + department);
    }
}
```

**Output:**
```
Address injected via setter
Department injected via setter
Employee -> Address{...}, Department{...}
```

---

### 3. **@Autowired on Constructor**

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Employee {
    private final Address address;
    private final Department department;

    // Constructor Injection
    @Autowired
    public Employee(Address address, Department department) {
        this.address = address;
        this.department = department;
        System.out.println("Employee created with constructor injection");
    }

    public void display() {
        System.out.println("Employee -> " + address + ", " + department);
    }
}
```

**Output:**
```
Employee created with constructor injection
Employee -> Address{...}, Department{...}
```

---

### 4. **@Autowired with @Qualifier**

When multiple beans of the same type exist, use `@Qualifier` to specify which one.

#### Java Classes:
```java
// Database interface
public interface DataSource {
    void connect();
}

// Implementation 1
@Component("mysqlDataSource")
public class MySQLDataSource implements DataSource {
    @Override
    public void connect() {
        System.out.println("Connecting to MySQL");
    }
}

// Implementation 2
@Component("oracleDataSource")
public class OracleDataSource implements DataSource {
    @Override
    public void connect() {
        System.out.println("Connecting to Oracle");
    }
}

// Using @Qualifier
@Component
public class DatabaseService {
    
    @Autowired
    @Qualifier("mysqlDataSource")  // Specify which bean to inject
    private DataSource dataSource;

    public void connectToDatabase() {
        dataSource.connect();
    }
}
```

**Output:**
```
Connecting to MySQL
```

---

### 5. **@Resource (JSR-250)**

Similar to `@Autowired` but searches by **name first**, then type.

```java
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class Employee {
    
    // By default, searches for bean named "address"
    @Resource
    private Address address;

    // Explicitly specify bean name
    @Resource(name = "myDepartment")
    private Department department;

    public void display() {
        System.out.println("Employee -> " + address + ", " + department);
    }
}
```

---

### 6. **@Inject (JSR-330)**

Standard Java annotation (similar to `@Autowired`).

```java
import javax.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class Employee {
    
    @Inject
    private Address address;

    @Inject
    private Department department;

    public void display() {
        System.out.println("Employee -> " + address + ", " + department);
    }
}
```

---

## Comparison: XML vs Annotations

### XML-based Autowiring

**Pros:**
- Centralized configuration in one file
- No changes to Java code needed
- Clear dependency graph
- Easy to manage in large projects

**Cons:**
- Verbose XML
- Not discoverable (must read XML)
- Configuration scattered from code
- Hard to refactor

### Annotation-based Autowiring

**Pros:**
- Concise and clean
- Annotations close to code
- Self-documenting
- Easy to refactor
- Less boilerplate

**Cons:**
- Configuration mixed with code
- Multiple files to check
- Less centralized control
- May scatter configuration

---

### Side-by-Side Comparison

| Feature | XML Autowiring | Annotation Autowiring |
|---------|----------------|----------------------|
| **Configuration** | Centralized in XML | Distributed in classes |
| **Verbosity** | More verbose | Less verbose |
| **Discoverability** | Requires reading XML | Visible in code |
| **Performance** | Same | Same |
| **Flexibility** | Very flexible | Less flexible |
| **Best for** | Large enterprise apps | Modern microservices |
| **Learning Curve** | Steeper | Easier |

---

## Practical Examples

### Example 1: Complete Project Structure

#### Project Structure:
```
src/
├── com/example/
│   ├── model/
│   │   ├── Address.java
│   │   ├── Department.java
│   │   └── Employee.java
│   ├── service/
│   │   ├── EmployeeService.java
│   │   └── AddressService.java
│   ├── AppConfig.java
│   └── Main.java
└── applicationContext.xml
```

#### Address.java:
```java
public class Address {
    private String street;
    private String city;
    private String zip;

    // Getters and Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    @Override
    public String toString() {
        return "Address{" + "street='" + street + "', city='" + city + 
               "', zip='" + zip + "'}";
    }
}
```

#### Department.java:
```java
public class Department {
    private String name;
    private String location;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return "Department{" + "name='" + name + "', location='" + location + "'}";
    }
}
```

#### Employee.java (Annotation-based):
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Employee {
    private String empId = "EMP001";
    private String name = "John Doe";

    @Autowired
    private Address address;

    @Autowired
    private Department department;

    @Autowired
    private EmployeeService employeeService;

    public void display() {
        System.out.println("Employee ID: " + empId);
        System.out.println("Name: " + name);
        System.out.println("Address: " + address);
        System.out.println("Department: " + department);
    }

    public void work() {
        System.out.println("Employee " + name + " is working...");
        employeeService.logWork(empId);
    }
}
```

#### EmployeeService.java:
```java
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    public void logWork(String empId) {
        System.out.println("Logged work for employee: " + empId);
    }
}
```

#### XML Configuration (applicationContext.xml):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- Enable component scanning for annotations -->
    <context:component-scan base-package="com.example" />

    <!-- XML-based bean definitions -->
    <bean id="address" class="com.example.model.Address" autowire="byName">
        <property name="street" value="123 Main Street" />
        <property name="city" value="New York" />
        <property name="zip" value="10001" />
    </bean>

    <bean id="department" class="com.example.model.Department" autowire="byName">
        <property name="name" value="Engineering" />
        <property name="location" value="New York Office" />
    </bean>

</beans>
```

#### Java Configuration (AppConfig.java):
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import com.example.model.*;

@Configuration
@ComponentScan("com.example")
public class AppConfig {
    
    @Bean
    public Address address() {
        Address address = new Address();
        address.setStreet("123 Main Street");
        address.setCity("New York");
        address.setZip("10001");
        return address;
    }

    @Bean
    public Department department() {
        Department dept = new Department();
        dept.setName("Engineering");
        dept.setLocation("New York Office");
        return dept;
    }
}
```

#### Main.java:
```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.example.model.Employee;

public class Main {
    public static void main(String[] args) {
        // Load from XML
        ApplicationContext context = new ClassPathXmlApplicationContext(
            "applicationContext.xml");
        
        Employee employee = context.getBean(Employee.class);
        employee.display();
        employee.work();
    }
}
```

**Output:**
```
Employee ID: EMP001
Name: John Doe
Address: Address{street='123 Main Street', city='New York', zip='10001'}
Department: Department{name='Engineering', location='New York Office'}
Employee John Doe is working...
Logged work for employee: EMP001
```

---

### Example 2: Using @Qualifier with Multiple Implementations

```java
// Interface
public interface PaymentService {
    void processPayment(double amount);
}

// Implementation 1
@Component("creditCardPayment")
public class CreditCardPayment implements PaymentService {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing credit card payment: $" + amount);
    }
}

// Implementation 2
@Component("upiPayment")
public class UPIPayment implements PaymentService {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing UPI payment: $" + amount);
    }
}

// Consumer class
@Component
public class OrderService {
    
    @Autowired
    @Qualifier("creditCardPayment")
    private PaymentService paymentService;

    public void placeOrder(double amount) {
        System.out.println("Order placed for amount: $" + amount);
        paymentService.processPayment(amount);
    }
}
```

**Usage:**
```java
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
OrderService orderService = context.getBean(OrderService.class);
orderService.placeOrder(100.0);
```

**Output:**
```
Order placed for amount: $100.0
Processing credit card payment: $100.0
```

---

## Common Issues & Solutions

### Issue 1: No Qualifying Bean of Type Found

**Error:**
```
No qualifying bean of type 'com.example.Address' available
```

**Causes:**
- Bean not defined
- Component not scanned
- Wrong bean type

**Solution:**
```java
// Ensure bean is defined
@Component
public class Address { }

// OR in XML
<bean id="address" class="com.example.Address" />

// Ensure component scanning is enabled
@ComponentScan("com.example")
```

---

### Issue 2: Multiple Beans of Same Type

**Error:**
```
expected single matching bean but found 2: mysqlDataSource, oracleDataSource
```

**Solution:**
Use `@Qualifier` to specify which bean:
```java
@Autowired
@Qualifier("mysqlDataSource")
private DataSource dataSource;
```

---

### Issue 3: @Autowired on Required Dependency Returns Null

**Error:**
```
NullPointerException when accessing autowired field
```

**Causes:**
- Bean not created
- Component scanning not enabled
- Autowire disabled

**Solution:**
```java
// Option 1: Enable component scanning
@ComponentScan("com.example")

// Option 2: Explicit bean definition
@Bean
public Address address() {
    return new Address();
}

// Option 3: Set required=false if optional
@Autowired(required = false)
private Address address;
```

---

### Issue 4: Circular Dependency

**Error:**
```
The dependencies of some of the beans in the application context form a cycle
```

**Example:**
```java
// Class A depends on B
@Component
public class A {
    @Autowired
    private B b;
}

// Class B depends on A (circular)
@Component
public class B {
    @Autowired
    private A a;
}
```

**Solution:**
Use constructor injection to detect at startup:
```java
// Constructor injection detects circular dependency immediately
@Component
public class A {
    private final B b;
    
    public A(B b) {  // Will throw BeanCurrentlyInCreationException if circular
        this.b = b;
    }
}
```

---

## Best Practices

### ✅ DO's

1. **Use Constructor Injection for Required Dependencies**
   ```java
   @Component
   public class Service {
       private final Repository repository;
       
       // Constructor injection - immutable, testable
       public Service(Repository repository) {
           this.repository = repository;
       }
   }
   ```

2. **Use @Qualifier When Multiple Beans Exist**
   ```java
   @Autowired
   @Qualifier("primaryDataSource")
   private DataSource dataSource;
   ```

3. **Enable Component Scanning Explicitly**
   ```java
   @Configuration
   @ComponentScan(basePackages = "com.example")
   public class AppConfig { }
   ```

4. **Use @Autowired(required = false) for Optional Dependencies**
   ```java
   @Autowired(required = false)
   private OptionalService optionalService;
   ```

5. **Prefer Annotations in Modern Spring Applications**
   ```java
   @Component
   @Autowired
   private Dependency dependency;  // Cleaner than XML
   ```

### ❌ DON'Ts

1. **Don't Mix Too Many Autowiring Modes**
   ```java
   // ✗ Confusing mix of autowiring modes
   <bean id="a" class="A" autowire="byType" />
   <bean id="b" class="B" autowire="byName" />
   <bean id="c" class="C" autowire="constructor" />
   ```

2. **Don't Autowire Everything**
   ```java
   // ✗ Autowiring configuration properties
   @Autowired
   private String databaseUrl;
   
   // ✓ Use @Value for properties
   @Value("${database.url}")
   private String databaseUrl;
   ```

3. **Don't Use Field Injection in Production Code**
   ```java
   // ✗ Hard to test, can't use final
   @Autowired
   private Service service;
   
   // ✓ Use constructor injection
   public class Client {
       private final Service service;
       
       public Client(Service service) {
           this.service = service;
       }
   }
   ```

4. **Don't Leave Circular Dependencies Unresolved**
   ```java
   // ✗ Causes runtime errors
   // Use constructor injection to detect at startup
   ```

---

## Interview Questions & Answers

### Q1: What is Autowiring in Spring?

**Answer:**
Autowiring is a Spring feature that automatically injects bean dependencies without explicit XML configuration or setter methods. Spring automatically discovers and wires beans based on the specified autowiring mode.

---

### Q2: What are the different autowiring modes?

**Answer:**
Spring supports 5 autowiring modes:

1. **no** - No automatic injection (default)
2. **byName** - Matches property name with bean name
3. **byType** - Matches property type with bean type
4. **constructor** - Injects via constructor parameters (byType)
5. **autodetect** - Chooses between constructor and byType (deprecated)

---

### Q3: Difference between @Autowired and @Resource?

**Answer:**

| Feature | @Autowired | @Resource |
|---------|-----------|-----------|
| **Search Mechanism** | By Type first, then name | By Name first, then type |
| **Package** | org.springframework.beans | javax.annotation |
| **Flexibility** | High with @Qualifier | Good with name attribute |
| **Use with @Qualifier** | Yes | Optional |

---

### Q4: What happens if multiple beans of same type exist?

**Answer:**
Spring throws `NoUniqueBeanDefinitionException`. Use `@Qualifier` to specify which bean:

```java
@Autowired
@Qualifier("specificBeanName")
private MyService service;
```

---

### Q5: Can we disable autowiring for a specific bean?

**Answer:**
Yes, set `autowire-candidate="false"`:

```xml
<bean id="notAutoWired" class="com.example.MyClass" 
      autowire-candidate="false" />
```

---

### Q6: Which autowiring mode should we use?

**Answer:**
- **byType**: Most common, recommended for most cases
- **byName**: When bean names are consistent with property names
- **constructor**: For immutable beans with required dependencies
- **no**: Legacy code or explicit control needed

Modern recommendation: **Use annotations (@Autowired) instead of XML autowiring**

---

### Q7: What is the difference between byName and byType autowiring?

**Answer:**

```java
public class Employee {
    private Address address;  // Type: Address, Name: address
}
```

**byName:**
- Looks for bean with ID/name = "address"
- Bean ID must match property name

**byType:**
- Looks for bean of type `Address`
- Bean ID can be anything

---

### Q8: Can @Autowired be used on constructors?

**Answer:**
Yes. Starting from Spring 4.3, `@Autowired` on constructor is optional:

```java
// Spring 4.3+: @Autowired is optional for single constructor
@Component
public class Service {
    private final Repository repository;
    
    public Service(Repository repository) {
        this.repository = repository;
    }
}

// Multiple constructors: @Autowired required
@Component
public class Service {
    private Repository repository;
    
    @Autowired  // Must specify which constructor
    public Service(Repository repo) {
        this.repository = repo;
    }
    
    public Service() { }
}
```

---

### Q9: What is the difference between autowire="no" and no autowiring?

**Answer:**
No difference. `autowire="no"` is the default. Beans must be explicitly wired:

```xml
<!-- Both are equivalent -->
<bean id="employee" class="com.example.Employee" autowire="no">
    <property name="address" ref="address" />
</bean>

<bean id="employee" class="com.example.Employee">
    <property name="address" ref="address" />
</bean>
```

---

### Q10: Why is constructor injection better than field injection?

**Answer:**
1. **Immutability**: Can use `final` fields
2. **Testability**: Easy to create instances with mock dependencies
3. **Null Safety**: Constructor won't accept null dependencies
4. **Clear Dependencies**: Obvious from constructor signature

```java
// ✓ Better: Constructor injection
public class Service {
    private final Repository repository;
    
    public Service(Repository repository) {
        this.repository = repository;
    }
}

// ✗ Avoid: Field injection
public class Service {
    @Autowired
    private Repository repository;  // Mutable, hard to test
}
```

---

## Key Takeaways for Interview

✓ Autowiring automates dependency injection in Spring  
✓ 5 modes: no, byName, byType, constructor, autodetect  
✓ byType is most commonly used  
✓ @Autowired is modern annotation-based approach  
✓ Use @Qualifier for multiple beans of same type  
✓ Constructor injection is preferred over field injection  
✓ @Resource searches by name first, then type  
✓ Circular dependencies are detected early with constructor injection  
✓ Component scanning must be enabled for annotations  
✓ XML autowiring for centralized config, annotations for modern apps  

---

## References & Further Learning

- [Spring Framework Autowiring Documentation](https://spring.io/projects/spring-framework)
- [Spring Best Practices](https://spring.io/guides)
- [Dependency Injection Pattern](https://refactoring.guru/design-patterns/dependency-injection)

