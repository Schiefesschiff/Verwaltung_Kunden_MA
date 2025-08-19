package org.verwaltung.verwaltung_kunden_ma;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.ExternalEmployeesData;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;
import org.verwaltung.verwaltung_kunden_ma.database_connection.ExternalEmployeesDAO;

import java.sql.SQLException;

public class ExternalEmployeeViewController
{

    private ExternalEmployeesDAO externalEmployeesDAO;
    private Integer currentId; // aktuell angezeigte mitarbeiternummer

    @FXML
    private AnchorPane root;

    @FXML
    private Label colId;
    @FXML
    private Label colFirstName;
    @FXML
    private Label colLastName;
    @FXML
    private Label colStreet;
    @FXML
    private Label colPlz;
    @FXML
    private Label colPlace;
    @FXML
    private Label colPhone;
    @FXML
    private Label colEmail;
    @FXML
    private Label colCompany;

    @FXML
    private TextField tfSearch; // Eingabefeld für "Mitarbeiternummer eingabe"

    @FXML
    private void initialize()
    {
        root.sceneProperty().addListener((obs, oldScene, newScene) ->
        {
            if (newScene != null)
            {
                javafx.application.Platform.runLater(this::checkCurrentIdAndRefresh);
            }
        });
    }

    /**
     * Vom "Host" (z. B. MainController) aufzurufen, sobald die DAO bereit ist.
     */
    public void setData(ExternalEmployeesDAO personDAO)
    {
        this.externalEmployeesDAO = personDAO;

        // Beim erstmaligen Setzen direkt den ersten Datensatz anzeigen
        try
        {
            PersonData first = personDAO.findFirst();
            if (first != null)
            {
                show(first);
            } else
            {
                clearView();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            clearView();
        }
    }

    // --- Button-Handler (werden im FXML via onAction gebunden) ---

    @FXML
    private void onNext()
    {
        if (externalEmployeesDAO == null) return;
        try
        {
            PersonData p = (currentId == null)
                    ? externalEmployeesDAO.findFirst()
                    : externalEmployeesDAO.findNextByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    private void onPrevious()
    {
        if (externalEmployeesDAO == null) return;
        try
        {
            PersonData p = (currentId == null)
                    ? externalEmployeesDAO.findLast()
                    : externalEmployeesDAO.findPreviousByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * optional: Suche per Button oder Enter im Textfeld
     */
    @FXML
    private void onSearch()
    {
        if (externalEmployeesDAO == null || tfSearch == null) return;

        String txt = tfSearch.getText();
        if (txt == null || txt.isBlank()) return;

        try
        {
            int id = Integer.parseInt(txt.trim());
            PersonData p = externalEmployeesDAO.findById(id);
            if (p != null)
            {
                show(p);
            } else
            {
                // keine Treffer -> UI leeren oder Hinweis anzeigen
                clearView();
                colId.setText("nicht gefunden");
            }
        } catch (NumberFormatException nfe)
        {
            // ungültige Zahl -> kurz anzeigen
            colId.setText("ungültige Nummer");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // --- interne Helfer ---

    private void show(PersonData p)
    {
        currentId = p.getId();

        colId.setText(String.valueOf(p.getId()));
        colFirstName.setText(s(p.getFirstName()));
        colLastName.setText(s(p.getLastName()));
        colStreet.setText(s(p.getStreet()));
        colPlz.setText(s(p.getPlz()));
        colPlace.setText(s(p.getPlace()));
        colPhone.setText(s(p.getPhone()));
        colEmail.setText(s(p.getEmail()));

        ExternalEmployeesData external = (ExternalEmployeesData) p;
        colCompany.setText(s(external.getCompany()));
    }

    private void clearView()
    {
        currentId = null;
        colId.setText("-");
        colFirstName.setText("-");
        colLastName.setText("-");
        colStreet.setText("-");
        colPlz.setText("-");
        colPlace.setText("-");
        colPhone.setText("-");
        colEmail.setText("-");
        colCompany.setText("-");
    }

    public void checkCurrentIdAndRefresh()
    {
        if (externalEmployeesDAO == null || currentId == null)
        {
            clearView();
            return;
        }

        try
        {
            PersonData p = externalEmployeesDAO.findById(currentId);
            if (p != null)
            {
                show(p);   // Datensatz neu anzeigen (falls sich Daten geändert haben)
            } else
            {
                clearView(); // Datensatz existiert nicht mehr
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            clearView(); // im Fehlerfall ebenfalls View leeren
        }
    }

    private String s(String v)
    {
        return v == null ? "" : v;
    }

    public void onDeleteAction(ActionEvent actionEvent)
    {
        if (externalEmployeesDAO == null || currentId == null) return;

        int idToDelete = currentId; // aktuelle ID merken

        try
        {
            // 1) Datensatz löschen
            externalEmployeesDAO.delete(idToDelete);

            // 2) Nächsten suchen
            PersonData next = externalEmployeesDAO.findNextByIdCircular(idToDelete);

            // 3) Falls kein nächster existiert, den vorherigen anzeigen
            if (next == null)
            {
                next = externalEmployeesDAO.findPreviousByIdCircular(idToDelete);
            }

            // 4) Ergebnis anzeigen oder View leeren
            if (next != null)
            {
                show(next); // setzt currentId neu
            } else
            {
                clearView(); // keine Mitarbeiter mehr
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            colId.setText("Fehler beim Löschen");
        }
    }
}
