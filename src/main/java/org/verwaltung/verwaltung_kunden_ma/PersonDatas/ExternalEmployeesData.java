package org.verwaltung.verwaltung_kunden_ma.PersonDatas;

/**
 * Data model representing an external employee.
 * <p>
 * Extends {@link PersonData} by adding the company attribute.
 * Used for mapping records from the database table {@code externe_mitarbeiter}.
 */
public class ExternalEmployeesData extends PersonData
{
    private String company;

    /**
     * Constructs a new external employee record.
     *
     * @param firstName first name of the employee
     * @param lastName  last name of the employee
     * @param street    street address
     * @param plz       postal code
     * @param place     city/place
     * @param phone     phone number
     * @param email     email address
     * @param company   associated company name
     */
    public ExternalEmployeesData(String firstName, String lastName, String street, String plz, String place, String phone, String email, String company)
    {
        super(firstName, lastName, street, plz, place, phone, email);
        this.company = company;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }
}
