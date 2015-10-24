/*
 * Created on 2/7/2005
 */
package com.dsoft.pb.idef;

import java.awt.Container;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 * @author ZDD
 */
public class ResourceLoader {
    private static ResourceBundle resources;

    static {
        try {
            resources = ResourceBundle.getBundle("resources.clasificators",
                    getLocale());

            if (Locale.getDefault().getLanguage().equals("uk")) {

                UIManager.put("FileChooser.detailsViewActionLabelText",
                        "Деталі");
                UIManager.put("FileChooser.fileAttrHeaderText", "Атрибути");
                UIManager.put("FileChooser.fileDateHeaderText", "Змінений");
                UIManager.put("FileChooser.fileNameHeaderText", "Назва");
                UIManager.put("FileChooser.fileSizeHeaderText", "Розмір");
                UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
                UIManager
                        .put("FileChooser.filesOfTypeLabelText", "Тип файлів:");
                UIManager.put("FileChooser.homeFolderAccessibleName", "Додому");
                UIManager.put("FileChooser.listViewActionLabelText", "Список");
                UIManager.put("FileChooser.listViewButtonAccessibleName",
                        "Список");
                UIManager
                        .put("FileChooser.listViewButtonToolTipText", "Список");
                UIManager.put("FileChooser.lookInLabelText", "Пергляд в:");
                UIManager.put("FileChooser.newFolderAccessibleName",
                        "Нова папка");
                UIManager.put("FileChooser.newFolderActionLabelText",
                        "Нова папка");
                UIManager.put("FileChooser.newFolderToolTipText",
                        "Створити нову папку");
                UIManager.put("FileChooser.refreshActionLabelText", "Оновити");
                UIManager.put("FileChooser.upFolderAccessibleName", "Наверх");
                UIManager.put("FileChooser.upFolderToolTipText",
                        "На один рівень вище");
                UIManager.put("FileChooser.viewMenuLabelText", "Вигляд");

                UIManager.put("FileChooser.lookInLabelText", "Поточна папка:");
                UIManager.put("FileChooser.fileNameLabelText", "Назва фалу:");
                UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлу:");
                UIManager.put("FileChooser.upFolderToolTipText",
                        "Повернутись ввех на один рівень");
                UIManager.put("FileChooser.upFolderAccessibleName", "Наверх");
                UIManager.put("FileChooser.homeFolderToolTipText", "Додому");
                UIManager.put("FileChooser.homeFolderAccessibleName", "Додому");
                UIManager.put("FileChooser.newFolderToolTipText",
                        "Створити папку");
                UIManager.put("FileChooser.newFolderAccessibleName",
                        "Нова папка");
                UIManager
                        .put("FileChooser.listViewButtonToolTipText", "Список");
                UIManager.put("FileChooser.listViewButtonAccessibleName",
                        "Список");
                UIManager.put("FileChooser.detailsViewButtonToolTipText",
                        "Детально");
                UIManager.put("FileChooser.detailsViewButtonAccessibleName",
                        "Детально");

                UIManager.put("FileChooser.saveInLabelText", "Збережено в:");
                UIManager.put("FileChooser.newFolderErrorText",
                        "Помилка створення нової папки");
                UIManager.put("FileChooser.newFolderErrorSeparator",
                        "В імені папки присутні недопустимі символи");
                UIManager.put("FileChooser.fileDescriptionText", "Опис файлу");
                UIManager.put("FileChooser.directoryDescriptionText",
                        "Опис папки");
                UIManager.put("FileChooser.saveButtonText", "Зберегти");
                UIManager.put("FileChooser.openButtonText", "Відкрити");
                UIManager.put("FileChooser.saveDialogTitleText",
                        "Збереження файлу");
                UIManager
                        .put("FileChooser.openDialogTitleText", "Відкриття...");
                UIManager.put("FileChooser.cancelButtonText", "Відмінити");
                UIManager.put("FileChooser.updateButtonText", "Оновити");
                UIManager.put("FileChooser.helpButtonText", "Допомога");

                UIManager
                        .put("FileChooser.acceptAllFileFilterText", "Всі типи");

                UIManager.put("FileChooser.saveButtonToolTipText", "Зберегти");
                UIManager.put("FileChooser.openButtonToolTipText", "Відкрити");
                UIManager
                        .put("FileChooser.cancelButtonToolTipTex", "Відмінити");
                UIManager.put("FileChooser.updateButtonToolTipText", "Оновити");
                UIManager.put("FileChooser.helpButtonToolTipText", "Допомога");

                UIManager.put("OptionPane.yesButtonText", "Так");
                UIManager.put("OptionPane.noButtonText", "Ні");
                UIManager.put("OptionPane.cancelButtonText", "Відмінити");
                UIManager.put("OptionPane.titleText", "Дайте відповідь");
            } else if (Locale.getDefault().getLanguage().equals("ru")) {

                UIManager.put("FileChooser.detailsViewActionLabelText",
                        "Детали");
                UIManager.put("FileChooser.fileAttrHeaderText", "Атрибуты");
                UIManager.put("FileChooser.fileDateHeaderText", "Измененный");
                UIManager.put("FileChooser.fileNameHeaderText", "Название");
                UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
                UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
                UIManager
                        .put("FileChooser.filesOfTypeLabelText", "Тип файлов:");
                UIManager.put("FileChooser.homeFolderAccessibleName", "Домой");
                UIManager.put("FileChooser.listViewActionLabelText", "Список");
                UIManager.put("FileChooser.listViewButtonAccessibleName",
                        "Список");
                UIManager
                        .put("FileChooser.listViewButtonToolTipText", "Список");
                UIManager.put("FileChooser.lookInLabelText", "Просмотр в:");
                UIManager.put("FileChooser.newFolderAccessibleName",
                        "Новая папка");
                UIManager.put("FileChooser.newFolderActionLabelText",
                        "Новая папка");
                UIManager.put("FileChooser.newFolderToolTipText",
                        "Создать новою папку");
                UIManager.put("FileChooser.refreshActionLabelText", "Обновить");
                UIManager.put("FileChooser.upFolderAccessibleName", "Наверх");
                UIManager.put("FileChooser.upFolderToolTipText",
                        "На один уровень выше");
                UIManager.put("FileChooser.viewMenuLabelText", "Вид");

                UIManager.put("FileChooser.lookInLabelText", "Папка:");
                UIManager.put("FileChooser.fileNameLabelText", "Название файла:");
                UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файла:");
                UIManager.put("FileChooser.upFolderToolTipText",
                        "Вернутся ввех на один уровень");
                UIManager.put("FileChooser.upFolderAccessibleName", "Наверх");
                UIManager.put("FileChooser.homeFolderToolTipText", "Домой");
                UIManager.put("FileChooser.homeFolderAccessibleName", "Домой");
                UIManager.put("FileChooser.newFolderToolTipText",
                        "Создать папку");
                UIManager.put("FileChooser.newFolderAccessibleName",
                        "Новая папка");
                UIManager
                        .put("FileChooser.listViewButtonToolTipText", "Список");
                UIManager.put("FileChooser.listViewButtonAccessibleName",
                        "Список");
                UIManager.put("FileChooser.detailsViewButtonToolTipText",
                        "Детально");
                UIManager.put("FileChooser.detailsViewButtonAccessibleName",
                        "Детально");

                UIManager.put("FileChooser.saveInLabelText", "Сохранено в:");
                UIManager.put("FileChooser.newFolderErrorText",
                        "Ошибка создания новой папки");
                UIManager.put("FileChooser.newFolderErrorSeparator",
                        "В имене папки существуют недопустимые символы");
                UIManager.put("FileChooser.fileDescriptionText", "Описание файла");
                UIManager.put("FileChooser.directoryDescriptionText",
                        "Описание папки");
                UIManager.put("FileChooser.saveButtonText", "Сохранить");
                UIManager.put("FileChooser.openButtonText", "Открыть");
                UIManager.put("FileChooser.saveDialogTitleText",
                        "Сохранения файла");
                UIManager
                        .put("FileChooser.openDialogTitleText", "Открытие...");
                UIManager.put("FileChooser.cancelButtonText", "Отменить");
                UIManager.put("FileChooser.updateButtonText", "Обновить");
                UIManager.put("FileChooser.helpButtonText", "Помощь");

                UIManager
                        .put("FileChooser.acceptAllFileFilterText", "Все типы");

                UIManager.put("FileChooser.saveButtonToolTipText", "Сохранить");
                UIManager.put("FileChooser.openButtonToolTipText", "Открыть");
                UIManager
                        .put("FileChooser.cancelButtonToolTipTex", "Отменить");
                UIManager.put("FileChooser.updateButtonToolTipText", "Обновить");
                UIManager.put("FileChooser.helpButtonToolTipText", "Помощь");

                UIManager.put("OptionPane.yesButtonText", "Да");
                UIManager.put("OptionPane.noButtonText", "Нет");
                UIManager.put("OptionPane.cancelButtonText", "Отменить");
                UIManager.put("OptionPane.titleText", "Дайте ответ");
            }

        } catch (final MissingResourceException mre) {
            System.err.println("\"resources.clasificators\" not found");
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isParent(final Class parent, final Class child) {
        Class par = child;
        do {
            if (par == parent)
                return true;
            par = par.getSuperclass();
        } while (par != null);
        return false;
    }

    public static void setJComponentsText(final Container component) {
        if (isParent(JFrame.class, component.getClass().getSuperclass())) {
            ((JFrame) component).setTitle(getString(((JFrame) component)
                    .getTitle()));
        }

        if (isParent(JDialog.class, component.getClass().getSuperclass())) {
            ((JDialog) component).setTitle(getString(((JDialog) component)
                    .getTitle()));
        }

        for (int i = 0; i < component.getComponentCount(); i++) {
            if (component.getComponent(i).getClass() == JMenu.class)
                setMenuText((JMenu) component.getComponent(i));

            if (component.getComponent(i).getClass() == JLabel.class) {
                ((JLabel) component.getComponent(i))
                        .setText(getString(((JLabel) component.getComponent(i))
                                .getText()));
                ((JLabel) component.getComponent(i))
                        .setToolTipText(getString(((JLabel) component
                                .getComponent(i)).getToolTipText()));
            }

            if (component.getComponent(i).getClass() == JButton.class) {
                ((JButton) component.getComponent(i))
                        .setText(getString(((JButton) component.getComponent(i))
                                .getText()));
                ((JButton) component.getComponent(i))
                        .setToolTipText(getString(((JButton) component
                                .getComponent(i)).getToolTipText()));
            } else if (component.getComponent(i).getClass() == JToggleButton.class) {
                ((JToggleButton) component.getComponent(i))
                        .setText(getString(((JToggleButton) component
                                .getComponent(i)).getText()));
                ((JToggleButton) component.getComponent(i))
                        .setToolTipText(getString(((JToggleButton) component
                                .getComponent(i)).getToolTipText()));
            }

            if (component.getComponent(i).getClass() == JCheckBox.class) {
                ((JCheckBox) component.getComponent(i))
                        .setText(getString(((JCheckBox) component
                                .getComponent(i)).getText()));
                ((JCheckBox) component.getComponent(i))
                        .setToolTipText(getString(((JCheckBox) component
                                .getComponent(i)).getToolTipText()));
            }

            if (component.getComponent(i).getClass() == JRadioButton.class) {
                ((JRadioButton) component.getComponent(i))
                        .setText(getString(((JRadioButton) component
                                .getComponent(i)).getText()));
                ((JRadioButton) component.getComponent(i))
                        .setToolTipText(getString(((JRadioButton) component
                                .getComponent(i)).getToolTipText()));
            }

            if (component.getComponent(i).getClass() == JComboBox.class) {
                ((JComboBox) component.getComponent(i))
                        .setToolTipText(getString(((JComboBox) component
                                .getComponent(i)).getToolTipText()));
            }

            if (isParent(Container.class, component.getComponent(i).getClass()))
                setJComponentsText((Container) component.getComponent(i));
        }
    }

    public static void setMenuItemText(final JMenuItem item) {
        item.setText(getString(item.getText()));
    }

    public static void setMenuText(final JMenu menu) {
        menu.setText(getString(menu.getText()));
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            if (isParent(JMenuItem.class, menu.getMenuComponent(i).getClass())
                    && !isParent(JMenu.class, menu.getMenuComponent(i)
                    .getClass()))
                setMenuItemText((JMenuItem) menu.getMenuComponent(i));
            else if (menu.getMenuComponent(i).getClass() == JMenu.class)
                setMenuText((JMenu) menu.getMenuComponent(i));
        }
    }

    public static void setMenuText(final JPopupMenu menu) {
        for (int i = 0; i < menu.getComponentCount(); i++) {
            if (isParent(JMenuItem.class, menu.getComponent(i).getClass()))
                setMenuItemText((JMenuItem) menu.getComponent(i));
            else if (menu.getComponent(i).getClass() == JMenu.class)
                setMenuText((JMenu) menu.getComponent(i));
            else if (isParent(JRadioButtonMenuItem.class, menu.getComponent(i)
                    .getClass()))
                setMenuItemText((JRadioButtonMenuItem) menu.getComponent(i));

        }
    }

    public static String getString(final String key) {
        if (key == null)
            return null;
        if (key.equals(""))
            return "";
        try {
            return resources.getString(key);
        } catch (final MissingResourceException mre) {
            return null;//key;
        }
    }

    /**
     * @param string
     * @param i
     * @return
     */
    public static int getInteger(final String string, final int i) {
        final String res = getString(string, Integer.toString(i));
        if (res == null)
            return i;
        return new Integer(res).intValue();
    }

    /**
     * @param string
     * @param string2
     * @return
     */
    private static String getString(final String string, final String string2) {
        try {
            return resources.getString(string);
        } catch (final MissingResourceException mre) {
            return string2;
        }
    }

    /**
     * @param string
     * @param d
     * @return
     */
    public static double getDouble(final String string, final double d) {
        final String res = getString(string, Double.toString(d));
        if (res == null)
            return d;
        return new Double(res).doubleValue();
    }

    public static Locale getLocale() {
        return Locale.getDefault();
    }

}
