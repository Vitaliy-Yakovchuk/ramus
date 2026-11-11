package com.ramussoft.local.ai;

import com.ramussoft.gui.common.*;
import com.ramussoft.gui.common.event.ActionEvent;
import javax.swing.*;
import java.awt.*;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.idef0.IDEF0ViewPlugin;
import com.ramussoft.idef0.OpenDiagram;

/** Native assistant: validated IDEF0 plans are applied only after approval. */
public final class RamusAiPlugin extends AbstractViewPlugin {
    public static final String VIEW_ID = "RamusAI";
    private static final String OPEN_ACTION = "RamusAI.Open";
    private final RamusAiClient client = new RamusAiClient();
    @Override public String getName() { return "AI Assistant"; }
    @Override public String getString(String key) {
        if (VIEW_ID.equals(key) || OPEN_ACTION.equals(key)) return "AI Assistant";
        if ((OPEN_ACTION + ".Short").equals(key) || (OPEN_ACTION + ".Long").equals(key))
            return "Создание модели с помощью ИИ";
        String value = com.ramussoft.gui.common.GlobalResourcesManager.getString(key);
        return value == null ? key : value;
    }
    @Override public UniqueView[] getUniqueViews() { return new UniqueView[]{new AssistantView(framework, client)}; }
    @Override public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor d = new ActionDescriptor(); d.setMenu("Tools");
        d.setAction(new AbstractAction("AI Assistant") { { putValue(Action.ACTION_COMMAND_KEY, OPEN_ACTION); } public void actionPerformed(java.awt.event.ActionEvent e) {
            framework.openView(new ActionEvent(ActionEvent.OPEN_STATIC_VIEW, VIEW_ID));
        }});
        return new ActionDescriptor[]{d};
    }
    private static final class AssistantView extends AbstractUniqueView {
        private final RamusAiClient client;
        private final RamusToolExecutor tools = new RamusToolExecutor();
        private final AiPlanParser planParser = new AiPlanParser();
        private final ModelSnapshotProvider snapshots = new ModelSnapshotProvider();
        private final JTextArea request = new JTextArea(6, 42);
        private final JTextArea result = new JTextArea(8, 42);
        AssistantView(GUIFramework f, RamusAiClient c) { super(f); client = c; result.setEditable(false); result.setLineWrap(true); result.setWrapStyleWord(true); }
        @Override public String getId() { return VIEW_ID; }
        // Reuse the built-in workspace until the plugin has a persisted workspace definition.
        @Override public String getDefaultWorkspace() { return "Workspace.IDEF0"; }
        @Override public String getString(String key) { return key; }
        @Override public Action[] getActions() { return new Action[0]; }
        @Override public JComponent createComponent() {
            JPanel root = new JPanel(new BorderLayout(10, 10)); root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            root.setMinimumSize(new Dimension(320, 260));
            JComboBox<String> provider = new JComboBox<>(new String[]{"DeepSeek (облако)", "Локальная LLM"});
            JPasswordField apiKey = new JPasswordField();
            apiKey.setToolTipText("Ключ используется только в текущем сеансе и не сохраняется");
            JTextField endpoint = new JTextField("http://127.0.0.1:11434/v1");
            JTextField model = new JTextField("deepseek-flash");
            JPanel fields = new JPanel(new GridLayout(0, 2, 8, 5));
            fields.add(new JLabel("Источник ИИ")); fields.add(provider);
            fields.add(new JLabel("Модель")); fields.add(model);
            fields.add(new JLabel("DeepSeek API-ключ")); fields.add(apiKey);
            fields.add(new JLabel("Адрес локального API")); fields.add(endpoint);
            JTextArea privacy = new JTextArea("DeepSeek получает задание и названия элементов проекта.");
            privacy.setEditable(false); privacy.setFocusable(false); privacy.setOpaque(false);
            privacy.setLineWrap(true); privacy.setWrapStyleWord(true);
            privacy.setFont(UIManager.getFont("Label.font"));
            endpoint.setEnabled(false);
            provider.addActionListener(e -> {
                boolean local = provider.getSelectedIndex() == 1;
                endpoint.setEnabled(local); apiKey.setEnabled(!local);
                model.setText(local ? "" : "deepseek-flash");
                model.setToolTipText(local ? "Название установленной локальной модели, например llama3.2" : "Название модели DeepSeek");
                privacy.setText(local ? "Данные отправляются на указанный локальный сервис." : "DeepSeek получает задание и названия элементов проекта.");
            });
            JPanel center = new JPanel(new BorderLayout(4,4));
            center.setBorder(BorderFactory.createTitledBorder("Задание"));
            request.setLineWrap(true); request.setWrapStyleWord(true);
            request.setToolTipText("Опишите процесс и желаемую диаграмму");
            center.add(new JScrollPane(request), BorderLayout.CENTER);
            JPanel bottom = new JPanel(new BorderLayout(4,4)); JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            JButton generate = new JButton("Сгенерировать");
            generate.addActionListener(e -> {
                boolean local = provider.getSelectedIndex() == 1;
                AgentProfile profile = new AgentProfile(local ? "Локальная LLM" : "DeepSeek",
                        local ? "local-llm" : "deepseek", local ? endpoint.getText().trim() : "https://api.deepseek.com",
                        local, "", model.getText().trim());
                analyze(root, profile, new String(apiKey.getPassword()), generate);
            });
            result.setLineWrap(true); result.setWrapStyleWord(true);
            bottom.setBorder(BorderFactory.createTitledBorder("Результат и журнал выполнения"));
            JButton example = new JButton("Пример: транспортная компания");
            example.setToolTipText("Проверенный учебный пример Лаб 1 без запроса к облаку");
            example.addActionListener(e -> applyPlan(root, TransportCompanyPlan.create()));
            buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
            buttons.add(generate); buttons.add(Box.createVerticalStrut(5)); buttons.add(example);
            buttons.add(new JLabel("Лаб 1 · подтверждение перед применением"));
            bottom.add(buttons, BorderLayout.NORTH); bottom.add(new JScrollPane(result), BorderLayout.CENTER);
            JSplitPane workArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT, center, bottom);
            workArea.setResizeWeight(0.55); workArea.setContinuousLayout(true); workArea.setOneTouchExpandable(true);
            JPanel header = new JPanel(new BorderLayout(0, 6));
            header.add(fields, BorderLayout.CENTER); header.add(privacy, BorderLayout.SOUTH);
            root.add(header, BorderLayout.NORTH); root.add(workArea, BorderLayout.CENTER); return root;
        }
        private void analyze(Component parent, AgentProfile profile, String enteredApiKey, JButton generate) {
            if (request.getText().isBlank()) { result.setText("Опишите задание перед генерацией."); return; }
            if (profile.model().isBlank()) { result.setText("Укажите название модели."); return; }
            if ("deepseek".equals(profile.type()) && !enteredApiKey.isBlank()) {
                profile = new AgentProfile(profile.name(), profile.type(), profile.endpoint(), profile.localOnly(), enteredApiKey, profile.model());
            }
            if ("deepseek".equals(profile.type()) && profile.apiKey().isBlank()) {
                JPasswordField key = new JPasswordField();
                int choice = JOptionPane.showConfirmDialog(parent, key,
                        "Введите DeepSeek API key (не сохраняется)", JOptionPane.OK_CANCEL_OPTION);
                if (choice != JOptionPane.OK_OPTION || key.getPassword().length == 0) {
                    result.setText("Введите API-ключ DeepSeek в поле настроек и повторите попытку.");
                    return;
                }
                profile = new AgentProfile(profile.name(), profile.type(), profile.endpoint(), profile.localOnly(),
                        new String(key.getPassword()), profile.model());
            }
            final AgentProfile selectedProfile = profile;
            final String prompt = request.getText();
            generate.setEnabled(false);
            result.setText("Подготавливаю данные проекта и запрашиваю план...");
            new SwingWorker<String,Void>() {
                protected String doInBackground() throws Exception {
                    ModelSnapshot snapshot = snapshots.capture(framework.getEngine());
                    String task = prompt;
                    for (int attempt = 0; attempt < 2; attempt++) {
                        String answer = "deepseek".equals(selectedProfile.type())
                                ? new DeepSeekClient().generatePlan(task, snapshot, selectedProfile.apiKey(), selectedProfile.model())
                                : new LocalLlmClient(selectedProfile.endpoint()).generatePlan(task, snapshot, selectedProfile.model());
                        try {
                            new Lab1PlanValidator().validate(planParser.parse(answer));
                            return answer;
                        } catch (IllegalArgumentException invalid) {
                            if (attempt == 1) throw invalid;
                            task = prompt + "\nИсправь предыдущий план. Ошибка проверки: "
                                    + invalid.getMessage() + "\nПредыдущий JSON:\n" + answer;
                        }
                    }
                    throw new IllegalStateException("План не прошёл проверку");
                }
                protected void done() { try {
                    String answer = get();
                    AiPlan plan = planParser.parse(answer);
                    applyPlan(parent, plan);
                } catch (Exception e) { Throwable cause = e.getCause() == null ? e : e.getCause(); result.setText("Не удалось сформировать план: " + cause.getMessage()); }
                finally { generate.setEnabled(true); } }
            }.execute();
        }
        private void applyPlan(Component parent, AiPlan plan) {
            new Lab1PlanValidator().validate(plan);
            result.setText(describePlan(plan));
            int approval = JOptionPane.showConfirmDialog(parent,
                    "План проверен: A-0, A0, второй уровень, стороны ICOM и баланс границ.\n"
                    + "Применить его к открытому проекту? Сохраните копию перед изменением.",
                    "Генерация IDEF0 по Лаб 1", JOptionPane.OK_CANCEL_OPTION);
            if (approval != JOptionPane.OK_OPTION) {
                result.append("\nИзменения не применены.");
                return;
            }
            try {
                java.util.List<com.ramussoft.common.Element> created = tools.execute(
                        framework.getEngine(), framework.getAccessRules(), plan,
                        message -> result.append("\n• " + message));
                NDataPluginFactory.fullRefrash(framework);
                com.ramussoft.common.Qualifier qualifier = RamusToolExecutor.functionQualifier(framework.getEngine());
                com.ramussoft.pb.DataPlugin data = NDataPluginFactory.getDataPlugin(
                        qualifier, framework.getEngine(), framework.getAccessRules());
                String contextName = plan.operations().stream()
                        .filter(op -> "create_element".equals(op.tool()) && "Работы".equals(op.qualifier()))
                        .findFirst().orElseThrow().name();
                com.ramussoft.pb.Function base = (com.ramussoft.pb.Function) data.getBaseFunction();
                com.ramussoft.pb.Function context = null;
                for (int i = 0; i < base.getChildCount(); i++) {
                    com.ramussoft.pb.Function child = (com.ramussoft.pb.Function) base.getChildAt(i);
                    if (contextName.equals(child.getName())) context = child;
                }
                if (context == null) throw new IllegalStateException("Контекст не сохранился");
                framework.propertyChanged(IDEF0ViewPlugin.OPEN_DIAGRAM,
                        new OpenDiagram(qualifier, context.getElement().getId(), true));
                long functions = plan.operations().stream().filter(op -> "create_element".equals(op.tool())).count();
                long arrows = plan.operations().size() - functions;
                result.append("\n\nГотово: функций " + functions + ", связей " + arrows
                        + ". Новых объектов: " + created.size() + ". Открыта A0.\n"
                        + "Вверх — контекст A-0. Дважды нажмите на этап для декомпозиции A1/A2/A3.\n"
                        + "Сохраните проект через Файл → Сохранить как.");
            } catch (Exception e) {
                result.append("\nОшибка применения: " + e.getMessage());
            }
        }
        private static String describePlan(AiPlan plan) {
            StringBuilder text = new StringBuilder(plan.summary()).append('\n');
            for (AiPlan.AiOperation operation : plan.operations()) {
                String parent = operation.qualifier().contains("::") ? operation.qualifier().substring(operation.qualifier().indexOf("::") + 2) : "";
                text.append("• ");
                if (!parent.isEmpty()) text.append("[").append(parent).append("] ");
                text.append("create_element".equals(operation.tool()) ? "Блок: " : "Стрелка: ")
                        .append(operation.name());
                if ("create_stream".equals(operation.tool()))
                    text.append(" ( ").append(operation.source()).append(" → ").append(operation.target()).append(" )");
                text.append('\n');
            }
            return text.toString();
        }
    }
}
