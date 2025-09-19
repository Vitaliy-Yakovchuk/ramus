# RAMUS

### Project by Vitaliy Yakovchuk

![Project Image](https://github.com/user-attachments/assets/72c8fcad-d8f1-4bc1-9186-ae4a1f1c9cf2)

**Java-based IDEF0 & DFD Modeler**

<img width="1792" alt="Screenshot 2019-11-18 at 11 14 26" src="https://user-images.githubusercontent.com/2261228/69039713-23c56d00-09f5-11ea-99c5-b6714efe3037.png">

<img width="1792" alt="Screenshot 2019-11-18 at 11 14 59" src="https://user-images.githubusercontent.com/2261228/69039723-27f18a80-09f5-11ea-9a8d-508069ce7bbd.png">

---

## Описание функциональности

### Ядро модели

* Плагиновый движок IEngineImpl поднимает JDBC-шаблон, подключает фабрику персистентных объектов, регистрирует плагины и последовательности, управляет кэшем атрибутов и веток, что обеспечивает расширяемую основу модели и поддержку undo/redo на ветках.【F:core/src/main/java/com/ramussoft/core/impl/IEngineImpl.java†L44-L142】
* Метод `initStartBranch` автоматически создаёт начальную ветку хранения и защищает от отсутствия базового состояния базы данных при первом запуске.【F:core/src/main/java/com/ramussoft/core/impl/IEngineImpl.java†L144-L181】

### Моделирование IDEF0 и DFD

* Плагин IDEF0 регистрирует десятки системных атрибутов для функций, потоков и визуальных свойств, управляет очисткой устаревших квалификаторов и хранит дерево модели для диаграмм IDEF0/DFD.【F:idef0-core/src/main/java/com/ramussoft/idef0/IDEF0Plugin.java†L31-L200】

### Диаграммы и визуализация

* ChartPlugin создаёт квалификаторы диаграмм, наборов и связей, а также системные атрибуты для позиционирования и связывания диаграмм внутри наборов, что обеспечивает модуль визуальной аналитики.【F:chart-core/src/main/java/com/ramussoft/chart/core/ChartPlugin.java†L17-L197】

### Отчётность и публикации

* ReportPlugin вводит квалификатор отчётов с атрибутами имени, связанного квалификатора и типа (XML, JSSP, DocBook), а также фабрику функциональных интерфейсов для выполнения запросов отчётов.【F:report-core/src/main/java/com/ramussoft/report/ReportPlugin.java†L13-L148】
* Плагин печати в PDF добавляет GUI-действие, позволяющее пользователю выбрать файл и выгрузить любую реализацию RamusPrintable в PDF через iText, с поддержкой картирования шрифтов и постраничного вывода IDEF0-диаграмм.【F:print-to-pdf/src/main/java/print/to/pdf/Plugin.java†L37-L204】

### Обмен данными

* Мастер импорта Excel открывает книгу, позволяет настраивать сопоставление столбцов для каждого листа, запускать пакетный импорт с проверкой уникальности и транзакционно записывает данные в набор строк модели.【F:excel-import-export/src/main/java/com/ramussoft/excel/Importer.java†L32-L163】
* Экспортёр Excel формирует рабочую книгу с оформленными заголовками, поддержкой дат и извлечением данных из иерархических таблиц представления модели.【F:excel-import-export/src/main/java/com/ramussoft/excel/Exporter.java†L25-L134】

### Клиент-серверная платформа

* Клиентское приложение собирает список плагинов, инициализирует движок и GUI-фреймворк, подхватывает дополнительные модули и управляет жизненным циклом главного окна и фона очистки кэша иконок.【F:client/src/main/java/com/ramussoft/client/Client.java†L33-L198】
* ServerIEngineImpl расширяет ядро для многопользовательского режима: применяет пользовательские правила доступа, переименовывает личные потоки, хранит бинарные данные по веткам и обеспечивает запись/удаление с учётом истории, а лёгкий сервер разворачивает Spring-контекст `base-content.xml`.【F:server/src/main/java/com/ramussoft/server/ServerIEngineImpl.java†L16-L142】【F:server/src/main/java/com/ramussoft/server/LightServer.java†L5-L11】

## How to Start the Application

### Step 1: Install JDK

Download and install the [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

### Step 2: Run the Application

In the console, navigate to the project folder and run:

```bash
./gradlew runLocal
```

### Step 3: Test the Application

#### For Linux (Tested on Ubuntu 20.04 and Fedora 34)

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/Vitaliy-Yakovchuk/ramus.git
   ```

2. **Navigate to the Project Folder:**

   ```bash
   cd ramus
   ```

3. **Run the Application:**

   ```bash
   ./gradlew runLocal
   ```

### Optional: Create a Shortcut to Launch the Application

1. Open your `.bash_aliases` file:
   ```bash
   nano ~/.bash_aliases
   ```

2. Add the following alias to easily launch the application:

   ```bash
   alias ramus='cd ~/path/to/ramus/folder/ && ./gradlew runLocal &'
   ```

3. Save the file and reload it:

   ```bash
   source ~/.bash_aliases
   ```

4. Now, you can simply run `ramus` in the terminal to launch the application.

