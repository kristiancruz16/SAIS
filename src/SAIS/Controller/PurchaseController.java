package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static SAIS.Controller.LogInController.userName;



public class PurchaseController implements Initializable {
    @FXML
    private ChoiceBox choiceBoxSearch;

    @FXML
    private Button btnSearchChoice,btnSelect,btnSave,btnNew,btnCancel,btnEdit,btnRemove;

    @FXML
    private JFXTextField txtSearch;

    @FXML
    private Label lblPurchaseOrderNo, lblSupplierIDNo, lblPurchaseOrderStatus, lblPurchaseOrderDate, lblTotalAmount;


    /*initializes Table, List,Observable list for column names and rows for the Inventory Table and Purchase Order Table*/
    @FXML
    private TableView<List<Object>> tableInventory, tablePurchaseOrder;
    private Object selectedInventoryRow,selectedPurchaseRow;
    private ObservableList<List<Object>> selectedTableRow = FXCollections.observableArrayList();
    private ObservableList<List<Object>> obsListTableInventory = FXCollections.observableArrayList();
    /*----------------------------------------------------------------------------------------------------*/


    private String initialQuery,subQuery,selectedSearch = "";
    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private  int purOrderNo = 100000;
    private  int rowCounter = 0;
    private  double totalAmount =0.00;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        connection= DBInitializer.initializeConnection();
        /*creates a drop down menu for the Choice Box*/
        choiceBoxSearch.setItems(ChoiceBoxSelection());
        initialQuery = "SELECT supplier_id_no,category_id,product_id,product_name,product_description,product_buying_price,product_critical_level,product_item_on_hand "+
                "FROM product";

