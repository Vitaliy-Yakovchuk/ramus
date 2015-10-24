package com.ramussoft.pb.frames.docking;

/**
 * Інтерфейс призначений для обробки зміни властивостей.
 *
 * @author Яковчук В. В.
 */

public interface MChangeListener {

    public static final String REFRESH_FUNCTIONS_TREE = "refrash_functions_tree";

    public static final String REFRESH_FUNCTION_IN_TREE = "refrash_function_in_tree";

    public static final String RELOAD_FUNCTION_IN_TREE = "reload_function_in_tree";

    public static final String EXPAND_FUNCTION_IN_TREE = "expand_function_in_tree";

    public static final String REPAINT_MOVEING_AREA = "repaint_moving_area";

    public static final String FILE_LOAD = "file_load";

    public static final String FILE_NEW = "file_new";

    public static final String FILE_SAVE = "file_save";

    public static final String CLOSE_CONNECTION = "closeConnection";

    public static final String REFRESH_REPORTS = "refreshReports";

    void propertyChange(String propertyName, Object newObject);
}
