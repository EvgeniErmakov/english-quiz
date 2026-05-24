# Code Style and Naming Conventions

---

## 1. Naming Conventions

### 1.1 Общие правила

| Элемент | Стиль | Пример |
| --- | --- | --- |
| Класс / Интерфейс | `UpperCamelCase` | `UserService` |
| Метод / Поле | `lowerCamelCase` | `findByEmail`, `isActive` |
| Константа | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Пакет | `lowercase` | `com.company.user.service` |
| Переменная | `lowerCamelCase` | `userDto`, `orderId` |
| Enum-значение | `UPPER_SNAKE_CASE` | `OrderStatus.IN_PROGRESS` |

---

### 1.2 Именование классов по типу

#### DTO

Суффикс `Dto` (не `DTO`) следуем `UpperCamelCase`. Префикс отражает **назначение**: `Create`, `Update`, `Response`.

```

```

❌ **Bad**

```java
public class UserData { ... }   // неясно, что это DTO
public class UserDTO { ... }    // нарушение CamelCase
```

✅ **Good**

```java
public class CreateUserDto { ... }    // запрос на создание
public class UpdateUserDto { ... }    // запрос на обновление
public class UserResponseDto { ... }  // ответ клиенту
```

UserDto можно использовать в слоях, используйте как общую модель данных пользователя вне слоя хранения. Это стандартная проекция Entity, предназначенная для безопасного обмена данными после маппинга

#### Service

Обязательно указываем постфикс Service для сервисных классов:

❌ **Bad**

```java
public class UserManager { ... }
public class UserHelper { ... }
```

✅ **Good**

```java
public class UserService { ... }                             // реализация без интерфейса
public interface UserService { }                             // интерфейс без суффикса
public class UserServiceImpl implements UserService { ... }  // реализация интерфейса
```

#### Repository

Обязательно указываем постфикс Repository для репозиторией:

❌ **Bad**

```java
public interface UserDao { ... }
public interface UserDB { ... }
```

✅ **Good**

```java
public interface UserRepository extends JpaRepository<UserEntity, Long> { ... }
```

#### Controller

Обязательно указываем постфикс Controller для контроллеров:

❌ **Bad**

```java
public class UserHandler { ... }
public class UserAPI { ... }
```

✅ **Good**

```java
@RestController
public class UserController { ... }
```

---

### 1.3 Именование интерфейсов

