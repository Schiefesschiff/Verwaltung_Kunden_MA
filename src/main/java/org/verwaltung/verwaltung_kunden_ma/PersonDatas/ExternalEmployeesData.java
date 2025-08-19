package org.verwaltung.verwaltung_kunden_ma.PersonDatas;

public class ExternalEmployeesData extends PersonData
{
    private String company;

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
