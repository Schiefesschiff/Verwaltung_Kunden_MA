package org.verwaltung.verwaltung_kunden_ma;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;
import org.verwaltung.verwaltung_kunden_ma.database_connection.EmployeesDAO;

import java.sql.SQLException;

public class IndividualEmployeeViewController {

    private EmployeesDAO employeesDAO;
    private Integer currentId; // aktuell angezeigte mitarbeiternummer

    @FXML private Label colId;
    @FXML private Label colFirstName;
    @FXML private Label colLastName;
    @FXML private Label colStreet;
    @FXML private Label colPlz;
    @FXML private Label colPlace;
    @FXML private Label colPhone;
    @FXML private Label colEmail;

    @FXML private TextField tfSearch; // Eingabefeld für "Mitarbeiternummer eingabe"

    /** Vom "Host" (z. B. MainController) aufzurufen, sobald die DAO bereit ist. */
    public void setData(EmployeesDAO personDAO) {
        this.employeesDAO = personDAO;

        // Beim erstmaligen Setzen direkt den ersten Datensatz anzeigen
        try {
            PersonData first = personDAO.findFirst();
            if (first != null) {
                show(first);
            } else {
                clearView();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            clearView();
        }
    }

    // --- Button-Handler (werden im FXML via onAction gebunden) ---

    @FXML
    private void onNext() {
        if (employeesDAO == null) return;
        try {
            PersonData p = (currentId == null)
                    ? employeesDAO.findFirst()
                    : employeesDAO.findNextByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onPrevious() {
        if (employeesDAO == null) return;
        try {
            PersonData p = (currentId == null)
                    ? employeesDAO.findLast()
                    : employeesDAO.findPreviousByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** optional: Suche per Button oder Enter im Textfeld */
    @FXML
    private void onSearch() {
        if (employeesDAO == null || tfSearch == null) return;

        String txt = tfSearch.getText();
        if (txt == null || txt.isBlank()) return;

        try {
            int id = Integer.parseInt(txt.trim());
            PersonData p = employeesDAO.findById(id);
            if (p != null) {
                show(p);
            } else {
                // keine Treffer -> UI leeren oder Hinweis anzeigen
                clearView();
                colId.setText("nicht gefunden");
            }
        } catch (NumberFormatException nfe) {
            // ungültige Zahl -> kurz anzeigen
            colId.setText("ungültige Nummer");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- interne Helfer ---

    private void show(PersonData p) {
        currentId = p.getId();

        colId.setText(String.valueOf(p.getId()));
        colFirstName.setText(s(p.getFirstName()));
        colLastName.setText(s(p.getLastName()));
        colStreet.setText(s(p.getStreet()));
        colPlz.setText(s(p.getPlz()));
        colPlace.setText(s(p.getPlace()));
        colPhone.setText(s(p.getPhone()));
        colEmail.setText(s(p.getEmail()));
    }

    private void clearView() {
        currentId = null;
        colId.setText("-");
        colFirstName.setText("-");
        colLastName.setText("-");
        colStreet.setText("-");
        colPlz.setText("-");
        colPlace.setText("-");
        colPhone.setText("-");
        colEmail.setText("-");
    }

    private String s(String v) {
        return v == null ? "" : v;
    }

    public void onDeleteAction(ActionEvent actionEvent)
    {
        if (employeesDAO == null || currentId == null) return;

        int idToDelete = currentId; // aktuelle ID merken

        try {
            // 1) Datensatz löschen
            employeesDAO.delete(idToDelete);

            // 2) Nächsten suchen
            PersonData next = employeesDAO.findNextByIdCircular(idToDelete);

            // 3) Falls kein nächster existiert, den vorherigen anzeigen
            if (next == null) {
                next = employeesDAO.findPreviousByIdCircular(idToDelete);
            }

            // 4) Ergebnis anzeigen oder View leeren
            if (next != null) {
                show(next); // setzt currentId neu
            } else {
                clearView(); // keine Mitarbeiter mehr
            }

        } catch (SQLException e) {
            e.printStackTrace();
            colId.setText("Fehler beim Löschen");
        }
    }
}
