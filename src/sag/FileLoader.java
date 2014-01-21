package sag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLoader {

	public static ArrayList<ArrayList<String>> loadFile(String path)
	{
		FileReader fr = null;
		String line = "";
		ArrayList<ArrayList<String>> dataArray = new ArrayList<>();
		
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		
		BufferedReader br = new BufferedReader(fr);
		ArrayList<String> tokens = new ArrayList<>();
		
		try {
			while((line = br.readLine())!=null){
				tokens = parseLine(line);
				dataArray.add(tokens);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dataArray;
	}
        
        public static ArrayList<ArrayList<String>> loadFile(File f)
	{
		FileReader fr = null;
		String line = "";
		ArrayList<ArrayList<String>> dataArray = new ArrayList<>();
		
		try {
			fr = new FileReader(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		
		BufferedReader br = new BufferedReader(fr);
		ArrayList<String> tokens = new ArrayList<>();
		
		try {
			while((line = br.readLine())!=null){
				tokens = parseLine(line);
				dataArray.add(tokens);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dataArray;
	}
	
	private static ArrayList<String> parseLine(String line)
	{
		StringTokenizer st = new StringTokenizer(line,";");
		ArrayList<String> tokens = new ArrayList<>();
		
		while(st.hasMoreTokens()){
			tokens.add(st.nextToken());
		}
		
		return tokens;
	}
}