        //displays the formatted table column names in the table view named tableData
        for (int i = 0; i < TableInventoryColumnNames().size() ; i++) {
            TableColumn<List<Object>, Object> column = new TableColumn<>(TableInventoryColumnNames().get(i));
            int columnIndex = i ;
            column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(columnIndex)));
            column.setPrefWidth(tableInventory.getPrefWidth()/TableInventoryColumnNames().size());
            column.setResizable(false);
            column.setReorderable(false);
            tableInventory.getColumns().add(column);
        }

        //displays the formatted table column names in the table view named tableData

        for (int i = 0; i < TablePurchaseOrderColumnNames().size(); i++) {
            TableColumn<List<Object>, Object> column = new TableColumn<>(TablePurchaseOrderColumnNames().get(i));
            int columnIndex = i;
            column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(columnIndex)));
            column.setPrefWidth(tablePurchaseOrder.getPrefWidth()/TablePurchaseOrderColumnNames().size());
            column.setResizable(false);
            column.setReorderable(false);
            tablePurchaseOrder.getColumns().add(column);
        }

        //change listener for the ChoiceBox
        choiceBoxSearch.getSelectionModel().selectedItemProperty().addListener((obsValue,oldValue,newValue)-> SelectedSearch((String) newValue));

        //change listener for the Table View tableInventory and clear selection in tablePurchaseOrder
        tableInventory.getSelectionModel().selectedItemProperty().addListener((obs,oldValue, newValue) -> {
           selectedInventoryRow = newValue;
           if(newValue!=null)
               tablePurchaseOrder.getSelectionModel().clearSelection();
        });

        /*change listener for the Table View tablePurchaseOrder and clear selection in tableInventory*/
        tablePurchaseOrder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<List<Object>>() {
            @Override
            public void changed(ObservableValue<? extends List<Object>> obs, List<Object> oldValue, List<Object> newValue) {
                selectedPurchaseRow = newValue;
                if (newValue != null)
                    tableInventory.getSelectionModel().clearSelection();
            }
        });



    }

    //create strings for ChoiceBox
    private ObservableList<String> ChoiceBoxSelection(){
        ObservableList <String>choiceBoxSelection = FXCollections.observableArrayList();
        choiceBoxSelection.add("Product");
        choiceBoxSelection.add("Critical Level");
        choiceBoxSelection.add("Supplier");
        choiceBoxSelection.add("Category");
        return choiceBoxSelection;
    }

    /*method invoked form the selected string in the ChoiceBox and  appends the initial query depends on what
    search does the user defined */
    private void SelectedSearch(String selectedSearch){
        this.selectedSearch = selectedSearch;
        subQuery = initialQuery;
        EditableProperty(txtSearch,false,.50);
        switch (selectedSearch){
            case "Product":
                btnSearchChoice.requestFocus();
                txtSearch.setText("");
                break;
            case "Critical Level" :
                subQuery += " WHERE product_item_on_hand <= product_critical_level";
                btnSearchChoice.requestFocus();
                txtSearch.setText("");
                break;
            case "Category":
                subQuery += " WHERE category_id = ";
                EditableProperty(txtSearch,true,1);
                txtSearch.requestFocus();
                break;
            case "Supplier" :
                subQuery += " WHERE supplier_id_no = ";
                EditableProperty(txtSearch,true,1);
                txtSearch.requestFocus();
                break;
            default:
                break;
        }
    }

    /*Action event listener for the Button btnSearchChoice that makes a query to the database and creates records in
    the Table View tableInventory */

    @FXML
    private void selectedBtnSearchChoice (ActionEvent event){
        TableInventoryDisplay();
    }

    /*invokes method to update the records in tableInventory
    * invokes during initial creation of purchase order and when a selection is removed in tablePurchaseOrder*/
    private void TableInventoryDisplay() {
        obsListTableInventory.clear();
        try {
            if (selectedSearch.equals("Critical Level")||selectedSearch.equals("Product"))
                resultSet = connection.createStatement().executeQuery(subQuery);
            else if(selectedSearch.equals("Supplier")||selectedSearch.equals("Category")){
                resultSet = connection.createStatement().executeQuery(subQuery + "'" + txtSearch.getText()+"'");
            }
            else if(selectedSearch.equals("")){
                ConfirmationPageController.Display("Warning","Illegal action performed!");
            }

            /*creates a temporary Observable List that will hold the records for the List of Observable List to be displayed in Table View
             * named tableInventory*/
            while (resultSet.next()){
                ObservableList<Object> row = FXCollections.observableArrayList();
                for(int i=1;i<=resultSet.getMetaData().getColumnCount();i++) {
                    row.add(resultSet.getObject(i));
                }
                obsListTableInventory.add(row);
            }

            /*Checks if the records in the List of Observable List is existing in tablePurchase Order
             * if it exists that record will be removed and will no longer be shown in tableInventory
             * purpose: is to remove redundancy in ordering the same product in the same purchase order
             *  */
            for (List<Object> objects : selectedTableRow) {
                for (int j = 0; j < obsListTableInventory.size(); j++) {
                    if (obsListTableInventory.get(j).contains(objects.get(1))) {
                        obsListTableInventory.remove(j);
                    }
                }
            }

            //creates records in tableInventory from List of Observable List obsListTableInventory
            tableInventory.setItems(obsListTableInventory);

        } catch (SQLException | IOException | NullPointerException throwables) {
            throwables.printStackTrace();
        }
    }

    //Create initial values and enables button for creating new purchase order
    @FXML
    private void selectedBtnNew (ActionEvent event){
        subQuery = "SELECT * FROM purchaseorder ORDER BY purchase_order_id DESC";

        /*search for the last record of purchase order on file and use the last record's purchase order id no
        * as marker for the new record
        *and fill out other label to be used in saving the new purchase order record
        * */
        try {
            resultSet = connection.createStatement().executeQuery(subQuery);
            if(resultSet.next()){
                purOrderNo = Integer.parseInt(resultSet.getString("purchase_order_id"));
            }
            lblPurchaseOrderNo.setText(String.valueOf(++purOrderNo));
            lblPurchaseOrderStatus.setText("NEW");
            lblPurchaseOrderDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));



        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //-------relocated block of codes at iniatilize method------------------------------------
