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
     * @param personDAO DAO for internal employees
     * @param externalEmployeesDAO DAO for external employees
     * @param customerDAO DAO for customers
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
     * Event handler for the "hinzufügen" button.
     * <p>
     * Determines which tab is currently active (employee, external employee, or customer),
     * reads the entered data, validates required fields and creates the corresponding
     * {@link PersonData} implementation. The new record is saved through the
     * appropriate DAO and added to the overview table.
     */
    @FXML
    private void onAddClicked()
    {
        Tab active = tabPane.getSelectionModel().getSelectedItem();

        if (active.getText().equals("neuer Mitarbeiter"))
        {
            try
            {
                var temp = readMitarbeiterData();
                employeesDAO.insert(temp);
                int id = parseIntSafe(mtfID.getText());
                System.out.println(id);
                employeesTableController.addAllPerson(employeesDAO.findAll());
            } catch (SQLException e)
            {
                // TODO open error dialog
                throw new RuntimeException(e);
            }

        } else if (active.getText().equals("neuer Externer Mitarbeiter"))
        {
            try
            {
                var temp = readExternerMitarbeiterData();
                externalEmployeesDAO.insert(temp);
                int id = parseIntSafe(etfID.getText());
                System.out.println(id);
                employeesTableController.addAllPerson(externalEmployeesDAO.findAll());
            } catch (SQLException e)
            {
                // TODO open error dialog
                throw new RuntimeException(e);
            }

        } else if (active.getText().equals("neuer Kunde"))
        {
            try
            {
                var temp = readCustomerData();
                customerDAO.insert(temp);
                int id = parseIntSafe(etfID.getText());
                System.out.println(id);
                employeesTableController.addAllPerson(customerDAO.findAll());
            } catch (SQLException e)
            {
                // TODO open error dialog
                throw new RuntimeException(e);
            }
        }

        Stage stage = (Stage) btnAdd.getScene().getWindow();
        stage.close();
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
