package com.ramussoft.ai.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.ramussoft.ai.AiConfig;
import com.ramussoft.ai.AiDiagramDefinition;
import com.ramussoft.ai.AiDiagramService;
import com.ramussoft.ai.OpenRouterMessage;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.OpenDiagram;

/**
 * Swing dialog used to capture the natural language prompt, invoke the AI
 * service and apply the generated diagram.
 */
public class AiDiagramDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String SYSTEM_PROMPT =
            "You are an assistant embedded into the RAMUS IDEF0 modeling tool. "
                    + "Read the user's task and produce a JSON object describing a single "
                    + "IDEF0 diagram. Output strictly valid JSON with the following structure:\n"
                    + "{\n"
                    + "  \"functions\": [\n"
                    + "    {\"id\":\"F1\", \"name\":\"Function name\", \"type\":\"process|operation|external_reference|data_store|role\",\n"
                    + "     \"x\":0.1, \"y\":0.1, \"width\":0.2, \"height\":0.12, \"description\":\"optional\"}\n"
                    + "  ],\n"
                    + "  \"streams\": [\n"
                    + "    {\"id\":\"S1\", \"name\":\"Input\", \"source\":{\"type\":\"boundary|function\", \"ref\":\"F1\", \"side\":\"LEFT|RIGHT|TOP|BOTTOM\"},\n"
                    + "     \"target\":{\"type\":\"function\", \"ref\":\"F2\", \"side\":\"LEFT|RIGHT|TOP|BOTTOM\"}, \"description\":\"optional\"}\n"
                    + "  ]\n"
                    + "}\n"
                    + "Coordinates x, y, width and height are fractions of the page width/height (0..1) "
                    + "measured from the top-left corner of the active diagram. LEFT means input, RIGHT output, "
                    + "TOP control and BOTTOM mechanism. All identifiers must be unique. If a flow comes from "
                    + "or goes to the context boundary use type=\"boundary\" and omit ref. Do not wrap the JSON in markdown.";

    private final GUIFramework framework;
    private final AiDiagramService service;
    private final AiConfig config;
    private final OpenDiagram target;
    private final AiDiagramApplier applier;

    private JTextArea promptArea;
    private JTextArea responseArea;
    private JButton generateButton;
    private JButton closeButton;
    private JLabel statusLabel;

    public AiDiagramDialog(Frame owner, GUIFramework framework, AiDiagramService service,
                           AiConfig config, OpenDiagram target) {
        super(owner, "AI Diagram Generator", true);
        this.framework = framework;
        this.service = service;
        this.config = config;
        this.target = target;
        this.applier = new AiDiagramApplier(framework);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout(8, 8));
        promptArea = new JTextArea(8, 70);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setBorder(BorderFactory.createTitledBorder("Описание задачи"));
        JScrollPane promptScroll = new JScrollPane(promptArea);

        responseArea = new JTextArea(10, 70);
        responseArea.setEditable(false);
        responseArea.setBorder(BorderFactory.createTitledBorder("Ответ модели (JSON)"));
        JScrollPane responseScroll = new JScrollPane(responseArea);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.add(promptScroll, BorderLayout.NORTH);
        centerPanel.add(responseScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        generateButton = new JButton(new AbstractAction("Сгенерировать") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                runGeneration();
            }
        });

        closeButton = new JButton(new AbstractAction("Закрыть") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(generateButton);
        buttonPanel.add(closeButton);

        statusLabel = new JLabel(" ");
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void runGeneration() {
        final String prompt = promptArea.getText().trim();
        if (prompt.length() == 0) {
            JOptionPane.showMessageDialog(this, "Введите текст задания", "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        setBusy(true);
        statusLabel.setText("Обращение к модели...");

        SwingWorker<AiDiagramDefinition, Void> worker = new SwingWorker<AiDiagramDefinition, Void>() {
            private String responseJson;

            @Override
            protected AiDiagramDefinition doInBackground() throws Exception {
                List<OpenRouterMessage> messages = new ArrayList<OpenRouterMessage>();
                messages.add(new OpenRouterMessage("system", SYSTEM_PROMPT));
                messages.add(new OpenRouterMessage("user", buildUserPrompt(prompt)));
                String model = (config != null && config.getModel() != null)
                        ? config.getModel() : "deepseek/deepseek-v3";
                String payload = service.createChatCompletionRequest(model, messages);
                String rawResponse = service.requestDiagram(payload);
                String content = service.extractFirstMessageContent(rawResponse);
                if (content == null) {
                    throw new IllegalStateException("Модель вернула пустой ответ");
                }
                responseJson = normalizeContent(content);
                AiDiagramDefinition definition = service.parseDiagramDefinition(responseJson);
                if (definition == null) {
                    throw new IllegalStateException("Не удалось разобрать ответ модели");
                }
                return definition;
            }

            @Override
            protected void done() {
                try {
                    AiDiagramDefinition definition = get();
                    applier.apply(target, definition);
                    responseArea.setText(responseJson);
                    statusLabel.setText("Диаграмма обновлена");
                } catch (Exception ex) {
                    statusLabel.setText("Ошибка: " + ex.getMessage());
                    JOptionPane.showMessageDialog(AiDiagramDialog.this,
                            ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void setBusy(boolean busy) {
        generateButton.setEnabled(!busy);
        closeButton.setEnabled(!busy);
    }

    private String buildUserPrompt(String prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("Пользовательское описание:\n").append(prompt).append("\n\n");
        sb.append("Сформируй JSON строго в формате, описанном в системной инструкции. "
                + "Не добавляй пояснений или форматирования кроме JSON.");
        return sb.toString();
    }

    private String normalizeContent(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewLine >= 0 && lastFence > firstNewLine) {
                trimmed = trimmed.substring(firstNewLine + 1, lastFence);
            } else if (lastFence > 0) {
                trimmed = trimmed.substring(3, lastFence);
            }
            trimmed = trimmed.trim();
        }
        return trimmed;
    }
}
