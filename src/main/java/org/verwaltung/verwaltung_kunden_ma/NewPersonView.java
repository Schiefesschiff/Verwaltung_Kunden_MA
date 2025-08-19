package org.verwaltung.verwaltung_kunden_ma;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.CustomerData;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.EmployeesTableController;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.ExternalEmployeesData;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;
import org.verwaltung.verwaltung_kunden_ma.database_connection.CustomerDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.EmployeesDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.ExternalEmployeesDAO;

import java.sql.SQLException;


/**
 * Controller for the "New Person" dialog.
 * <p>
 * Allows the user to create new records (employee, external employee or customer)
 * by entering data into the respective tab. After validation the data is persisted
 * using the appropriate DAO. The dialog is loaded by {@link ManagementController}
 * when the user chooses to create a new dataset.
 */
public class NewPersonView
{
    @FXML
    public Button btnAdd;
    public TabPane tabPane;
    private EmployeesDAO employeesDAO;
    private ExternalEmployeesDAO externalEmployeesDAO;
    private CustomerDAO customerDAO;
    private EmployeesTableController employeesTableController;

    @FXML
    private TextField mtfID, mtfVorname, mtfNachname, mtfStraße, mtfPLZ, mtfOrt, mtfTelefon, mtfEmail;
    @FXML
    private TextField etfID, etfVorname, etfNachname, etfStraße, etfPLZ, etfOrt, etfTelefon, etfEmail, etfFirma;
    @FXML
    private TextField tfID, tfVorname, tfNachname, tfStraße, tfPLZ, tfOrt, tfTelefon, tfEmail, tfIndustry;

    /**
     * JavaFX lifecycle method.
     * <p>
     * Called automatically after FXML loading. Initializes UI behavior,
     * such as restricting ID fields to numeric input.
     */
    @FXML
    private void initialize()
    {
        limitNumericField(mtfID, 6);
        limitNumericField(etfID, 6);
        limitNumericField(tfID, 6);
    }

    /**
     * Injects the required DAOs and the overview table controller into this dialog.
     * <p>
     * Must be called by the parent controller after FXML loading to allow saving
     * and updating of the overview table after insertion.
     *
     * @param personDAO                DAO for internal employees
     * @param externalEmployeesDAO     DAO for external employees
     * @param customerDAO              DAO for customers
     * @param employeesTableController controller of the overview table
     */
    public void SetData(EmployeesDAO personDAO, ExternalEmployeesDAO externalEmployeesDAO, CustomerDAO customerDAO, EmployeesTableController employeesTableController)
    {
        this.employeesDAO = personDAO;
        this.externalEmployeesDAO = externalEmployeesDAO;
        this.customerDAO = customerDAO;
        this.employeesTableController = employeesTableController;
    }

    /**
     * Event handler for the "Add" button.
     * <p>
     * Determines which tab is currently active (employee, external employee, or customer),
     * validates the required fields (e.g. ID, company, industry), and checks whether the
     * entered ID already exists in the database.
     * <ul>
     *   <li>If validation fails, an alert dialog is shown and the process is aborted.</li>
     *   <li>If the ID already exists, the user is notified and the record is not inserted.</li>
     *   <li>If all checks pass, the corresponding {@link PersonData} (or subclass) is created,
     *       inserted into the database via the appropriate DAO, and added to the overview table.</li>
     * </ul>
     * Finally, the dialog window is closed after a successful insertion.
     *
     * @implNote
     * The method performs database lookups using the {@code findById()} method of the
     * corresponding DAO to prevent duplicate IDs from being inserted.
     *
     * @see EmployeesDAO#findById(int)
     * @see ExternalEmployeesDAO#findById(int)
     * @see CustomerDAO#findById(int)
     */
    @FXML
    private void onAddClicked()
    {
        Tab active = tabPane.getSelectionModel().getSelectedItem();
        if (active == null) return;

        try
        {
            if (active.getText().equals("neuer Mitarbeiter"))
            {
                int id = validateId(mtfID, "employee");
                if (id == -1) return; // Abbruch, falls ungültig

                if (employeesDAO.findById(id) != null)
                {
                    warn("An employee with ID " + id + " already exists.");
                    mtfID.requestFocus();
                    return;
                }

                var data = readMitarbeiterData();
                employeesDAO.insert(data);
                employeesTableController.addAllPerson(employeesDAO.findAll());
                closeDialog();

            } else if (active.getText().equals("neuer Externer Mitarbeiter"))
            {
                int id = validateId(etfID, "external employee");
                if (id == -1) return;

                if (externalEmployeesDAO.findById(id) != null)
                {
                    warn("An external employee with ID " + id + " already exists.");
                    etfID.requestFocus();
                    return;
                }

                if (etfFirma.getText() == null || etfFirma.getText().isBlank())
                {
                    warn("Please enter a company name for the external employee.");
                    etfFirma.requestFocus();
                    return;
                }

                var data = readExternerMitarbeiterData();
                externalEmployeesDAO.insert(data);
                employeesTableController.addAllPerson(externalEmployeesDAO.findAll());
                closeDialog();

            } else if (active.getText().equals("neuer Kunde"))
            {
                int id = validateId(tfID, "customer");
                if (id == -1) return;

                if (customerDAO.findById(id) != null)
                {
                    warn("A customer with ID " + id + " already exists.");
                    tfID.requestFocus();
                    return;
                }

                if (tfIndustry.getText() == null || tfIndustry.getText().isBlank())
                {
                    warn("Please enter an industry for the customer.");
                    tfIndustry.requestFocus();
                    return;
                }

                var data = readCustomerData();
                customerDAO.insert(data);
                employeesTableController.addAllPerson(customerDAO.findAll());
                closeDialog();
            }

        } catch (SQLException e)
        {
            showError("Database error:\n" + e.getMessage());
        }
    }

