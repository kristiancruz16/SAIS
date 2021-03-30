package SAIS.Others;

public class Customer{
     private Integer custIdNo;
     private String custFirstName,custLastName,custStreet,custCity,custProv,custEmail,custPhone;


    public Customer(Integer custIdNo, String custFirstName, String custLastName, String custStreet, String custCity, String custProv, String custEmail, String custPhone) {
        this.custIdNo = custIdNo;
        this.custFirstName = custFirstName;
        this.custLastName = custLastName;
        this.custStreet = custStreet;
        this.custCity = custCity;
        this.custProv = custProv;
        this.custEmail = custEmail;
        this.custPhone = custPhone;
    }

    public Integer getCustIdNo() {
        return custIdNo;
    }

    public void setCustIdNo(Integer custIdNo) {
        this.custIdNo = custIdNo;
    }

    public String getCustFirstName() {
        return custFirstName;
    }

    public void setCustFirstName(String custFirstName) {
        this.custFirstName = custFirstName;
    }

    public String getCustLastName() {
        return custLastName;
    }

    public void setCustLastName(String custLastName) {
        this.custLastName = custLastName;
    }

    public String getCustStreet() {
        return custStreet;
    }

    public void setCustStreet(String custStreet) {
        this.custStreet = custStreet;
    }

    public String getCustCity() {
        return custCity;
    }

    public void setCustCity(String custCity) {
        this.custCity = custCity;
    }

    public String getCustProv() {
        return custProv;
    }

    public void setCustProv(String custProv) {
        this.custProv = custProv;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public void setCustEmail(String custEmail) {
        this.custEmail = custEmail;
    }

    public String getCustPhone() {
        return custPhone;
    }

    public void setCustPhone(String custPhone) {
        this.custPhone = custPhone;
    }
}
