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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {
    @FXML
    private JFXTextField txtSupIDNo,txtSupName, txtSupStreet, txtSupCity, txtSupProv,txtSupEmail, txtSupPhoneNo;

    @FXML
    private Button btnSave, btnNew;

    private Connection connection;
    private String subQuery,initQuery;
    private static final Integer SUPIDNO = 10001;
    private PreparedStatement preparedStatement;
    private ResultSet rset;
    private boolean isNewRecord;
    private TextFieldButtonLoader textFieldButtonLoader;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connection = DBInitializer.initializeConnection();
        initQuery = "SELECT * FROM supplier ORDER BY supplier_id_no DESC"; //place the last supplier_id_no on top of the record to be used for the next supplier_id_no
        textFieldButtonLoader = new TextFieldButtonLoader(JFXTextFieldInitializer(),ButtonInitializer(), SUPIDNO);
        try {
            preparedStatement = connection.prepareStatement(initQuery);
            rset = preparedStatement.executeQuery();

            if(rset.next()){   //get the last record of the supplier_id_no and invokes the DisplayTextField to display the last record on file
                textFieldButtonLoader.DisplayTextField(initQuery);
            }
            else{
                txtSupIDNo.setText(String.valueOf(SUPIDNO)); //display supIDNo's default value if there is no record yet
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

    /*method for creating a new record for supplier*/
    @FXML
    private void selectedBtnNew (ActionEvent event){
        textFieldButtonLoader.DefaultValues(initQuery);
        textFieldButtonLoader.TextFieldEditableProperty(true,1);
        txtSupName.requestFocus();
        isNewRecord = true;
    }

    /*saves a new record for supplier or save changes or update on the database
     */
    @FXML
    private void selectedBtnSave(ActionEvent event) throws IOException{
        DialogBoxController.DialogBoxDisplay("Save Changes?","Do you want to save changes?","Save"); //display a dialog box to confirm if changes wants to be saved
        boolean isNotNull=textFieldButtonLoader.NullValidator();
        try {
            if (isNotNull && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                try {
                    if (isNewRecord) { //condition that checks if the data are new records, if true INSERT SQL is executed or else UPDATE SQL is executed
                        subQuery = "INSERT INTO supplier VALUE(?,?,?,?,?,?,?)";
                    } else {
                        subQuery = "UPDATE supplier SET supplier_id_no=?, supplier_name=?, supplier_street=?, supplier_city=?, " +
                                "supplier_province=?, supplier_email=?, supplier_phoneno=? WHERE supplier_id_no = '" +
                                txtSupIDNo.getText() + "'";
                    }
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setString(1, txtSupIDNo.getText());
                    preparedStatement.setString(2, txtSupName.getText());
                    preparedStatement.setString(3, txtSupStreet.getText());
                    preparedStatement.setString(4, txtSupCity.getText());
                    preparedStatement.setString(5, txtSupProv.getText());
                    preparedStatement.setString(6, txtSupEmail.getText());
                    preparedStatement.setString(7, txtSupPhoneNo.getText());

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
                    subQuery = "SELECT * FROM supplier WHERE supplier_id_no = '" + txtSupIDNo.getText() + "' ";
                    textFieldButtonLoader.DisplayTextField(subQuery);
                }
            }
        }catch (NullPointerException nullEx){
            /*purpose of blank code is to catch the NullPointerException whenever user closed the DialogBox FXML page*/
        }
    }
    /*this event handler is invoked if search button is clicked and another Stage will open to search for
   a supplier*/
    @FXML
    private void selectedBtnSearch(ActionEvent event) throws IOException{
        SearchController.setColumnName(setColumnNames());
        SearchController.Query("SELECT * FROM supplier");
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
        Stage searchSupPage = new Stage();
        searchSupPage.initModality(Modality.APPLICATION_MODAL);
        Scene searchSupScene = new Scene(root);
        searchSupPage.setTitle("Search Supplier");
        searchSupPage.setScene(searchSupScene);
        searchSupPage.setResizable(false);
        searchSupPage.showAndWait();

        /* Get the searched supplier from the SearchController class
         *display it on the text fields */

        List<?> resultList = SearchController.getResultList();
        if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null) { //checks if user selected a row in search table if user clicked a row then if conditions is true
            subQuery = "SELECT * FROM supplier WHERE supplier_id_no = '"+resultList.get(0)+"'";
            textFieldButtonLoader.DisplayTextField(subQuery);
            textFieldButtonLoader.TextFieldEditableProperty(false,.50);
        }
        else{
            textFieldButtonLoader.TextFieldEditableProperty(txtSupName.editableProperty().getValue(),1.00);
        }

    }

    @FXML
    private void selectedBtnEdit(ActionEvent event){
        textFieldButtonLoader.TextFieldEditableProperty(true,1.00);
        try {
            subQuery ="SELECT * FROM supplier WHERE supplier_id_no = '"+ txtSupIDNo.getText() + "'";
            rset = connection.prepareStatement(subQuery).executeQuery();
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
                String query = "DELETE FROM supplier WHERE supplier_id_no = ? ";
                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, txtSupIDNo.getText());
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

    /*created an arraylist of string for the attributes of the table Supplier to be used in creating a table view
   in Search Controller*/
    public static List<String> setColumnNames(){
        List <String> columnName = new ArrayList<>();
        columnName.add("Supplier ID No");
        columnName.add("Supplier Name");
        columnName.add("Supplier Street");
        columnName.add("Supplier City");
        columnName.add("Supplier Province");
        columnName.add("Email Address");
        columnName.add("Mobile/Phone No");

        return columnName;
    }

    /*creates a List of JFXtext fields that will be used in creating object of  TextFieldButtonLoader*/
    private List<JFXTextField> JFXTextFieldInitializer(){
        List<JFXTextField> jfxTextFieldList = new ArrayList<>();
        jfxTextFieldList.add(txtSupIDNo);
        jfxTextFieldList.add(txtSupName);
        jfxTextFieldList.add(txtSupStreet);
        jfxTextFieldList.add(txtSupCity);
        jfxTextFieldList.add(txtSupProv);
        jfxTextFieldList.add(txtSupEmail);
        jfxTextFieldList.add(txtSupPhoneNo);
        return  jfxTextFieldList;
    }
    /*creates a List of Buttons that will be used in creating object of  TextFieldButtonLoader*/
    private List<Button> ButtonInitializer(){
        List<Button> buttonList = new ArrayList<>();
        buttonList.add(btnSave);
        return  buttonList;
    }
}
