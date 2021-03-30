package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
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
import java.util.Scanner;

public class CategoryController implements Initializable {

    @FXML
    private JFXComboBox cboCategory;

    @FXML
    private Button btnSave,btnClose;

    @FXML
    private JFXTextField txtCategoryNo, txtCategoryName;

    private ObservableList<String> obslistCBOCategory = FXCollections.observableArrayList();
    private List<Object> resultList = new ArrayList<>();
    private Connection connection;
    private ResultSet rset;
    private boolean isNotNull;
    private boolean isNewRecord;
    private static Integer duplicateCounter;
    private static boolean haveDuplicate;
    private String query;
    private PreparedStatement preparedStatement;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connection = DBInitializer.initializeConnection();
        ComboBoxLoader();

    }
    @FXML
    private void selectedBtnNew(ActionEvent event){
        DefaultValues();
        TextFieldEditableProperty(true,1.0);
        isNewRecord = true;
    }

    @FXML
    private void selectedBtnSave (ActionEvent event) throws SQLException, IOException {
        DialogBoxController.DialogBoxDisplay("Save Changes?","Do you want to save changes?","Save"); // invokes the confirmation window
        InputTextValidator(); //checks if there is null on the text fields
        HaveDuplicate();
        /*checks condition if there is a button selected in DialogBoxController and Save button is selected and text fields are not null*/
        try {
            if (isNotNull && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                if (isNewRecord && haveDuplicate) { //checks if file is new record and have duplicate and displays warning
                    ConfirmationPageController.Display("Warning", "Error!Duplicate Record!");
                } else if (isNewRecord || duplicateCounter < 2) { //save new and edited file on the record if without duplicate record on the database
                    if (isNewRecord) {
                        query = "INSERT INTO category VALUE(?,?)";
                        //Update all info from the textfields of the page to the database
                    } else {
                        query = "UPDATE category SET category_id=?, category_name=? WHERE category_id = '" +
                                txtCategoryNo.getText() + "' OR category_name = '" + txtCategoryName.getText() + "'";
                    }
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, txtCategoryNo.getText());
                    preparedStatement.setString(2, txtCategoryName.getText());
                    preparedStatement.executeUpdate();
                    TextFieldEditableProperty(false, .50);
                    ConfirmationPageController.Display("Confirmation", "Changes has been saved!");
                    ComboBoxLoader(); //invokes this method to clear the previous list in combo box and updates with new list
                }
            }
            if (DialogBoxController.getStringSelectedBtn().equals("Save") && !isNotNull) {
                ConfirmationPageController.Display("Warning", "Error! Empty fields cannot be saved!");
            }
            if (duplicateCounter > 1) {
                TextFieldEditableProperty(true, 1.0);
                ConfirmationPageController.Display("Warning", "Error!Duplicate Record!");
            }
        }catch (NullPointerException nullEx){
            /*purpose of blank code is to catch the NullPointerException whenever user closed the DialogBox FXML page*/
        }
    }

    //edits the selected category in the combo box
    @FXML
    private void selectedBtnEdit(ActionEvent event){
        try {
            query="SELECT * FROM category WHERE category_id = '"+ txtCategoryNo.getText() +"'";
            rset = connection.prepareStatement(query).executeQuery();
            isNewRecord = !rset.next(); //set a flag to the record retrieved as existing record and used to determine if an SQL for insert or update should be done
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        TextFieldEditableProperty(true,1.00);
    }

    //closes the New Category page and returns back to product page
    @FXML
    private void selectedBtnClose(ActionEvent event){
        Stage exit = (Stage)btnClose.getScene().getWindow();
        exit.close();
    }

    //disable editable property and sets opacity to .50 whenever there is an action event in the combo box
    @FXML
    private void selectedCboCategory(ActionEvent event){

        TextFieldEditableProperty(false,.50);
    }

    //invokes this method at initialize and after a new record has saved
    private void ComboBoxLoader (){
        try {
            if (obslistCBOCategory.size()!=0)
                obslistCBOCategory.clear();
            /*add the records from the database to the ComboBox*/
            rset = connection.createStatement().executeQuery("SELECT * FROM category");
            while (rset.next()) {
                String row ="";
                for(int i=1;i<=rset.getMetaData().getColumnCount();i++){
                    row += rset.getObject(i) + " ";
                }
                obslistCBOCategory.add(row); //add string to the observable list
            }

            cboCategory.setItems(obslistCBOCategory); //add observable list into the combo box
            //text alignment to center
            cboCategory.setButtonCell(new ListCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item);
                        setAlignment(Pos.CENTER);
                        Insets old = getPadding();
                        setPadding(new Insets(old.getTop(), 0, old.getBottom(), 0));
                    }
                }
            });

            //listener to the chosen list in the combo box and displays it to the text fields
            cboCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue)->{
                if(newValue!=null){
                Scanner textDisplay = new Scanner((String) newValue);
                txtCategoryNo.setText(textDisplay.next());
                txtCategoryName.setText(textDisplay.nextLine());}

            });

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    //sets the default text in the textfields
    private void DefaultValues(){
        txtCategoryNo.setText("");
        txtCategoryName.setText("");
    }

    //sets the editable property and opacity of the text fields
    private void TextFieldEditableProperty(boolean booleanEditProperty,double opacity){
        txtCategoryNo.setEditable(booleanEditProperty);
        txtCategoryName.setEditable(booleanEditProperty);
        btnSave.setDisable(!booleanEditProperty);

        txtCategoryNo.setOpacity(opacity);
        txtCategoryName.setOpacity(opacity);

    }
    /*checks if text fields are blank*/
    private void InputTextValidator () {
        isNotNull = !txtCategoryNo.getText().equals("") && !txtCategoryName.getText().equals("");
    }

    //checks if saved file has existing record
    private void HaveDuplicate() {
        haveDuplicate = false;
        duplicateCounter = 0;
        try {
            query="SELECT * FROM category WHERE category_id = '"+ txtCategoryNo.getText() +"' OR category_name = '"+
            txtCategoryName.getText() + "'";
            rset = connection.createStatement().executeQuery(query);
            while (rset.next()) {
                haveDuplicate = true;
                duplicateCounter++;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
    /*created an arraylist of string for the attributes of the table Category to be used in creating a table view
  in Search Controller*/
    public static List<String> setColumnNames(){
        List <String> columnName = new ArrayList<>();
        columnName.add("Category No");
        columnName.add("Category Name");

        return columnName;
    }

}
