import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.Types;

public class P2 {
    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    public static void init(String filename) {
		try {
			Properties props = new Properties();						// Create a new Properties object
			FileInputStream input = new FileInputStream(filename);		// Create a new FileInputStream object using our filename parameter
			props.load(input);											// Load the file contents into the Properties object
			driver = props.getProperty("jdbc.driver");				// Load the driver
			url = props.getProperty("jdbc.url");					// Load the url
			username = props.getProperty("jdbc.username");			// Load the username
			password = props.getProperty("jdbc.password");			// Load the password
		} catch (Exception e) {
			e.printStackTrace();
		}
	}//init
    
    public static void MainMenu() {
        Scanner scanner = new Scanner(System.in);
        String name = null;
        char gender = 0;
        int age = 0;
        int pin = 0;
        int id = 0;
        int input = 0;
        boolean stop = false;

        System.out.println("Welcome to the Self Service Banking System!");
        while(input != 3) {
            System.out.println("Main Menu\n" 
                                + "1. New Customer\n"
                                + "2. Customer Login\n"
                                + "3. Exit");
            input = scanner.nextInt();
            scanner.nextLine();
            
            switch(input) {
                case 1:
                    System.out.println("Enter yout name:");
                    name = scanner.nextLine();
                    
                    //Gender
                    gender = 0;
                    while(Character.toUpperCase(gender) != 'M' && Character.toUpperCase(gender) != 'F') {
                    System.out.println("Enter your gender(M/F):");
                    gender = Character.toUpperCase(scanner.next().trim().charAt(0));
                    }

                    //Age 
                    age = 0;
                    stop = false;
                    do {
                        System.out.println("Enter your age:");
                        try {
                            age = scanner.nextInt();
                            stop = true;
                        }catch(InputMismatchException e) {
                            System.out.println("Acceptable digits are between 0-9. Try again!");
                            scanner.next();
                        }
                    }while(!stop);

                    //PIN
                    pin = checkPin("Create 4 digit PIN:");
                    BankingSystem.newCustomer(name, Character.toString(gender), String.valueOf(age), String.valueOf(pin));
                    break;
                case 2:
                    id = checkId();
                    pin = checkPin("Enter 4 digit PIN:");
    
                    if(id == 0 && pin == 0)
                        AdminMainMenu();
                    else if(checkIfInDB(id, pin)) {
                        CustomerMainMenu(id);
                    }
                    else
                        System.out.println("INCORRECT ID OR PIN\n");
                    break;
                case 3:
                    scanner.close();
                    System.exit(0);
                    break;
            }
        }
    }//MainMenu

    public static void CustomerMainMenu(int loginId) {
        Scanner scanner = new Scanner(System.in);
        int id = 0;
        char type = 0;
        int amount = -1;
        int accNum = 0;
        int destAccNum = 0;
        int input = 0;
        boolean stop = false;
        
        while(input != 7) {
            System.out.println("\nCustomer Main Menu\n"
            + "1. Open Account\n" 
            + "2. Close Account\n" 
            + "3. Deposit\n" 
            + "4. Withdraw\n" 
            + "5. Transfer\n" 
            + "6. Account Summary\n" 
            + "7. Exit");

            input = scanner.nextInt();
            scanner.nextLine();
            switch(input) {
                case 1: //Open Account
                    id = checkId();
                    type = 0;
                    while(Character.toUpperCase(type) != 'C' && Character.toUpperCase(type) != 'S') { 
                        System.out.println("Would you like to open a checking(C) or saving(S) account?(C/S):");
                        type = Character.toUpperCase(scanner.next().trim().charAt(0));
                        }
                    
                    amount = checkAmount();
                    BankingSystem.openAccount(String.valueOf(id), String.valueOf(amount), Character.toString(type));
                    //return account id 
                    break;
                case 2: //Close Account
                    accNum = checkAccNum();
                    //check if an account exists and check if an accout belongs to the current customer id
                    if(checkIfAccBelongsToId(loginId, accNum))
                        BankingSystem.closeAccount(String.valueOf(accNum));
                    else
                        System.out.println("PROVIDED ACCOUNT DOES NOT BELONG TO THE CURRENT CUSTOMER!\n");
                    break;
                case 3: //Deposit
                    accNum = checkAccNum();
                     //check if an account exists
                    amount = checkAmount();
                    BankingSystem.deposit(String.valueOf(accNum), String.valueOf(amount));
                    break;
                case 4: //Withdraw
                    accNum = checkAccNum();
                    if(checkIfAccBelongsToId(loginId, accNum)) {
                        amount = checkAmount();
                        BankingSystem.withdraw(String.valueOf(accNum), String.valueOf(amount));
                    }
                    else 
                        System.out.println("PROVIDED ACCOUNT DOES NOT BELONG TO THE CURRENT CUSTOMER!");
                    break;
                case 5: //Transfer
                    //A source account 
                    stop = false;
                    do {
                    System.out.println("Enter a source account number:");
                    try {
                        accNum = scanner.nextInt();
                        stop = true;
                    } catch(InputMismatchException e) {
                    System.out.println("Acceptable digits are between 0-9. Try again!");
                    scanner.next();
                    }
                    }while(!stop);
                    
                    if(checkIfAccBelongsToId(loginId, accNum)) {
                    //A destination account
                        stop = false;
                        do {
                        System.out.println("Enter a destination account number:");
                        try {
                            destAccNum = scanner.nextInt();
                            stop = true;
                        } catch(InputMismatchException e) {
                        System.out.println("Acceptable digits are between 0-9. Try again!");
                        scanner.next();
                        }
                        }while(!stop);
                        
                        if(accNum == destAccNum)
                            System.out.println("THE SOURCE AND DESTINATION ACCOUNTS ARE IDENTICAL!");
                        else {
                            amount = checkAmount();
                            BankingSystem.transfer(String.valueOf(accNum), String.valueOf(destAccNum), String.valueOf(amount));
                        }
                    }
                    else
                        System.out.println("PROVIDED ACCOUNT DOES NOT BELONG TO THE CURRENT CUSTOMER OR INACTIVE!");
                    break;
                case 6: //Account Summary
                    BankingSystem.accountSummary(String.valueOf(loginId));
                    break;
                case 7:
                    return;
            }
        }
    }//CustomerMainMenu

