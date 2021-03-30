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
import javafx.scene.control.TextField;
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
import java.util.Scanner;

public class ProductController implements Initializable {

    private Connection connection;
    private static String initQuery,subQuery;
    private static final String QUERY = "SELECT product_id, product_name, product_description, product_critical_level, " +
            "product_buying_price, product_selling_price, supplier_id_no, category_id FROM product ";
    private PreparedStatement preparedStatement;
    private ResultSet rset;
    private static final Integer PROD_IDNO = 10000001;
    private boolean isNewRecord;
    private int tempItemOnHand;
    private TextFieldButtonLoader textFieldButtonLoader;

    @FXML
    private JFXTextField txtSupplier,txtProductIDNo,txtProductName,txtDesc,txtCriticalLevel,txtBuyingPrice,txtSellingPrice,txtCategory;

    @FXML
    private Button btnSave,btnSearchSup,btnSearchCat,btnNew;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connection = DBInitializer.initializeConnection();
        //place the last product_id on top of the record to be used for the next product_id
        textFieldButtonLoader = new TextFieldButtonLoader(JFXTextFieldInitializer(),ButtonInitializer(), PROD_IDNO);
        try {
            initQuery = QUERY + "ORDER BY product_id DESC";
            preparedStatement = connection.prepareStatement(initQuery);
            rset = preparedStatement.executeQuery();

            if(rset.next()) {   //get the last record of the product_id and invokes the DisplayTextField to display the last record on file
                textFieldButtonLoader.DisplayTextField(initQuery);
                TextValidator(txtCategory,"category_id","category_name","category");
                TextValidator(txtSupplier,"supplier_id_no","supplier_name","supplier");
            }else{
                txtProductIDNo.setText(String.valueOf(PROD_IDNO)); //display prodIDNo's default value if there is no record yet
            }

            /*focus listener of text field txtCategory
             * out focus invoke the method TextValidator to check if input exists in the supplier database
             * input can be category_no or category_name
             * */
            txtCategory.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                if(oldValue&&!txtCategory.getText().equals(""))
                    TextValidator(txtCategory,"category_id","category_name","Category");
            });

            /*focus listener of text field txtSupplier
            * out focus invoke the method TextValidator to check if input exists in the supplier database
            * input can be supplier_id_no or supplier_name
            * */
            txtSupplier.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                    if(oldValue&&!txtSupplier.getText().equals(""))
                        TextValidator(txtSupplier,"supplier_id_no","supplier_name","Supplier");
            });

        } catch (SQLException ex) {
            ex.printStackTrace();

        }
        for(int i=1;i<JFXTextFieldInitializer().size()-2;i++){
            int index = i;
            JFXTextFieldInitializer().get(i).setOnKeyReleased(event -> {
                if(event.getCode() == KeyCode.ENTER) {
                    if (index==5)
                        btnNew.requestFocus();
                    else
                        JFXTextFieldInitializer().get(index + 1).requestFocus();
                }
            });
        }
        for(int i=6;i<JFXTextFieldInitializer().size();i++){
            int index = i;
            JFXTextFieldInitializer().get(i).setOnKeyReleased(event -> {
                if(event.getCode() == KeyCode.ENTER) {
                    if (index==6)
                        txtProductName.requestFocus();
                    else
                        txtSupplier.requestFocus();
                }
            });
        }

        /*focus listener that checks if the value entered is numeric value if not numeric
        * an exception will be thrown and catch will display error and clears the input*/
        txtCriticalLevel.focusedProperty().addListener((obs,lostFocus,gotFocus)->{
            if(lostFocus&&!txtCriticalLevel.getText().equals("")){
                try {
                    Integer.parseInt(txtCriticalLevel.getText());
                }catch (NumberFormatException numEx) {
                    try {
                        ConfirmationPageController.Display("Warning","Attention! Input must be a numeric value.");
                        txtCriticalLevel.setText("");
                        txtCriticalLevel.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /*focus listener that checks if the value entered is numeric value if not numeric
         * an exception will be thrown and catch will display error and clears the input*/
        txtBuyingPrice.focusedProperty().addListener((obs,lostFocus,gotFocus)->{
            if(lostFocus&&!txtBuyingPrice.getText().equals("")){
                try {
                   Double.parseDouble(txtBuyingPrice.getText());
                }catch (NumberFormatException numEx) {
                    try {
                        ConfirmationPageController.Display("Warning","Attention! Input must be a numeric value.");
                        txtBuyingPrice.setText("");
                        txtBuyingPrice.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /*focus listener that checks if the value entered is numeric value if not numeric
         * an exception will be thrown and catch will display error and clears the input*/
        txtSellingPrice.focusedProperty().addListener((obs,lostFocus,gotFocus)->{
            if(lostFocus&&!txtSellingPrice.getText().equals("")){
                try {
                    Double.parseDouble(txtSellingPrice.getText());
                }catch (NumberFormatException numEx) {
                    try {
                        ConfirmationPageController.Display("Warning","Attention! Input must be a numeric value.");
                        txtSellingPrice.setText("");
                        txtSellingPrice.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    //button that invokes the category page to create,edit and delete Category
    @FXML
    private void selectedBtnCategory (ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Category.fxml"));
        Stage categoryPage = new Stage();
        categoryPage.initModality(Modality.APPLICATION_MODAL);
        Scene categoryPageScene = new Scene(root);
        categoryPage.setTitle("Category Page");
        categoryPage.setScene(categoryPageScene);
        categoryPage.setResizable(false);
        categoryPage.showAndWait();
    }

    //creates new product
    //gets the last record and increment the product_id by 1 to be the product_id of the new record
    @FXML
    private void selectedBtnNew (ActionEvent event) {
        textFieldButtonLoader.DefaultValues(QUERY + "ORDER BY product_id DESC");
        textFieldButtonLoader.TextFieldEditableProperty(true, 1.00);
        txtCategory.requestFocus();
        isNewRecord = true;

    }
    /*saves a new record for product or save changes or update on the database
     */
    @FXML
    private void selectedBtnSave(ActionEvent event) throws IOException {
        DialogBoxController.DialogBoxDisplay("Save Changes?","Do you want to save changes?","Save"); //display a dialog box to confirm if changes wants to be saved
        boolean isNotNull = textFieldButtonLoader.NullValidator();
        try {
            if (isNotNull && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                try {
                    if (isNewRecord) { //condition that checks if the data are new records, if true INSERT SQL is executed or else UPDATE SQL is executed
                        subQuery = "INSERT INTO product (product_id,product_name,product_description,product_critical_level,product_buying_price,product_selling_price" +
                                ",supplier_id_no, category_id, product_item_on_hand) VALUES (?,?,?,?,?,?,?,?,?)";
                        tempItemOnHand=0;
                        //Update all info on the textfields of the page to the database
                    } else {
                        subQuery = "UPDATE product SET product_id=?, product_name=?, product_description=?, product_critical_level=?, " +
                                "product_buying_price=?, product_selling_price=?, supplier_id_no=?, category_id=?, product_item_on_hand = ?  WHERE product_id = '" +
                                txtProductIDNo.getText() + "'";
                    }
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setString(1, txtProductIDNo.getText());
                    preparedStatement.setString(2, txtProductName.getText());
                    preparedStatement.setString(3, txtDesc.getText());
                    preparedStatement.setString(4, txtCriticalLevel.getText());
                    preparedStatement.setString(5, txtBuyingPrice.getText());
                    preparedStatement.setString(6, txtSellingPrice.getText());
                    preparedStatement.setString(7, new Scanner(txtSupplier.getText()).next());
                    preparedStatement.setString(8, new Scanner(txtCategory.getText()).next());
                    preparedStatement.setInt(9,tempItemOnHand);

                    preparedStatement.executeUpdate();
                    textFieldButtonLoader.TextFieldEditableProperty(false, .50);

                    /*Confirmation page for saved  changes in the databases
                     * creates a confirmation page with parameters title of the stage and text of the label
                     **/
                    ConfirmationPageController.Display("Confirmation", "Changes has been saved!");


                } catch (SQLException | IOException ex) {
                    ex.printStackTrace();
                }

            }  else if (!isNotNull && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                ConfirmationPageController.Display("Warning", "Error! Empty fields cannot be saved!");
                if(isNewRecord){
                    /*restores the text fields to default value if changes not saved*/
                    subQuery = QUERY + " WHERE product_id = '" + txtProductIDNo.getText() + "'";
                    textFieldButtonLoader.DisplayTextField(subQuery);
                }
            }
        }catch (NullPointerException nullEx){
            /*purpose of blank code is to catch the NullPointerException whenever user closed the DialogBox FXML page*/
        }
    }

    /*this event handler is invoked if search button is clicked and another Stage will open to search for
  a product*/
    @FXML
    private void selectedBtnSearch(ActionEvent event) throws IOException {
        SearchController.setColumnName(setColumnNames());
        SearchController.Query("SELECT supplier_id_no,category_id,product_id,product_name,product_description,product_critical_level,product_buying_price," +
                        "product_selling_price FROM product");
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
        Stage searchProdPage = new Stage();
        searchProdPage.initModality(Modality.APPLICATION_MODAL);
        Scene searchProdScene = new Scene(root);
        searchProdPage.setTitle("Search Product");
        searchProdPage.setScene(searchProdScene);
        searchProdPage.setResizable(false);
        searchProdPage.showAndWait();

        /* Get the searched product from the SearchController class
         *display it on the text fields */

        List<?> resultList = SearchController.getResultList();
        if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null) { //checks if user selected a row in search table if user clicked a row then if conditions is true
            subQuery = QUERY + " WHERE product_id = '"+resultList.get(2)+"'";
            textFieldButtonLoader.DisplayTextField(subQuery);
            TextValidator(txtCategory,"category_id","category_name","category");
            TextValidator(txtSupplier,"supplier_id_no","supplier_name","supplier");
            textFieldButtonLoader.TextFieldEditableProperty(false,.50);
        }
        else{
            textFieldButtonLoader.TextFieldEditableProperty(txtProductName.editableProperty().getValue(),1.00);
        }
    }

    //Button for editting records in the database
    @FXML
    private void selectedBtnEdit(ActionEvent event){
        textFieldButtonLoader.TextFieldEditableProperty(true,1.00);
        try {
            subQuery = "SELECT product_item_on_hand FROM product WHERE product_id = '"+ txtProductIDNo.getText() + "'";
            rset = connection.prepareStatement(subQuery).executeQuery();
          /*  isNewRecord = !rset.next();*/
            if(rset.next()){
                isNewRecord = false;
                tempItemOnHand = Integer.parseInt(rset.getString("product_item_on_hand"));
            }
            else
                isNewRecord = true;

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
                String query = "DELETE FROM product WHERE product_id = ? ";
                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, txtProductIDNo.getText());
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
                btnSearchSup.setDisable(true);
                btnSearchCat.setDisable(true);
            }
        }catch (NullPointerException | IOException nullEx){}
    }

    /*search button for supplier
    * gets the column names from SupplierController and invokes the Search Contoller setcolumnames to create a columname for
    * table view and once row has been selected, selected row is displayed on txtSupplier text field
    * */
    @FXML
    private void selectedBtnSearchSup (ActionEvent event){
        SearchController.setColumnName(SupplierController.setColumnNames());
        SearchController.Query("Select * FROM supplier");
        txtSupplier.setEditable(txtSupplier.editableProperty().getValue());
        txtSupplier.setOpacity(txtSupplier.opacityProperty().getValue());
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
            Stage searchSupPage = new Stage();
            searchSupPage.initModality(Modality.APPLICATION_MODAL);
            Scene searchSupScene = new Scene(root);
            searchSupPage.setTitle("Search Supplier");
            searchSupPage.setScene(searchSupScene);
            searchSupPage.setResizable(false);
            searchSupPage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<?> resultList = SearchController.getResultList();
        if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null) {
            txtSupplier.setText(resultList.get(0) + " - " + resultList.get(1));
            txtProductName.requestFocus();
        }
    }

    /*search button for category
     * gets the column names from CategoryController and invokes the Search Controller's setcolumnames to create a columname for
     * table view and once row has been selected, selected row is displayed on txtCategory text field
     * */
    @FXML
    private void selectedBtnSearchCat (ActionEvent event){
        SearchController.setColumnName(CategoryController.setColumnNames());
        SearchController.Query("Select * FROM category");
        txtSupplier.setEditable(txtSupplier.editableProperty().getValue());
        txtSupplier.setOpacity(txtSupplier.opacityProperty().getValue());
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
            Stage searchCatPage = new Stage();
            searchCatPage.initModality(Modality.APPLICATION_MODAL);
            Scene searchCatScene = new Scene(root);
            searchCatPage.setTitle("Search Category");
            searchCatPage.setScene(searchCatScene);
            searchCatPage.setResizable(false);
            searchCatPage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<?> resultList = SearchController.getResultList();
        if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null) {
            txtCategory.setText(resultList.get(0) + " - " + resultList.get(1));
            txtSupplier.requestFocus();
        }
    }

      /*this method is invoked from the text field listener
      * checks whether input exists in the database
      * if exists it displays the table's column_id_no and the column_name
      * it also set editable property and opacity so that no changes can be made
      * if input does not exists in the database confirmationpagecontroller is invoked to display warning that
      * record does not exists.
      * */
      private void TextValidator (TextField textField, String columnTableNo, String columnTableName, String tableName){
        subQuery="SELECT "+columnTableNo+","+columnTableName+ " FROM " +tableName+ " WHERE " +columnTableNo+" = ? OR "+columnTableName+" = ?";
        PreparedStatement prepStatement;
        ResultSet resultSet;
          try {
               prepStatement = connection.prepareStatement(subQuery);
               prepStatement.setString(1,textField.getText());
               prepStatement.setString(2,textField.getText());
               resultSet = prepStatement.executeQuery();

               if(resultSet.next()){
                   textField.setText(resultSet.getString(columnTableNo)+ " - "+resultSet.getString(columnTableName));
               }
               else{
                   try {
                       ConfirmationPageController.Display("Warning",tableName+" does not exists!");
                       textField.setText("");
                       textField.requestFocus();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }

          } catch (SQLException throwables) {
              throwables.printStackTrace();
          }
      }

    /*created an arraylist of string for the attributes of the table Product to be used in creating a table view
  in Search Controller*/
    public static List<String> setColumnNames(){
        List <String> columnName = new ArrayList<>();
        columnName.add("Supplier ID No");
        columnName.add("Category No");
        columnName.add("Product ID No");
        columnName.add("Product Name");
        columnName.add("Description");
        columnName.add("Critical Level");
        columnName.add("Buying Price");
        columnName.add("Selling Price");
        return columnName;
    }

    private List<JFXTextField> JFXTextFieldInitializer(){
        List<JFXTextField> jfxTextFieldList = new ArrayList<>();
        jfxTextFieldList.add(txtProductIDNo);
        jfxTextFieldList.add(txtProductName);
        jfxTextFieldList.add(txtDesc);
        jfxTextFieldList.add(txtCriticalLevel);
        jfxTextFieldList.add(txtBuyingPrice);
        jfxTextFieldList.add(txtSellingPrice);
        jfxTextFieldList.add(txtSupplier);
        jfxTextFieldList.add(txtCategory);
        return  jfxTextFieldList;
    }

    private List<Button> ButtonInitializer(){
        List<Button> buttonList = new ArrayList<>();
        buttonList.add(btnSave);
        buttonList.add(btnSearchSup);
        buttonList.add(btnSearchCat);
        return  buttonList;
    }

}
