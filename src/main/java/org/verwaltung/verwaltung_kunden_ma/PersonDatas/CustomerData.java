package org.verwaltung.verwaltung_kunden_ma.PersonDatas;

/**
 * Model class representing a customer.
 * <p>
 * Extends {@link PersonData} with an additional attribute for the customer's industry.
 * Instances of this class are typically created and returned by the {@code CustomerDAO}.
 */
public class CustomerData extends PersonData
{
    private String industry;

    /**
     * Constructs a new customer with the given personal and contact details.
     *
     * @param firstName first name of the customer
     * @param lastName  last name of the customer
     * @param street    street and house number
     * @param plz       postal code
     * @param place     city or town
     * @param phone     telephone number
     * @param email     e-mail address
     * @param industry  industry or business sector
     */
    public CustomerData(String firstName, String lastName, String street, String plz, String place, String phone, String email, String industry)
    {
        super(firstName, lastName, street, plz, place, phone, email);
        this.industry = industry;
    }


    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }
}
