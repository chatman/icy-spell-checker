import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class NGramStore {
	Connection conn[] = new Connection[5];
	Statement stat[] = new Statement[5];
	String tables[] = {"one_gram", "two_gram", "three_gram", "four_gram", "five_gram"};

	public NGramStore() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		for (int i=0; i<5; i++) { 
			conn[i] = DriverManager.getConnection("jdbc:sqlite:res/frequency"+(i+1)+".indexed.db");
			stat[i] = conn[i].createStatement();
		}
		System.out.println("SQLite DBs loaded.");
	}

	int getFrequency (String phrase) {
		String grams[] = phrase.split(" ");
		if (grams.length<1 || grams.length>5)
			return -1;

		int sum = 0;
		
		try {
			ResultSet rs = stat[grams.length-1].executeQuery("select * from "+tables[grams.length-1]+" where ngram='"+phrase+"';");
			
			while (rs.next()) {
				String ngram = rs.getString("ngram");
				int freq = (int)rs.getLong("freq");
				sum+=freq;
			}
			rs.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return sum;
	}
}
