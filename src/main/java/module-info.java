module org.verwaltung.verwaltung_kunden_ma {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.verwaltung.verwaltung_kunden_ma to javafx.fxml;
    exports org.verwaltung.verwaltung_kunden_ma;
}