# START_REVIEW — стан підсистеми зберігання на момент початку робіт

Дата зрізу: 2026-09-06
Гілка: `master`, коміт `73a6001`
Версія застосунку: `2.0.2` (`common/.../Metadata.java:5`)

Документ описує **як воно є зараз**, до будь-яких змін. Це база для порівняння
й обґрунтування плану в [FORMAT_MIGRATION_PLAN.md](FORMAT_MIGRATION_PLAN.md).

---

## 1. Що таке Ramus

Java/Swing застосунок для моделювання бізнес-процесів у нотаціях **IDEF0 та DFD**.
Gradle-мультипроєкт, 28 модулів, ~175 000 рядків Java.

Працює у двох режимах:

| Режим     | Точка входу                         | Сховище                    |
| --------- | ----------------------------------- | -------------------------- |
| Локальний | `local-client`                      | файл `.rsf` → H2 in-memory |
| Мережевий | `client` + `server` / `web-service` | H2 на диску або PostgreSQL |

Обидва режими використовують **одну й ту саму схему БД і той самий `Engine` API**.
Файл `.rsf` — це, по суті, дамп тієї ж бази.

---

## 2. Стек зберігання

```
 .rsf на диску
      │
      │  FileIEngineImpl.open() / writeToStream()
      │  TableToXML / XMLToTable
      ▼
 H2 in-memory  (jdbc:h2:mem:…, data-framework-common/.../MemoryDatabase.java:246)
      │
      │  IEngineImpl + JDBCTemplate  (SQL)
      ▼
 Engine API   (common/.../Engine.java)
      │
      ▼
 ~175k рядків GUI / плагінів / звітів
```

Ключові класи:

| Клас                                           | Роль                                                |
| ---------------------------------------------- | --------------------------------------------------- |
| `core/.../impl/FileIEngineImpl.java`           | відкриття/збереження `.rsf`, ZIP, сесії, блокування |
| `core/.../impl/TableToXML.java`                | серіалізація таблиці БД → XML                       |
| `core/.../impl/XMLToTable.java`                | десеріалізація XML → таблиця БД                     |
| `core/.../impl/IEngineImpl.java`               | реалізація `Engine` поверх SQL                      |
| `common/.../persistent/PersistentWrapper.java` | reflection-шар над `@Table`-класами                 |

**Важливо:** SQL зустрічається лише у 12 файлах, з них 7 у модулі `core`.
Тобто формат файлу ізольований у трьох класах, а не розмазаний по кодовій базі.

---

## 3. Анатомія `.rsf`

`.rsf` = **ZIP-архів**. MIME `application/x-ramus-extension-rsf`
(`dest/izpack/ramus-shortcuts-patch/.../x-ramus-extension-rsf.xml`).

Перевірено на реальному файлі `dest/doc/en/Enterprise activity.rsf`:

```
data/application_metadata.xml     версія, мінімальна версія, список плагінів
data/sequences.xml                лічильники ID
data/qualifiers.xml               класифікатори (класи сутностей / моделі)
data/elements.xml                 елементи (екземпляри)
data/attributes.xml               визначення атрибутів
data/qualifiers_attributes.xml    прив'язка атрибутів до класифікаторів
data/streams.xml                  реєстр бінарних вкладень
data/branches.xml                 гілки версійності
data/*_history.xml                історія змін (журнал / undo)
data/formulas.xml
data/formula_dependences.xml
data/Core/attribute_texts.xml     ⎫
data/Core/attribute_longs.xml     ⎬ значення атрибутів, окрема таблиця на тип
data/Core/attribute_dates.xml     ⎭
data/IDEF0/attribute_sectors.xml         геометрія стрілок
data/IDEF0/attribute_sector_borders.xml  прив'язки кінців стрілок
data/IDEF0/attribute_rectangles.xml      рамки функційних блоків
data/IDEF0/attribute_visual_datas.xml    ⚠ бінарний блоб
data/IDEF0/attribute_fonts.xml, attribute_colors.xml, …
elements/<id>/<attrId>/…          вкладення (звіти, прикріплені файли)
properties/idef0.xml              налаштування моделі
user/gui/session.binary           ⚠ стан UI
user/gui/table/view/…             ⚠ розкладки таблиць
user/gui/spell.xml                ⚠ словник
```

