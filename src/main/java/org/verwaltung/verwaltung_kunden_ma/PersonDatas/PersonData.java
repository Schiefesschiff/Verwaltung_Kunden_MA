package org.verwaltung.verwaltung_kunden_ma.PersonDatas;

/**
 * Base data model representing a person.
 * <p>
 * Contains common attributes such as ID, name, address,
 * phone number, and e-mail. It serves as the superclass
 * for more specific types like {@link CustomerData} and
 * {@link ExternalEmployeesData}.
 */
public class PersonData
{
    private int id;
    private String firstName;
    private String lastName;
    private String street;
    private String plz;
    private String place;
    private String phone;
    private String email;


    /**
     * Constructs a new person with the given details.
     *
     * @param firstName first name
     * @param lastName  last name
     * @param street    street address
     * @param plz       postal code
     * @param place     city or town
     * @param phone     phone number
     * @param email     e-mail address
     */
    public PersonData(String firstName, String lastName, String street, String plz, String place, String phone, String email)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.street = street;
        this.plz = plz;
        this.place = place;
        this.phone = phone;
        this.email = email;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getStreet()
    {
        return street;
    }

    public void setStreet(String street)
    {
        this.street = street;
    }

    public String getPlz()
    {
        return plz;
    }

    public void setPlz(String plz)
    {
        this.plz = plz;
    }

    public String getPlace()
    {
        return place;
    }

    public void setPlace(String place)
    {
        this.place = place;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

}
