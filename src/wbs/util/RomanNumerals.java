package wbs.util;

public class RomanNumerals {

   public static int ToArabic(String number) {
	   int length = number.length();
	   if (number.isEmpty()) return 0;
	   if (number.startsWith("M")) return 1000 + ToArabic(number.substring(1, length));
	   if (number.startsWith("CM")) return 900 + ToArabic(number.substring(2, length));
	   if (number.startsWith("D")) return 500 + ToArabic(number.substring(1, length));
	    if (number.startsWith("CD")) return 400 + ToArabic(number.substring(2, length));
	    if (number.startsWith("C")) return 100 + ToArabic(number.substring(1, length));
	    if (number.startsWith("XC")) return 90 + ToArabic(number.substring(2, length));
	    if (number.startsWith("L")) return 50 + ToArabic(number.substring(1, length));
	    if (number.startsWith("XL")) return 40 + ToArabic(number.substring(2, length));
	    if (number.startsWith("X")) return 10 + ToArabic(number.substring(1, length));
	    if (number.startsWith("IX")) return 9 + ToArabic(number.substring(2, length));
	    if (number.startsWith("V")) return 5 + ToArabic(number.substring(1, length));
	    if (number.startsWith("IV")) return 4 + ToArabic(number.substring(2, length));
	    if (number.startsWith("I")) return 1 + ToArabic(number.substring(1, length));
	    return -1;
   }

   public static String toRoman(int input) {
	    if (input < 1 || input > 3999)
	        return "Invalid Roman Number Value (" + Integer.toString(input) + ")";
	    String s = "";
	    while (input >= 1000) {
	        s += "M";
	        input -= 1000;        }
	    while (input >= 900) {
	        s += "CM";
	        input -= 900;
	    }
	    while (input >= 500) {
	        s += "D";
	        input -= 500;
	    }
	    while (input >= 400) {
	        s += "CD";
	        input -= 400;
	    }
	    while (input >= 100) {
	        s += "C";
	        input -= 100;
	    }
	    while (input >= 90) {
	        s += "XC";
	        input -= 90;
	    }
	    while (input >= 50) {
	        s += "L";
	        input -= 50;
	    }
	    while (input >= 40) {
	        s += "XL";
	        input -= 40;
	    }
	    while (input >= 10) {
	        s += "X";
	        input -= 10;
	    }
	    while (input >= 9) {
	        s += "IX";
	        input -= 9;
	    }
	    while (input >= 5) {
	        s += "V";
	        input -= 5;
	    }
	    while (input >= 4) {
	        s += "IV";
	        input -= 4;
	    }
	    while (input >= 1) {
	        s += "I";
	        input -= 1;
	    }    
	    return s;
	}
}
