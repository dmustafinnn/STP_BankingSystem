import java.io.FileInputStream;
import java.lang.ProcessBuilder.Redirect.Type;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;

/**
 * Manage connection to database and perform SQL statements.
 */
public class BankingSystem extends Exception {
	// Connection properties
	private static String driver;
	private static String url;
	private static String username;
	private static String password;
	
	// JDBC Objects
	private static Connection con;
	private static Statement stmt;
	private static ResultSet rs;
	private static CallableStatement cStmt;

	/**
	 * Initialize database connection given properties file.
	 * @param filename name of properties file
	 */
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
	
	/**
	 * Test database connection.
	 */
	public static void testConnection() {
		System.out.println(":: TEST - CONNECTING TO DATABASE");
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, username, password);
			con.close();
			System.out.println(":: TEST - SUCCESSFULLY CONNECTED TO DATABASE");
			} catch (Exception e) {
				System.out.println(":: TEST - FAILED CONNECTED TO DATABASE");
				e.printStackTrace();
			}
	  }//testConnection

	/**
	 * Create a new customer.
	 * @param name customer name
	 * @param gender customer gender
	 * @param age customer age
	 * @param pin customer pin
	 */

	public static void newCustomer(String name, String gender, String age, String pin)
	{
		System.out.println("\n:: CREATE NEW CUSTOMER - RUNNING");
		if(age.matches("[0-9]+") && pin.matches("[0-9]+")) {

			try {
				System.out.println(":: CALLING THE STORED PROCEDURE - P2.CUST_CRT");
				con = DriverManager.getConnection(url, username, password); 

				cStmt = con.prepareCall("{call P2.CUST_CRT(?,?,?,?,?,?,?)}");
				cStmt.setString(1, name);
				cStmt.setString(2, gender);
				cStmt.setInt(3, Integer.valueOf(age));
				cStmt.setInt(4, Integer.valueOf(pin));
				cStmt.registerOutParameter("id", Types.INTEGER);
				cStmt.registerOutParameter("sql_code", Types.INTEGER);
				cStmt.registerOutParameter("err_msg", Types.CHAR);
				cStmt.execute();

				System.out.print("YOUR ID: " + cStmt.getInt("id"));
				cStmt.close();
				con.close();
				System.out.println("\n:: CREATE NEW CUSTOMER - SUCCESS\n");                                                                       
			}catch (Exception e) {
				System.out.println("Exception in newCustomer()");
				e.printStackTrace();
			}
		}
		else
			System.out.println("AGE OR PIN IS NOT AN INTEGER!");
	}//newCustomer

	/**
	 * Open a new account.
	 * @param id customer id
	 * @param type type of account
	 * @param amount initial deposit amount
	 */
	public static void openAccount(String id, String balance, String type) 
	{
		System.out.println("\n:: OPEN ACCOUNT - RUNNING");
		try {                                                                  
			con = DriverManager.getConnection(url, username, password);                 
			cStmt = con.prepareCall("{call P2.ACCT_OPN(?,?,?,?,?,?)}");
			cStmt.setInt(1, Integer.valueOf(id));
			cStmt.setInt(2, Integer.valueOf(balance));
			cStmt.setString(3, type);
			cStmt.registerOutParameter("p_num", Types.INTEGER);
			cStmt.registerOutParameter("sql_code", Types.INTEGER);
			cStmt.registerOutParameter("err_msg", Types.CHAR);
			cStmt.execute();

			System.out.println("YOUR ACCOUNT NUMBER: " + cStmt.getInt("p_num"));
			cStmt.close();                        
			con.close();
			System.out.println(":: OPEN ACCOUNT - SUCCESS\n");                                                                       
		  }catch (Exception e) {
			System.out.println("Exception in openAccount()");
			e.printStackTrace();
		  }
	}//openAccount

	/**
	 * Close an account.
	 * @param accNum account number
	 */
	public static void closeAccount(String accNum) 
	{
		System.out.println("\n:: CLOSE ACCOUNT - RUNNING");
		try {                                                                  
			con = DriverManager.getConnection(url, username, password);
			cStmt = con.prepareCall("{call P2.ACCT_CLS(?,?,?)}");
			cStmt.setInt(1, Integer.valueOf(accNum));
			cStmt.registerOutParameter("sql_code", Types.INTEGER);
			cStmt.registerOutParameter("err_msg", Types.CHAR);
			cStmt.execute();
			System.out.println(":: " + cStmt.getString("err_msg") + "\n"); 
			cStmt.close();                                                                           
			con.close();                                                                         
		}catch (Exception e) {
			System.out.println("Exception in closeAccount()");
			e.printStackTrace();
		}
	}//closeAccount

	/**
	 * Deposit into an account.
	 * @param accNum account number
	 * @param amount deposit amount
	 */
	public static void deposit(String accNum, String amount) 
	{
		System.out.println("\n:: DEPOSIT - RUNNING");
		if(amount.matches("[0-9]+")) {
			try {
				con = DriverManager.getConnection(url, username, password);  
				cStmt = con.prepareCall("{call P2.ACCT_DEP(?,?,?,?)}");
				cStmt.setInt(1, Integer.valueOf(accNum));
				cStmt.setInt(2, Integer.valueOf(amount));
				cStmt.registerOutParameter("sql_code", Types.INTEGER);
				cStmt.registerOutParameter("err_msg", Types.CHAR);
				cStmt.execute();
				System.out.print(":: " + cStmt.getString("err_msg") + "\n");
				cStmt.close();                                                                           
				con.close();                                                                   
				}catch (Exception e) {
					System.out.println("Exception in deposit()");
					e.printStackTrace();
				}
		}
		else
			System.out.println("AMOUNT IS NOT AN INTEGER!");
	}//deposit

	/**
	 * Withdraw from an account.
	 * @param accNum account number
	 * @param amount withdraw amount
	 */
	public static void withdraw(String accNum, String amount)
	{
		System.out.println("\n:: WITHDRAW - RUNNING");
		if(amount.matches("[0-9]+")) {
			try {                                                                  
				con = DriverManager.getConnection(url, username, password);                 
				cStmt = con.prepareCall("{call P2.ACCT_WTH(?,?,?,?)}"); 
				cStmt.setInt(1, Integer.valueOf(accNum));
				cStmt.setInt(2, Integer.valueOf(amount));
				cStmt.registerOutParameter("sql_code", Types.INTEGER);
				cStmt.registerOutParameter("err_msg", Types.CHAR);
				cStmt.execute();
				
				System.out.println(":: " + cStmt.getString("err_msg")+ "\n");  	
				cStmt.close();                                                                           
				con.close();                                                                         
				}catch (Exception e) {
					System.out.println("Exception in withdraw()");
					e.printStackTrace();
				}
			}
		else
			System.out.println("AMOUNT IS NOT AN INTEGER!");
	}//withdraw

	/**
	 * Transfer amount from source account to destination account. 
	 * @param srcAccNum source account number
	 * @param destAccNum destination account number
	 * @param amount transfer amount
	 */
	public static void transfer(String srcAccNum, String destAccNum, String amount) 
	{
		System.out.println("\n:: TRANSFER - RUNNING");
		try {                                                                  
			con = DriverManager.getConnection(url, username, password);
			cStmt = con.prepareCall("{call P2.ACCT_TRX(?,?,?,?,?)}");                 
			cStmt.setInt(1, Integer.valueOf(srcAccNum));
			cStmt.setInt(2, Integer.valueOf(destAccNum));
			cStmt.setInt(3, Integer.valueOf(amount));
			cStmt.registerOutParameter("sql_code", Types.INTEGER);
			cStmt.registerOutParameter("err_msg", Types.CHAR);
			cStmt.execute();
			System.out.println(":: " + cStmt.getString("err_msg") + "\n");
			cStmt.close();                                                                           
			con.close();                                                                           
		  }catch (Exception e) {
			System.out.println("Exception in withdraw()");
			e.printStackTrace();
		  }
	}//transfer

	/**
	 * Display account summary.
	 * @param cusID customer ID
	 */
	public static void accountSummary(String cusID) 
	{
		int totalBalance = 0;
		System.out.println("\n:: ACCOUNT SUMMARY - RUNNING");
		try {                                                                  
			con = DriverManager.getConnection(url, username, password);                 
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); 
			//Check if a customer exists 
			rs = stmt.executeQuery("SELECT 1 FROM p1.account WHERE id = " + Integer.valueOf(cusID) + " LIMIT 1");
			if(!rs.next())
				throw new AccountNotExistException("CUSTOMER NOT FOUND\n");
			//Execute the accountSummary operation
			rs = stmt.executeQuery("SELECT number, balance FROM p1.account WHERE status <> 'I' AND id = " + Integer.valueOf(cusID));
			if(!rs.next())
				System.out.println("NO ACCOUNT EXISTS FOR A CUSTOMER: " + cusID + "\n");
			else {
				rs.beforeFirst();
				System.out.println("NUMBER\tBALANCE");
				while(rs.next()) {
				System.out.println(rs.getInt(1) + "\t" + rs.getInt(2));
				totalBalance += rs.getInt(2);
				}
				System.out.println("TOTAL BALANCE: " + totalBalance);
				System.out.println(":: ACCOUNT SUMMARY - SUCCESS\n");
			}
			rs.close();                                     
			stmt.close();                                                                           
			con.close();                                                                          
		}catch(AccountNotExistException ex) {
			System.out.println(ex.getMessage());
		}catch (Exception e) {
			System.out.println("Exception in accountSummary()");
			e.printStackTrace();
		}	
	}//accountSummary

	/**
	 * Display Report A - Customer Information with Total Balance in Decreasing Order.
	 */
	public static void reportA() 
	{
		System.out.println(":: REPORT A - RUNNING");
		try {                                                                   
			con = DriverManager.getConnection(url, username, password);                 
			stmt = con.createStatement(); 
			rs = stmt.executeQuery("SELECT customer.id, name, gender, age, totalBalance FROM p1.customer AS customer INNER JOIN" 
								+ " (SELECT id, SUM(balance) as totalBalance FROM p1.account WHERE status <> 'I' GROUP BY id) AS account" 
								+ " ON customer.id = account.id ORDER BY totalBalance DESC");
			System.out.printf("%-10s %-10s %-10s %-10s %-10s\n", "NUMBER", "NAME", "GENDER", "AGE", "Total Balance");
			while(rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				String gender = rs.getString(3);
				int age = rs.getInt(4);
				int totalBalance = rs.getInt(5);
				System.out.printf("%-10s %-10s %-10s %-10s %-10s\n", String.valueOf(id), name, gender, String.valueOf(age), String.valueOf(totalBalance));
			}
			rs.close();
			stmt.close();
			con.close(); 
			System.out.println(":: REPORT A - SUCCESS\n");                                                                   
		  } catch (Exception e) {
			System.out.println("Exception in reportA()");
			e.printStackTrace();
		  }		
	}//reportA

	/**
	 * Display Report B - Find the average total balance in decreasing order 
	 * @param min minimum age
	 * @param max maximum age
	 */
	public static void reportB(String min, String max) 
	{
		System.out.println(":: REPORT B - RUNNING");
		try {                                                                  
			con = DriverManager.getConnection(url, username, password);                 
			stmt = con.createStatement(); 
			
			rs = stmt.executeQuery("SELECT AVG(totalBalances) FROM (SELECT SUM(balance) as totalBalances FROM p1.account" 
									+ " INNER JOIN (SELECT id, age FROM p1.customer WHERE age >= " + Integer.valueOf(min) 
									+ " AND age <= " + Integer.valueOf(max) + ") AS customer ON p1.account.id = customer.id" 
									+ " AND p1.account.status <> 'I' GROUP BY(p1.account.id))");
			while(rs.next())
				System.out.println("AVERAGE BALANCE: " + rs.getInt(1));
			
			rs.close();
			stmt.close();
			con.close(); 
			System.out.println(":: REPORT B - SUCCESS\n");                                                                     
		  } catch (Exception e) {
			System.out.println("Exception in reportB()");
			e.printStackTrace();
		  }		
	}//reportB
}

class InsufficientBalanceException extends Exception {
	public InsufficientBalanceException(String s) {
		super(s);
	}
}
class InactiveAccountException extends Exception {
	public InactiveAccountException(String s) {
		super(s);
	}
}

class AccountNotExistException extends Exception {
	public AccountNotExistException(String s) {
		super(s);
	}
}
