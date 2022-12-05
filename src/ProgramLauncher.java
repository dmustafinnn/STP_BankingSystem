import java.util.Scanner;

/**
 * Main entry to program.
 */
public class ProgramLauncher {
	public static void main(String argv[]) {
		System.out.println(":: PROGRAM START");
		
		if (argv.length < 1) {
			System.out.println("Need database properties filename");
		} else {
			BankingSystem.init(argv[0]);
			BankingSystem.testConnection();
			System.out.println();
			P2.init(argv[0]);
			P2.MainMenu();
		}

		System.out.println(":: PROGRAM END");
	}
}