package com.ramussoft.ai.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ramussoft.ai.AiDiagramDefinition;
import com.ramussoft.ai.AiDiagramService;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.OpenDiagram;

/**
 * Dialog allowing a user to paste JSON describing a diagram and apply it
 * directly without contacting the AI service.
 */
public class AiDiagramImportDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String TEMPLATE_FILE_NAME = "ai-diagram-template.txt";

    private final GUIFramework framework;
    private final AiDiagramService service;
    private OpenDiagram target;
    private final AiDiagramApplier applier;

    private JTextArea jsonArea;
    private JLabel statusLabel;
    private JButton applyButton;
    private JButton closeButton;

    public AiDiagramImportDialog(Frame owner, GUIFramework framework, AiDiagramService service,
            OpenDiagram target) {
        super(owner, "Импорт JSON диаграммы", true);
        this.framework = framework;
        this.service = service;
        this.target = target;
        this.applier = new AiDiagramApplier(framework);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout(8, 8));

        jsonArea = new JTextArea(18, 70);
        jsonArea.setBorder(BorderFactory.createTitledBorder("Вставьте JSON диаграммы"));
        JScrollPane scrollPane = new JScrollPane(jsonArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton downloadTemplateButton = new JButton(new AbstractAction("Скачать шаблон") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                downloadTemplate();
            }
        });

        applyButton = new JButton(new AbstractAction("Применить") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                applyJson();
            }
        });

        closeButton = new JButton(new AbstractAction("Закрыть") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(downloadTemplateButton);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(applyButton);
        rightPanel.add(closeButton);
        buttonPanel.add(leftPanel, BorderLayout.WEST);
        buttonPanel.add(rightPanel, BorderLayout.EAST);

        statusLabel = new JLabel(" ");
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void applyJson() {
        String json = jsonArea.getText();
        if (json == null || json.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Вставьте JSON диаграммы", "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            setBusy(true);
            statusLabel.setText("Проверка JSON...");
            AiDiagramDefinition definition = service.parseDiagramDefinition(json);
            if (definition == null) {
                throw new IllegalStateException("Не удалось разобрать JSON");
            }
            target = applier.apply(target, definition);
            statusLabel.setText("Диаграмма обновлена");
        } catch (Exception ex) {
            statusLabel.setText("Ошибка: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } finally {
            setBusy(false);
        }
    }

    private void setBusy(boolean busy) {
        applyButton.setEnabled(!busy);
        closeButton.setEnabled(!busy);
    }

    private void downloadTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(TEMPLATE_FILE_NAME));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File targetFile = chooser.getSelectedFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile),
                StandardCharsets.UTF_8)) {
            writer.write(buildTemplateContent());
            statusLabel.setText("Шаблон сохранён: " + targetFile.getName());
        } catch (IOException ex) {
            statusLabel.setText("Ошибка сохранения: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildTemplateContent() {
        String line = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Правила формирования JSON диаграммы:").append(line)
                .append("1. JSON должен описывать один лист IDEF0.").append(line)
                .append("2. Объекты перечисляются в массивах functions и streams.").append(line)
                .append("3. Координаты x, y, width, height задаются в долях от ширины/высоты листа (0..1).").append(line)
                .append("4. Стороны потоков: LEFT/RIGHT/TOP/BOTTOM соответствуют входу, выходу, управлению и механизму.")
                .append(line)
                .append("5. Идентификаторы объектов должны быть уникальны.").append(line)
                .append("6. Для границы используйте type=\"boundary\" и не указывайте ref.").append(line)
                .append(line)
                .append("Пример структуры:").append(line)
                .append("{").append(line)
                .append("  \"functions\": [").append(line)
                .append("    {").append(line)
                .append("      \"id\": \"F1\",").append(line)
                .append("      \"name\": \"Основная функция\",").append(line)
                .append("      \"type\": \"process\",").append(line)
                .append("      \"x\": 0.15,").append(line)
                .append("      \"y\": 0.2,").append(line)
                .append("      \"width\": 0.35,").append(line)
                .append("      \"height\": 0.18,").append(line)
                .append("      \"description\": \"описание по желанию\"").append(line)
                .append("    }").append(line)
                .append("  ],").append(line)
                .append("  \"streams\": [").append(line)
                .append("    {").append(line)
                .append("      \"id\": \"S1\",").append(line)
                .append("      \"name\": \"Входные данные\",").append(line)
                .append("      \"source\": {\"type\": \"boundary\", \"side\": \"LEFT\"},").append(line)
                .append("      \"target\": {\"type\": \"function\", \"ref\": \"F1\", \"side\": \"LEFT\"}").append(line)
                .append("    }").append(line)
                .append("  ]").append(line)
                .append("}").append(line)
                .append(line)
                .append("Удалите ненужные элементы и дополните данными вашей диаграммы.");
        return sb.toString();
    }
}

