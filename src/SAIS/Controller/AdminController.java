package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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

public class AdminController implements Initializable {

    @FXML
    private JFXTextField txtUsername, txtPassword, txtFirstName, txtLastName;

    @FXML
    private CheckBox chkSale, chkInventory, chkReports, chkReceiveOrder, chkPurchaseOrder, chkCustomer,
            chkProduct, chkSupplier, chkAdmin;
    @FXML
    private Button btnSave;

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private boolean isNewRecord;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connection = DBInitializer.initializeConnection();

        txtUsername.setOnKeyReleased(event -> {
            if (event.getCode()== KeyCode.ENTER)
                txtPassword.requestFocus();
        });
        txtUsername.focusedProperty().addListener((obsValue,lostFocus,gotFocus)->{
            if(!txtUsername.getText().equals("")&&lostFocus) {
                try {
                    resultSet = connection.createStatement().executeQuery("SELECT * FROM user WHERE username = '"+txtUsername.getText()+"'");
                    if (resultSet.next()) {
                        ConfirmationPageController.Display("Warning","Attention! Username already exists.");
                        txtUsername.setText("");
                        txtUsername.requestFocus();
                    }
                } catch (SQLException | IOException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

    }

    @FXML
    private void selectedBtnNew(ActionEvent event) {
        EditableAndOpacityProperties(true,1);

        for (JFXTextField textField: getJFXTextField()) {
            textField.setText("");
        }
        for (CheckBox checkBox:getCheckBox())
            checkBox.setSelected(false);
        txtUsername.requestFocus();
        isNewRecord = true;

    }

    @FXML
    private void selectedBtnSave (ActionEvent event){
        String query = new String();
        DialogBoxController.DialogBoxDisplay("Save Changes?","Do you want to save changes?","Save");
        try {
            if (!isNull() && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                try{
                    if(isNewRecord) {
                        query = "INSERT INTO user VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    }else {
                        query = "UPDATE user SET username = ?, password = ?, user_firstname = ?, user_lastname = ?, "+
                                "sale = ?, inventory = ?, reports = ?, receive_order = ?, purchase_order = ?, customer = ?," +
                                "product = ?, supplier = ?, admin = ? WHERE username = '"+txtUsername.getText()+"'";
                    }
                    preparedStatement = connection.prepareStatement(query);
                    for (int i =0;i<getJFXTextField().size();i++)
                        preparedStatement.setString(i+1,getJFXTextField().get(i).getText());
                    for (int i = 0; i<getCheckBox().size(); i++ )
                        preparedStatement.setBoolean(i+5,getCheckBox().get(i).isSelected());
                    preparedStatement.executeUpdate();
                    ConfirmationPageController.Display("Confirmation", "Changes has been saved!");
                    txtUsername.setEditable(false);
                    txtUsername.setOpacity(.50);
                    EditableAndOpacityProperties(false,.50);

                }catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (isNull()&& DialogBoxController.getStringSelectedBtn().equals("Save")) {
                ConfirmationPageController.Display("Warning", "Error! Empty fields cannot be saved!");
                 if (!isNewRecord) {
                    //*restores the text fields to default value if changes not saved*//*
                    query = "SELECT * FROM user WHERE username = '" + txtUsername.getText() + "'";
                    resultSet = connection.createStatement().executeQuery(query);
                     if (resultSet.next()) {
                         txtUsername.setText(resultSet.getString(1));
                         for (int i = 1; i < getJFXTextField().size(); i++)
                             getJFXTextField().get(i).setText(resultSet.getString(i + 1));
                         for (int i = 0; i < getCheckBox().size(); i++)
                             getCheckBox().get(i).setSelected(resultSet.getBoolean(i + 5));

                     }
                }
            }
        }catch (NullPointerException | IOException | SQLException nullEx) {}
    }
    /*ActionEvent handler for Delete Button*/
    @FXML
    private void selectedBtnDelete(ActionEvent event) {
        DialogBoxController.DialogBoxDisplay("Confirm","Are you sure you want to delete this record?","Ok");
        try {
            if(DialogBoxController.getStringSelectedBtn().equals("Ok")) {
                String query = "DELETE FROM user WHERE username = ? ";
                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, txtUsername.getText());
                    preparedStatement.executeUpdate();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
                ConfirmationPageController.Display("Confirmation","Record has been successfully deleted.");
                for(JFXTextField textField: getJFXTextField()) {
                    textField.setText("");
                    textField.setEditable(false);
                    textField.setOpacity(.50);
                }
                for(CheckBox checkBox:getCheckBox()) {
                    checkBox.setSelected(false);
                    checkBox.setDisable(true);
                }
            }
        }catch (NullPointerException | IOException nullEx){}
    }
    @FXML
    private void selectedBtnSearch (ActionEvent event) throws IOException, SQLException {
        SearchController.setColumnName(getColumnNames());
        SearchController.Query("SELECT * FROM user");
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
        Stage searchUserPage = new Stage();
        searchUserPage.initModality(Modality.APPLICATION_MODAL);
        Scene searchUserScene = new Scene(root);
        searchUserPage.setTitle("Search Supplier");
        searchUserPage.setScene(searchUserScene);
        searchUserPage.setResizable(false);
        searchUserPage.showAndWait();

        /* Get the searched user from the SearchController class
         *display it on the text fields */

        List<?> resultList = SearchController.getResultList();
        if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null) { //checks if user selected a row in search table if user clicked a row then if conditions is true
            String query = "SELECT * FROM user WHERE username = '"+resultList.get(0)+"'";
            resultSet = connection.createStatement().executeQuery(query);
            if (resultSet.next()) {
                txtUsername.setText(resultSet.getString(1));
                for (int i = 1; i < getJFXTextField().size(); i++)
                    getJFXTextField().get(i).setText(resultSet.getString(i + 1));
                for (int i = 0; i < getCheckBox().size(); i++)
                    getCheckBox().get(i).setSelected(resultSet.getBoolean(i + 5));

                EditableAndOpacityProperties(false, .50);
            }
        }
        else{
            EditableAndOpacityProperties(txtPassword.editableProperty().getValue(),txtPassword.getOpacity());
            txtUsername.setEditable(false);
            txtUsername.setOpacity(.50);
        }
    }

    @FXML
    private void selectedBtnEdit(ActionEvent event){
        EditableAndOpacityProperties(true,1.00);

        try {
            String query ="SELECT * FROM user WHERE username = '"+ txtUsername.getText() + "'";
            resultSet = connection.createStatement().executeQuery(query);
            if (resultSet.next()){
                isNewRecord = false;
                txtUsername.setEditable(false);
                txtUsername.setOpacity(.50);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private List <String> getColumnNames (){
        List <String> columnNames = new ArrayList<>();
        columnNames.add("Username");
        columnNames.add("Password");
        columnNames.add("First Name");
        columnNames.add("Last Name");
        for(CheckBox checkBox:getCheckBox())
            columnNames.add(checkBox.getText());
        return columnNames;
    }
    private List<JFXTextField> getJFXTextField () {
        List <JFXTextField> textFieldList = new ArrayList<>();
        textFieldList.add(txtUsername);
        textFieldList.add(txtPassword);
        textFieldList.add(txtFirstName);
        textFieldList.add(txtLastName);
        return textFieldList;
    }

    private List<CheckBox> getCheckBox () {
        List <CheckBox> checkBoxList = new ArrayList<>();
        checkBoxList.add(chkSale);
        checkBoxList.add(chkInventory);
        checkBoxList.add(chkReports);
        checkBoxList.add(chkReceiveOrder);
        checkBoxList.add(chkPurchaseOrder);
        checkBoxList.add(chkCustomer);
        checkBoxList.add(chkProduct);
        checkBoxList.add(chkSupplier);
        checkBoxList.add(chkAdmin);
        return checkBoxList;
    }

    private boolean isNull () {
        boolean isNull = false;
        for (JFXTextField textField:getJFXTextField()){
            if(textField.getText().equals(""))
                isNull = true;
        }
        return  isNull;
    }

    private void EditableAndOpacityProperties (boolean booleanEditProperty, double opacity) {
      for(JFXTextField textField:getJFXTextField()){
            textField.setEditable(booleanEditProperty);
            textField.setOpacity(opacity);
        }

        for(CheckBox checkBox:getCheckBox()){
            checkBox.setDisable(!booleanEditProperty);
        }
        btnSave.setDisable(!booleanEditProperty);
    }

}
