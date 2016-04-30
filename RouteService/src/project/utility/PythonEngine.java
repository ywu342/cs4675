package project.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PythonEngine {
	   public static String runPython(String[] args, String pyPath){
		   List<String> process_args = new ArrayList<String>(Arrays.asList("python", pyPath));
		    process_args.addAll(Arrays.asList(args));
		    String s = "";
		    Runtime r = Runtime.getRuntime();
		    try {

		        Process p = r.exec(process_args.toArray(new String[] {}));
		        BufferedReader stdInput = new BufferedReader(new
		                 InputStreamReader(p.getInputStream()));
		 
		        BufferedReader stdError = new BufferedReader(new
		                 InputStreamReader(p.getErrorStream()));
		 
		        // read the output from the command
//		        System.out.println("Here is the standard output of the command:\n");
		        // read any errors from the attempted command
//		        System.out.println("Here is the standard error of the command (if any):\n");
		        boolean errorFlag = false;
		        while ((s = stdError.readLine()) != null) {
		                System.out.println(s);
		                errorFlag = true;
		        }
		        if(errorFlag){
		        	return "ERROR";
		        }
		        
		        StringBuilder builder = new StringBuilder();
		        String aux = "";

		        while ((aux = stdInput.readLine()) != null) {
		            builder.append(aux);
		        }

		        s = builder.toString();        
		  
		        
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
		    return s;
	   }
}