### Формат XML-таблиці

```xml
<table generate-from-table="qualifiers"
       generate-time="Sat Oct 17 15:44:09 EEST 2009"
       prefix="ramus_">
  <fields>
    <field id="0" name="QUALIFIER_ID" type="BIGINT"/>
    <field id="1" name="QUALIFIER_NAME" type="CLOB"/>
    …
  </fields>
  <data>
    <row><f id="0">9</f><f id="1">Enterprise activity</f><f id="2">FALSE</f><f id="3">38</f></row>
  </data>
</table>
```

Рядки **позиційні**: щоб зрозуміти `<f id="3">38</f>`, треба тримати в голові
блок `<fields>` з початку файлу **і** вміст `attributes.xml`.

Бінарні колонки (`VARBINARY`/`BLOB`) кодуються hex-рядком **зі зсувом +128**:
байт `b` пишеться як hex від `b + 128`
(`TableToXML.java` `ByteAConverter.toHexString`, зворотно `XMLToTable.java:139`).
Це не звичайний hex — зовнішній інструмент без цього знання прочитає сміття.

---

## 4. Модель даних

Класичний **EAV**:

```
Qualifier  (клас / тип сутності; модель IDEF0 — теж Qualifier)
    │
    ├── Attribute…      (визначення атрибутів, спільні між класифікаторами)
    │
    └── Element…        (екземпляри: функційний блок, стрілка, рядок довідника)
            │
            └── значення атрибута → рядок у таблиці, специфічній для типу
                                    (`Core/attribute_texts`, `IDEF0/attribute_sectors`, …)
```

Таблиці значень описані декларативно, через анотації:

```java
// idef0-core/.../attribute/SectorPointPersistent.java
@Table(name = "sector_points")
public class SectorPointPersistent extends AbstractPersistent {
    @Long(primary = true, id = 2)  public long   getXOrdinateId()
    @Double(id = 4)                public double getXPosition()
    @Integer(id = 6)               public int    getPointType()
    …
}
```

`PersistentWrapper` (`common/.../persistent/PersistentWrapper.java`) читає ці
анотації через reflection і вже вміє віддавати `getFields()`, `getGetter()`,
`getSetter()`, `getAnnotationType()`. **Поля там сортуються** (`Arrays.sort(fields)`
у `initFields()`), тобто порядок стабільний.

Це фактично готова схема — на ній можна побудувати generic-серіалізатор
без ручного опису кожного типу.

---

## 5. Де живе геометрія діаграм

| Що                       | Де                                                          | Стан                          |
| ------------------------ | ----------------------------------------------------------- | ----------------------------- |
| Рамки функційних блоків  | `FRectanglePersistent` → `IDEF0/attribute_rectangles`       | ✅ структуровано (x, y, w, h) |
| Точки ламаних стрілок    | `SectorPointPersistent` → `IDEF0/attribute_sector_points`   | ✅ структуровано              |
| Кінці стрілок            | `SectorBorderPersistent` → `IDEF0/attribute_sector_borders` | ✅ структуровано              |
| Шрифти, кольори          | `FontPersistent`, `ColorPersistent`                         | ✅ структуровано              |
| Атрибути вигляду сектора | `SectorPersistent.visualAttributes` `byte[]`                | ⚠ блоб                        |
| Вільні текстові підписи  | `VisualDataPersisitent.data` `byte[]`                       | ⚠ блоб                        |

### Бік прив'язки стрілки зберігається як геометрія, а не як роль

`SectorBorderPersistent.borderType` містить **геометричний бік**, константи з
`idef0-common/.../idef/visual/MovingPanel.java:24-30`:

```java
public static final int RIGHT  = 0;
public static final int BOTTOM = 1;
public static final int LEFT   = 2;
public static final int TOP    = 3;
```

Але в IDEF0 бік прив'язки **однозначно визначається роллю ICOM**:
Input → зліва, Control → зверху, Output → справа, Mechanism → знизу.

Тобто зараз семантична властивість (роль стрілки) зберігається у вигляді
похідної від неї геометричної. Роль ніде не зберігається явно — вона
виводиться з боку при відображенні.

### Точки не зберігають координати — вони посилаються на спільні напрямні

