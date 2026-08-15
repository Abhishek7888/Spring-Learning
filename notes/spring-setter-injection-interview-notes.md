# Spring Setter Injection — Beginner Interview Notes

This note gives a clear, beginner-friendly overview of setter injection in Spring, short code examples, pros/cons, common pitfalls, and sample interview questions with answers. Add this to your repo to review before interviews.

## What is Setter Injection?
Setter injection is a form of Dependency Injection (DI) where the container (Spring) calls setter methods on your bean to inject dependencies. Instead of passing dependencies through the constructor, Spring sets them after creating the bean using public setter methods.

Key idea:
- Beans expose `setX(...)` methods for dependencies.
- Spring calls those setters and passes in other beans or values from configuration.

## How it works (high level)
- Spring creates the bean instance (using the no-arg constructor).
- Spring resolves the dependency bean/value.
- Spring calls the setter method to assign the dependency.

Because the bean is created before dependencies are set, setter injection allows optional dependencies and can help resolve certain circular dependencies.

## Examples
### 1) Plain Java class
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

    // getters, toString, business methods...
}
```

### 2) XML configuration
```xml
<!-- bean definitions -->
<bean id="department" class="com.example.Department"/>

<bean id="employee" class="com.example.Employee">
    <property name="name" value="Alice"/>
    <property name="department" ref="department"/>
</bean>
```

### 3) Java + Spring annotations
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
- `@Autowired` can be used on setter methods for injection from the container.
- `@Value` injects literal values or properties into setters.
- `@Autowired(required=false)` can make a dependency optional.

## Setter vs Constructor Injection (short)
- Constructor injection:
  - Dependencies are provided when object is created (immutable after construction).
  - Promotes immutability and easier testing.
  - Preferred for mandatory dependencies.
- Setter injection:
  - Dependencies are set after construction.
  - Good for optional dependencies and when you want a no-arg constructor.
  - Can help with circular dependency resolution.

When to prefer each:
- Use constructor injection for required dependencies and when writing immutable components.
- Use setter injection for optional dependencies or when a bean has many optional settings.

## Pros of Setter Injection
- Allows optional dependencies.
- Beans can be created with default construction and configured later.
- Easier to provide many optional properties without long constructor signatures.

## Cons of Setter Injection
- Dependencies can be left unset accidentally (risk of NullPointerException if not handled).
- Objects may be in a partially-initialized state between construction and setter calls.
- Harder to enforce required dependencies compared to constructor injection.

## Circular Dependencies
- Setter injection can resolve certain circular dependencies because Spring can create bean instances first and then inject dependencies via setters.
- Constructor injection cannot resolve circular constructor dependencies (it fails with a circular reference error).

## Common Pitfalls and Tips
- If a dependency is required, prefer constructor injection to fail fast on startup rather than getting a runtime NPE.
- Use `@Autowired(required=false)` for optional dependencies; always check for null in code.
- Keep setters simple and avoid heavy logic in setters—treat them as injection points only.
- Use `@PostConstruct` for initialization logic that depends on injected fields rather than in setters.

## Sample Interview Questions (with short answers)
1) Q: What is setter injection?
   A: A DI technique where Spring injects dependencies by calling setter methods on a bean after creating it.

2) Q: How do you annotate a setter for dependency injection?
   A: Use `@Autowired` on the setter method. For values, use `@Value("...")`.

3) Q: When would you use setter injection instead of constructor injection?
   A: When the dependency is optional, when there are many properties (to avoid long constructors), or to help resolve circular dependencies.

4) Q: Which injection type is better for mandatory dependencies?
   A: Constructor injection, because it enforces the dependency at object creation time.

5) Q: Can setter injection help with circular dependencies?
   A: Yes, because Spring can instantiate beans first and then inject dependencies through setters.

6) Q: How do you make a setter-injected dependency optional?
   A: Use `@Autowired(required=false)` on the setter or accept `@Nullable` (from Spring) and check for null.

7) Q: Any drawbacks of setter injection?
   A: Risk of partially-initialized bean state, accidental nulls, and weaker guarantees about required dependencies.

## Quick checklist for interviews
- Explain what setter injection is and how it differs from constructor injection.
- Be able to show a simple setter method with `@Autowired` and an XML `<property>` example.
- Mention pros/cons and when to prefer each style.
- Know how circular dependencies behave with setter injection.

## Further reading
- Official Spring Framework documentation on Dependency Injection: https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans

---

Happy studying! Keep this note in your repo and review the sample Q&A aloud to prepare for interviews.