Интерфейс **не** должен иметь префикса `I` (это устаревший C#-стиль). Реализация получает суффикс `Impl` только если есть несколько реализаций или это требуется архитектурно.

❌ **Bad**

```java
public interface IUserService { ... }         // префикс I это не Java-стиль
public interface UserServiceInterface { ... } // многословно
```

✅ **Good**

```java
public interface UserService { ... }
public class UserServiceImpl implements UserService { ... }

// Если реализация одна и слой не требует интерфейса:
@Service
public class UserService { ... }
```

---

### 1.4 Именование методов

#### Общие методы

| Категория | Префикс | Пример |
| --- | --- | --- |
| Получить объект | `find` | `findById()`, `findActiveOrders()` |
| Булев результат | `is`, `has`, `can`, `exists` | `isActive()`, `hasPermission()`, `existsByEmail()` |
| Создать | `create`, `register` | `createOrder()`, `registerUser()` |
| Обновить | `update`, `change` | `updateStatus()`, `changePassword()` |
| Удалить | `delete`, `remove` | `deleteById()`, `removeExpired()` |
| Рассчитать | `calculate`, `compute` | `calculateTotal()` |
| Отправить | `send`, `publish`, `notify` | `sendEmail()`, `publishEvent()` |

Примеры:

✅ **Good**

```java
public UserResponseDto findById(Long id) { ... }
public void createUser(CreateUserDto dto) { ... }
public void updateUser(Long id, UpdateUserDto dto) { ... }
public void deleteUser(Long id) { ... }
```

#### Boolean-методы

Boolean-методы **обязательно** начинаются с `is`, `has`, `can`, `should`, `exists`.

❌ **Bad**

```java
public boolean activeUser(Long id) { ... }
public boolean checkEmail(String email) { ... }
public boolean userExists(Long id) { ... }  // существительное + глагол, это читается плохо
```

✅ **Good**

```java
public boolean isActive(Long id) { ... }
public boolean isEmailTaken(String email) { ... }
public boolean existsById(Long id) { ... }
public boolean hasPermission(Long userId, String permission) { ... }
public boolean canBeDeleted(Long orderId) { ... }
```

---

### 1.5 Именование переменных

Переменные коллекций следует писать всегда во **множественном числе**. Однобуквенные имена запрещены (кроме счётчиков `i`, `j` в циклах).

❌ **Bad**

```java
UserEntity u = userRepository.findById(id);        // однобуквенные переменные
List<UserEntity> list = userRepository.findAll();  // list - ничего не говорит
Optional<UserEntity> opt = ...;                    // opt - непонятно, что внутри
List<OrderEntity> orderList = ...;                 // суффикс List - лишний
Set<String> roleSet = ...;                         // суффикс Set - лишний
```

✅ **Good**

```java
UserEntity user = userRepository.findById(id);
List<UserEntity> users = userRepository.findAll();
Optional<UserEntity> userOptional = ...;
List<OrderEntity> orders = ...;
Set<String> roles = ...;
```

---

## 2. Constants and Statics

### 2.1 Объявление констант

Константы объявляются как `static final` в `UPPER_SNAKE_CASE`. Порядок модификаторов: `public/private static final`.

❌ **Bad**

```java
public static final int maxRetry = 3;         // нарушение UPPER_SNAKE_CASE
public final static String BASE_URL = "/api"; // неверный порядок модификаторов
private static final String url = "/users";   // не выглядит как константа
```

✅ **Good**

```java
public static final int MAX_RETRY_COUNT = 3;
public static final String API_BASE_PATH = "/api/v1";
private static final String DEFAULT_SORT_FIELD = "createdAt";
```

---

### 2.2 Где размещать константы

**Правило:** константа живёт там, где используется. Чем уже область применения, тем ближе к использованию.

#### Внутри класса - если используется только в нём

```java
@Service
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    public void authenticate(String login, String password) {
        if (loginAttempts > MAX_LOGIN_ATTEMPTS) { ... }
    }
}
```

#### Утилитный класс - если используется в нескольких местах

```java
public final class ApiPaths {

    private ApiPaths() {}

    public static final String USERS  = "/api/v1/users";
    public static final String ORDERS = "/api/v1/orders";
    public static final String AUTH   = "/api/v1/auth";
}

public final class ErrorMessages {

    private ErrorMessages() {}

    public static final String USER_NOT_FOUND       = "User with id=%d not found";
    public static final String EMAIL_ALREADY_EXISTS = "Email %s is already taken";
}
```

### 2.3 Изменяемые static-поля

Изменяемые `static`-поля это почти всегда признак проблемы в многопоточной среде. Используются только с явным обоснованием.

❌ **Bad**

```java
public class RequestCounter {
    public static int count = 0; // состояние гонки в многопоточной среде
}
```

✅ **Good**: атомарный тип, если глобальный счётчик действительно нужен

```java
public class RequestCounter {
    private static final AtomicInteger REQUEST_COUNT = new AtomicInteger(0);

    public static int increment() {
        return REQUEST_COUNT.incrementAndGet();
    }
}
```

---

## 3. Class Structure (Layout)

### 3.1 Эталонный порядок элементов класса

Следующий порядок применяется ко **всем** классам, он делает любой файл предсказуемым для чтения:

1. `static final` константы
2. `static` поля (изменяемые только с обоснованием)
3. Поля экземпляра (instance fields)
4. Внедряемые зависимости (через конструктор `final` поля)
5. Конструктор(ы)
6. Публичные методы (`public`)
7. Методы пакетного доступа (package-private)
8. Защищённые методы (`protected`)
9. Приватные вспомогательные методы (`private`)
10. Вложенные классы и enum  если есть

---

### 3.2 Пример эталонного класса для Service

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    // 1. Константы
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final String DEFAULT_ROLE = "ROLE_USER";

    // 2. Зависимости final, конструктор генерирует @RequiredArgsConstructor
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // 3. Публичные методы
    public UserResponseDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public UserResponseDto create(CreateUserDto dto) {
        validateEmailUniqueness(dto.getEmail());
        UserEntity user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        UserEntity saved = userRepository.save(user);
        log.info("User created: id={}", saved.getId());
        return userMapper.toDto(saved);
    }

    public void delete(Long id) {
        UserEntity user = getOrThrow(id);
        userRepository.delete(user);
        log.info("User deleted: id={}", id);
    }

    // 4. Приватные вспомогательные методы
    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already taken: " + email);
        }
    }

    private UserEntity getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
