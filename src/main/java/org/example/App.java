package org.example;

import org.example.services.TextSearchService;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final TextSearchService textSearchService = new TextSearchService();

//    public static void main( String[] args )
//    {
//        String text = "Очень большой большой текст";
//        String subText = "большой";
//        textSearchService.findSubTextFirstEntry(subText, text).forEach(System.out::println);
//    }

//    public static void main( String[] args )
//    {
//        String text = "abcabcabc";
//        System.out.println(textSearchService.findMaxEdge(text));
//    }

    public static void main( String[] args )
    {
//        String text = "очень большой, большой текст";
//        String subString = "большой";
        String text = "aabbaabbaabbaa";
        String subString = "aabbaa";
        System.out.println("Z-blocks");
        textSearchService.findSubstringsZBlocks(text, subString).forEach(System.out::println);
        System.out.println("KMP");
        textSearchService.findSubstringsKMP(text, subString).forEach(System.out::println);
        System.out.println("BM");
        textSearchService.findSubstringsBMChar(text, subString).forEach(System.out::println);

        int[] byteText = {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0};
        int[] byteSubstring = {0, 0, 1, 1, 0, 0};
        System.out.println("KR");
        textSearchService.findSubstringsKR(byteSubstring, byteText, 9973).forEach(System.out::println);

        System.out.println("Shift-And");
        textSearchService.findSubstringsShiftAnd(text, subString).forEach(System.out::println);
    }

}
