package org.verwaltung.verwaltung_kunden_ma;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.ExternalEmployeesData;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.EmployeesTableController;
import org.verwaltung.verwaltung_kunden_ma.database_connection.CustomerDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.EmployeesDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.ExternalEmployeesDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.SQLConnector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagementController
{
    public enum DataViewType
    {
        EMPLOYEES,
        EXTERNAL,
        CUSTOMERS
    }

    @FXML
    private Button btnNewDataSet;

    private SQLConnector sqlConnector;
    private EmployeesDAO employeesDAO;
    private ExternalEmployeesDAO externalEmployeesDAO;
    private CustomerDAO costumerDAO;

    //Later, transfer to file
    private final String ip = "192.168.50.138";
    private final String database = "sharehop";
    private final String user = "admin";
    private final String password = "123";

    private DataViewType lastView = DataViewType.EMPLOYEES; // Default, falls noch nichts gewählt

    @FXML
    private EmployeesTableController employeesTableController;
    @FXML
    private IndividualEmployeeViewController individualEmployeeViewController;
    @FXML
    private ExternalEmployeeViewController externalEmployeeViewController;
    @FXML
    private NewPersonView newPersonView;
    @FXML
    private Tab tabOverview;

    @FXML
    private void initialize()
    {
        // hier im Controller den Click-Handler setzen
        btnNewDataSet.setOnAction(e -> OpenNewPersonView());

        sqlConnector = new SQLConnector(ip, database, user, password);
        employeesDAO = new EmployeesDAO(sqlConnector);
        externalEmployeesDAO = new ExternalEmployeesDAO(sqlConnector);
        costumerDAO = new CustomerDAO(sqlConnector);
        employeesTableController.setData(employeesDAO, externalEmployeesDAO, costumerDAO);
        individualEmployeeViewController.setData(employeesDAO);
        externalEmployeeViewController.setData(externalEmployeesDAO);

        tabOverview.setOnSelectionChanged(e ->
        {
            if (tabOverview.isSelected())
            {
                if (employeesTableController != null)
                {
                    switch (lastView)
                    {
                        case EMPLOYEES -> onMitarbeiterClicked();
                        case EXTERNAL -> onExternClicked();
                        case CUSTOMERS -> onKundenClicked();
                    }
                }
            }
        });
    }

    @FXML
    private void onTestDataClicked()
    {
        System.out.println("Button 'Mitarbeiter' wurde geklickt!");

        List<PersonData> personData = new ArrayList<PersonData>();
        Collections.addAll(
                personData,
                new ExternalEmployeesData("Max", "Mustermann", "Musterstraße 1", "12345", "Musterstadt", "0123-456789", "max@example.com", "Firma A"),
                new ExternalEmployeesData("Erika", "Musterfrau", "Beispielweg 2", "54321", "Beispielstadt", "0987-654321", "erika@example.com", "Firma B")
        );
        employeesTableController.addAllPerson(personData);
    }

    @FXML
    private void onMitarbeiterClicked()
    {
        try
        {
            lastView = DataViewType.EMPLOYEES;
            employeesTableController.addAllPerson(employeesDAO.findAll());
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onExternClicked()
    {
        try
        {
            lastView = DataViewType.EXTERNAL;
            employeesTableController.addAllPerson(externalEmployeesDAO.findAll());
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void onKundenClicked()
    {
        try
        {
            lastView = DataViewType.CUSTOMERS;
            employeesTableController.addAllPerson(costumerDAO.findAll());
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    @FXML
    private void OpenNewPersonView()
    {
        try
        {
            System.out.println("Neuer Mitarbeiter");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/verwaltung/verwaltung_kunden_ma/NewPersonView.fxml"));

            Parent root = loader.load();

            newPersonView = loader.getController();
            newPersonView.SetData(employeesDAO, externalEmployeesDAO, costumerDAO, employeesTableController);

            Stage stage = new Stage();
            stage.setTitle("Neuer Mitarbeiter");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();

        } catch (IOException ex)
        {
            ex.printStackTrace();
            showErrorDialog("Error loading the FXML file: " + ex.getMessage());
        }
    }

    private void showErrorDialog(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}