```

---

### 3.3 Пример эталонного класса для Entity

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    // 1. Константы
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // 2. Первичный ключ
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 3. Бизнес-поля
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // 4. Технические поля
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 5. Связи
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<OrderEntity> orders = new ArrayList<>();

    // 6. equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity other)) return false;
        return id != null && id.equals(other.id);
    }
}
```

---

### 3.4 Инжекция зависимостей выполнять только через конструктор

❌ **Bad** Field Injection

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;   // нельзя тестировать без Spring-контекста
    @Autowired
    private PasswordEncoder passwordEncoder; // зависимость скрыта, может быть null
}
```

❌ **Bad** Setter Injection

```java
@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository repo) {
        this.userRepository = repo; // поле не final, такую зависимость можно заменить
    }
}
```

✅ **Good** Constructor Injection + Lombok

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;   // final зависимость неизменна
    private final PasswordEncoder passwordEncoder; // тест: передаём mock в конструктор
}
```

**Почему constructor injection:**

- Зависимости явны и обязательны, для них нельзя создать объект без них
- Тестируется без Spring-контекста: просто `new UserService(mockRepo, ...)`
- `final` поля исключают случайную замену зависимости в рантайме

---

## 4. Spring Boot Specifics

### 4.1 Использование Lombok

Lombok экономит время, но неосознанное использование создаёт скрытые баги.

#### Шпаргалка что использовать и где

| Аннотация | Где использовать | Где запрещено |
| --- | --- | --- |
| `@Slf4j` | везде, где нужен логгер | - |
| `@RequiredArgsConstructor` | Service, Component, Controller | - |
| `@Getter` / `@Setter` | DTO, Entity | - |
| `@Builder` | DTO с многими полями | Entity (осторожно) |
| `@NoArgsConstructor` | Entity (JPA требует), DTO | - |
| `@Data` | простые Value Object | для Entity  категорически запрещено |
| `@ToString` | DTO | для Entity с коллекциями |

#### Запрет на @Data над Entity

❌ **Bad**

```java
// @Data генерирует equals/hashCode по всем полям, включая lazy-коллекции.
// Это вызывает LazyInitializationException и бесконечную рекурсию в toString().
@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderEntity> orders; // @Data вызовет эту коллекцию в equals(), это ошибка и удар по производительности
}
```

✅ **Good**

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderEntity> orders;

    // equals/hashCode только по id, вручную
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

#### @ToString с коллекциями вызовет N+1

❌ **Bad**

```java
@ToString
@Entity
public class UserEntity {
    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderEntity> orders; // toString() загрузит всю коллекцию из БД
}
```

✅ **Good**

```java
@ToString(exclude = "orders") // исключаем lazy-коллекции
@Entity
public class UserEntity { ... }
```

## 5. Clean Code Tips

### 5.1 Длина метода, это правило одного экрана (~20–30 строк)

Метод должен делать **одно**. Если он длиннее экрана, тогда он делает слишком много. Декомпозируйте на приватные методы с говорящими именами.

❌ **Bad**  один метод делает валидацию, расчёт, сохранение и уведомление

```java
public OrderResponseDto processOrder(CreateOrderDto dto) {
    UserEntity user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    if (!user.isActive()) throw new BusinessException("User is not active");

    List<ProductEntity> products = productRepository.findAllById(dto.getProductIds());
    if (products.size() != dto.getProductIds().size())
        throw new NotFoundException("Some products not found");

    BigDecimal total = products.stream()
            .map(ProductEntity::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (dto.getDiscountCode() != null) {
        Discount discount = discountRepository.findByCode(dto.getDiscountCode())
                .orElseThrow(() -> new NotFoundException("Discount not found"));
        total = total.multiply(BigDecimal.ONE.subtract(discount.getPercent()));
    }

    OrderEntity order = new OrderEntity();
    order.setUser(user);
    order.setProducts(products);
    order.setTotal(total);
    order.setStatus(OrderStatus.PENDING);
    OrderEntity saved = orderRepository.save(order);
    notificationService.sendOrderConfirmation(user.getEmail(), saved.getId());
    return orderMapper.toDto(saved);
    // 30+ строк, смешаны ответственности, это нечитаемо
}
```

✅ **Good**  публичный метод читается как текст, а каждый приватный  делает одно

