package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import static SAIS.Controller.LogInController.userName;

public class ReceiveController implements Initializable {
    private Connection connection = DBInitializer.initializeConnection();
    private static final Integer RCV_ORDER_NO = 100000;
    private Double totalAmount = 0.00;
    private ResultSet resultSet;
    private String initQuery,subQuery;
    private ObservableList<List <Object>> obsListTableReceiveOrder = FXCollections.observableArrayList();
    private Object selectedRow;

    @FXML
    private JFXTextField txtPurchaseOrderNo;

    @FXML
    private Button btnNew,btnSelect;

    @FXML
    private TableView<List<Object>> tableReceiveOrder;

    @FXML
    private Label lblReceiveIDNo,lblSupplierIDNo, lblPurchaseOrderStatus, lblPurchaseOrderDate, lblTotalAmount;

    /*Implementation method initialize of Initializable interface
    *  creates a tablecolumn and callback of table tableReceiveOrder
    * and also provides a listener for selected row in tableReceiveOrder and assign it to selectedRow
    * to be used if user wants to edit the actual quantity received for the purchase order delivery*/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int i=0;i<TABLE_RECEIVE_ORDER().size();i++){
            TableColumn<List<Object>,Object> column = new TableColumn<>(TABLE_RECEIVE_ORDER().get(i));
            int columnIndex=i;
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<Object>, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(TableColumn.CellDataFeatures<List<Object>, Object> cell) {
                    return new SimpleObjectProperty<>(cell.getValue().get(columnIndex));
                }
            });
            column.setPrefWidth(tableReceiveOrder.getPrefWidth()/TABLE_RECEIVE_ORDER().size());
            column.setResizable(false);
            column.setReorderable(false);
            tableReceiveOrder.getColumns().add(column);
        }

        tableReceiveOrder.getSelectionModel().selectedItemProperty().addListener((obsValue, oldValue, newValue) -> {
            selectedRow = newValue;
        });
    }

    /*Action event handler of BtnNew Button
    * initializes initial values of text fields and labels and enables buttons and table view
    * that will be used in receiving new purchase order delivery
    * */
    @FXML
    private void selectedBtnNew (ActionEvent event) throws SQLException {
        totalAmount=0.00;
        txtPurchaseOrderNo.setText("");
        lblPurchaseOrderStatus.setText("");
        lblSupplierIDNo.setText("");
        lblPurchaseOrderDate.setText("");
        lblTotalAmount.setText("PHP0.00");
        tableReceiveOrder.getItems().removeAll(obsListTableReceiveOrder);
        txtPurchaseOrderNo.setEditable(true);
        txtPurchaseOrderNo.setOpacity(1);
        btnSelect.setDisable(false);
        tableReceiveOrder.setDisable(false);
        txtPurchaseOrderNo.requestFocus();

        /*returns the last record on receiveorder table to be used as an indicator
        * for the new receive_id_no to be created*/
        initQuery = "SELECT * FROM receiveorder ORDER BY receive_id_no DESC LIMIT 1";
        resultSet = connection.createStatement().executeQuery(initQuery);
        if (resultSet.next())
            lblReceiveIDNo.setText(String.valueOf(resultSet.getInt("receive_id_no")+1));
        else
            lblReceiveIDNo.setText(String.valueOf(RCV_ORDER_NO+1));

    }

    /*Event handler for the button BtnEdit
    * edits the selected row in tableReceiveOrder and prompts a window asking to edit the quantity received
    * input quantity should not be more than the ordered quantity of the purchase order so as to avoid
    * over stocking and input quantity should not also be less than 0, 0 is entered if ordered is not fulfilled
    * at delivery
    * also creates a try catch block after the prompt edit window if no exception
    * it continues with editing the quantity but catches NullPointerEx to do nothing and
    * catches NumberFormatException for non numeric input and prompt a message of invalid input*/
    @FXML
    private void selectedBtnEdit(ActionEvent event) throws IOException {
        try {
            ObservableList<Object> row = FXCollections.observableArrayList((ObservableList<Object>)selectedRow);
            for (int i = 0; i < obsListTableReceiveOrder.size(); i++) {
                if (obsListTableReceiveOrder.get(i).get(1).equals(row.get(1))) {
                    QuantityController.Display("Edit", "Edit Receive Quantity", String.valueOf(row.get(6)));
                    try {
                        if (QuantityController.getStringSelectedBtn().equals("Ok") && Integer.parseInt(QuantityController.getTxtQuantity()) >= 0
                                && Integer.parseInt(String.valueOf(obsListTableReceiveOrder.get(i).get(5))) >=
                                Integer.parseInt(QuantityController.getTxtQuantity())) {
                            //  selectedTableRow.get(i).contains(row.get(1));
                            /*subtract the amount of the selected row that will be edited from totalAmount*/
                            totalAmount -= Double.parseDouble(String.valueOf(obsListTableReceiveOrder.get(i).get(6))) *
                                    Double.parseDouble(String.valueOf(obsListTableReceiveOrder.get(i).get(4)));
                            obsListTableReceiveOrder.get(i).remove(6);
                            obsListTableReceiveOrder.get(i).add(6, QuantityController.getTxtQuantity());
                            /*adds the amount of the new quantity to totalAmount and refreshes the value of lblTotalAmount */
                            totalAmount += Double.parseDouble(String.valueOf(obsListTableReceiveOrder.get(i).get(6))) *
                                    Double.parseDouble(String.valueOf(obsListTableReceiveOrder.get(i).get(4)));
                            lblTotalAmount.setText(String.format("PHP%.2f", totalAmount));

                        } else if (QuantityController.getStringSelectedBtn().equals("Ok") && Integer.parseInt(QuantityController.getTxtQuantity()) <= 0) {
                            ConfirmationPageController.Display("Warning", "Invalid! Input should be greater than 0!");
                            tableReceiveOrder.requestFocus();
                        } else if (QuantityController.getStringSelectedBtn().equals("Cancel")) {
                            tableReceiveOrder.requestFocus();
                        } else if (Integer.parseInt(String.valueOf(obsListTableReceiveOrder.get(i).get(5))) <
                                Integer.parseInt(QuantityController.getTxtQuantity())) {
                            ConfirmationPageController.Display("Warning", "Invalid! Input should be less than or equal to Order Quantity!");
                        }

                    } catch (NullPointerException nullEx) {
                        tableReceiveOrder.requestFocus();
                    } catch (NumberFormatException numberEx) {
                        ConfirmationPageController.Display("Warning", "Invalid! Please enter numeric values only.");
                        tableReceiveOrder.requestFocus();
                    } finally {

                    }
                }
            }
            tableReceiveOrder.refresh();
        } catch (NullPointerException nullEx){
            ConfirmationPageController.Display("Warning", "No selected row to edit!");
        }
    }

    /*Action event handler for Button Receive*/
    /*checks if there is data on the tableReceive order
    * if no data prompt an error message
    * if there is data a prompt message asking to confirm saving
    * if saving confirmed data are saved in receiveorder table,
    * and creates a for loop to save data from tableReceiveOrder to
    * product_has_receiveorder table, product_history table and updates
    * the product_item_on_hand on product table and updates the purchase_order_status
    * from PENDING to RECEIVED in purchaseorder table*/
    @FXML
    private void selectedBtnReceive (ActionEvent event) throws SQLException, IOException {
        Integer itemOnHand=0;
        int prodHistIDNo=0;
        PreparedStatement preparedStatement;
        if(obsListTableReceiveOrder.size()!=0) {
            DialogBoxController.DialogBoxDisplay("Receive Purchase Order?", "Do you want to receive Purchase Order?","Proceed");
            try {
                /*save new record to receiveorder table*/
                if (DialogBoxController.getStringSelectedBtn().equals("Proceed")){
                subQuery = "INSERT INTO receiveorder VALUES (?,?,?,?,?)";
                preparedStatement = connection.prepareStatement(subQuery);
                preparedStatement.setString(1, lblReceiveIDNo.getText());
                preparedStatement.setString(2, txtPurchaseOrderNo.getText());
                preparedStatement.setDate(3, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                preparedStatement.setString(4, lblTotalAmount.getText().substring(3));
                preparedStatement.setString(5,userName);
                preparedStatement.executeUpdate();

                for (int i = 0; i < obsListTableReceiveOrder.size(); i++) {
                    /*save new record to product_has_receiveorder table*/
                    subQuery = "INSERT INTO product_has_receiveorder VALUES(?,?,?,?,?)";
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setString(1, lblReceiveIDNo.getText());
                    preparedStatement.setString(2, String.valueOf(obsListTableReceiveOrder.get(i).get(0)));
                    preparedStatement.setString(3, String.valueOf(obsListTableReceiveOrder.get(i).get(1)));
                    preparedStatement.setString(4, String.valueOf(obsListTableReceiveOrder.get(i).get(6)));
                    preparedStatement.setDouble(5, Double.parseDouble(obsListTableReceiveOrder.get(i).get(4).toString())*
                           Double.parseDouble(String.valueOf(obsListTableReceiveOrder.get(i).get(6))));
                    preparedStatement.executeUpdate();

                    /*gets the quantity of the product's product_item_on_hand and reference it to itemOnHand*/
                    resultSet = connection.createStatement().executeQuery("SELECT product_item_on_hand FROM product WHERE product_id = '" +
                            obsListTableReceiveOrder.get(i).get(1) + "'");
                    if (resultSet.next())
                        itemOnHand = Integer.valueOf(resultSet.getString(1));

                    /*gets the last record of product_history and get the product_history_id*/
                    resultSet = connection.createStatement().executeQuery("SELECT * FROM product_history ORDER BY product_history_id DESC LIMIT 1");
                    if (resultSet.next())
                        prodHistIDNo = Integer.parseInt(resultSet.getString(1))+1;
                    else
                        prodHistIDNo += 1;

                    /*save new record to product_history table*/
                    subQuery = "INSERT INTO product_history VALUES(?,?,?,?)";
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setInt(1, prodHistIDNo);
                    preparedStatement.setString(2, String.valueOf(obsListTableReceiveOrder.get(i).get(1)));
                    preparedStatement.setInt(3, itemOnHand + Integer.parseInt(String.valueOf(obsListTableReceiveOrder.get(i).get(6))));
                    preparedStatement.setDate(4, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                    preparedStatement.executeUpdate();

                    /*updates the product_item_on_hand on product table*/
                    subQuery = "UPDATE product SET product_item_on_hand=? WHERE product_id = '" + obsListTableReceiveOrder.get(i).get(1) + "'";
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setInt(1, itemOnHand + Integer.parseInt(String.valueOf(obsListTableReceiveOrder.get(i).get(6))));
                    preparedStatement.executeUpdate();

                    /*updates the purchase_order_status of purchaseorder from PENDING to RECEIVED*/
                    subQuery = "UPDATE purchaseorder SET purchase_order_status=? WHERE purchase_order_id = '"+txtPurchaseOrderNo.getText()+"'";
                    preparedStatement = connection.prepareStatement(subQuery);
                    preparedStatement.setString(1,"RECEIVED");
                    preparedStatement.executeUpdate();

                    }

                    /*clears text of the txtfields and labels
                    * set values to default
                    * disables buttons and table views and erases data
                    */
                    totalAmount=0.00;
                    txtPurchaseOrderNo.setText("");
                    lblReceiveIDNo.setText("");
                    lblPurchaseOrderStatus.setText("");
                    lblSupplierIDNo.setText("");
                    lblPurchaseOrderDate.setText("");
                    lblTotalAmount.setText("PHP0.00");

                    tableReceiveOrder.getItems().removeAll(obsListTableReceiveOrder);

                    txtPurchaseOrderNo.setEditable(false);
                    txtPurchaseOrderNo.setOpacity(.50);
                    btnSelect.setDisable(true);
                    tableReceiveOrder.setDisable(true);
                    btnNew.requestFocus();

                    ConfirmationPageController.Display("Confirmation","Purchase Order received!");

                }
            }catch (NullPointerException nullEx) {
                //Do nothing when null pointer exception is catch
            }
        }else
            ConfirmationPageController.Display("Warning!","Error! No Purchase Order to receive!");

    }

    /*Action event for button btnCancel
    * clears text of the txtfields and labels
    * set values to default
    * disables buttons and table views and erases data*/
    @FXML
    private void selectedBtnCancel(ActionEvent event){
        txtPurchaseOrderNo.setEditable(false);
        txtPurchaseOrderNo.setOpacity(0.50);
        txtPurchaseOrderNo.setText("");
        lblReceiveIDNo.setText("");
        lblSupplierIDNo.setText("");
        lblPurchaseOrderStatus.setText("");
        lblPurchaseOrderDate.setText("");
        lblTotalAmount.setText("PHP0.00");
        tableReceiveOrder.setDisable(true);
        tableReceiveOrder.getItems().removeAll(obsListTableReceiveOrder);
    }

    /*Action event handler for button BtnSelect
    * searches the entered purchase order id no on the textfield
    * if found it generates the details of the purchase order to the labels and
    * tableReceiveOrder
    * if purchase order status is PENDING it displays a message that Purchase Order and
    * if purchase order status is not found it display a warning the record not found
    *  */
    @FXML
    private void selectedBtnSelect (ActionEvent event) throws SQLException, IOException {
        subQuery = "SELECT * FROM purchaseorder WHERE purchase_order_id = '"+txtPurchaseOrderNo.getText()+"' AND purchase_order_status ='PENDING'" ;
        resultSet = connection.createStatement().executeQuery(subQuery);
        if (resultSet.next()){
            lblSupplierIDNo.setText(resultSet.getString("supplier_id_no"));
            lblPurchaseOrderStatus.setText(resultSet.getString("purchase_order_status"));
            lblPurchaseOrderDate.setText(resultSet.getString("purchase_date"));
            lblTotalAmount.setText(String.format("PHP%.2f",resultSet.getDouble("purchase_total_amount")));
            totalAmount = Double.parseDouble(lblTotalAmount.getText().substring(3));
            txtPurchaseOrderNo.setEditable(false);
            txtPurchaseOrderNo.setOpacity(.70);

            subQuery = "SELECT purchase_order_line_item, product_id, product_name, product_description, purchase_amount/purchase_order_quantity,"+
                    "purchase_order_quantity, purchase_order_quantity FROM product_has_purchaseorder INNER JOIN purchaseorder "+
                    "USING (purchase_order_id) INNER JOIN product USING (product_id) WHERE purchase_order_id = '"+txtPurchaseOrderNo.getText()+"'";

            resultSet=connection.createStatement().executeQuery(subQuery);
            while (resultSet.next()){
                ObservableList<Object> row = FXCollections.observableArrayList();
                for(int i=1;i<=resultSet.getMetaData().getColumnCount();i++){
                    row.add(resultSet.getObject(i));
                }
                obsListTableReceiveOrder.add(row);
            }
            tableReceiveOrder.setItems(obsListTableReceiveOrder);
        }
        else{
            subQuery = "SELECT * FROM receiveorder WHERE purchase_order_id = '"+txtPurchaseOrderNo.getText()+"'";
            resultSet = connection.createStatement().executeQuery(subQuery);
            if(resultSet.next())
                ConfirmationPageController.Display("Attention","Purchase Order already in RECEIVED status!");
            else
                ConfirmationPageController.Display("Warning", "Purchase Order does not exists!");
        }


    }

    /*creates a list of strings for the column names of the tableReceiveOrder*/
    private final List<String> TABLE_RECEIVE_ORDER (){
        List <String> tableReceiveOrder = new ArrayList<>();
        tableReceiveOrder.add("Line Item No");
        tableReceiveOrder.add("Product ID No");
        tableReceiveOrder.add("Product Name");
        tableReceiveOrder.add("Description");
        tableReceiveOrder.add("Buying Price");
        tableReceiveOrder.add("Order Quantity");
        tableReceiveOrder.add("Receive Quantity");
        return tableReceiveOrder;
    }

}
