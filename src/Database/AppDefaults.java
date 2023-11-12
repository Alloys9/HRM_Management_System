package Database;

import java.awt.*;

public class AppDefaults {
    public static final Dimension DEFAULT_FRAME_SIZE = new Dimension(600, 600);

    // Database connection defaults
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_URL = "jdbc:mysql://localhost:3306/b_manager?serverTimezone=UTC";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "";

    public static final String COPYRIGHT_TEXT = "\u00A9 2023 A9. All rights reserved.";
}
