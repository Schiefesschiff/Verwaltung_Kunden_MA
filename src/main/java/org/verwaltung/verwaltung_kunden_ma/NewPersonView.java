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

    @FXML
    private void initialize()
    {
        limitNumericField(mtfID, 6);
        limitNumericField(etfID, 6);
        limitNumericField(tfID, 6);
    }

    public void SetData(EmployeesDAO personDAO, ExternalEmployeesDAO externalEmployeesDAO, CustomerDAO customerDAO, EmployeesTableController employeesTableController)
    {
        this.employeesDAO = personDAO;
        this.externalEmployeesDAO = externalEmployeesDAO;
        this.customerDAO = customerDAO;
        this.employeesTableController = employeesTableController;
    }

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

    private int parseIntSafe(String s)
    {
        try
        {
            return Integer.parseInt(s.trim());
        } catch (Exception e)
        {
            System.out.println("bnlubb");
            ;
            return 0;
        } // oder Validation werfen
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