`SectorPointPersistent` має не `x`/`y`, а `xOrdinateId` / `yOrdinateId`.
`Ordinate` (`idef0-common/.../idef/elements/Ordinate.java`) — це **спільна
координатна лінія**, до якої прив'язано багато точок:

```java
public void addPoint(final Point point)
public void removePoint(final Point point)
public Point[] getPoints()
public void replacePoints(final Ordinate ordinate)
public boolean isMoveable(final double position)
```

Кілька точок різних стрілок можуть ділити одну ординату — і тоді рухаються
разом. Саме так тримається ортогональність трас.

**Наслідок для формату:** дамп «сирих» `x`/`y` для кожної точки **втратить
інформацію** — зникне факт спільності напрямної. Це не просто координати,
це вже граф обмежень.

### Зародок рушія розкладки вже є

`idef0-common/.../pb/dmaster/` — «майстер діаграм»: `ClassicTemplate`,
`SimpleTemplate`, `DetailedTemplate` поверх `AbstractClassicTemplate`.
Ці шаблони **самі будують топологію стрілок** за списком функцій
(`AbstractClassicTemplate.java:40-110`: `createInPoint`, `createOutPoint`,
`createRightBorderPoint`, з явним `borderType = RIGHT` тощо).

Тобто автоматичне розставляння в кодовій базі частково вже реалізоване,
просто як разова генерація шаблону, а не як перерахунок при відображенні.

### Стан блобів — краще, ніж здається

`SectorRefactor.java:55` — `BIN_VERSION = 2`.

У `loadFromFunction()` (рядок ~677) для `ver >= 2` сектори читаються
**не з блоба**, а з `function.getSectors()`, тобто з нормальних таблиць.
З блоба лишається прочитати тільки список вільних текстів.

Симетрично, `getSectorData()` (рядок 758) для v2 у потік пише лише:

- `BIN_VERSION`
- кількість текстів
- на кожен текст: font, color, FRectangle, string

(цикл по секторах там викликає `PaintSector.save(sector, memoryData, engine)`,
який пише в engine, а **не** в потік).

**Висновок:** міграція з блоба на структуровані таблиці вже виконана на ~90%.
Лишились вільні текстові підписи діаграми — це один невеликий persistent-клас.

Формат блоба, поки він є: little-endian, фіксовані 4 байти на int
(`com/dsoft/utils/DataSaver.java:37`), рядки як `int len + UTF-8`.

---

## 6. Проблеми, що блокують агентне редагування

### 6.1 Недетермінований запис

Це найважче, бо ламає git-diff незалежно від синтаксису.

| #   | Де                                 | Що                                                                                                       |
| --- | ---------------------------------- | -------------------------------------------------------------------------------------------------------- |
| 1   | `TableToXML.java:132`              | `generate-time="…new Date().toString()…"` у **кожному** з ~40 файлів → кожне збереження змінює всі файли |
| 2   | `FileIEngineImpl.java:546-548`     | `CurrentTimeMillis` + `CurrentDateTime` у метаданих                                                      |
| 3   | `TableToXML.java:143`              | `SELECT * FROM …` без `ORDER BY` → порядок рядків не гарантований                                        |
| 4   | `FileIEngineImpl.java:503`         | ітерація по `Hashtable extractedFiles` → порядок ZIP-записів плаває                                      |
| 5   | `FileIEngineImpl.createMetadata()` | `Properties.storeToXML` — `Properties` є `Hashtable`                                                     |

Пункт 5 видно неозброєним оком у реальному файлі — ключі йдуть у хаотичному порядку:

```
<entry key="Plugin_19">…</entry>
<entry key="FileOpenMinimumVersion">1.0</entry>
<entry key="Plugin_18">…</entry>
<entry key="CurrentTimeMillis">1255783449532</entry>
<entry key="Plugin_17">…</entry>
```

### 6.2 Нестабільні ідентифікатори

Усі зв'язки — через числа з sequence (`IEngineImpl.java:264`):

```java
elementId = nextValue("elements_sequence");
```

У файлі це виглядає як `768`, `17`, `-1`. Немає жодного людського імені,
за яке можна зачепитись при редагуванні; немає гарантії стабільності
між збереженнями на різних машинах.

### 6.3 Один архів замість дерева

- git бачить бінарник → немає diff, немає merge
- зміна однієї діаграми переписує весь файл
- модель, геометрія та стан UI змішані в одному контейнері