/*        condition to check if the New button has previously been selected
        * if previously selected, following block of codes below will not be performed
        * if not previously selected, column names for the tablePurchaseOrder will be created
        *
        tblPurchaseOrderColumnNames.clear();
        if(tablePurchaseOrder.isDisable()) {
            //displays the formatted table column names in the table view named tableData
            TablePurchaseOrderColumnNames();
            for (int i = 0; i < tblPurchaseOrderColumnNames.size(); i++) {
                TableColumn<List<Object>, Object> column1 = new TableColumn<>(tblPurchaseOrderColumnNames.get(i));
                int columnIndex = i;
                column1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<Object>, Object>, ObservableValue<Object>>() {
                    @Override
                    public ObservableValue<Object> call(TableColumn.CellDataFeatures<List<Object>, Object> cellData) {
                        return new SimpleObjectProperty<>(cellData.getValue().get(columnIndex));
                    }
                });
                column1.setPrefWidth(165);
                tablePurchaseOrder.getColumns().add(column1);
            }
        }
        //-------------------------------------------------------------------------------------------

       /* block of codes below will be executed if New button has previously been selected
        * this initializes default disabled property of text fields, table views, buttons and others
        * and also restore default values of txtSearch, lblSupplierIDNo, choiceBoxSearch and rowCounter
        * */

        rowCounter=0;
        totalAmount=0.00;
        tablePurchaseOrder.setDisable(false);
        btnSelect.setDisable(false);
        btnSave.setDisable(false);
        btnEdit.setDisable(false);
        btnRemove.setDisable(false);
        btnCancel.setDisable(false);
        choiceBoxSearch.setDisable(false);
        txtSearch.setEditable(false);
        btnSearchChoice.setDisable(false);
        tableInventory.setDisable(false);
        lblSupplierIDNo.setText("");
        lblTotalAmount.setText("PHP0.00");
        txtSearch.setText("");
        choiceBoxSearch.setValue("View All");
        tablePurchaseOrder.getItems().removeAll(selectedTableRow);
        tableInventory.getItems().removeAll(obsListTableInventory);
        choiceBoxSearch.requestFocus();

    }

    /*Save new record in Database table purchase order and Database table product_has_purchaseorder*/
    @FXML
    private void selectedBtnSave(ActionEvent event) throws SQLException, ParseException, IOException {
        if(selectedTableRow.size()!=0) {
            DialogBoxController.DialogBoxDisplay("Save Changes?", "Do you want to save changes?", "Save");
            try {
                if (!lblSupplierIDNo.getText().equals("") && DialogBoxController.getStringSelectedBtn().equals("Save")) {
                    subQuery = "INSERT INTO purchaseorder VALUES (?,?,?,?,?,?)";
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setString(1, lblPurchaseOrderNo.getText());
                    preparedStatement.setString(2, lblSupplierIDNo.getText());
                    preparedStatement.setString(3, "PENDING");
                    preparedStatement.setDate(4, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                    preparedStatement.setDouble(5, Double.parseDouble(lblTotalAmount.getText().substring(3)));
                    preparedStatement.setString(6,userName);
                    preparedStatement.executeUpdate();

                    /*get cell's data of every rows in tablePurchaseOrder and saved to the database's table product_has_purchaseorder*/
                    for (int i = 0; i < selectedTableRow.size(); i++) {
                        subQuery = "INSERT INTO product_has_purchaseorder VALUES(?,?,?,?,?)";
                        preparedStatement = connection.prepareStatement(subQuery);
                        preparedStatement.setString(1, lblPurchaseOrderNo.getText());
                        preparedStatement.setString(2, String.valueOf(selectedTableRow.get(i).get(0)));
                        preparedStatement.setString(3, String.valueOf(selectedTableRow.get(i).get(1)));
                        preparedStatement.setString(4, String.valueOf(selectedTableRow.get(i).get(5)));
                        preparedStatement.setDouble(5,Double.parseDouble(selectedTableRow.get(i).get(4).toString())*
                                Double.parseDouble(String.valueOf(selectedTableRow.get(i).get(5))));
                        preparedStatement.executeUpdate();
                    }
                    ConfirmationPageController.Display("Confirmation", "Purchase Order has been saved!");

                    /*restores default properties and values of the fxml page*/
                    totalAmount = 0.00;
                    lblPurchaseOrderNo.setText("");
                    lblSupplierIDNo.setText("");
                    lblPurchaseOrderDate.setText("");
                    lblPurchaseOrderStatus.setText("");
                    lblTotalAmount.setText("PHP0.00");

                    btnSearchChoice.setDisable(true);
                    btnSelect.setDisable(true);
                    btnSave.setDisable(true);
                    btnCancel.setDisable(true);

                    choiceBoxSearch.setDisable(true);

                    txtSearch.setEditable(false);

                    tablePurchaseOrder.setDisable(true);
                    tableInventory.setDisable(true);

                    /*removes or destroys the list of observable list used in creating tables for tableInventory and tablePurchase order
                     * it refreshes the page to create a new record*/
                    tableInventory.getItems().removeAll(obsListTableInventory);
                    tablePurchaseOrder.getItems().removeAll(selectedTableRow);
                    btnNew.requestFocus();

                }
            } catch (NullPointerException nullEx) {
            //Do nothing when null pointer exception is catch
            }
        }else  {
            ConfirmationPageController.Display("Warning!", "Error! No records to save!");
        }
    }

    /*Button event handler that edits the ordered quantity of the selected row in tablePurchaseOrder
    * selected row is being searched in List of Observable list named selectedTableRow that created the records for tablePurchaseOrder
    * if searched, a prompt message asking to edit the quantity will be invoked, once the value has been edited
    * the ordered quantity of the found observable list will be removed from the list and a new ordered quantity will be added on the same index
    * that the user has input in the prompt message and the tablePurchaseOrder will be refreshed to show new ordered
    * quantity for the selected row*/
    @FXML
    private void selectedBtnEdit(ActionEvent event) throws IOException {
        try {
            ObservableList<Object> row = FXCollections.observableArrayList((ObservableList<Object>) selectedPurchaseRow);
            for (int i = 0; i < selectedTableRow.size(); i++) {
                if (selectedTableRow.get(i).get(1).equals(row.get(1))) {
                    QuantityController.Display("Edit", "Edit Order Quantity", String.valueOf(row.get(5)));
                    try {
                        if (QuantityController.getStringSelectedBtn().equals("Ok") && Integer.parseInt(QuantityController.getTxtQuantity()) > 0 ){
                          //  selectedTableRow.get(i).contains(row.get(1));
                            /*subtract the amount of the selected row that will be edited from totalAmount*/
                            totalAmount -= Double.parseDouble(String.valueOf(selectedTableRow.get(i).get(5)))*Double.parseDouble(String.valueOf(selectedTableRow.get(i).get(4)));
                            selectedTableRow.get(i).remove(5);
                            selectedTableRow.get(i).add(5, QuantityController.getTxtQuantity());
                            /*adds the amount of the new quantity to totalAmount and refreshes the value of lblTotalAmount */
                            totalAmount += Double.parseDouble(String.valueOf(selectedTableRow.get(i).get(5)))*Double.parseDouble(String.valueOf(selectedTableRow.get(i).get(4)));
                            lblTotalAmount.setText(String.format("PHP%.2f",totalAmount));
                        } else if (QuantityController.getStringSelectedBtn().equals("Ok") && Integer.parseInt(QuantityController.getTxtQuantity()) <= 0) {
                            ConfirmationPageController.Display("Warning", "Invalid! Input should be greater than 0!");
                            tablePurchaseOrder.requestFocus();
                        } else if (QuantityController.getStringSelectedBtn().equals("Cancel")) {
                            tablePurchaseOrder.requestFocus();
                        }

                    } catch (NullPointerException nullEx) {
                        tablePurchaseOrder.requestFocus();
                    } catch (NumberFormatException numEx) {
                        ConfirmationPageController.Display("Warning", "Invalid! Please enter numeric values only.");
                        tablePurchaseOrder.requestFocus();
                    }
                }
            }

            tablePurchaseOrder.refresh();

        }catch (NullPointerException nullEx){
            ConfirmationPageController.Display("Warning", "No selected row to edit!");
        }
    }

    /*Button event handler that removes the selected row in tablePurchaseOrder
    * selected row is searched in List of Observable list named selectedTableRow
    * and once found the list of the selected row will be removed.
    * Line item will be updated after the row has been removed and TableInventoryDisplay()
    * will be invoked to update the tableInventory and will show list of products that has not been
    * yet selected in the tablePurchaseOrder
    * */
    @FXML
    private void selectedBtnRemove (ActionEvent event) throws IOException {
        try{
            ObservableList<Object> row = FXCollections.observableArrayList((ObservableList<Object>) selectedPurchaseRow);
          for (int j=0;j<selectedTableRow.size();j++)
            if (selectedTableRow.get(j).contains(row.get(1))) {
                totalAmount -= Double.parseDouble(String.valueOf(selectedTableRow.get(j).get(5)))*Double.parseDouble(String.valueOf(selectedTableRow.get(j).get(4)));
                selectedTableRow.remove(row);
                lblTotalAmount.setText(String.format("PHP%.2f",totalAmount));
                    for(int i=0;i<selectedTableRow.size();i++){
                        selectedTableRow.get(i).remove(0);
                        selectedTableRow.get(i).add(0,i+1);
                    }
                rowCounter=selectedTableRow.size();
                TableInventoryDisplay();
            }
            /*checks if selectedTableRow is empty, if empty lblSupplierIDNo should be blank
            * so that user can enter new record with different supplied id*/
            if(selectedTableRow.size()==0)
                lblSupplierIDNo.setText("");
            tablePurchaseOrder.refresh();
        }
        catch (NullPointerException nullEx){
            ConfirmationPageController.Display("Warning", "No selected row to delete!");
        }
    }

    /*Button event handler that discards all the input on the form and starts again a new form
    * */
    @FXML
    private void selectedBtnCancel (ActionEvent event) throws IOException {
            DialogBoxController.DialogBoxDisplay("Discard Changes?", "Are you sure? Records will not be saved!","Discard");
        try {
            if(DialogBoxController.getStringSelectedBtn().equals("Discard")) {
                totalAmount=0.00;
                lblPurchaseOrderNo.setText("");
                lblSupplierIDNo.setText("");
                lblPurchaseOrderDate.setText("");
                lblPurchaseOrderStatus.setText("");
                lblTotalAmount.setText("PHP0.00");

                btnSearchChoice.setDisable(true);
                btnSelect.setDisable(true);
                btnSave.setDisable(true);
                btnCancel.setDisable(true);

                choiceBoxSearch.setDisable(true);

                txtSearch.setText("");
                txtSearch.setEditable(false);

                tablePurchaseOrder.setDisable(true);
                tableInventory.setDisable(true);

                tableInventory.getItems().removeAll(obsListTableInventory);
                tablePurchaseOrder.getItems().removeAll(selectedTableRow);
                btnNew.requestFocus();
                ConfirmationPageController.Display("Confirmation","Changes Discarded!");
            }
        }catch (NullPointerException nullEx){

        }
    }

    /*Button event handler that searches for a purchase order and display it to the tablePurchaseOrder
    * Disable buttons and table to avoid data manipulation once user selected a purchase order form the search window
    * invokes SearchController for search window and pass a parameter to InnerJoinQuery method
    * once user selected a record from the Search window and that window will close but saving the selected record from that window
    * that saved selected record can be accessed to display records in tablePurchase Order*/
    @FXML
    private void selectedBtnSearch (ActionEvent event) throws IOException {

        /*SearchController.Query("SELECT purchaseorder.purchase_order_id, supplier.supplier_id_no, " +
                "supplier_name,purchase_order_status,purchase_date, sum(purchase_order_quantity*product_buying_price)" +
                " AS Total_Amount FROM purchaseorder, supplier,product,product_has_purchaseorder  " +
                "WHERE supplier.supplier_id_no = purchaseorder.supplier_id_no AND " +
                "purchaseorder.purchase_order_id=product_has_purchaseorder.purchase_order_id AND " +
                "product.product_id=product_has_purchaseorder.product_id GROUP BY purchase_order_id ORDER BY purchase_order_id");*/
        SearchController.InnerJoinQuery("SELECT purchase_order_id, supplier.supplier_id_no, supplier_name, purchase_order_status, "+
                "purchase_date, purchase_total_amount FROM purchaseorder INNER JOIN supplier " +
                "USING (supplier_id_no) INNER JOIN product_has_purchaseorder USING (purchase_order_id) INNER JOIN product "+
                "USING (product_id) ", "GROUP BY purchase_order_id ORDER BY purchase_order_id","purchaseorder");

        SearchController.setColumnName(getColumnames());
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Search.fxml"));
        Stage searchPurchasePage = new Stage();
        searchPurchasePage.initModality(Modality.APPLICATION_MODAL);
        Scene searchPurchaseScene = new Scene(root);
        searchPurchasePage.setTitle("Search Purchase Order");
        searchPurchasePage.setScene(searchPurchaseScene);
        searchPurchasePage.setResizable(false);
        searchPurchasePage.showAndWait();

        List <?> resultList = SearchController.getResultList();

        if(resultList.size()!=0&&SearchController.getSelectedBtn()!=null) {
            txtSearch.setEditable(false);
            choiceBoxSearch.setDisable(true);

            btnSearchChoice.setDisable(true);
            btnSelect.setDisable(true);
            btnSave.setDisable(true);
            btnEdit.setDisable(true);
            btnRemove.setDisable(true);
            btnCancel.setDisable(true);

            tableInventory.getItems().removeAll(obsListTableInventory);
            tableInventory.setDisable(true);

            tablePurchaseOrder.getItems().removeAll(selectedTableRow);
            String query = "SELECT * FROM purchaseorder " +
                    "WHERE purchase_order_id = '" + resultList.get(0) + "'";
            try {
                resultSet = connection.createStatement().executeQuery(query);
                if (resultSet.next()) {
                    lblPurchaseOrderNo.setText(resultSet.getString("purchase_order_id"));
                    lblSupplierIDNo.setText(resultSet.getString("supplier_id_no"));
                    lblPurchaseOrderStatus.setText(resultSet.getString("purchase_order_status"));
                    lblPurchaseOrderDate.setText(resultSet.getString("purchase_date"));
                    lblTotalAmount.setText(String.format("PHP%.2f",resultSet.getDouble("purchase_total_amount")));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            query = "SELECT purchase_order_id, purchase_order_line_item, product.product_id,product_name, product_description, purchase_amount/purchase_order_quantity "+
                    ",purchase_order_quantity FROM purchaseorder INNER JOIN product_has_purchaseorder USING (purchase_order_id) INNER JOIN " +
                    "product USING (product_id) WHERE purchase_order_id = '" + resultList.get(0) + "'" +
                    "GROUP BY product_id ORDER BY purchase_order_line_item";

            try {
                resultSet = connection.createStatement().executeQuery(query);
                while (resultSet.next()) {
                    ObservableList<Object> row = FXCollections.observableArrayList();
                    for (int i = 2; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        row.add(resultSet.getObject(i));
                    }
                    selectedTableRow.add(row);
                }
                tablePurchaseOrder.setDisable(false);
                tablePurchaseOrder.setItems(selectedTableRow);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    /*Adds the selected rows from the tableInventory to tablePurchaseOrder
    * it removes unnecessary column from tableInventory and store the needed columns in tablePurchaseOrder
    * it also removes the selected row from the Table View tableInventory
    * */
    @FXML
    private void selectedBtnSelect (ActionEvent event) throws IOException {
        try {
            ObservableList<Object> row = FXCollections.observableArrayList((ObservableList<Object>) selectedInventoryRow);
            /*invokes the static method Display from QuantityController class to prompt for quantity the user wants
             * to order for the selected product  and adds it to the Observable List row*/
            QuantityController.Display("Input", "Enter Order Quantity", "");
            /*---------------------------------------------*/
            try {
                if (QuantityController.getStringSelectedBtn().equals("Ok") && Integer.parseInt(QuantityController.getTxtQuantity()) > 0
                        && (lblSupplierIDNo.getText().equals("") || (String.valueOf(row.get(0))).equals(lblSupplierIDNo.getText()))) {
                    lblSupplierIDNo.setText(String.valueOf(row.remove(0)));
                    row.remove(0); //removes the Category Id column
                    row.remove(row.size() - 1); //removes the Item_on_hand column
                    row.remove(row.size() - 1); //removes the critical level column
                    row.add(0, ++rowCounter); //adds a column for line item and uses the rowCounter for line item column
                    row.add(QuantityController.getTxtQuantity()); //adds a column for user defined order quantity
                    totalAmount += Double.parseDouble(String.valueOf(row.get(row.size()-1)))*Double.parseDouble(String.valueOf(row.get(row.size()-2)));
                    lblTotalAmount.setText(String.format("PHP%.2f",totalAmount));



                    //adds the Observable List row to the List of Observable list selectedTableRow
                    selectedTableRow.add(row);

                    //creates rows of records in tablePurchaseOrder from List of Observable List selectedTableRow
                    tablePurchaseOrder.setItems(selectedTableRow);

                    //removes the selected row from the tableInventory
                    tableInventory.getItems().remove(selectedInventoryRow);


                } else if (QuantityController.getStringSelectedBtn().equals("Ok") && !lblSupplierIDNo.getText().equals("")
                        && !(String.valueOf(row.get(0))).equals(lblSupplierIDNo.getText())) {
                    /*a prompt message that limits the user in adding records to tablePurchaseOrder that has the same supplier id from the previous records
                     * a purchase order should be created with a product that has the same supplier(1 PO 1 Supplier)
                     * */
                    ConfirmationPageController.Display("Warning", "Supplier ID No does not match with previous records. Create a new Purchase Order");
                    tableInventory.requestFocus();

                } else if (QuantityController.getStringSelectedBtn().equals("Ok") && Integer.parseInt(QuantityController.getTxtQuantity()) <= 0) {
                    ConfirmationPageController.Display("Warning", "Invalid! Input should be greater than 0!");
                    tableInventory.requestFocus();
                } else if (QuantityController.getStringSelectedBtn().equals("Cancel")) {
                    tableInventory.requestFocus();
                }
            } catch (NullPointerException nullEx) {
                tableInventory.requestFocus();
            } catch (NumberFormatException numberEx) {
                System.out.println(QuantityController.getTxtQuantity()+"sadasdas");
                if(QuantityController.getStringSelectedBtn().equals("Ok"))
                    ConfirmationPageController.Display("Warning", "Invalid! Please enter numeric values only.");
                tableInventory.requestFocus();
            }
        }catch (NullPointerException nullEx) {
            ConfirmationPageController.Display("Warning", "No selected row to add!");
        }

    }

    //columnames for TableInventory
    private List<String> TableInventoryColumnNames(){
        List<String> tableInventoryColumnNames = new ArrayList<>();
        tableInventoryColumnNames.add("Supplier ID");
        tableInventoryColumnNames.add("Category No");
        tableInventoryColumnNames.add("Product ID No");
        tableInventoryColumnNames.add("Product Name");
        tableInventoryColumnNames.add("Description");
        tableInventoryColumnNames.add("Buying Price");
        tableInventoryColumnNames.add("Critical Level");
        tableInventoryColumnNames.add("Item on Hand");
        return tableInventoryColumnNames;
    }

    //columnnames for TablePurchaseOrder
    private List<String> TablePurchaseOrderColumnNames(){
        List<String> tablePurchaseColumnNames = new ArrayList<>();
        tablePurchaseColumnNames.add("Line Item No");
        tablePurchaseColumnNames.add("Product ID No");
        tablePurchaseColumnNames.add("Product Name");
        tablePurchaseColumnNames.add("Description");
        tablePurchaseColumnNames.add("Buying Price");
        tablePurchaseColumnNames.add("Order Quantity");
        return tablePurchaseColumnNames;
    }

    private void EditableProperty(JFXTextField txtfield, boolean booleanEditProperty, double opacity){
        txtfield.setEditable(booleanEditProperty);
        txtfield.setOpacity(opacity);
    }

    private List<String> getColumnames (){
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Purchase Order No.");
        columnNames.add("Supplier ID");
        columnNames.add("Supplier Name");
        columnNames.add("Status");
        columnNames.add("Purchase Order Date");
        columnNames.add("Amount");
        return columnNames;
    }

}
