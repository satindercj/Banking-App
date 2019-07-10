import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.Date;
import java.text.*;

public class NewTransactionScreenController {

    @FXML
    private Button buttonLogoutCheckingAccount;

    @FXML
    private TextField textboxTransferToAccountNumber;

    @FXML
    private Button buttonCreditCardCheckingAccount;

    @FXML
    private Label labelAccountBalance;

    @FXML
    private RadioButton radiobuttonWithdraw;

    @FXML
    private RadioButton radiobuttonIsCash;

    @FXML
    private RadioButton radiobuttonTransfer;

    @FXML
    private Button buttonCheckingAccountCheckingAccount;

    @FXML
    private Button buttonReportsCheckingAccount;

    @FXML
    private TextArea textboxTransactionDescription;

    @FXML
    private Button buttonCancelTransaction;

    @FXML
    private Button buttonChooseCheckFile;

    @FXML
    private RadioButton radiobuttonDeposit;

    @FXML
    private ToggleGroup groupTransactionType;

    @FXML
    private Button buttonSaveTransaction;

    @FXML
    private Button buttonUserAccountCheckingAccount;

    @FXML
    private Label labelAccountNumber;

    @FXML
    private TextField textboxTransactionAmount;

    @FXML
    private Label labelCustomerName;

    @FXML
    private RadioButton radiobuttonIsCheck;

    private String customerID, filePath, accountNumber, type;

    private String val, description, transferAccountNumber;

    private double amount;

    // method for initializing the window
    public void initialize(String id, String accountNum) {
        customerID = id;
        accountNumber = accountNum;

        final String DB_URL = "jdbc:mysql://142.93.91.169:3306/spDemorganDB";
        final String USERNAME = "root";
        final String PASSWORD = "password123";

        DecimalFormat moneyFormat = new DecimalFormat("$#,##0.00");

        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD); //Establish database connection
            Statement stmt = conn.createStatement(); //Create new statement object

            String sql = "SELECT Name from Customer WHERE CustomerID='" + customerID + "'";

            ResultSet rs = stmt.executeQuery(sql);

            //Display the content of the result set
            while (rs.next()) {
                labelCustomerName.setText(rs.getString("Name"));
            }

            sql = "SELECT * from CheckingAccount WHERE CustomerID='" + customerID + "'";

            rs = stmt.executeQuery(sql);

            //Display the content of the result set
            while (rs.next()) {
                labelAccountNumber.setText(rs.getString("AccountNumber"));
                double bal = Double.parseDouble(rs.getString("Balance"));
                labelAccountBalance.setText(moneyFormat.format(bal));
            }

            conn.close();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void availableOptions(){
        if(radiobuttonWithdraw.isSelected()){
            radiobuttonIsCash.setVisible(false);
            radiobuttonIsCheck.setVisible(false);
            buttonChooseCheckFile.setVisible(false);
            textboxTransferToAccountNumber.setVisible(false);
        }
        else if(radiobuttonDeposit.isSelected()){
            radiobuttonIsCash.setVisible(true);
            radiobuttonIsCash.setSelected(true);
            radiobuttonIsCheck.setVisible(true);
            buttonChooseCheckFile.setVisible(false);
            textboxTransferToAccountNumber.setVisible(false);
        }
        else if(radiobuttonTransfer.isSelected()){
            radiobuttonIsCash.setVisible(false);
            radiobuttonIsCheck.setVisible(false);
            buttonChooseCheckFile.setVisible(false);
            textboxTransferToAccountNumber.setVisible(true);
        }
    }

    public void fileBtnOption(){
        if(radiobuttonIsCheck.isSelected())
            buttonChooseCheckFile.setVisible(true);
        else if(radiobuttonIsCash.isSelected())
            buttonChooseCheckFile.setVisible(false);
    }

    public void filePicker(ActionEvent e){
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);

        if(selectedFile != null){
            filePath = selectedFile.getAbsolutePath();
        } else {
            System.out.println("File is not Valid.");
        }
    }

    public String readCheck() throws IOException{
        String amount = "0";

        // Open the file.
        File file = new File(filePath);
        Scanner inputFile = new Scanner(file);

        //When the files reader finds the next line to read
        while (inputFile.hasNext()){
            // Read the next line.
            amount = inputFile.nextLine();
        }

        // Close the file.
        inputFile.close();

        return amount;
    }

    public String dateTime() {
        String date;
        Date today = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("MM-dd-yyyy 'at' hh:mm:ss");

        date = ft.format(today);
        return date;
    }

    public void save(ActionEvent e) throws IOException{
        Transaction transaction;

        if(radiobuttonWithdraw.isSelected()){
            val = textboxTransactionAmount.getText();
            amount = Double.parseDouble(val);
            description = textboxTransactionDescription.getText();
            type = "Withdraw";
            transaction = new Transaction(customerID, accountNumber, type, description, amount, dateTime());
            transaction.withdraw();
            transaction.updateDBWithTransaction();
        }
        else if(radiobuttonDeposit.isSelected()){
            if(radiobuttonIsCash.isSelected())
                val = textboxTransactionAmount.getText();
            else if(radiobuttonIsCheck.isSelected())
                val = readCheck();

            System.out.println(val);
            amount = Double.parseDouble(val);
            description = textboxTransactionDescription.getText();
            type = "Deposit";
            transaction = new Transaction(customerID, accountNumber, type, description, amount, dateTime());
            transaction.deposit();
            transaction.updateDBWithTransaction();
        } else if(radiobuttonTransfer.isSelected()){
            val = textboxTransactionAmount.getText();
            amount = Double.parseDouble(val);
            description = textboxTransactionDescription.getText();
            transferAccountNumber = textboxTransferToAccountNumber.getText();
            type = "Transfer";
            transaction = new Transaction(customerID, accountNumber, type, description, amount, dateTime(), transferAccountNumber);
            transaction.transfer();
            transaction.updateDBWithTransaction();
        }
        goBack(e);
    }

    public void goBack(ActionEvent e){
        // the FXML loader object to load the UI design
        FXMLLoader loader = new FXMLLoader();
        // specify the file location
        loader.setLocation(getClass().getResource("CheckingAccountScreen.fxml"));

        // the object representing the root node of the scene
        Parent parent;
        // try-catch for possible errors in reading the FXML file.
        try {
            // load the UI
            parent = loader.load();

            // set the scene
            Scene scene = new Scene(parent);

            CheckingAccountScreenController controller = loader.getController();
            controller.initialize(customerID);

            // get the current window; i.e. the stage
            Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
            // set the scene for the stage
            stage.setScene(scene);
            // show the stage
            stage.show();
        } catch (IOException e1) {
            System.out.print(e1.getMessage());
        }
    }

}