### 6.4 Стан UI у файлі моделі

`user/gui/session.binary`, `user/gui/table/view/com.ramussoft.gui.qualifier.QualifierPlugin$1$2.20.xml`,
`user/gui/spell.xml` — це персональні налаштування, які не мають версіонуватись,
але зараз лежать поруч з моделлю і змінюються при кожному відкритті.

---

## 7. Дефекти, знайдені під час рев'ю

Не пов'язані напряму з форматом, але їх варто виправити при переписуванні
серіалізатора, бо це втрата даних.

### 7.1 Обрізання часу до хвилин

```java
// core/.../impl/XMLToTable.java:40
public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
        DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH);
```

`DateFormat.SHORT` для часу — це `h:mm a`. **Секунди втрачаються** при кожному
збереженні. Поля типу `TIMESTAMP` (зокрема `revDate`, `systemRevDate` функцій)
після round-trip зсуваються.

### 7.2 Залежність дат від часового поясу

Той самий `DATE_FORMAT` не задає `TimeZone` → використовується системний.
Файл, збережений у Києві та відкритий у Лондоні, дасть інші дати.
Записується як `10/17/09 3:44 PM` без зсуву.

### 7.3 Мовчазне поглинання винятків

`FileIEngineImpl.loadTable()` ловить `IOException` і `SQLException` та робить
`e.printStackTrace()`. Пошкоджена таблиця → застосунок відкриє файл частково,
без жодного сигналу користувачу.

---

### 7.4 Гілки — це живі дані, а не історія

Легко сприйняти `branches` та `*_data_metadata` як журнал змін і викинути їх.
Насправді `attributes_data_metadata` — індекс, який визначає, **яке значення
атрибута є поточним**: `IEngineImpl.java:968` бере з нього `MAX(branch_id)`
і лише за відсутності рядка відкочується до `value_branch_id = 0`.

При цьому гілки досяжні й у файловому режимі: `local-client` підключає
`QualifierPluginSuit`, а `QualifierPlugin.java:98` реєструє `BranchView`.

Отже, проєкт із кількох гілок, у якого прибрали ці таблиці, відкриється без
помилки, але покаже значення кореневої гілки замість актуальних. Це не втрата
історії, а тиха підміна даних.

## 8. Що працює на нас

1. **Формат ізольований у 3 класах** — `FileIEngineImpl`, `TableToXML`, `XMLToTable`.
2. **Модель уже структурована** — `@Table`-персистенти з типізованими полями.
3. **`PersistentWrapper` — готовий reflection-шар** зі стабільним порядком полів.
4. **Блоби майже викорінені** — лишились вільні текстові підписи.
5. **JUnit 4.11 уже підключений** у `build.gradle` модулів як `testImplementation`.
6. **Є три реальні `.rsf`-файли** у `dest/doc/` для регресійних тестів.
7. **Версійність формату вже закладена** — `FileOpenMinimumVersion` у метаданих.

## 9. Що працює проти нас

1. **Тестів немає взагалі** — жодного каталогу `src/test`. `Test.java`, `TestFrame.java`,
   `TestImpl.java` у `src/main` — це не тести, а демо/утиліти.
   Інфраструктура оголошена, але не використовується.
   _(Виправлено на етапі 0: додано модуль `storage-test`.)_
2. **Тестові файли з 2009 року** — `ApplicationVersion=1.2`, `BIN_VERSION=1`.
   Вони покривають старий шлях завантаження (сектори з блоба), а не поточний.
   Потрібен свіжий файл, збережений версією 2.0.2.
3. **Історія та гілки** (`branches`, `*_history`) — окремий пласт складності
   для будь-якого текстового формату.
4. **Мережевий режим** ділить схему з файловим — зміни в схемі зачіпають `server`.

---

## 10. Метрики (baseline)

| Показник                             | Значення                       |
| ------------------------------------ | ------------------------------ |
| Модулів Gradle                       | 28                             |
| Рядків Java                          | ~175 000                       |
| Файлів із SQL                        | 12 (7 у `core`)                |
| XML-таблиць у типовому `.rsf`        | ~40                            |
| Класів `@Table`-персистентів у IDEF0 | 15                             |
| Бінарних полів у моделі IDEF0        | 2                              |
| Тестів                               | 0 → 20 (модуль `storage-test`) |
