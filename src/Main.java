import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File documentFolder = new File(args[0]);

		Indicies indicies = new Indicies(documentFolder);
		indicies.buildIndicies();
		
		
		
		QueryProcessor q = new QueryProcessor(indicies);
		
		
		//process queries from input file
		try{
			File queries = new File(args[1]);
			Scanner input = new Scanner(queries);
			String line;
			File output = new File(args[2]);
			FileWriter fWriter = new FileWriter(output);
	        BufferedWriter writer =new BufferedWriter(fWriter);
	        
	        
			//each line is a separate query
			while(input.hasNextLine()){	
				line = input.nextLine();
				ArrayList<DocumentTerm> resultSet= q.processQuery(line);
				Collections.sort(resultSet);
				String resultList = q.listDocSet(resultSet);
				writer.write("Results of Query: "+ "'"+line +"'" +"\n");
				writer.write(resultList + "\n");
			
			}
			writer.close();
			input.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
