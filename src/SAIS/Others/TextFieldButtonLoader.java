package SAIS.Others;

import SAIS.Controller.ConfirmationPageController;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TextFieldButtonLoader {
    private  static  List<JFXTextField> jfxTextFieldList;
    private  static  List<Button> buttonList;
    private  static int IDNo;
    private  static final Connection connection = DBInitializer.initializeConnection();

    public TextFieldButtonLoader(List<JFXTextField> jfxTextFieldList, List<Button> buttonList,int IDNo ){
        this.jfxTextFieldList = jfxTextFieldList;
        this.buttonList = buttonList;
        this.IDNo = IDNo;
    }


    /*method that enable and disable editable property of the text fields and sets the opacity of text fields*/
    public void TextFieldEditableProperty(boolean booleanEditProperty,double opacity){
        //condition if changing text field editable property is still required
        if(jfxTextFieldList.get(1).editableProperty().getValue()==!booleanEditProperty) {
            //enable and disable editable property of text fields
            for (int i = 1; i < jfxTextFieldList.size(); i++) {
                jfxTextFieldList.get(i).setEditable(booleanEditProperty);
                jfxTextFieldList.get(i).setOpacity(opacity);
            }

            //sets the opacity of the text fields
            for (int i=0; i < buttonList.size(); i++)
                buttonList.get(i).setDisable(!booleanEditProperty);
        }
    }

    /*method that clears all the value on the text fields and add 1 to the IDNo*/
    public void DefaultValues(String query){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);  //creates a query every time the new button is selected to get the last record on file
            ResultSet rset = preparedStatement.executeQuery();
            if(rset.next())
                IDNo = Integer.parseInt(rset.getString(1));
            else
                IDNo-=1;
            jfxTextFieldList.get(0).setText(String.valueOf(++IDNo));
            for(int i=1;i<jfxTextFieldList.size();i++)
                jfxTextFieldList.get(i).setText("");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /*a method that is invoked during initial loading of page and after a search is done */
    public void DisplayTextField(String query){
        try {
            ResultSet rset = connection.createStatement().executeQuery(query); //re execute the query to get the first record of the database
            if(rset.next()){
                for (int i=0;i<jfxTextFieldList.size();i++)
                    jfxTextFieldList.get(i).setText(rset.getString(i+1));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /*checks if text fields are blank*/
    public boolean NullValidator() {
        for  (int i=0;i<jfxTextFieldList.size();i++){
            if(jfxTextFieldList.get(i).getText().equals("")) {
                jfxTextFieldList.get(i).requestFocus();
                return false;
            }
        }
        return true;
    }
}
