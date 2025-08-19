package org.verwaltung.verwaltung_kunden_ma;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import org.verwaltung.verwaltung_kunden_ma.PersonDatas.EmployeesTableController;
import org.verwaltung.verwaltung_kunden_ma.database_connection.CustomerDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.EmployeesDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.ExternalEmployeesDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.SQLConnector;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main controller for the management application.
 * <p>
 * This controller coordinates all subviews (employees, external employees,
 * customers) and wires them with their respective DAOs. It also handles
 * tab navigation, initializes the database connectors and delegates
 * actions such as loading data or opening dialogs.
 */
public class ManagementController
{
    /**
     * Enumeration of the possible data view types shown in the overview.
     */
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
    private EmployeeViewController individualEmployeeViewController;
    @FXML
    private ExternalEmployeeViewController externalEmployeeViewController;
    @FXML
    private CustomerViewController customerViewController;
    @FXML
    private NewPersonView newPersonView;
    @FXML
    private Tab tabOverview;
    @FXML
    private Tab tabEmployee;
    @FXML
    private Tab tabExternal;
    @FXML
    private Tab tabCustomer;

    /**
     * JavaFX lifecycle method, called automatically after FXML loading.
     * <p>
     * Initializes the SQL connection, instantiates the DAOs,
     * injects them into child controllers and sets up tab selection listeners.
     */
    @FXML
    private void initialize()
    {
        btnNewDataSet.setOnAction(e -> OpenNewPersonView());

        sqlConnector = new SQLConnector(ip, database, user, password);
        employeesDAO = new EmployeesDAO(sqlConnector);
        externalEmployeesDAO = new ExternalEmployeesDAO(sqlConnector);
        costumerDAO = new CustomerDAO(sqlConnector);
        employeesTableController.setData(employeesDAO, externalEmployeesDAO, costumerDAO);
        individualEmployeeViewController.setData(employeesDAO);
        externalEmployeeViewController.setData(externalEmployeesDAO);
        customerViewController.setData(costumerDAO);

        tabOverview.setOnSelectionChanged(e ->
        {
            if (tabOverview.isSelected())
            {
                if (employeesTableController != null)
                {
                    switch (lastView)
                    {
                        case EMPLOYEES -> onEmployeeClicked();
                        case EXTERNAL -> onExternalClicked();
                        case CUSTOMERS -> onCustomerClicked();
                    }
                }
            }
        });

        tabEmployee.setOnSelectionChanged(evt ->
        {
            if (tabEmployee.isSelected())
            {
                individualEmployeeViewController.checkCurrentIdAndRefresh();
            }
        });

        tabExternal.setOnSelectionChanged(evt ->
        {
            if (tabExternal.isSelected())
            {
                externalEmployeeViewController.checkCurrentIdAndRefresh();
            }
        });

        tabCustomer.setOnSelectionChanged(evt ->
        {
            if (tabCustomer.isSelected())
            {
                customerViewController.checkCurrentIdAndRefresh();
            }
        });
    }

    /**
     * Loads all employees into the overview table and
     * marks this view as the last selected one.
     *
     * @throws RuntimeException if a database error occurs
     */
    @FXML
    private void onEmployeeClicked()
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

    /**
     * Loads all external employees into the overview table and
     * marks this view as the last selected one.
     *
     * @throws RuntimeException if a database error occurs
     */
    @FXML
    private void onExternalClicked()
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

    /**
     * Loads all customers into the overview table and
     * marks this view as the last selected one.
     *
     * @throws RuntimeException if a database error occurs
     */
    @FXML
    private void onCustomerClicked()
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

    /**
     * Opens the dialog for creating a new record (employee, external employee or customer).
     * <p>
     * Loads the {@code NewPersonView.fxml}, injects the DAOs into its controller
     * and shows the dialog in a new stage.
     */
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


    /**
     * Displays an error dialog with the given message.
     *
     * @param message the error message to show
     */
    private void showErrorDialog(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}