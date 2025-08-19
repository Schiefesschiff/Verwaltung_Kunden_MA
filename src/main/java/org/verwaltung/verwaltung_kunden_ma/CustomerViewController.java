package org.verwaltung.verwaltung_kunden_ma;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.CustomerData;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.ExternalEmployeesData;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;
import org.verwaltung.verwaltung_kunden_ma.database_connection.CustomerDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.EmployeesDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.ExternalEmployeesDAO;

import java.sql.SQLException;

/**
 * Controller for the detail view of customers.
 * <p>
 * Provides navigation (next/previous), search by customer number, deletion of the
 * current record, and refreshing of the currently displayed customer.
 * Data is retrieved via {@link CustomerDAO} and displayed in label fields defined in FXML.
 */
public class CustomerViewController
{

    private CustomerDAO customerDAO;
    private Integer currentId;

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
    private Label colIndustry;

    @FXML
    private TextField tfSearch; // Eingabefeld für "Mitarbeiternummer eingabe"

    /**
     * Injects the DAO and immediately displays the first record if available.
     * <p>
     * This method should be called by the hosting controller once the DAO is ready.
     *
     * @param personDAO the {@link CustomerDAO} used for database access
     */
    public void setData(CustomerDAO personDAO)
    {
        this.customerDAO = personDAO;

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

    /**
     * Navigates to the next customer (circular) and displays it.
     */
    @FXML
    private void onNext()
    {
        if (customerDAO == null) return;
        try
        {
            PersonData p = (currentId == null)
                    ? customerDAO.findFirst()
                    : customerDAO.findNextByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the previous customer (circular) and displays it.
     */
    @FXML
    private void onPrevious()
    {
        if (customerDAO == null) return;
        try
        {
            PersonData p = (currentId == null)
                    ? customerDAO.findLast()
                    : customerDAO.findPreviousByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Searches a customer by the number entered in {@code tfSearch} and displays it.
     * <p>
     * If the input is not a valid number, a short hint is shown in {@code colId}.
     * If no record is found, the view is cleared and marked as "nicht gefunden".
     */
    @FXML
    private void onSearch()
    {
        if (customerDAO == null || tfSearch == null) return;

        String txt = tfSearch.getText();
        if (txt == null || txt.isBlank()) return;

        try
        {
            int id = Integer.parseInt(txt.trim());
            PersonData p = customerDAO.findById(id);
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

    /**
     * Renders the given customer record into the UI and updates {@code currentId}.
     *
     * @param p the customer record to display (must not be {@code null})
     */
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

        CustomerData external = (CustomerData) p;
        colIndustry.setText(s(external.getIndustry()));
    }

    /**
     * Resets the view to a neutral state (no record displayed) and clears {@code currentId}.
     */
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
        colIndustry.setText("-");
    }

    /**
     * Reloads and re-renders the currently displayed customer by {@code currentId}.
     * <p>
     * If the record no longer exists, the view is cleared. On SQL errors, the view
     * is also cleared after logging the exception.
     */
    public void checkCurrentIdAndRefresh()
    {
        if (customerDAO == null || currentId == null)
        {
            clearView();
            return;
        }

        try
        {
            PersonData p = customerDAO.findById(currentId);
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

    /**
     * Null-safe string helper.
     *
     * @param v the input value (may be {@code null})
     * @return {@code v} or an empty string if {@code v} is {@code null}
     */
    private String s(String v)
    {
        return v == null ? "" : v;
    }

    /**
     * Deletes the currently displayed customer and navigates to the next or previous one.
     * <p>
     * If no further records exist after deletion, the view is cleared.
     *
     * @param actionEvent the triggering button event
     */
    public void onDeleteAction(ActionEvent actionEvent)
    {
        if (customerDAO == null || currentId == null) return;

        int idToDelete = currentId; // aktuelle ID merken

        try
        {
            // 1) Datensatz löschen
            customerDAO.delete(idToDelete);

            // 2) Nächsten suchen
            PersonData next = customerDAO.findNextByIdCircular(idToDelete);

            // 3) Falls kein nächster existiert, den vorherigen anzeigen
            if (next == null)
            {
                next = customerDAO.findPreviousByIdCircular(idToDelete);
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