    public static void AdminMainMenu() {
        Scanner scanner = new Scanner(System.in);
        int id = 0;
        int min = 0;
        int max = 0;
        int input = 0;
        boolean stop = false;

        while(input != 4) {
            System.out.println("1. Account Summary for a Customer\n"
                            + "2. Report A :: Customer Information with Total Balance in Decreasing Order\n"
                            + "3. Report B :: Find the Average Total Balance Between Age Groups\n"
                            + "4. Exit\n");
            input = scanner.nextInt();
            switch(input) {
                case 1:
                    id = checkId();
                    BankingSystem.accountSummary(String.valueOf(id));
                    break;
                case 2:
                    BankingSystem.reportA();
                    break;
                case 3:
                    stop = false;
                    do {
                        System.out.println("Enter a minimum age:");
                        try {
                        min = scanner.nextInt();
                        stop = true;
                        }catch(InputMismatchException e) {
                            System.out.println("Acceptable digits are between 0-9. Try again!");
                            scanner.next();
                        }
                    }while(!stop);

                    stop = false;
                    do {
                        System.out.println("Enter a maximum age:");
                        try {
                        max = scanner.nextInt();
                        stop = true;
                        }catch(InputMismatchException e) {
                            System.out.println("Acceptable digits are between 0-9. Try again!");
                            scanner.next();
                        }
                    }while(!stop);
                    
                    BankingSystem.reportB(String.valueOf(min), String.valueOf(max));
                    break;
                case 4:
                    return;
            }
        }
    }//AdminMainMenu

    public static boolean checkIfInDB(int id, int pin) {
        int valid = 0;
        String name = null;
		try {
			Connection con = DriverManager.getConnection(url, username, password);
            CallableStatement cStmt = con.prepareCall("{call P2.CUST_LOGIN(?,?,?,?,?,?)}");
            cStmt.setInt(1, id);
            cStmt.setInt(2, pin);
            cStmt.registerOutParameter("valid", Types.INTEGER);
            cStmt.registerOutParameter("p_name", Types.CHAR);
            cStmt.registerOutParameter("sql_code", Types.INTEGER);
            cStmt.registerOutParameter("err_msg", Types.CHAR);
            cStmt.execute();

            valid = cStmt.getInt("valid");
            name = cStmt.getString("p_name");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

        if(valid == 1) {
            System.out.println("\nWelcome Back, " + name + "!");
            return true;
        }
        else
        return false;
	}//CheckIfInDB

    public static int checkId() {
        Scanner scanner = new Scanner(System.in);
        int id = 0;
        boolean stop = false;
        do {
            System.out.println("Enter a customer ID:");
            try {
            id = scanner.nextInt();
            stop = true;
            }catch(InputMismatchException e) {
                System.out.println("Acceptable digits are between 0-9. Try again!");
                scanner.next();
            }
        }while(!stop);

        return id;
    }//checkId

    public static int checkPin(String msg) {
        Scanner scanner = new Scanner(System.in);
        int pin = 0;
        boolean stop = false;
        while(String.valueOf(pin).length() != 4) {
            do {
                System.out.println(msg);
                try {
                    pin = scanner.nextInt();
                    stop = true;
            }catch(InputMismatchException e) {
                System.out.println("Acceptable digits are between 0-9. Try again!:");
                scanner.next();
            }
            }while(!stop);
        }
        return pin;
    }//checkPin

    public static boolean checkIfAccBelongsToId(int loginId, int accNum) {
        try {
			Connection con = DriverManager.getConnection(url, username, password);
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT number FROM p2.account WHERE p2.account.id = " + loginId 
                                            + " AND p2.account.number = " + accNum);
			if(rs.next())
				return true;
        
            rs.close();
            stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
        return false;
    }//checkIfAccBelongsToId

    public static int checkAccNum() {
        Scanner scanner = new Scanner(System.in);
        int num = 0;
        boolean stop = false;
        do {
            System.out.println("Enter an account number:");
            try {
                num = scanner.nextInt();
                stop = true;
            }catch(InputMismatchException e) {
                System.out.println("Acceptable digits are between 0-9. Try again!");
                scanner.next();
            }
        }while(!stop);
        return num;
    }//checkAccNum

    public static int checkAmount() {
        Scanner scanner = new Scanner(System.in);
        int amount = -1;
        boolean stop = false;
        while(amount <= 0) {
            stop = false;
            do {
                System.out.println("Enter an amount:");
                try {
                    amount = scanner.nextInt();
                    stop = true;
                }catch(InputMismatchException e) {
                    System.out.println("Acceptable digits are between 0-9. Try again!");
                    scanner.next();
                }
            }while(!stop);
        }
        return amount;
    }//checkAmount
    
}
