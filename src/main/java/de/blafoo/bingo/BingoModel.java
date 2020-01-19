package de.blafoo.bingo;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BingoModel {
	
	private static final Map<String, List<String>> DATA = new HashMap<>();
	
	static {
		// https://de.wikipedia.org/wiki/Liste_der_gr%C3%B6%C3%9Ften_deutschen_St%C3%A4dte#Die_gr%C3%B6%C3%9Ften_deutschen_St%C3%A4dte_2015
		DATA.put("Städte", Arrays.asList(
				"Berlin", "Hamburg", "München", "Köln", 
				"Frankfurt", "Stuttgart", "Düsseldorf", "Dortmund",
				"Essen", "Leipzig", "Bremen", "Dresden", 
				"Hannover", "Nürnberg", "Duisburg", "Bochum"));
	}
	
	public static List<String> getData(String key) {
		return DATA.get(key);
	}
	
}
