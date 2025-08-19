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
            PersonData p = cd.getValue(); // Zeilenobjekt holen
            if (p instanceof ExternalEmployeesData e)
            {
                return new ReadOnlyStringWrapper(e.getCompany()); // Wert aus Unterklasse
            }
            return new ReadOnlyStringWrapper(""); // sonst leer
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

    public void addAllPerson(List<? extends PersonData> personList)
    {
        this.personList.clear();
        this.personList.addAll(personList);
        if (!personList.isEmpty()) checkPersonData(personList.get(0));
    }

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