    /**
     * Shows a simple warning alert.
     */
    private void warn(String msg)
    {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    /**
     * Shows an error alert.
     */
    private void showError(String msg)
    {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    /**
     * Closes the dialog safely.
     */
    private void closeDialog()
    {
        Stage stage = (Stage) btnAdd.getScene().getWindow();
        stage.close();
    }

    /**
     * Helper method to validate an ID TextField.
     *
     * @param tf      the TextField containing the ID
     * @param context used for error messages (e.g. "employee", "customer")
     * @return the parsed ID, or -1 if invalid
     */
    private int validateId(TextField tf, String context)
    {
        if (tf.getText() == null || tf.getText().isBlank())
        {
            warn("Please enter an " + context + " ID.");
            tf.requestFocus();
            return -1;
        }
        try
        {
            int id = Integer.parseInt(tf.getText().trim());
            if (id <= 0)
            {
                warn("The " + context + " ID must be a positive number.");
                tf.requestFocus();
                return -1;
            }
            return id;
        } catch (NumberFormatException e)
        {
            warn("The " + context + " ID must be a valid number.");
            tf.requestFocus();
            return -1;
        }
    }

    /**
     * Reads and builds a {@link PersonData} instance for an internal employee
     * from the corresponding text fields and assigns the parsed ID.
     *
     * @return a populated {@link PersonData} representing the employee
     */
    private PersonData readMitarbeiterData()
    {
        PersonData p = new PersonData(
                mtfVorname.getText(),
                mtfNachname.getText(),
                mtfStraße.getText(),
                mtfPLZ.getText(),
                mtfOrt.getText(),
                mtfTelefon.getText(),
                mtfEmail.getText()
        );
        p.setId(parseIntSafe(mtfID.getText()));
        return p;
    }

    /**
     * Reads and builds an {@link ExternalEmployeesData} instance from the
     * "external employee" input fields and assigns the parsed ID.
     *
     * @return a populated {@link ExternalEmployeesData} record
     */
    private ExternalEmployeesData readExternerMitarbeiterData()
    {
        ExternalEmployeesData e = new ExternalEmployeesData(
                etfVorname.getText(),
                etfNachname.getText(),
                etfStraße.getText(),
                etfPLZ.getText(),
                etfOrt.getText(),
                etfTelefon.getText(),
                etfEmail.getText(),
                etfFirma.getText()
        );
        e.setId(parseIntSafe(etfID.getText()));
        return e;
    }

    /**
     * Reads and builds a {@link CustomerData} instance from the "customer"
     * input fields and assigns the parsed ID.
     *
     * @return a populated {@link CustomerData} record
     */
    private CustomerData readCustomerData()
    {
        CustomerData e = new CustomerData(
                tfVorname.getText(),
                tfNachname.getText(),
                tfStraße.getText(),
                tfPLZ.getText(),
                tfOrt.getText(),
                tfTelefon.getText(),
                tfEmail.getText(),
                tfIndustry.getText()
        );
        e.setId(parseIntSafe(tfID.getText()));
        return e;
    }

    /**
     * Parses an integer from the given string in a null- and whitespace-safe manner.
     * <p>
     * If parsing fails, this method currently returns {@code 0}. Consider replacing
     * this behavior with validation and user feedback to avoid silently creating
     * invalid IDs.
     *
     * @param s the string to parse (may be {@code null})
     * @return the parsed integer, or {@code 0} if parsing fails
     */
    private int parseIntSafe(String s)
    {
        try
        {
            return Integer.parseInt(s.trim());
        } catch (Exception e)
        {
            System.out.println("not a Number");
            ;
            return 0;
        }
    }


    /**
     * Restricts input in a TextField to digits (0–9)
     * and enforces a maximum length.
     *
     * @param textField the TextField to restrict
     * @param maxLength maximum number of digits allowed
     */
    private void limitNumericField(TextField textField, int maxLength)
    {
        textField.setTextFormatter(new TextFormatter<String>(change ->
        {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*") && newText.length() <= maxLength)
            {
                return change;
            } else
            {
                return null;
            }
        }));
    }
}
