# Checkstyle Rules

> **Info.**
>
> Этот документ объясняет все правила нашего Checkstyle простым языком.
>
> Основа конфига: **Google Java Style Guide**, адаптированный под наш проект.

---

## Файл checkstyle

## �� Быстрая шпаргалка

| Правило | Суть за одну строку |
| --- | --- |
| Длина строки | Максимум 140 символов |
| Отступы | 4 пробела , без табов |
| Один класс — один файл | Имя файла = имя публичного класса |
| Пакеты | Только строчные буквы |
| Классы | UpperCamelCase |
| Методы и поля | lowerCamelCase, минимум 2 символа |
| Параметры и переменные | lowerCamelCase, можно 1 символ |
| Аббревиатуры | Запрещены: `DTO` → `Dto`, `ID` → `Id` |
| Литерал long | Заглавная L: `100L`, не `100l` |
| Массивы | `String[] args`, не `String args[]` |
| Несколько переменных | Каждая на своей строке |
| Несколько операторов | Каждый на своей строке |
| Открывающая скобка { | На той же строке, не на новой |
| Фигурные скобки | Обязательны даже для однострочных if/for |
| Пустой catch | Нужен комментарий или переменная `expected` |
| Switch | Всегда нужен `default` |
| Fall-through | Только с комментарием |
| Аннотации на классах/методах | Каждая на своей строке |
| Перегруженные методы | Стоят рядом в классе |
| Лямбды | Не длиннее 20 строк |
| finalize() | Запрещён |
| Импорты | Нет неиспользуемых; порядок: static → пустая строка → остальные |
| Перенос точки | Точка идёт на новую строку: `.filter()` |
| Перенос запятой | Запятая остаётся в конце строки |
| Перенос операторов | Оператор идёт на новую строку: `&& condition` |
| Порядок модификаторов | `public static final`, не иначе |
| Пустые строки | Между методами — одна пустая строка |
| Комментарии | Отступ = отступ кода после |
| Javadoc | Обязателен для public методов (не @Override) и protected+ классов |

## 1. Файлы и строки

### Длина строки: максимум 140 символов

Одна строка кода не должна быть длиннее **140 символов**. Длинные строки неудобно читать, особенно в code review.

❌ **Плохо**

```java
public ResponseEntity<UserResponseDto> createUserAndSendWelcomeEmail(@RequestBody @Valid CreateUserRequestDto createUserRequestDto, @RequestHeader("X-Request-Id") String requestId) {
```

✅ **Хорошо: переносим параметры**

```java
public ResponseEntity<UserResponseDto> createUserAndSendWelcomeEmail(
        @RequestBody @Valid CreateUserRequestDto createUserRequestDto,
        @RequestHeader("X-Request-Id") String requestId) {
```

---

### Табуляция запрещена:только пробелы

В коде нельзя использовать символ Tab (`\t`). Только пробелы. Это касается каждой строки файла.

**Как настроить в IntelliJ IDEA:** Settings → Editor → Code Style → Java → вкладка Tabs and Indents → снять галку «Use tab character».

---

### Кодировка файлов:UTF-8

Все `.java`, `.properties` и `.xml` файлы должны быть в кодировке UTF-8. В IntelliJ это настраивается в Settings → Editor → File Encodings.

---

### Один публичный класс = один файл

В одном `.java` файле может быть только один класс верхнего уровня (top-level). И его имя должно совпадать с именем файла.

❌ **Плохо**: два класса в одном файле `UserService.java`

```java
// Файл UserService.java
public class UserService { ... }
public class UserHelper { ... }  // второй публичный класс — нельзя
```

✅ **Хорошо:** каждый класс в своём файле

```java
// Файл UserService.java
public class UserService { ... }

// Файл UserHelper.java
public class UserHelper { ... }
```

---

## 2. Именование

### Пакеты: только строчные буквы

Имена пакетов пишутся только строчными буквами и цифрами. Никаких заглавных букв и подчёркиваний.

❌ **Плохо**

```java
package com.company.UserService;
package com.company.user_service;
package com.company.userService;
```

✅ **Хорошо**

```java
package com.company.user;
package com.company.user.service;
package com.company.order.repository;
```

---

### Классы, интерфейсы, enum: UpperCamelCase

Каждое слово начинается с заглавной буквы. Без подчёркиваний.

❌ **Плохо**

```java
public class user_service { ... }
public class userService { ... }
public class USERSERVICE { ... }
```

✅ **Хорошо**

```java
public class UserService { ... }
public interface OrderRepository { ... }
public enum OrderStatus { ... }
```

---

### Методы и поля: lowerCamelCase, минимум 2 символа

Имена методов и полей начинаются со строчной буквы, каждое следующее слово с заглавной. Важно: **минимум 2 символа** — однобуквенные имена запрещены.

❌ **Плохо**

```java
private String n;           // однобуквенное имя
private String Name;        // начинается с заглавной
public void D() { ... }     // однобуквенный метод
public void get_user() { }  // подчёркивание
```

✅ **Хорошо**

```java
private String name;
private String firstName;
public void findById() { ... }
public void createOrder() { ... }
```

---

### Параметры и локальные переменные: lowerCamelCase, допускается 1 символ

Параметры методов, переменные в теле метода, параметры лямбд и в блоке catch. В отличие от полей однобуквенные допускаются (но не рекомендуются).

❌ **Плохо**

```java
public void createUser(String User_Name, Long ID) {
    String Result = "ok";
}
```

✅ **Хорошо**

```java
public void createUser(String userName, Long userId) {
    String result = "ok";
}

// Лямбды
users.stream().map(u -> u.getName());        // однобуквенная — допустима
users.stream().map(user -> user.getName());  // полное имя — лучше
```

---

### Аббревиатуры запрещены: пиши полные слова

Это одно из самых неожиданных правил. Аббревиатуры типа `URL`, `HTTP`, `ID`, `DTO` в именах классов, методов и переменных **запрещены**. Нужно писать как обычное слово только первая буква заглавная.

Почему: аббревиатуры вперемешку с обычными словами сложно читать. `parseHTTPSURL` vs `parseHttpsUrl` второе читается лучше.

| ❌ Плохо | ✅ Хорошо |
| --- | --- |
| `class UserDTO` | `class UserDto` |
| `class HTTPCLIENT` | `class HttpClient` |
| `String userID` | `String userId` |
| `void parseURL()` | `void parseUrl()` |
| `String baseURL` | `String baseUrl` |
| `class XMLParser` | `class XmlParser` |

> **Note.**
>
> Это правило **отключено в тестовых файлах** (`src/test`, `src/unitTest`). В тестах аббревиатуры можно использовать свободно.

---

### Дженерики (Generic типы): одна заглавная буква или слово с T на конце

Параметры типов (то, что пишется в угловых скобках при объявлении дженерика) должны быть либо одной заглавной буквой с необязательной цифрой, либо словом заканчивающимся на `T`.

✅ **Правильные примеры**

```java
class Box<T> { ... }          // одна буква — классика
class Pair<K, V> { ... }      // K и V — принятые обозначения
class Result<E> { ... }       // E — элемент

// Слово с суффиксом T:
class Converter<InputT, OutputT> { ... }
class ResponseT { ... }
```

---

## 3. Отступы и форматирование

### ↔️ Отступы: 4 пробела везде

Базовый отступ: **4 пробела**. Применяется везде: тело класса, тело метода, операторы `case`, перенос строки, инициализация массива.

❌ **Плохо**

```java
public class UserService {
  // 2 пробела — неверно
  public void createUser() {
      // 6 пробелов — тоже неверно
  }
}
```

✅ **Хорошо**

```java
public class UserService {
    // 4 пробела
    public void createUser() {
        // 8 пробелов (4+4)
        if (condition) {
            // 12 пробелов (4+4+4)
        }
    }
}
```

---

### Порядок модификаторов

Модификаторы (`public`, `private`, `static`, `final` и т.д.) должны стоять в строго определённом порядке.

Правильный порядок:

```java
// Порядок: public/protected/private → abstract → static → final → ...
public static final String BASE_URL = "/api";
private static final int MAX_COUNT = 10;
protected abstract void process();
```

❌ **Плохо**

```java
static public final String BASE_URL = "/api";  // static перед public — неверно
final static private int MAX = 10;             // неверный порядок
```

✅ **Хорошо**

```java
public static final String BASE_URL = "/api";
private static final int MAX = 10;
```

---

### Одна переменная: одно объявление

Нельзя объявлять несколько переменных в одной строке через запятую.

❌ **Плохо**

```java
int min, max, count;
String firstName, lastName;
```

✅ **Хорошо**

```java
int min;
int max;
int count;
String firstName;
String lastName;
```

---

### Одно выражение: одна строка

Нельзя писать несколько операторов через точку с запятой в одной строке.

❌ **Плохо**

```java
int a = 1; int b = 2; int c = a + b;
```

✅ **Хорошо**

```java
int a = 1;
int b = 2;
int c = a + b;
```

---

### Стиль объявления массивов

Скобки `[]` пишутся рядом с **типом**, а не с именем переменной. Это Java-стиль, а не C-стиль.

❌ **Плохо** (C-стиль)

```java
String args[];   // скобки у имени переменной
int values[];
```

✅ **Хорошо** (Java-стиль)

```java
String[] args;   // скобки у типа
int[] values;
```

---

### Литерал long: заглавная L

Когда пишешь число типа `long`, суффикс должен быть заглавной буквой `L`, а не строчной `l`. Строчная `l` легко перепутать с цифрой `1`.

❌ **Плохо**

```java
long timeout = 1000l;   // l похожа на 1 — можно прочитать как 10001
```

✅ **Хорошо**

```java
long timeout = 1000L;   // L читается однозначно
```

---

### Переменная объявляется близко к использованию

Не нужно объявлять все переменные в начале метода. Объявляй переменную как можно ближе к тому месту, где она используется. Это улучшает читаемость.

❌ **Плохо**

```java
public void process() {
    String email;       // объявлена в самом начале
    int count;
    boolean isActive;

    // ... 20 строк кода ...

    email = user.getEmail();  // используется только здесь
    sendEmail(email);
}
```

✅ **Хорошо**

```java
public void process() {
    // ... 20 строк кода ...

    String email = user.getEmail();  // объявлена прямо перед использованием
    sendEmail(email);
}
```

---

## 4. Пробелы вокруг операторов и символов

### Пробелы вокруг операторов

Вокруг операторов присваивания, арифметических, логических, сравнения — **пробел с обеих сторон**.

❌ **Плохо**

```java
int result=a+b;
if(a==b&&c>0) {
boolean flag=!isActive;
```

✅ **Хорошо**

```java
int result = a + b;
if (a == b && c > 0) {
boolean flag = !isActive;
```

---

### Пробел после ключевых слов

После `if`, `else`, `while`, `for`, `return`, `switch`, `try`, `catch` — обязательный пробел.

❌ **Плохо**

```java
if(condition) {
while(running) {
return(value);
for(int i = 0; i < 10; i++) {
```

✅ **Хорошо**

```java
if (condition) {
while (running) {
return value;
for (int i = 0; i < 10; i++) {
```

---

### Нет пробела между именем метода и скобкой

При вызове или объявлении метода между именем и открывающей скобкой **нет пробела**.

❌ **Плохо**

```java
public void findById (Long id) {   // пробел перед (
userService.findById (id);         // пробел перед (
```

✅ **Хорошо**

```java
public void findById(Long id) {
userService.findById(id);
```

---

### Нет пробелов внутри скобок

Внутри круглых скобок `()` пробелов нет.

❌ **Плохо**

```java
if ( condition ) {
findById( id );
String s = ( String ) object;
```

✅ **Хорошо**

```java
if (condition) {
findById(id);
String s = (String) object;
```

---

### Нет пробелов внутри дженериков

Внутри угловых скобок дженериков `<>` пробелов нет.

❌ **Плохо**

```java
List < String > names;
Map < Long, String > userMap;
Optional < UserEntity > user;
```

✅ **Хорошо**

```java
List<String> names;
Map<Long, String> userMap;
Optional<UserEntity> user;
```

---

### Нет пробела перед запятой и точкой с запятой

❌ **Плохо**

```java
findById(id , true) ;
for (int i = 0 ; i < 10 ; i++) {
```

✅ **Хорошо**

```java
findById(id, true);
for (int i = 0; i < 10; i++) {
```

---

### ↩️ Правила переноса строк

Когда строка слишком длинная и нужно перенести — важно знать, куда идёт символ переноса: в **конец** текущей строки или в **начало** следующей.

| Символ | Куда идёт при переносе | Пример |
| --- | --- | --- |
| Точка `.` | На **новую** строку | `stream`      `.filter(...)`      `.map(...)` |
| Запятая `,` | В **конец** строки | `method(param1,`      `param2)` |
| Операторы `+`, `-`, `&&`, `==` и др. | На **новую** строку | `boolean result = a`      `&& b`      `&& c;` |
| Ссылка на метод `::` | На **новую** строку | `users.stream()`      `.map(User`      `::getName)` |

✅ **Пример правильного переноса**

```java
// Точка на новой строке
List<String> names = users.stream()
        .filter(user -> user.isActive())
        .map(user -> user.getName())
        .collect(Collectors.toList());

// Оператор на новой строке
boolean isValid = name != null
        && !name.isBlank()
        && name.length() < 100;

// Запятая в конце строки
userService.createUser(
        firstName,
        lastName,
        email);
```

---

## 5. Фигурные скобки

### { Открывающая скобка на той же строке

Открывающая фигурная скобка `{` пишется на той же строке, что и объявление. Отдельная строка запрещена.

❌ **Плохо** (Allman стиль)

```java
public class UserService
{
    public void create()
    {
        if (condition)
        {
        }
    }
}
```

✅ **Хорошо** (K&R стиль)

```java
public class UserService {
    public void create() {
        if (condition) {
        }
    }
}
```

---

### } Закрывающая скобка

Для `try/catch/finally` и `if/else`: закрывающая `}` блока стоит **на той же строке** что и следующий оператор (`catch`, `else`, `finally`).

Для классов, методов, циклов: закрывающая `}` на **отдельной строке**.

✅ **Правильно**

```java
try {
    doSomething();
} catch (Exception e) {   // } и catch на одной строке
    handleError(e);
} finally {               // } и finally на одной строке
    cleanup();
}

if (condition) {
    doA();
} else {                  // } и else на одной строке
    doB();
}

public void create() {
    // тело метода
}                         // } метода отдельная строка
```

---

### { } Фигурные скобки обязательны всегда

Даже если тело `if`, `else`, `for`, `while`, `do-while` состоит из одной строки, скобки обязательны.

❌ **Плохо**

```java
if (condition)
    doSomething();   // нет фигурных скобок опасно и запрещено

for (int i = 0; i < 10; i++)
    process(i);
```

✅ **Хорошо**

```java
if (condition) {
    doSomething();
}

for (int i = 0; i < 10; i++) {
    process(i);
}
```

---

### Пустые блоки запрещены без комментария

Пустой блок `{}` должен содержать хотя бы комментарий объяснение, почему он пустой. Исключение: пустой `catch` разрешён только если переменная называется `expected` (для тестов).

❌ **Плохо**

```java
try {
    riskyOperation();
} catch (Exception e) {
    // пустой catch - ошибка проглочена молча
}

if (condition) {
}
```

✅ **Хорошо**

```java
try {
    riskyOperation();
} catch (Exception e) {
    log.warn("Operation failed, skipping: {}", e.getMessage()); // объясняем решение
}

// Если в тесте ожидаем исключение:
try {
    service.create(null);
} catch (ValidationException expected) {
    // переменная называется "expected" - Checkstyle разрешает пустой блок
}
```

---

## 6. Пустые строки между элементами

### Пустые строки между методами и полями

Между методами, конструкторами, полями, статическими блоками - должна быть **одна пустая строка**. При этом поля можно писать без пустых строк между ними (правило `allowNoEmptyLineBetweenFields`).

❌ **Плохо**

```java
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserResponseDto findById(Long id) {   // нет пустой строки перед методом
        return ...;
    }
    public void delete(Long id) {               // нет пустой строки между методами
        ...;
    }
}
```

✅ **Хорошо**

```java
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
                                                 // пустая строка перед методом
    public UserResponseDto findById(Long id) {
        return ...;
    }
                                                 // пустая строка между методами
    public void delete(Long id) {
        ...;
    }
}
```

---

## 7. Импорты

### Неиспользуемые импорты запрещены

Каждый `import` должен использоваться в коде. Неиспользуемые - удаляем.

**IntelliJ IDEA:** автоматически подсвечивает лишние импорты серым. Горячая клавиша для оптимизации: `Ctrl+Alt+O` (или `Cmd+Alt+O` на Mac).

---

### Порядок импортов

Импорты делятся на группы, между группами **пустая строка**:

1. `import static ...` - статические импорты
2. пустая строка
3. Все остальные импорты (`import ...`) - в алфавитном порядке

Внутри каждой группы — алфавитный порядок.

✅ **Хорошо**

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.company.order.OrderEntity;
import com.company.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
```

---

## 8. Switch и специальные конструкции

### В switch обязателен default

В каждом `switch`-блоке должна быть ветка `default` - даже если ты уверен, что покрыл все случаи.

❌ **Плохо**

```java
switch (status) {
    case PENDING:
        process();
        break;
    case DONE:
        complete();
        break;
    // нет default — что будет при новом статусе?
}
```

✅ **Хорошо**

```java
switch (status) {
    case PENDING:
        process();
        break;
    case DONE:
        complete();
        break;
    default:
        throw new IllegalStateException("Unexpected status: " + status);
}
```

---

### Fall-through в switch запрещён без комментария

Fall-through - это когда после `case` нет `break` и выполнение «проваливается» в следующий `case`. Если это сделано специально — нужен комментарий.

❌ **Плохо** - молчаливый fall-through

```java
switch (day) {
    case MONDAY:
        startWeek();
    case TUESDAY:    // провалится сюда без break — это баг или замысел?
        doWork();
        break;
}
```

✅ **Хорошо** — явный комментарий о намерении

```java
switch (day) {
    case MONDAY:
        startWeek();
        // проваливание — намеренно продолжаем
    case TUESDAY:
        doWork();
        break;
}
```

---

## 9. Аннотации

### Аннотации на классах и методах - отдельная строка каждая

Каждая аннотация над классом, методом или конструктором пишется на своей строке.

❌ **Плохо**

```java
@Service @Slf4j @RequiredArgsConstructor
public class UserService { ... }

@Override @Transactional
public void delete(Long id) { ... }
```

✅ **Хорошо**

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService { ... }

@Override
@Transactional
public void delete(Long id) { ... }
```

На **полях** несколько аннотаций в одной строке — допустимо.

```java
@NotNull @Size(max = 255)
private String email;   // допустимо для полей
```

---

## 10. Методы: специальные правила

### Перегруженные методы

Если в классе есть несколько методов с одним именем но разными параметрами (overload), тогда они должны стоять рядом, не разбросаны по классу.

❌ **Плохо**

```java
public void send(String message) { ... }

public void delete(Long id) { ... }  // между перегрузками send другой метод

public void send(String message, String recipient) { ... }  // второй send далеко от первого
```

✅ **Хорошо**

```java
public void send(String message) { ... }
public void send(String message, String recipient) { ... }  // рядом с первым send

public void delete(Long id) { ... }
```

---

### Метод finalize() запрещён

Переопределение метода `finalize()` запрещено. Это устаревший механизм, который ненадёжен и создаёт проблемы с памятью. Вместо него используй `AutoCloseable` / `try-with-resources`.

---

### Лямбда-тело: максимум 20 строк

Тело лямбда-выражения не должно превышать 20 строк. Если лямбда длиннее, тогда вынеси логику в отдельный метод.

❌ **Плохо**

```java
users.forEach(user -> {
    // 25 строк логики прямо в лямбде
    String name = user.getName();
    // ... много кода ...
});
```

✅ **Хорошо:** вынести в отдельный метод

```java
users.forEach(this::processUser);  // лямбда — 1 строка

private void processUser(UserEntity user) {
    // вся логика здесь, метод можно читать отдельно
}
```
> **Note.**
>
> Правило отключено для файлов `*Specification.java` — JPA Specification-запросы бывают объективно длинными.

---

## 11. Javadoc

### Когда Javadoc обязателен

| Что | Когда обязателен | Исключения |
| --- | --- | --- |
| Класс, интерфейс, enum, record | Если доступ `protected` или `public` | — |
| Метод | Если `public` и длиннее 2 строк | Методы с `@Override` и `@Test` — не нужен |

---

### Правила написания Javadoc

❌ **Плохо**

```java
/**
 * Этот метод возвращает пользователя по Id.  // запрещённая фраза "This method returns"
 * @param id
 * @return
 * @throws                              // пустые теги запрещены
 */
public UserDto findById(Long id) { ... }
```

✅ **Хорошо**

```java
/**
 * Найти пользователя по Id.
 *
 * @param id пользователя.
 * @return Dto пользователя
 * @throws NotFoundException если пользователь не найден
 */
public UserDto findById(Long id) { ... }
```

**Правила Javadoc в нашем проекте:**

- Порядок тегов строго: `@param` → `@return` → `@throws` → `@deprecated`
- Перед группой тегов (`@param`, `@return`) — обязательна пустая строка
- Запрещены фразы: *"Этот метод возвращает.."*, в начале
- Каждый тег должен содержать описание, пустые `@param id` без текста запрещены

---

## 12. Прочие правила

### Escape-последовательности: используй символьные, не числовые

Для специальных символов используй именованные escape-последовательности, а не числовые коды.

| ❌ Плохо | ✅ Хорошо | Что это |
| --- | --- | --- |
| `"\u0009"` | `"\t"` | Табуляция |
| `"\u000a"` | `"\n"` | Перевод строки |
| `"\u000d"` | `"\r"` | Возврат каретки |
| `"\u0022"` | `"\""` | Кавычка |
| `"\u0027"` | `"\'"` | Апостроф |
| `"\u005c"` | `"\\"` | Обратный слэш |

---

### Отступ комментариев совпадает с кодом

Однострочные (`//`) и блочные (`/* */`) комментарии должны иметь такой же отступ, что и код, который идёт после них.

❌ **Плохо**

```java
public void create() {
// комментарий без отступа
    String name = dto.getName();
}
```

✅ **Хорошо**

```java
public void create() {
    // комментарий с правильным отступом
    String name = dto.getName();
}
```

---

## Как отключить правило для конкретного участка кода

Иногда правило нужно отключить для конкретного места. Используй аннотацию `@SuppressWarnings` или специальный комментарий.

```java
// Отключить для метода или класса через аннотацию:
@SuppressWarnings("checkstyle:MagicNumber")
public void legacyMethod() {
    int result = value * 365;  // исторически так, нельзя трогать
}

// Отключить для блока кода через комментарии:
// CHECKSTYLE.OFF: LineLength
String veryLongStringThatCannotBeSplit = "some long configuration string that must remain on one line";
// CHECKSTYLE.ON: LineLength
```
> **Warning.**
>
> Злоупотреблять `@SuppressWarnings` нельзя. Если хочешь отключить правило — объясни причину в PR-описании. Систематическое игнорирование правил — повод для обсуждения на ретро.
