module org.verwaltung.verwaltung_kunden_ma {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.base;
    // Removed self-referential module requirement

    opens org.verwaltung.verwaltung_kunden_ma to javafx.fxml;
    exports org.verwaltung.verwaltung_kunden_ma;
    exports org.verwaltung.verwaltung_kunden_ma.PersonDatas;
    opens org.verwaltung.verwaltung_kunden_ma.PersonDatas to javafx.fxml;
    exports org.verwaltung.verwaltung_kunden_ma.database_connection;
    opens org.verwaltung.verwaltung_kunden_ma.database_connection to javafx.fxml;
}