package org.verwaltung.verwaltung_kunden_ma.PersonDatas;

public class CustomerData extends PersonData
{
    private String industry;

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