```java
public OrderResponseDto processOrder(CreateOrderDto dto) {
    UserEntity user = validateAndGetUser(dto.getUserId());
    List<ProductEntity> products = validateAndGetProducts(dto.getProductIds());
    BigDecimal total = calculateTotal(products, dto.getDiscountCode());
    OrderEntity saved = createAndSaveOrder(user, products, total);
    notificationService.sendOrderConfirmation(user.getEmail(), saved.getId());
    return orderMapper.toDto(saved);
}

private UserEntity validateAndGetUser(Long userId) {
    UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    if (!user.isActive()) throw new BusinessException("User is not active: " + userId);
    return user;
}

private List<ProductEntity> validateAndGetProducts(List<Long> ids) {
    List<ProductEntity> products = productRepository.findAllById(ids);
    if (products.size() != ids.size()) throw new NotFoundException("Some products not found");
    return products;
}

private BigDecimal calculateTotal(List<ProductEntity> products, String discountCode) {
    BigDecimal subtotal = products.stream()
            .map(ProductEntity::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return discountCode != null ? applyDiscount(subtotal, discountCode) : subtotal;
}
```

---

### 5.2 Количество параметров не более 3–4

Если у метода больше 4 параметров, следует объединить в объект-команду (DTO).

❌ **Bad**

```java
// 6 параметров, так легко перепутать порядок при вызове
public OrderEntity createOrder(Long userId, Long productId, int quantity,
                               String address, String comment, boolean isPriority) { ... }

// Вызов нечитаем: что означает true? что такое "Москва"?
createOrder(1L, 42L, 3, "Москва", null, true);
```

✅ **Good**  параметры объединены в объект-команду

```java
// Параметры объединены в DTO
public OrderEntity createOrder(CreateOrderDto dto) { ... }

// Вызов с Builder, где каждое поле подписано
CreateOrderDto dto = CreateOrderDto.builder()
        .userId(1L)
        .productId(42L)
        .quantity(3)
        .shippingAddress("Москва")
        .isPriority(true)
        .build();
```

---

### 5.3 Обработка исключений

**Правила:**

- Не проглатывайте исключения
- Не используйте `Exception` как тип
- Создавайте иерархию собственных исключений
- Клиент никогда не должен видеть стектрейс

❌ **Bad**

```java
// Проглатывание, приведёт к тому, что баг исчезнет в тишине
try {
    userService.create(dto);
} catch (Exception e) {
    // ничего
}

// Слишком широкий catch + потеря стектрейса
try {
    userService.create(dto);
} catch (Exception e) {
    log.error("Error", e); // вызывающий код думает, что всё OK, это плохо
}
```

✅ **Good** иерархия исключений + глобальный обработчик

```java
// Иерархия бизнес-исключений
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
    public BusinessException(String message, Throwable cause) { super(message, cause); }
}
public class NotFoundException extends BusinessException { ... }
public class ConflictException extends BusinessException { ... }
public class ValidationException extends BusinessException { ... }

// Правильное использование Optional
UserEntity user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found: " + id));

// Глобальный обработчик для единого формат ответа, клиент не видит детали
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);                   // стектрейс только для логов
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal server error")); // клиенту отправляет ошибку без деталей
    }
}
```

---

### 5.4 Не возвращайте null, всегда используйте Optional и пустые коллекции

❌ **Bad**

```java
public UserEntity findByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null); // вызывающий забудет проверить null
}

public List<OrderEntity> findOrdersByUser(Long userId) {
    if (!userExists(userId)) return null; // должна быть пустая коллекция, не null
}
```

✅ **Good**

```java
// Явный контракт: результат может отсутствовать
public Optional<UserEntity> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

// Пустая коллекция, никогда не возвращайте null
public List<OrderEntity> findOrdersByUser(Long userId) {
    if (!userExists(userId)) return Collections.emptyList();
    return orderRepository.findByUserId(userId);
}
```

---

### 5.5 Магические числа и строки следует выносить в константы

❌ **Bad**

```java
if (loginAttempts > 5) { lockAccount(); }   // что означает 5?
Duration.ofSeconds(900);                    // что означает 900 секунд?
user.setRole("ROLE_USER");                  // строка, здесь легко опечататься
```

✅ **Good**

```java
private static final int MAX_LOGIN_ATTEMPTS = 5;
private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(15);
private static final String DEFAULT_ROLE = "ROLE_USER";

if (loginAttempts > MAX_LOGIN_ATTEMPTS) { lockAccount(); }
user.setRole(DEFAULT_ROLE);
```
