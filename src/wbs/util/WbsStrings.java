package wbs.util;

import java.util.Collection;

public abstract class WbsStrings {
	
	public static String capitalizeAll(String str) {
		String[] words = str.toLowerCase().split(" ");
		String display = ""; 
		for (String word : words) {
			display += " " + capitalize(word);
		}
		return display.substring(1);
	}
	
	public static String capitalize(String str) {
		String display = str.substring(1);
		display = str.substring(0, 1).toUpperCase() + display;
		return display;
	}
	
	public static String getLineWith(String find, Collection<String> in) {
		for (String node : in) {
			if (node.contains(find)) {
				return node;
			}
		}
		return null;
	}
	
	public static String getInvisibleString(String original) {
		char[] charList = new char[original.length()*2];
		int i = 0;
		for (char c : original.toCharArray()) {
			charList[i] = '§';
			charList[i+1] = c;
			i+=2;
		}
		return charList.toString();
	}
	
	public static String revealString(String invisibleString) {
		return invisibleString.replaceAll("§", "");
	}

	/***
	 * 
	 * @param strings The array of Strings to combine
	 * @param index The index to start combining at
	 * @return A single String containing all entries in {strings} split with " ", excluding the first {index} entries
	 */
	
	public static String combineLast(String[] strings, int index) {
		String[] newStringList = new String[strings.length-index];
		for (int i = index; i < strings.length; i++) {
			newStringList[i-index] = strings[i];
		}
		return String.join(" ", newStringList);
	}
	
	public static String asList(Enum<?>[] strings) {
		String[] newStringList = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			newStringList[i] = capitalizeAll(strings[i].name()).replace('_', ' ');
		}
		return String.join(", ", newStringList);
	}
}
