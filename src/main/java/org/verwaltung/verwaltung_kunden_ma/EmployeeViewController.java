package org.verwaltung.verwaltung_kunden_ma;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;
import org.verwaltung.verwaltung_kunden_ma.database_connection.EmployeesDAO;

import java.sql.SQLException;

/**
 * Controller for the detail view of internal employees.
 * <p>
 * Provides navigation (next/previous), search by employee ID, deletion of the
 * current record, and refreshing of the currently displayed employee. Data is
 * retrieved via {@link EmployeesDAO} and rendered into label fields defined in FXML.
 */
public class EmployeeViewController
{

    private EmployeesDAO employeesDAO;
    private Integer currentId; // aktuell angezeigte mitarbeiternummer

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
    private TextField tfSearch;

    /**
     * Injects the DAO and shows the first record immediately if available.
     * <p>
     * This method should be called by the hosting controller once the DAO is ready.
     *
     * @param personDAO the {@link EmployeesDAO} used for database access
     */
    public void setData(EmployeesDAO personDAO)
    {
        this.employeesDAO = personDAO;

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
     * Navigates to the next employee (circular) and displays it.
     */
    @FXML
    private void onNext()
    {
        if (employeesDAO == null) return;
        try
        {
            PersonData p = (currentId == null)
                    ? employeesDAO.findFirst()
                    : employeesDAO.findNextByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the previous employee (circular) and displays it.
     */
    @FXML
    private void onPrevious()
    {
        if (employeesDAO == null) return;
        try
        {
            PersonData p = (currentId == null)
                    ? employeesDAO.findLast()
                    : employeesDAO.findPreviousByIdCircular(currentId);
            if (p != null) show(p);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Searches an employee by the number entered in {@code tfSearch} and displays it.
     * <p>
     * If the input is not a valid number, a short hint is shown in {@code colId}.
     * If no record is found, the view is cleared and marked as "nicht gefunden".
     */
    @FXML
    private void onSearch()
    {
        if (employeesDAO == null || tfSearch == null) return;

        String txt = tfSearch.getText();
        if (txt == null || txt.isBlank()) return;

        try
        {
            int id = Integer.parseInt(txt.trim());
            PersonData p = employeesDAO.findById(id);
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
     * Renders the given person data into the UI and updates {@code currentId}.
     *
     * @param p the employee record to display (must not be null)
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
    }

    /**
     * Reloads and re-renders the currently displayed employee by {@code currentId}.
     * <p>
     * If the record no longer exists, the view is cleared. On SQL errors, the view
     * is also cleared after logging the exception.
     */
    public void checkCurrentIdAndRefresh()
    {
        if (employeesDAO == null || currentId == null)
        {
            clearView();
            return;
        }

        try
        {
            PersonData p = employeesDAO.findById(currentId);
            if (p != null)
            {
                show(p);
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
     * Deletes the currently displayed employee and navigates to the next or previous one.
     * <p>
     * If no further records exist after deletion, the view is cleared.
     *
     * @param actionEvent the triggering button event
     */
    public void onDeleteAction(ActionEvent actionEvent)
    {
        if (employeesDAO == null || currentId == null) return;

        int idToDelete = currentId;

        try
        {
            employeesDAO.delete(idToDelete);

            PersonData next = employeesDAO.findNextByIdCircular(idToDelete);

            if (next == null)
            {
                next = employeesDAO.findPreviousByIdCircular(idToDelete);
            }

            if (next != null)
            {
                show(next);
            } else
            {
                clearView();
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            colId.setText("Fehler beim Löschen");
        }
    }
}
