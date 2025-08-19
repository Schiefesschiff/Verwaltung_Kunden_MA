package org.verwaltung.verwaltung_kunden_ma.PersonDatas;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.verwaltung.verwaltung_kunden_ma.database_connection.CustomerDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.EmployeesDAO;
import org.verwaltung.verwaltung_kunden_ma.database_connection.ExternalEmployeesDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX controller for the overview {@link TableView} of employees, external employees and customers.
 * <p>
 * Configures the table columns, handles population with data from DAOs, and
 * adds delete buttons to each row. Depending on the {@link PersonData} subtype
 * (employee, external employee, or customer) additional columns such as
 * company or industry are displayed dynamically.
 */
public class EmployeesTableController
{
    @FXML
    private TableView<PersonData> employeesTable;
    @FXML
    private TableColumn<PersonData, Integer> colId;
    @FXML
    private TableColumn<PersonData, String> colFirstName;
    @FXML
    private TableColumn<PersonData, String> colLastName;
    @FXML
    private TableColumn<PersonData, String> colStreet;
    @FXML
    private TableColumn<PersonData, String> colPlz;
    @FXML
    private TableColumn<PersonData, String> colPlace;
    @FXML
    private TableColumn<PersonData, String> colPhone;
    @FXML
    private TableColumn<PersonData, String> colEmail;
    @FXML
    private TableColumn<PersonData, String> colCompany;
    @FXML
    private TableColumn<PersonData, String> colIndustry;
    @FXML
    private TableColumn<PersonData, Void> colAction;

    private final ObservableList<PersonData> personList = FXCollections.observableArrayList();

    private EmployeesDAO employeesDAO;
    private ExternalEmployeesDAO externalEmployeesDAO;
    private CustomerDAO customerDAO;

    /**
     * Initializes the table after FXML loading.
     * <p>
     * Configures cell value factories for basic person attributes
     * and installs custom logic for {@code company}, {@code industry}
     * and the row-specific delete button.
     */
    @FXML
    public void initialize()
    {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colStreet.setCellValueFactory(new PropertyValueFactory<>("street"));
        colPlz.setCellValueFactory(new PropertyValueFactory<>("plz"));
        colPlace.setCellValueFactory(new PropertyValueFactory<>("place"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colCompany.setCellValueFactory(cd ->
        {
            PersonData p = cd.getValue();
            if (p instanceof ExternalEmployeesData e)
            {
                return new ReadOnlyStringWrapper(e.getCompany());
            }
            return new ReadOnlyStringWrapper("");
        });

        colIndustry.setCellValueFactory(cd ->
        {
            PersonData p = cd.getValue();
            if (p instanceof CustomerData c)
            {
                return new ReadOnlyStringWrapper(c.getIndustry());
            }
            return new ReadOnlyStringWrapper("");
        });

        colAction.setCellFactory(col -> createDeleteButtonCell());
        employeesTable.setItems(personList);
    }

    /**
     * Injects DAOs used for delete operations and initial loading of data.
     *
     * @param personDAO            DAO for employees
     * @param externalEmployeesDAO DAO for external employees
     * @param customerDAO          DAO for customers
     */
    public void setData(EmployeesDAO personDAO, ExternalEmployeesDAO externalEmployeesDAO, CustomerDAO customerDAO)
    {
        this.employeesDAO = personDAO;
        this.externalEmployeesDAO = externalEmployeesDAO;
        this.customerDAO = customerDAO;

        try
        {
            addAllPerson(personDAO.findAll());
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a custom cell with a "Delete" button
     * that removes the corresponding row both from the table and the database.
     *
     * @return a table cell containing the delete button
     */
    private TableCell<PersonData, Void> createDeleteButtonCell()
    {
        return new TableCell<>()
        {
            private final Button deleteButton = new Button("Löschen");

            {
                deleteButton.setOnAction(event ->
                {
                    PersonData person = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(person);

                    deletePerson(person);
                });
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        };
    }

    /**
     * Replaces all table entries with the given list of persons.
     * Automatically adjusts column visibility for company/industry depending on type.
     *
     * @param personList list of employees, external employees, or customers
     */
    public void addAllPerson(List<? extends PersonData> personList)
    {
        this.personList.clear();
        this.personList.addAll(personList);
        if (!personList.isEmpty()) checkPersonData(personList.get(0));
    }

    /**
     * Adjusts visibility of special columns depending on the type of person.
     *
     * @param person first element of the list (used for type detection)
     */
    private void checkPersonData(PersonData person)
    {    // Spezialspalten standardmäßig aus
        colCompany.setVisible(false);
        colIndustry.setVisible(false);

        if (person instanceof ExternalEmployeesData)
        {
            colCompany.setVisible(true);
        } else if (person instanceof CustomerData)
        {
            colIndustry.setVisible(true);
        }
    }

    /**
     * Deletes the given person both from the table model and from the database.
     * The DAO used depends on the runtime type of the {@link PersonData}.
     *
     * @param person person record to delete
     */
    public void deletePerson(PersonData person)
    {
        if (personList.contains(person))
            personList.remove(person);

        try
        {
            if (person instanceof ExternalEmployeesData)
            {
                externalEmployeesDAO.delete(person.getId());
            } else if (person instanceof CustomerData)
            {
                customerDAO.delete(person.getId());
            } else
            {
                employeesDAO.delete(person.getId());
            }
        } catch (SQLException e)
        {
            // TODO open error dialog
            throw new RuntimeException(e);
        }
    }
}
