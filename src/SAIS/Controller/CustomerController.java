package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import SAIS.Others.TextFieldButtonLoader;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {
    @FXML
    private JFXTextField txtCustIDNo, txtCustFirstName, txtCustLastName, txtCustStreet,
            txtCustCity, txtCustProv, txtCustEmail, txtCustPhoneNo;

    @FXML
    private Button btnSave,btnNew;


    private Connection connection;
    private String initQuery,subQuery;
    private static final Integer CUSTIDNO = 1001;
    private PreparedStatement preparedStatement;
    private ResultSet rset;
    private boolean isNewRecord;
    private TextFieldButtonLoader textFieldButtonLoader;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
            connection = DBInitializer.initializeConnection();
            initQuery = "SELECT * FROM customer ORDER BY customer_id_no DESC"; //place the last customer_id_no on top of the record to be used for the next customer_id_no
            textFieldButtonLoader = new TextFieldButtonLoader(JFXTextFieldInitializer(),ButtonInitializer(), CUSTIDNO);
            try {
               preparedStatement = connection.prepareStatement(initQuery);
                rset = preparedStatement.executeQuery();

               if(rset.next()){   //get the last record of the customer_id_no and invokes the DisplayTextField to display the last record on file
                   textFieldButtonLoader.DisplayTextField(initQuery);
               }
               else{
                   txtCustIDNo.setText(String.valueOf(CUSTIDNO)); //display custidno's default value if there is no record yet
               }

            } catch (SQLException ex) {
                ex.printStackTrace();

            }

            for(int i=1;i<JFXTextFieldInitializer().size();i++){
                int index = i;
                JFXTextFieldInitializer().get(i).setOnKeyReleased(event -> {
                    if(event.getCode() == KeyCode.ENTER) {
                        if (index +1== JFXTextFieldInitializer().size())
                            btnNew.requestFocus();
                        else
                            JFXTextFieldInitializer().get(index + 1).requestFocus();
                    }
                });
            }


    }

    /*method for creating a new record for customer*/
    @FXML
    private void selectedBtnNew(ActionEvent event){

        textFieldButtonLoader.DefaultValues(initQuery);
        textFieldButtonLoader.TextFieldEditableProperty(true,1.00);
        txtCustFirstName.requestFocus();
        isNewRecord = true;
    }

    /*saves a new record for customer or save changes or update on the database
    */
    @FXML
    private void selectedBtnSave(ActionEvent event) throws IOException{
        DialogBoxController.DialogBoxDisplay("Save Changes?","Do you want to save changes?","Save"); //display a dialog box to confirm if changes wants to be saved
        boolean isNotNull=textFieldButtonLoader.NullValidator();
        try {
            if (isNotNull && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                try {
                    if (isNewRecord) { //condition that checks if the data are new records, if true INSERT SQL is executed or else UPDATE SQL is executed
                        subQuery = "INSERT INTO customer VALUE(?,?,?,?,?,?,?,?)";
                        //Update all info on the textfields of the page to the database
                    } else {
                        subQuery = "UPDATE customer SET customer_id_no=?, cust_firstname=?, cust_lastname=?, cust_street=?, " +
                                "cust_city=?, cust_province=?, cust_email=?, cust_phoneno=? WHERE customer_id_no = '" +
                                txtCustIDNo.getText() + "'";
                    }
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setString(1, txtCustIDNo.getText());
                    preparedStatement.setString(2, txtCustFirstName.getText());
                    preparedStatement.setString(3, txtCustLastName.getText());
                    preparedStatement.setString(4, txtCustStreet.getText());
                    preparedStatement.setString(5, txtCustCity.getText());
                    preparedStatement.setString(6, txtCustProv.getText());
                    preparedStatement.setString(7, txtCustEmail.getText());
                    preparedStatement.setString(8, txtCustPhoneNo.getText());

                    preparedStatement.executeUpdate();
                    textFieldButtonLoader.TextFieldEditableProperty(false, .50);

                    /*Confirmation page for saved  changes in the databases
                     * creates a confirmation page with parameters title of the stage and text of the label
                     **/
                    ConfirmationPageController.Display("Confirmation", "Changes has been saved!");


                } catch (SQLException | IOException ex) {
                    ex.printStackTrace();
                }

            } else if (!isNotNull && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                ConfirmationPageController.Display("Warning", "Error! Empty fields cannot be saved!");
                if (!isNewRecord) {
                    /*restores the text fields to default value if changes not saved*/
                    subQuery = "SELECT * FROM Customer WHERE customer_id_no = '" + txtCustIDNo.getText() + "'";
                    textFieldButtonLoader.DisplayTextField(subQuery);
                }
            }
        }catch (NullPointerException nullEx){
            /*purpose of blank code is to catch the NullPointerException whenever user closed the DialogBox FXML page*/
        }
    }

    /*this event handler is invoked if search button is clicked and another Stage will open to search for
    a customer*/
    @FXML
    private void selectedBtnSearch(ActionEvent event) throws IOException {
        SearchController.setColumnName(CustomerController.setColumnNames());
        SearchController.Query("SELECT * FROM customer");
       Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
       Stage searchCustPage = new Stage();
       searchCustPage.initModality(Modality.APPLICATION_MODAL);
       Scene searchCustScene = new Scene(root);
       searchCustPage.setTitle("Search Customer");
       searchCustPage.setScene(searchCustScene);
       searchCustPage.setResizable(false);
       searchCustPage.showAndWait();

       /* Get the searched customer from the SearchController class
       *display it on the text fields */

       List<?> resultList = SearchController.getResultList();
       if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null) { //checks if user selected a row in search table if user clicked a row then if conditions is true
            subQuery = "SELECT * FROM customer WHERE customer_id_no = '"+resultList.get(0)+"'";
            textFieldButtonLoader.DisplayTextField(subQuery);
           textFieldButtonLoader.TextFieldEditableProperty(false,.50);
        }
        else{
           textFieldButtonLoader.TextFieldEditableProperty(txtCustFirstName.editableProperty().getValue(),1.00);
       }

    }

    @FXML
    private void selectedBtnEdit(ActionEvent event){
        textFieldButtonLoader.TextFieldEditableProperty(true,1.00);
        try {
            subQuery="SELECT * FROM customer WHERE customer_id_no = '"+ txtCustIDNo.getText() + "'";
            rset = connection.prepareStatement(initQuery).executeQuery();
            isNewRecord = !rset.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    /*ActionEvent handler for Delete Button*/
    @FXML
    private void selectedBtnDelete(ActionEvent event) {
        DialogBoxController.DialogBoxDisplay("Confirm","Are you sure you want to delete this record?","Ok");
        try {
            if(DialogBoxController.getStringSelectedBtn().equals("Ok")) {
                String query = "DELETE FROM customer WHERE customer_id_no = ? ";
                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, txtCustIDNo.getText());
                    preparedStatement.executeUpdate();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
                ConfirmationPageController.Display("Confirmation","Record has been successfully deleted.");
                for(JFXTextField textField:JFXTextFieldInitializer()) {
                    textField.setText("");
                    textField.setEditable(false);
                    textField.setOpacity(.50);
                }
            }
        }catch (NullPointerException | IOException nullEx){}
    }

    /*created an arraylist of string for the attributes of the table Customer to be used in creating a table view
    in Search Controller*/
    public static List<String> setColumnNames(){
        List <String> columnName = new ArrayList<>();
        columnName.add("Customer ID No");
        columnName.add("First Name");
        columnName.add("Last Name");
        columnName.add("Street");
        columnName.add("City");
        columnName.add("Province");
        columnName.add("Email Address");
        columnName.add("Mobile/Phone No");

        return columnName;
    }

    private List<JFXTextField> JFXTextFieldInitializer(){
        List<JFXTextField> jfxTextFieldList = new ArrayList<>();
        jfxTextFieldList.add(txtCustIDNo);
        jfxTextFieldList.add(txtCustFirstName);
        jfxTextFieldList.add(txtCustLastName);
        jfxTextFieldList.add(txtCustStreet);
        jfxTextFieldList.add(txtCustCity);
        jfxTextFieldList.add(txtCustProv);
        jfxTextFieldList.add(txtCustEmail);
        jfxTextFieldList.add(txtCustPhoneNo);
        return  jfxTextFieldList;
    }

    private List<Button> ButtonInitializer(){
        List<Button> buttonList = new ArrayList<>();
        buttonList.add(btnSave);
        return  buttonList;
    }

}
