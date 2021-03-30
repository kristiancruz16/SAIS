package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import static SAIS.Controller.LogInController.userName;

public class SaleController implements Initializable {

    private Connection connection;
    private String subQuery;
    private ResultSet resultSet;
    private final int SALE_ID = 10000;
    private ObservableList<List<Object>>obsListTable=FXCollections.observableArrayList();
    private ObservableList<Object> selectedRow;
    private int rowCounter;
    private Double totalAmount;
    String getTxtProdIDNo;

    @FXML
    private Button btnCustomerSearch,btnProductSearch, btnClear,btnAdd,btnEdit,btnRemove,btnPayment;

    @FXML
    private Label lblTotalAmount,lblTax,lblSubTotal;


    @FXML
    private JFXTextField txtProductIDNo, txtCustomerIDNo,txtSaleIDNo,txtProductName, txtDescription,
                txtPrice,txtQuantity;

    @FXML
    private TableView<List<Object>> tableSale;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connection = DBInitializer.initializeConnection();
        /*a focus listener that when a user entered a Customer ID NO and loses focus
        a method named TextValidator is invoked*/
        txtCustomerIDNo.focusedProperty().addListener((obsValue, lostFocus, gotFocus) -> {
            if(lostFocus&&!txtCustomerIDNo.getText().equals("")) {
                try {
                    TextValidator(txtCustomerIDNo.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        /*An action event that when a ENTER is pressed it passes the focus on the next node*/
        txtCustomerIDNo.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER)
                txtProductIDNo.requestFocus();
        });

        /*An action event that when a ENTER is pressed it passes the focus on the next node*/
        txtProductIDNo.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER)
                btnAdd.requestFocus();
        });

        /*a focus listener that when the user enters a new text to txtProductIDNo it invokes
        * the ProductTextDisplay that auto populated the other text fields related to Product ID No and
        * prompts a window that asks to enter quantity
        * If the text in txtProductIDNo is not new and came from the selected row in tableSale no arguments
        * is done*/
        txtProductIDNo.focusedProperty().addListener((obsValue, lostFocus, gotFocus) ->{
            if(lostFocus) {
                /*the if condition checks if after TxtProductIDNo loses its focus does the initial value
                * of TxtProductIDNo is blank(it means that the entry is new and didn't got from the selection
                * on tableSale*/
                if (getTxtProdIDNo.equals("")&&!txtProductIDNo.getText().equals(""))
                    ProductTextDisplay(txtProductIDNo.getText());
            }
            /*getTxtProdIDNo indicates if the txtProductIDNo is blank when it got its focus
            * and it is a marker that the text field will be getting a new value*/
            if(gotFocus) {
                getTxtProdIDNo = txtProductIDNo.getText();
            }

        });

        /*sets the columnnames for table tableSale*/
        for (int i=0;i<TableColumnNames().size();i++){
            TableColumn<List<Object>,Object> column = new TableColumn<>(TableColumnNames().get(i));
            int columnIndex = i;
            column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(columnIndex)));
            column.setReorderable(false);
            column.setPrefWidth(tableSale.getPrefWidth()/TableColumnNames().size());
            column.setResizable(false);
            tableSale.getColumns().add(column);
        }

        /*a selection listener of tableSale that displays the selected row to the text fields*/
        tableSale.getSelectionModel().selectedItemProperty().addListener((obsValue,oldValue, newValue)->{
           selectedRow = (ObservableList<Object>) newValue;
           if (selectedRow!=null) {
               for (int i = 0; i < getTextField().size(); i++) {
                   getTextField().get(i).setText(String.valueOf(selectedRow.get(i + 1)));
               }
           }
           /*Since this listener is executed first before the listener of TxtProductIDNo, getTxtProdINo is being
           * assigned to txtProductIDNo to mark that the text in the textfield is no longer new and should
           * not invoke a prompt message asking for a quantity*/
           getTxtProdIDNo = txtProductIDNo.getText();
        });
    }

    /*Action event handler of btnNew
    * it restores the default values, properties of the text fields, buttons, labels and table view
    * also searches that last record saved in the database to be used as indicator for the
    * new Sale ID No */
    @FXML
    private void selectedBtnNew (ActionEvent event) {
        rowCounter=0;
        totalAmount=0.00;
        btnClear.setDisable(false);
        btnCustomerSearch.setDisable(false);
        btnProductSearch.setDisable(false);
        btnAdd.setDisable(false);
        btnEdit.setDisable(false);
        btnRemove.setDisable(false);
        btnPayment.setDisable(false);
        txtCustomerIDNo.setEditable(true);
        txtProductIDNo.setEditable(true);
        txtCustomerIDNo.requestFocus();
        txtCustomerIDNo.clear();
        lblTotalAmount.setText("PHP0.00");
        lblTax.setText("PHP0.00");
        lblSubTotal.setText("PHP0.00");
        txtCustomerIDNo.setOpacity(1);
        tableSale.setDisable(false);
        tableSale.getItems().removeAll(obsListTable);
        for (int i =0;i<getTextField().size();i++)
            getTextField().get(i).clear();

        try {
            resultSet = connection.createStatement().executeQuery("SELECT * FROM sale ORDER BY sale_id_no DESC LIMIT 1");
            if(resultSet.next())
                txtSaleIDNo.setText(String.valueOf(Integer.parseInt(resultSet.getString("sale_id_no"))+1));
            else
                txtSaleIDNo.setText(String.valueOf(SALE_ID+1));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    /*Action event  handler of bntAdd
    adds the text that the user inputs on the textfields to an Observable list named row
    and add row to a List of an Observable list called obsListTable that will
    be added to Table View tableSale to display all the user inputs*/
    @FXML
    private void selectedBtnAdd (ActionEvent event) throws IOException {
        Double lineAmount;
        if (!txtCustomerIDNo.getText().equals("")) {
            if (!txtProductIDNo.getText().equals("")) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                /*adds the text in the text fields to an Observable List named row*/
                row.add(String.valueOf(++rowCounter));
                for (int i = 0; i < getTextField().size(); i++) {
                    row.add(getTextField().get(i).getText());
                }
                /*compute for the line amount by getting the txtSellingPrice and  txtQuantity
                 * and adds it to the Observable List row which will be used in the last column of the tableSale*/
                lineAmount = Double.parseDouble(getTextField().get(3).getText().substring(3)) * Double.parseDouble(getTextField().get(4).getText());
                row.add(String.format("PHP%.2f", lineAmount)); //formats the Double into text that has PHP at the beginning and limits the decimal place

                //adds the line amount to total amount and compute for the tax and subtotal using the totalAmount that will be displayed on the Labels
                totalAmount += lineAmount;
                lblTotalAmount.setText(String.format("PHP%.2f", totalAmount));
                lblSubTotal.setText(String.format("PHP%.2f", totalAmount / 1.12));
                lblTax.setText(String.format("PHP%.2f", (totalAmount / 1.12) * .12));

                obsListTable.add(row);
                tableSale.setItems(obsListTable);

           /*it sets the editable of the text field to false and disables the button
            btnCustomerSearch to make the txtCustomerIDNo immutable
           * once a customer is chosen*/
                txtCustomerIDNo.setEditable(false);
                txtCustomerIDNo.setOpacity(.70);
                btnCustomerSearch.setDisable(true);

                /*clears the text on the text fields*/
                for (int i = 0; i < getTextField().size(); i++)
                    getTextField().get(i).clear();
                txtProductIDNo.requestFocus();
            } else {
                ConfirmationPageController.Display("Warning", "Error! No records to add!");
            }
        }else {
            ConfirmationPageController.Display("Warning", "Attention! Please select customer for the transaction.");
            txtCustomerIDNo.requestFocus();
        }
    }
    /*Action event handler for btnEdit
    * it first checks if there is a value for selectedRow(row selected from tableSale)
    * if row is selected it prompts a window asking user to edit the quantity and once new quantity is entered
    * it removes the existing quantity from the List selectedRow and adds the new quantity to the List
    * modifying the values in selectedRow will also modify the List of Observable List named obsTableList
    * which holds the value of tableSale*/
    @FXML
    private void selectedBtnEdit (ActionEvent event) throws IOException {
        Double lineAmount;
        if(selectedRow!=null){ //condition that checks if there is a row selected in the Table View
            QuantityController.Display("Edit", "Edit Order Quantity", String.valueOf(selectedRow.get(5)));
            try {
                if (QuantityController.getStringSelectedBtn().equals("Ok") && Integer.parseInt(QuantityController.getTxtQuantity()) > 0) {

                    //remove the old value of the quantity from row Observable list and  adds the new quantity that the user entered
                    selectedRow.remove(5);
                    selectedRow.add(5,QuantityController.getTxtQuantity());

                    //computes for the lineAmount of the old selected row and subtract if from the totalAmount
                    lineAmount = Double.parseDouble(String.valueOf(selectedRow.remove(6)).substring(3));
                    totalAmount -=lineAmount;

                    /*compute for the lineAmount of the new selected row and adds it to the totalAmount and be added also
                    to the ObservableList named row that will also update the values in List of ObservableList named obsListTable*/
                    lineAmount = Double.parseDouble(String.valueOf(selectedRow.get(4)).substring(3))
                            *Double.parseDouble(QuantityController.getTxtQuantity());
                    selectedRow.add(6,String.format("PHP%.2f",lineAmount));
                    totalAmount += lineAmount;

                    /*display new label after the edit on the row is done*/
                    lblTotalAmount.setText(String.format("PHP%.2f",totalAmount));
                    lblSubTotal.setText(String.format("PHP%.2f",totalAmount/1.12));
                    lblTax.setText(String.format("PHP%.2f",(totalAmount/1.12)*.12));
                    txtQuantity.setText(QuantityController.getTxtQuantity());
                    tableSale.refresh();

                }
            }catch (NullPointerException nullEx){
                btnEdit.requestFocus();
            }catch (NumberFormatException numEx){
                ConfirmationPageController.Display("Warning", "Invalid! Input only accept numeric values!");
                btnEdit.requestFocus();
            }
        }else
            ConfirmationPageController.Display("Warning", "No selected row to edit!");
    }

    /*Action event handler for btnRemove
    * gets the selected item or row on tableSale and removes it from the table and from
    * the List of Observable List
    * adjusts the subtotal, tax and total amount on the label*/
    @FXML
    private void selectedBtnRemove(ActionEvent event) throws IOException {
        Double lineAmount;
        //condition that checks if there is a selected item on the selection listener of tableSale
        if(selectedRow!=null){
            //deduct the row counter and use this counter to if user wish to add another row on the table
            rowCounter--;

            //gets the line amount of the row that will be removed and will be deducted from the total amount
            lineAmount = Double.parseDouble(String.valueOf(selectedRow.get(4)).substring(3))*Double.parseDouble(String.valueOf(selectedRow.get(5)));
            totalAmount -= lineAmount;
            obsListTable.remove(selectedRow);

            //rearranges the numbers on the line item no column after selected row has been removed
            for (int i=0;i<obsListTable.size();i++){
                obsListTable.get(i).remove(0);
                obsListTable.get(i).add(0,i+1);
            }
            //clears all the text on the text fields after selected item on the table has been removed
            for(int i=0;i<getTextField().size();i++)
                getTextField().get(i).setText("");

            /*display new label after the row is removed*/
            lblTotalAmount.setText(String.format("PHP%.2f",totalAmount));
            lblSubTotal.setText(String.format("PHP%.2f",totalAmount/1.12));
            lblTax.setText(String.format("PHP%.2f",(totalAmount/1.12)*.12));
            tableSale.refresh();
        }else
            ConfirmationPageController.Display("Warning", "No selected row to delete!");
    }

    /*Action event handler for btnPayment
    * it saves new record on sale, product_has_sale, product_history table and
    * updates the item_oh_hand on the product table*/
    @FXML
    private void selectBtnPayment (ActionEvent event) throws IOException, SQLException {
        PreparedStatement preparedStatement;
        Integer itemOnHand=0;
        int prodHistIDNo=0;
        if (obsListTable.size()!=0){
            PaymentFormController.Display(totalAmount);
            try {
                if (PaymentFormController.getSelectedBtn().equals("Ok")) {

                    //adds record in sale table
                    subQuery = "INSERT INTO sale VALUES(?,?,?,?,?)";
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setString(1, txtSaleIDNo.getText());
                    preparedStatement.setString(2, new Scanner(txtCustomerIDNo.getText()).next());
                    preparedStatement.setDate(3, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                    preparedStatement.setDouble(4, Double.parseDouble(lblTotalAmount.getText().substring(3)));
                    preparedStatement.setString(5,userName);
                    preparedStatement.executeUpdate();

                    //adds record in product_has_sale
                    for (int i = 0; i < obsListTable.size(); i++) {
                        subQuery = "INSERT INTO product_has_sale VALUES (?,?,?,?,?)";
                        preparedStatement = connection.prepareStatement(subQuery);
                        preparedStatement.setString(1, txtSaleIDNo.getText());
                        preparedStatement.setString(2, String.valueOf(obsListTable.get(i).get(0)));
                        preparedStatement.setString(3, String.valueOf(obsListTable.get(i).get(1)));
                        preparedStatement.setString(4, String.valueOf(obsListTable.get(i).get(5)));
                        preparedStatement.setDouble(5, Double.parseDouble(obsListTable.get(i).get(4).toString().substring(3))*
                                Integer.parseInt(obsListTable.get(i).get(5).toString()));
                        preparedStatement.executeUpdate();


                        /*gets the quantity of the product's product_item_on_hand and reference it to itemOnHand*/
                        resultSet = connection.createStatement().executeQuery("SELECT product_item_on_hand FROM product WHERE product_id = '" +
                                obsListTable.get(i).get(1) + "'");
                        if (resultSet.next())
                            itemOnHand = Integer.valueOf(resultSet.getString(1));

                        /*gets the last record of product_history and get the product_history_id*/
                        resultSet = connection.createStatement().executeQuery("SELECT * FROM product_history ORDER BY product_history_id DESC LIMIT 1");
                        if (resultSet.next())
                            prodHistIDNo = resultSet.getInt(1) + 1;
                        else
                            prodHistIDNo += 1;

                        /*save new record to product_history table*/
                        subQuery = "INSERT INTO product_history VALUES(?,?,?,?)";
                        preparedStatement = connection.prepareStatement(subQuery);
                        preparedStatement.setInt(1, prodHistIDNo);
                        preparedStatement.setString(2, String.valueOf(obsListTable.get(i).get(1)));
                        preparedStatement.setInt(3, itemOnHand - Integer.parseInt(String.valueOf(obsListTable.get(i).get(5))));
                        preparedStatement.setDate(4, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                        preparedStatement.executeUpdate();

                        /*updates the product_item_on_hand on product table*/
                        subQuery = "UPDATE product SET product_item_on_hand=? WHERE product_id = '" + obsListTable.get(i).get(1) + "'";
                        preparedStatement = connection.prepareStatement(subQuery);
                        preparedStatement.setInt(1, itemOnHand - Integer.parseInt(String.valueOf(obsListTable.get(i).get(5))));
                        preparedStatement.executeUpdate();
                    }

                    ConfirmationPageController.Display("Payment", "Payment successful! Transaction has been saved.");

                    //sets the default text and properties
                    txtSaleIDNo.clear();
                    txtCustomerIDNo.clear();
                    txtCustomerIDNo.setOpacity(1);
                    txtCustomerIDNo.setEditable(false);
                    txtProductIDNo.clear();
                    txtProductIDNo.setOpacity(1);
                    txtProductIDNo.setEditable(false);
                    for (int i = 0; i < getTextField().size(); i++)
                        getTextField().get(i).clear();
                    btnClear.setDisable(true);
                    lblSubTotal.setText("PHP0.00");
                    lblTax.setText("PHP0.00");
                    lblTotalAmount.setText("PHP0.00");
                    tableSale.getItems().removeAll(obsListTable);
                    tableSale.setDisable(false);
                    btnCustomerSearch.setDisable(true);
                    btnProductSearch.setDisable(true);
                }

            }catch (NullPointerException nullEx){ }
        }else
            ConfirmationPageController.Display("Warning", "Error! Cannot make payment for empty fields!");
    }

    /*Action event handler of btnSearch
    * invokes the generic Search Form and once user selected a record, it will then
    * display that record on tableSale */
    @FXML
    private void selectedBtnSearch(ActionEvent event) throws IOException, SQLException {

        //invokes the method InnerJoinQuery of the Search Controller and pass a sql query as a parameter
        SearchController.InnerJoinQuery("SELECT sale_id_no, sale.customer_id_no, cust_lastname, cust_firstname, sale_date, "+
                "sale_total_amount FROM sale INNER JOIN customer USING (customer_id_no) "," GROUP BY sale_id_no ORDER BY sale_id_no","sale");
        SearchController.setColumnName(getSearchColumnNames());
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
        Stage searchSalePage = new Stage();
        searchSalePage.initModality(Modality.APPLICATION_MODAL);
        Scene searchSaleScene = new Scene(root);
        searchSalePage.setTitle("Search Purchase Order");
        searchSalePage.setScene(searchSaleScene);
        searchSalePage.setResizable(false);
        searchSalePage.showAndWait();

        /*after user selected a record on the Search Controller it will then disables buttons to avoid
        * data manipulation and it will then display the record selected on the text fields, labels and table view*/
        List<?> resultList = SearchController.getResultList();
        if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null){
            tableSale.getItems().removeAll(obsListTable);
            tableSale.refresh();
            Double totalAmount =0.00;
            txtSaleIDNo.setText(String.valueOf(resultList.get(0)));
            txtCustomerIDNo.setText(resultList.get(1) + " - " + resultList.get(2) + ", "+ resultList.get(3));
            txtCustomerIDNo.setEditable(false);
            txtCustomerIDNo.setOpacity(.70);
            txtProductIDNo.setEditable(false);
            txtProductIDNo.setOpacity(.70);
            btnAdd.setDisable(true);
            btnEdit.setDisable(true);
            btnRemove.setDisable(true);
            btnPayment.setDisable(true);
            tableSale.setDisable(false);
            totalAmount = Double.parseDouble(String.valueOf(resultList.get(5)));
            lblTotalAmount.setText(String.format("PHP%.2f",totalAmount));
            lblSubTotal.setText(String.format("PHP%.2f",totalAmount/1.12));
            lblTax.setText(String.format("PHP%.2f",(totalAmount/1.12)*.12));

            /*creates a query to find the searched records by using the sale_id_no*/
            subQuery = "SELECT sale_line_item_no, product_has_sale.product_id, product_name, product_description, sale_amount/sale_quantity, "+
                    "sale_quantity, sale_amount FROM product_has_sale INNER JOIN product "+
                    "USING (product_id) WHERE sale_id_no = '"+resultList.get(0)+"'";

            resultSet=connection.createStatement().executeQuery(subQuery);

            /*display the first row of the record found on the text fields*/
            if (resultSet.next()) {
                for (int i=0;i<getTextField().size();i++)
                    getTextField().get(i).setText(resultSet.getString(i+2));
            }
            resultSet=connection.createStatement().executeQuery(subQuery);

            /*display the record found on the table view*/
            while (resultSet.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    row.add(resultSet.getString(i));
                }
                obsListTable.add(row);
            }
            tableSale.setItems(obsListTable);
        }
    }

    /*Action event handler for Cancel button
    * it discards all changes made on the form and
    * restores default values and properties of the text, labels, buttons and table view*/
    @FXML
    private void selectedBtnCancel (ActionEvent event){
        if(!txtSaleIDNo.getText().equals("")) {
            DialogBoxController.DialogBoxDisplay("Warning", "Payment transaction will be cancelled! Do you want to continue?", "Continue");
            try {
                if (DialogBoxController.getStringSelectedBtn().equals("Continue")) {
                    txtSaleIDNo.clear();
                    txtCustomerIDNo.clear();
                    txtCustomerIDNo.setOpacity(1);
                    txtCustomerIDNo.setEditable(false);
                    for (int i = 0; i < getTextField().size(); i++)
                        getTextField().get(i).clear();
                    txtProductIDNo.setEditable(false);
                    btnCustomerSearch.setDisable(true);
                    btnProductSearch.setDisable(true);
                    btnClear.setDisable(true);
                    btnAdd.setDisable(false);
                    btnEdit.setDisable(false);
                    btnRemove.setDisable(false);
                    btnPayment.setDisable(false);
                    lblSubTotal.setText("PHP0.00");
                    lblTax.setText("PHP0.00");
                    lblTotalAmount.setText("PHP0.00");
                    tableSale.getItems().removeAll(obsListTable);
                }
            }catch (NullPointerException nullEx){}
        }
    }

    /*Action event handler for the Customer search button
    * it invokes the SearchController class and displays customer records*/
    @FXML
    private void selectedBtnCustomerSearch(ActionEvent event) throws IOException, SQLException {
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
        if (resultList.size()!=0&&SearchController.getSelectedBtn()!=null){
            txtCustomerIDNo.setText(String.valueOf(resultList.get(0)));
            TextValidator(String.valueOf(resultList.get(0)));
            txtProductIDNo.requestFocus();
        }

    }

    /*Action event handler for the products search button
     * it invokes the SearchController class and displays products records*/
    @FXML
    private void selectedBtnProductSearch(ActionEvent event) throws IOException {
        SearchController.setColumnName(ColumnNames());
        SearchController.Query("SELECT concat(category_id,' ',category_name),product_id, "+
                        "product_name,product_description,product_selling_price,product_item_on_hand "+
                        "FROM product INNER JOIN category USING (category_id) "+
                        "ORDER BY category_id, product_id");
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
        Stage searchCustPage = new Stage();
        searchCustPage.initModality(Modality.APPLICATION_MODAL);
        Scene searchCustScene = new Scene(root);
        searchCustPage.setTitle("Search Product");
        searchCustPage.setScene(searchCustScene);
        searchCustPage.setResizable(false);
        searchCustPage.showAndWait();


        /* Get the searched product from the SearchController class
         *display it on the text fields */

        List<?> resultList = SearchController.getResultList();
        if (resultList.size()!=0&&SearchController.getSelectedBtn()!=null){
            txtProductIDNo.setText(String.valueOf(resultList.get(1)));
            ProductTextDisplay(String.valueOf(resultList.get(1)));

        }
    }

    /*Action event handler for Clear Button
    * this clears the text fields to make a new entry on the tableSale*/
    @FXML
    private void selectedBtnClear (ActionEvent event){
        if (!txtProductIDNo.getText().equals("")){
            for (int i=0;i<getTextField().size();i++)
                getTextField().get(i).setText("");
            txtProductIDNo.requestFocus();
        }
    }

    /*this method displays the product's details once user enter the product on the text field
    * and will ask for the quantity that customer will buy for that product and checks if quantity
    * entered is a valid input*/
    private void ProductTextDisplay(String productIDNo){
        subQuery = "SELECT * FROM product WHERE product_id = '" +productIDNo+"'";
        try {
            resultSet = connection.createStatement().executeQuery(subQuery);

            /*if condition that checks if an entered productIDNo exists and if it exists then proceed*/
            if(resultSet.next()){

                //display details of the product
                txtProductName.setText(resultSet.getString("product_name"));
                txtDescription.setText(resultSet.getString("product_description"));
                txtPrice.setText(String.format("PHP%.2f",resultSet.getDouble("product_selling_price")));

                //prompt message for quantity
                QuantityController.Display("Input","Enter Quantity","1");
                try{

                    //if else if block of codes to check a valid input

                    /*checks condition if the user input is acceptable and selects the ok button
                    * proceed if input is numeric and if non numeric it will cause a number format exception and will be catched
                    * will prompt and error message
                    * proceed if input is greater than 0 */
                    if(QuantityController.getStringSelectedBtn().equals("Ok")
                            &&Integer.parseInt(QuantityController.getTxtQuantity())>0){
                        txtQuantity.setText(QuantityController.getTxtQuantity());
                        btnAdd.requestFocus();

                        /*proceed if the user input is less than or equal to 0*/
                    }  else if (QuantityController.getStringSelectedBtn().equals("Ok")
                            &&Integer.parseInt(QuantityController.getTxtQuantity())<=0) {
                        ConfirmationPageController.Display("Warning", "Invalid! Input should be greater than 0!");
                        txtProductIDNo.requestFocus();

                        //proceed if cancel button is selected and will clear the text fields of the product
                    } else if (QuantityController.getStringSelectedBtn().equals("Cancel")){
                        for(int i=0;i<getTextField().size();i++)
                            getTextField().get(i).clear();
                    }

                //catches a null pointer exception if user closes the QuantityController and it clears the product text fields
                }catch (NullPointerException nullEx){
                    for(int i=0;i<getTextField().size();i++)
                        getTextField().get(i).clear();

                    // catches a number format exception for input that is non numeric
                }catch (NumberFormatException numEx){
                    ConfirmationPageController.Display("Warning", "Invalid! Input only accept numeric values!");
                    txtProductIDNo.requestFocus();
                }
            }
            //an else condition for productIDNo that don't exists on record
            else {
                ConfirmationPageController.Display("Warning", "Product ID does not exists!");
                txtProductIDNo.setText("");
                txtProductIDNo.requestFocus();
            }
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    /*a method that searches for the customer using the customer id no
    * if found it concatenates the id no, last name and first name on the customer text field*/
    private void TextValidator(String queryID) throws IOException {
        subQuery = "SELECT * FROM customer WHERE customer_id_no = '"+queryID+"'";
        try {
            resultSet = connection.createStatement().executeQuery(subQuery);
            if(resultSet.next()){
                txtCustomerIDNo.setText(resultSet.getString("customer_id_no") + " - " +
                        resultSet.getString("cust_lastname") +", "+resultSet.getString("cust_firstname"));
                txtProductIDNo.requestFocus();

            }
            else{
                ConfirmationPageController.Display("Warning", "Customer does not exists!");
                txtCustomerIDNo.setText("");
                txtCustomerIDNo.requestFocus();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //a column names used for product search
    private List<String> ColumnNames(){
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Category");
        columnNames.add("Product ID No");
        columnNames.add("Product Name");
        columnNames.add("Description");
        columnNames.add("Selling Price");
        columnNames.add("Item On Hand");
        return columnNames;
    }

    //column names for tableSale
    private List<String> TableColumnNames (){
        List<String> tableColumnNames = new ArrayList<>();
        tableColumnNames.add("Line Item No");
        tableColumnNames.add("Product ID No");
        tableColumnNames.add("Product Name");
        tableColumnNames.add("Description");
        tableColumnNames.add("Unit Price");
        tableColumnNames.add("Quantity");
        tableColumnNames.add("Amount");
        return tableColumnNames;
    }

    // column names used for sale search
    private List<String> getSearchColumnNames (){
        List<String> searchColumnNames = new ArrayList<>();
        searchColumnNames.add("Sale ID No");
        searchColumnNames.add("Customer ID No");
        searchColumnNames.add("Last Name");
        searchColumnNames.add("First Name");
        searchColumnNames.add("Sale Date");
        searchColumnNames.add("Total Amount");

        return searchColumnNames;
    }

    //arraylist of text fields
    private List<JFXTextField> getTextField(){
        List<JFXTextField> textFieldList = new ArrayList<>();
        textFieldList.add(txtProductIDNo);
        textFieldList.add(txtProductName);
        textFieldList.add(txtDescription);
        textFieldList.add(txtPrice);
        textFieldList.add(txtQuantity);
        return textFieldList;
    }
}
