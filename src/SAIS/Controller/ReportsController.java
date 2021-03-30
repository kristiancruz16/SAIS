package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ReportsController implements Initializable {
    @FXML
    private AnchorPane displayPane;

    @FXML
    private ChoiceBox choiceReport, choiceComparative = new ChoiceBox();

    @FXML
    private CheckBox chkBoxFilter = new CheckBox();

    @FXML
    private TextField txtFilter = new TextField();

    @FXML
    private DatePicker dateTo = new DatePicker();

    @FXML
    private DatePicker dateFrom = new DatePicker();

    @FXML
    private Button btnTableView;

    private String selectedReport = new String();
    private String selectedComparative = new String();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        /*adds items to Choice Box choiceReport for the type of reports that the user wants to generate*/
        choiceReport.getItems().add("Overall Sales Summary");
        choiceReport.getItems().add("Sales Summary by Category");
        choiceReport.getItems().add("Sales Summary by Product");
        choiceReport.getItems().add("Sales Summary by Customer");

        /*adds items to Choice Box choiceComparative for the comparative type for the selected report*/
        choiceComparative.getItems().add("Daily");
        choiceComparative.getItems().add("Monthly");
        choiceComparative.getItems().add("Yearly");

        /*stores the item selected to a string selectedReport whenever there are selection changes to choiceReport*/
        choiceReport.getSelectionModel().selectedItemProperty().addListener((obsValue, oldValue, newValue) -> selectedReport = newValue.toString());

        /*stores the item selected to a string selectedReport whenever there are selection changes to choiceComparative*/
        choiceComparative.getSelectionModel().selectedItemProperty().addListener((obsValue, oldValue, newValue) -> selectedComparative = newValue.toString());

        /*set editable property, opacity and text of txtFilter whenever there are changes in the selection of chkBoxFilter*/
        chkBoxFilter.selectedProperty().addListener((obsValue, oldValue, newValue) -> {
            /*if chkBoxFilter is marked checked, text field txtFilter enables the Editable Property and set the opacity to 1*/
            if (newValue) {
                txtFilter.setEditable(true);
                txtFilter.setOpacity(1.00);
            } else { // if chkBoxFilter is unmarked, it disables the Editable Property and opacity to .50 and clears the text
                txtFilter.setEditable(false);
                txtFilter.setOpacity(.50);
                txtFilter.setText("");
            }
        });

        /*a change listener to choiceReport
        * it changes the Disable Property of the chkBoxFilter, changes the Prompt Text of the txtFilter and clears it
        * its text depending on what the user selected in choiceReport*/
        choiceReport.getSelectionModel().selectedItemProperty().addListener((obsValue, oldValue, newValue) -> {
            switch (newValue.toString()) {
               /*if below items is selected in choiceReport it enables the chkBoxFilter and set the Prompt Text
               * according to what the user selected and clears the text if there are previous text to it*/
                case ("Sales Summary by Category") -> {
                    chkBoxFilter.setDisable(false);
                    txtFilter.setText("");
                    txtFilter.setPromptText("Filter by Category");
                }
                case ("Sales Summary by Product") -> {
                    chkBoxFilter.setDisable(false);
                    txtFilter.setText("");
                    txtFilter.setPromptText("Filter by Product");
                }
                case ("Sales Summary by Customer") -> {
                    chkBoxFilter.setDisable(false);
                    txtFilter.setText("");
                    txtFilter.setPromptText("Filter by Customer");
                }
                /*if none of the items above is selected it will proceed in executing the default which disables
                * chkBoxFilter and clears the text and Prompt Text of txtFilter*/
                default -> {
                    chkBoxFilter.setDisable(true);
                    chkBoxFilter.selectedProperty().setValue(false);
                    txtFilter.setText("");
                    txtFilter.setPromptText("");
                }
            }
        });
    }

    /*ActionEvent handler for button btnTableView
    * which creates a query depending on what user selected in the choiceReport, choiceComparative and date ranges entered in
    * dateFrom and dateTo DatePicker and if filter is enabled and what text is in txtFilter and displays the report in a Table View*/
    @FXML
    private void selectedBtnTableView(ActionEvent event) throws IOException {
        //an array list that will be used in generating column names for the table view  of the report
        List<String> setColumn = tableColumns();

        //creates a generic query that will be used by all type of reports that the user will select
        String query = "sum(sale_quantity) AS total_quantity, round(sum(sale_amount),2) as gross_sales, " +
                "round(sum((sale_amount)/1.12)*.12,2) AS tax, round(sum(sale_amount)/1.12,2)  AS  net_sales " +
                "FROM sale INNER JOIN product_has_sale USING (sale_id_no)";

        /*switch case condition for selected item in choiceReport*/
        switch (selectedReport) {
            /*case Overall Sales Summary is a generic report that displays only the total sales
            * it invokes the method ComparativeTypeMethod which pass the parameter of String query and blank for columnId and columnName
            * since no other information are required from the other table to genereate this report and default tableColumns referenced to setColumn
            **/
            case ("Overall Sales Summary") -> ComparativeTypeMethod(query, "", "", setColumn);
            /*case Sales Summary by Category  is a report that displays total sales per category
            * it adds the category id and category name to setColumn and appends the query to filter the database
            * invokes the ComparativeTypeMethod with parameters of the appended query , category id and category name that will be used if the
            * user selected to filter the search and setColumn*/
            case ("Sales Summary by Category") -> {
                setColumn.add(0, "Category Name");
                setColumn.add(0, "Category ID");
                query = "category_id, category_name, " + query + " INNER JOIN product USING (product_id) INNER JOIN category USING (category_id)";
                ComparativeTypeMethod(query, "category_id", "category_name", setColumn);
            }
            /*case Sales Summary by Product  is a report that displays total sales per product
             * it adds the product id and product name to setColumn and appends the query to filter the database
             * invokes the ComparativeTypeMethod with parameters of the appended query , product id and product name that will be used if the
             * user selected to filter the search and setColumn*/
            case ("Sales Summary by Product") -> {
                setColumn.add(0, "Description");
                setColumn.add(0, "Product Name");
                setColumn.add(0, "Product ID No");
                query = "product_has_sale.product_id, product_name, product_description, " + query + " INNER JOIN product USING (product_id)";
                ComparativeTypeMethod(query, "product_id", "product_name", setColumn);
            }
            /*case Sales Summary by Customer  is a report that displays total sales per customer
             * it adds the customer id and concatenated last name and first name as Name to setColumn and appends the query to filter the database
             * invokes the ComparativeTypeMethod with parameters of the appended query , customer id and customer lastname that will be used if the
             * user selected to filter the search and setColumn*/
            case ("Sales Summary by Customer") -> {
                setColumn.add(0, "Customer Name");
                setColumn.add(0, "Customer ID No");
                query = "sale.customer_id_no, concat(cust_lastname,', ',cust_firstname) as Name, " + query + " INNER JOIN customer USING (customer_id_no)";
                ComparativeTypeMethod(query, "customer_id_no", "cust_lastname", setColumn);
            }
            /*a prompt message will display if user didn't select any in ChoiceReport*/
            default -> ConfirmationPageController.Display("Warning", "Attention! Please select Summary Type.");
        }
    }

    /*this method is invoked from selectedBtnTableView if case condition from choiceReport is selected
    * this method checks if chkBoxFilter is marked and creates a String named filterSearch that uses columnId or columnName and
    * text entered in txtFilter. This will be appended in query to filter the records that will show in the database
    * this also creates a switch case condition that will append the query for specific selection of user from choiceComparative*/
    private void ComparativeTypeMethod(String query, String columnId, String columnName, List<String> setColumn) throws IOException {
        String filterSearch = new String();
        String finalQuery;

        /*if chkBoxFilter is marked, it creates a string using the columnId and columnName passed from btnTableView and user inputted text from
        * txtFilter and referenced it to filterSearch that will be appended to String finalQuery */
        if (chkBoxFilter.isSelected()) {
            filterSearch = " AND " + columnId + " = '" + txtFilter.getText() + "' OR " + columnName + " = '" + txtFilter.getText() + "'";
        }

        /*condition that checks if columnId is blank
        * if blank? it remains blank and it will be appended to the end of the finalQuery String
        * if not blank, it creates a string the will comply with the syntax of sql and append it to the end of finalQuery String*/
        if (!columnId.equals("")) {
            columnId = ", " + columnId + " , " + columnName;
        }

        /*a switch case condition that creates a string for a specific type of selection in choiceComparative and appends
        * condition using the dateFrom and dateTo from DatePicker to specify a date range for the sql query*/
        switch (selectedComparative) {
            case ("Daily") -> {
                setColumn.add(0, "Sale Date"); //adds Sale Date column for the table view that will display the report

                /*appends the query and reference it as finalQuery which will be used as SQL for generating records from the database*/
                finalQuery = "SELECT sale_date, " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                        "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY sale_date" + columnId;

                DatePickerValidator(finalQuery, setColumn); //invokes DatePickerValidator with finalQuery and setColumn as parameters
            }
            case ("Monthly") -> {
                setColumn.add(0, "Sale Date (Month) ");

                /*appends the query and reference it as finalQuery which will be used as SQL for generating records from the database
                * it groups the sale_date into month to show total sales monthly*/
                finalQuery = "SELECT date_format(sale_date, '%M %Y'), " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                        "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY month(sale_date)" + columnId;

                DatePickerValidator(finalQuery, setColumn);
            }
            case ("Yearly") -> {
                setColumn.add(0, "Sale Date (Year)");

                /*appends the query and reference it as finalQuery which will be used as SQL for generating records from the database
                 * it groups the sale_date into year to show total sales in a year*/
                finalQuery = "SELECT year(sale_date), " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                        "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY year(sale_date)" + columnId;
                DatePickerValidator(finalQuery, setColumn);
            }
            default -> ConfirmationPageController.Display("Warning", "Please select Comparative Type.");
        }
    }

    private void DatePickerValidator(String query, List<String> setColumn) throws IOException {
        AnchorPane paneLoader;

        /*condition that checks if values of DatePicker are not null
        * if true proceed
        * if false an error message will display*/
        if (dateFrom.getValue() != null && dateTo.getValue() != null) {
            /*condition that checks if dateFrom value is greater than dateTo
            * if true proceed and it will pass the query that will be as SQL query and setColumn for the table's column name
            * to the ReportDisplayController that will display the report
            * if false a error message will display*/
            if (dateFrom.getValue().compareTo(dateTo.getValue()) < 1) {
                ReportDisplayController.setQuery(query, setColumn);
                try {
                    displayPane.getChildren().clear();
                    paneLoader = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/ReportDisplay.fxml"));
                    displayPane.getChildren().add(paneLoader);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                ConfirmationPageController.Display("Warning", "Attention! You have entered an invalid date range.");
        } else
            ConfirmationPageController.Display("Warning", "Attention! Please enter date range.");

    }

    /*a List of Strings that will be used as Column Name for the table view that will display the report*/
    private List<String> tableColumns() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Total Quantity");
        columnNames.add("Gross Sales");
        columnNames.add("Tax");
        columnNames.add("Net Sales");
        return columnNames;
    }


    /*ActionEvent handler for button btnGraphView
    * creates a query depending on what the user selected in choiceReport, choiceComparative, Date Ranges in values entered in Date Pickers
    * and if chkBoxFilter is marked
    * Use the query generated to display report in Graph view*/
    @FXML
    private void selectedBtnGraphView(ActionEvent event) throws SQLException, ParseException, IOException {

        //create a generic string referenced to query that will be used to all types of SQL query
        String query = "round(sum(sale_amount),2) as total_amount FROM product_has_sale INNER JOIN sale USING (sale_id_no)";

        /*a switch case condition that will append the query depending on what user selected in choiceReport and
        * if case condition is met, query will be appended and invokes GraphComparativeType with parameters of
        * the appended query and a column id and column name depending on the selection*/
        switch (selectedReport) {
            case ("Overall Sales Summary") -> {
                GraphComparativeType(query, "", "");
            }
            case ("Sales Summary by Category") -> {
                query = "category_id, category_name, " + query + " INNER JOIN product USING (product_id) INNER JOIN category USING (category_id)";
                GraphComparativeType(query, "category_id", "category_name");
            }
            case ("Sales Summary by Product") -> {
                query = "product_has_sale.product_id, product_name, " + query + " INNER JOIN product USING (product_id)";
                GraphComparativeType(query, "product_id", "product_name");
            }
            case ("Sales Summary by Customer") -> {
                query = "customer_id_no, concat(cust_lastname,', ',cust_firstname), " + query + " INNER JOIN customer USING (customer_id_no)";
                GraphComparativeType(query, "customer_id_no", "cust_lastname");
            }

            //if case condition is not met, a dialog box will display
            default -> ConfirmationPageController.Display("Warning", "Attention! Please select Summary Type.");

        }
    }


    private void GraphComparativeType(String query, String columnId, String columnName) throws IOException, ParseException {
        /*an array list that creates a date and an initial value of 0 depending on the date range enter in DatePicker and
        * depending on the selection in choiceComparative
        * this will be used for the Y axis in the Graph View*/
        List<List<String>> dateRangeList = new ArrayList<>();
        long difference; //counts the difference of dateTo and dateFrom of Date Picker

        String filterSearch = new String();
        String finalQuery, seriesQuery;

        /*if chkBoxFilter is marked, it creates a string using the columnId and columnName passed from btnGraphView and user inputted text from
         * txtFilter and referenced it to filterSearch that will be appended to String finalQuery */
        if (chkBoxFilter.isSelected()) {
            filterSearch = " AND " + columnId + " = '" + txtFilter.getText() + "' OR " + columnName + " = '" + txtFilter.getText() + "'";
        }

        /*condition that checks if columnId is blank
         * if blank? it assign columnName to sale_date and it will be appended to the end of the finalQuery String
         * if not blank, it creates a string the will comply with the syntax of sql and append it to the end of finalQuery String*/
        if (!columnId.equals("")) {
            columnId = ", " + columnName;
        }else
            columnName = "sale_date";


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd"); // creates an object of SimpleDateFormat with a specified pattern
        /*condition that checks if DatePicker is null
        * if not null, proceed
        * if null, error message will display*/
        if (dateFrom.getValue() != null && dateTo.getValue() != null) {
            /*checks if dateFrom.value is less than dateTo.value to determine if the inputs are valid date range
            * if true proceed
            * if false, error message will be displayed*/
            if (dateFrom.getValue().compareTo(dateTo.getValue()) < 1) {
                String stringDate = String.valueOf(dateFrom.getValue()); //converts LocalDate from dateFrom.getValue to String

                /*a switch case condition of selected item in choiceComparative
                * Appending the specific sale_date or sale_date using the month() function or year() function of mysql to the finalQuery
                * depending to what the user has selected in choiceComparative
                * also appends the WHERE condition from date range inputted at dateFrom and dateTo
                * appends the string filterSearch if user marked the chkBoxFilter or use the blank filterSearch if user did not mark the chkBoxFilter
                * lastly appends to what type of GROUP BY depending on what the user selected in choiceReport
                * 2 type of  Query appending in this switch case statement
                * **finalQuery is the SQL that will be used to filter the records that will be used to display in the Graph
                * seriesQuery is the SQL to extract the data (ex. Product A, Product B, Product C) that will be used as a reference
                * for the object of XYChart.Series and sorts the records in a descending order of sum(sales_amount)
                * In the body of case statement: It computes for the difference of the date range dateFrom and dateTo
                * and creates  a list of arraylist named dateRangeList with values of the dates from the dateFrom and dateTo.
                * ex output: [[2021-03-01,0][2021-03-02,0]] it depends on what is selected on choiceComparative
                * last is to invoke the GraphDisplay with parameters of dateRangeList, finalQuery and seriesQuery
                * */
                switch (selectedComparative) {
                    case ("Daily") -> {
                        finalQuery = "SELECT sale_date, " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                                "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY sale_date" + columnId;
                        seriesQuery = "SELECT sale_date, " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                                "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY " + columnName + " ORDER BY total_amount DESC";
                        difference = ChronoUnit.DAYS.between(
                                dateFrom.getValue(),
                                dateTo.getValue());
                        Calendar calendar = Calendar.getInstance();
                        calendar.getTime();
                        for (int i = 0; i <= difference; i++) {
                            List<String> datesArray = new ArrayList<>();
                            try {
                                calendar.setTime(format.parse(stringDate));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            datesArray.add(new SimpleDateFormat("yyyy-MM-dd").format(format.parse(stringDate)));
                            datesArray.add(String.valueOf(0));
                            dateRangeList.add(datesArray);
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            stringDate = format.format(calendar.getTime());
                        }

                        GraphDisplay(dateRangeList, finalQuery, seriesQuery);

                    }
                    case ("Monthly") -> {
                        finalQuery = "SELECT date_format(sale_date, '%M %Y'), " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                                "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY month(sale_date)" + columnId;
                        seriesQuery = "SELECT date_format(sale_date, '%M %Y'), " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                                "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY " + columnName + " ORDER BY total_amount DESC";
                        difference = ChronoUnit.MONTHS.between(
                                YearMonth.from(dateFrom.getValue()),
                                YearMonth.from(dateTo.getValue()));
                        Calendar calendar = Calendar.getInstance();
                        calendar.getTime();
                        for (int i = 0; i <= difference; i++) {
                            List<String> datesArray = new ArrayList<>();
                            try {
                                calendar.setTime(format.parse(stringDate));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            datesArray.add(new SimpleDateFormat("MMMM YYYY").format(format.parse(stringDate)));
                            datesArray.add(String.valueOf(0));
                            dateRangeList.add(datesArray);
                            calendar.add(Calendar.MONTH, 1);
                            stringDate = format.format(calendar.getTime());
                        }
                        GraphDisplay(dateRangeList, finalQuery, seriesQuery);
                    }
                    case ("Yearly") -> {
                        finalQuery = "SELECT year(sale_date), " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                                "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY year(sale_date)" + columnId;
                      ;
                        seriesQuery = "SELECT year(sale_date), " + query + " WHERE (sale_date >= '" + dateFrom.getValue() + "' AND " +
                                "sale_date <= '" + dateTo.getValue() + "')" + filterSearch + " GROUP BY " + columnName + " ORDER BY total_amount DESC";
                        difference = ChronoUnit.YEARS.between(
                                dateFrom.getValue(),
                                dateTo.getValue());
                        Calendar calendar = Calendar.getInstance();
                        calendar.getTime();
                        for (int i = 0; i <= difference; i++) {
                            List<String> datesArray = new ArrayList<>();
                            try {
                                calendar.setTime(format.parse(stringDate));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            datesArray.add(new SimpleDateFormat("YYYY").format(format.parse(stringDate)));
                            datesArray.add(String.valueOf(0));
                            dateRangeList.add(datesArray);
                            calendar.add(Calendar.YEAR, 1);
                            stringDate = format.format(calendar.getTime());
                        }
                        GraphDisplay(dateRangeList, finalQuery, seriesQuery);
                    }
                    default -> ConfirmationPageController.Display("Warning", "Please select Comparative Type.");
                }

            } else
                ConfirmationPageController.Display("Warning", "Attention! You have entered an invalid date range.");
        } else
            ConfirmationPageController.Display("Warning", "Attention! Please enter date range.");
    }

    /*this method is invoked from GraphComparativeType that searches the database using the SQL from String finalQuery and
    * seriesQuery  and displays the Graph*/
    private void GraphDisplay(List<List<String>> dateRangeList, String query, String seriesQuery) {
        //an arraylist that holds the name of the title for the object of XYChart.Series
        List<String> seriesName = new ArrayList<>();
        Connection connection = DBInitializer.initializeConnection();

        //define the X and Y axis
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        //create the chart
        final LineChart<String, Number> lineChart =
                new LineChart<String, Number>(xAxis, yAxis);
        lineChart.setTitle(choiceReport.getValue().toString());

        //creates an object of XYChart.Series, an arraylist is used just in case an object of series is need to be created
        //more than once
        List<XYChart.Series> series = new ArrayList<>();

        try {

            /*filter the database with the 3 most number of sales_amount*/
            ResultSet resultSet = connection.createStatement().executeQuery(seriesQuery);

            /*condition that checks if user selected Sales Summary by Category, by Product or by Customer
            * by using the Column Count after the query, these three category has more than 2 column and can
            * be identified if these three have been chosen*/
            if (resultSet.getMetaData().getColumnCount() > 2) {
                int i = 0;
                /*stores the column name with most number of sales_amount to seriesName*/
                while (resultSet.next()) {
                    if (i < 3)
                        seriesName.add(resultSet.getString(3));
                    i++;
                }
                /*creates a list of object of XYChart.Series and set its name from the list seriesName*/
                for (int j = 0; j < seriesName.size(); j++) {
                    series.add(new XYChart.Series());
                    series.get(j).setName(seriesName.get(j));
                }

                /*creates a loop with number of times depending on the size of seriesName*/
                for (i = 0; i < seriesName.size(); i++) {

                    /*filter the database given the SQL query from GraphComparativeType*/

                    resultSet = connection.createStatement().executeQuery(query);
                    while (resultSet.next()) {
                        /*proceed if the value of the column 3 index is equals to the series name*/
                        if (seriesName.get(i).equals(resultSet.getString(3))) {
                            String dateHolder = resultSet.getString(1); // holds the date of the record from the database
                            /*a for loop that search if the value of the dateRangeList is equals to dateHolder
                            * if true it replaces the value 0 of the dateRangeList with the total_amount of resultSet.getString(4)*/
                            for (int j = 0; j < dateRangeList.size(); j++) {
                                if (dateRangeList.get(j).contains(dateHolder)) {
                                    dateRangeList.get(j).remove(1); //removes the value 0
                                    dateRangeList.get(j).add(1, resultSet.getString(4)); //replace the value 0 that was removed
                                }
                            }
                        }

                    }

                    /*a loop that creates the x and y axis of this particular seriesName*/
                    for (int k = 0; k < dateRangeList.size(); k++) {
                        Double value = Double.parseDouble(dateRangeList.get(k).get(1));
                        //populating the series of the particular seriesName of the loop with data from dateRangeList
                        series.get(i).getData().add(new XYChart.Data(dateRangeList.get(k).get(0), value));
                    }

                    /*a for loop restores the original value of index 1 which is 0 of dateRangeList
                    * purpose of doing this is because not to retain the old values when the new value for that index is 0
                    problem encountered is if the previous series has a value on that date and current series has 0 value
                    * the previous value will still be in the dateRangeList so below for loop solve that problem*/
                    for (int k=0; k<dateRangeList.size();k++) {
                        dateRangeList.get(k).remove(1);
                        dateRangeList.get(k).add("0");
                    }
                }
            } else {

                //creates an object of XYChart.Series and add it to the arraylist named series
                series.add(new XYChart.Series());
                series.get(0).setName("Sales Total"); //setName for the objected created of XYChart.Series

                List <List<String>> copyDateRange = new ArrayList<>(dateRangeList);

                /*filter the database given the SQL query from GraphComparativeType*/
                resultSet = connection.createStatement().executeQuery(query);
                while (resultSet.next()) {
                    String dateHolder = resultSet.getString(1); // holds the date of the record from the database
                    /*a for loop that search if the value of the dateRangeList is equals to dateHolder
                     * if true it replaces the value 0 of the dateRangeList with the total_amount of resultSet.getString(2)*/
                    for (int j = 0; j < copyDateRange.size(); j++) {
                        if (copyDateRange.get(j).contains(dateHolder)) {
                            copyDateRange.get(j).remove(1);
                            copyDateRange.get(j).add(1, resultSet.getString(2));
                        }
                    }
                }
                for (int k = 0; k < copyDateRange.size(); k++) {
                    Double value = Double.parseDouble(copyDateRange.get(k).get(1));
                    //populating the series of the particular seriesName of the loop with data from dateRangeList
                    series.get(0).getData().add(new XYChart.Data(copyDateRange.get(k).get(0), value));
                }

            }

            // clears the AnchorPane if there is existing node
            displayPane.getChildren().clear();
            //add the object of XYChart.Series to LineChart
            for (int i = 0; i < series.size(); i++)
                lineChart.getData().add(series.get(i));
            lineChart.setPrefWidth(1000);
            lineChart.setLayoutX((displayPane.getPrefWidth() / 2) - (lineChart.getPrefWidth() / 2));
            lineChart.setLayoutY(35);
            // adds the LineChart to the AnchorPane displayPane
            displayPane.getChildren().addAll(lineChart);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}




