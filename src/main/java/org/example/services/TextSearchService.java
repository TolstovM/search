package org.example.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextSearchService {

    /**
     * Поиск первого вхождения строки в текст
     * @param subText строка
     * @param text текст
     * @return индекс первого вхождения строки в текст, если текст содержит строку, иначе - -1
     */
    public List<Integer> findSubTextFirstEntry(String subText, String text) {
        return findSubTextFirstEntry(subText.toCharArray(), text.toCharArray());
    }

    public List<Integer> findSubTextFirstEntry(char[] subText, char[] text) {
        return IntStream.range(0, text.length)
                .filter(textIdx ->
                        IntStream.range(0, subText.length)
                                .allMatch(subTextIdx -> isCharsEquals(subText, text, subTextIdx, textIdx)))
                .boxed().collect(Collectors.toList());
    }

    private boolean isCharsEquals(char[] subText, char[] text, int subTextIdx, int textIdx) {
        return subTextIdx < subText.length && textIdx + subTextIdx < text.length &&
                subText[subTextIdx] == text[textIdx + subTextIdx];
    }

    /**
     * Поиск максимальной грани
     */
    public int findMaxEdge(String text) {
        return findMaxEdge(text.toCharArray());
    }

    public int findMaxEdge(char[] text) {
        for (int i = text.length - 1; i > 0; i--){
            if (isPrefixAndSuffixEqual(text, i)) {
                return i;
            }
        }
        return 0;
    }

    private boolean isPrefixAndSuffixEqual(char[] text, int len) {
        return IntStream.range(0, len).allMatch(i -> text[i] == text[text.length - len + i]);
    }

    //------------------- Z - блоки --------------------------

    public List<Integer> findSubstringsZBlocks(String text, final String subString) {
        int[] zBlocks = zFunction((subString + "#" + text).toCharArray());
        return IntStream.range(subString.length() + 1, zBlocks.length)
                .filter(i -> zBlocks[i] == subString.length())
                .map(i -> i - subString.length() - 1)
                .boxed()
                .collect(Collectors.toList());
    }

    private int[] zFunction(char[] text) {
        int[] zp = new int[text.length];
        int l = 0;
        int r = 0;
        for (int i = 1; i < zp.length; i++) {
            zp[i] = i < r ? Math.min(zp[i - l], r - i) : 0;
            while (i + zp[i] < text.length && text[zp[i]] == text[i + zp[i]]) {
                zp[i]++;
            }
            if (i + zp[i] > r) {
                l = i;
                r = i + zp[i];
            }
        }
        return zp;
    }

    //------------------- Префиксы --------------------------

    public int[] getPrefixesBorders(char[] text) {
        int[] bp = new int[text.length];

        for (int i=1; i < text.length; i++) {
            int right = bp[i-1];
            while (right != 0 && text[i] != text[right]) {
                right = bp[right-1];
            }
            if (text[i] == text[right]) {
                bp[i] = right + 1;
            } else {
                bp[i] = 0;
            }
        }
        return bp;
    }

    public int[] getModifiedPrefixesBorders(int[] bp) {
        int[] bpm = new int[bp.length];
        for (int i=1; i < bp.length - 1; i++) {
            if (bp[i] + 1 != bp[i + 1]) {
                bpm[i] = bp[i];
            }
        }
        bpm[bp.length-1] = bp[bp.length-1];
        return bpm;
    }

    //------------------- Кнута-Морриса-Пратта --------------------------
    public List<Integer> findSubstringsKMP(String sText, String sSubstring) {
        char[] text = sText.toCharArray();
        char[] substring = sSubstring.toCharArray();
        int[] bp = getPrefixesBorders(substring);
        bp = getModifiedPrefixesBorders(bp);

        List<Integer> results = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < text.length; i++) {
            while (j != 0 && text[i] != substring[j]) {
                j = bp[j - 1];
            }
            if (text[i] == substring[j]) {
                j++;
            }
            if (j == substring.length) {
                results.add(i - j + 1);
                j = bp[j - 1];
            }
        }
        return results;
    }

    //------------------- Боера-Мура --------------------------
    public List<Integer> findSubstringsBMChar(String sText, String sSubstring) {
        char[] text = sText.toCharArray();
        char[] substring = sSubstring.toCharArray();
        List<Integer>[] positionList = getPositionList(substring);

        int[] bs = getSuffixesBorders(substring);
        int[] bsm = getSuffixesBordersModified(bs);
        int[] nsh = getNearestSuffixes(bsm);
        int[] br = getRestrictedBorders(bs);

        List<Integer> results = new ArrayList<>();

        int rightPointer = substring.length;
        while (rightPointer <= text.length) {
            int j = substring.length - 1;
            int i = rightPointer - 1;
            for (; j >= 0; j--, i--) {
                if (substring[j] != text[i]) {
                    break;
                }
            }
            if (j < 0) {
                results.add(i + 1);
                rightPointer++;
            } else {
                rightPointer += Math.max(getBadCharShift(positionList, text[i], j),
                        getGoodSuffixShift(nsh, br, j, substring.length));
            }
        }

        return results;
    }

    private List<Integer>[] getPositionList(char[] substring) {
        List<Integer>[] positions = new List[256];
        for(int i = substring.length-1; i >= 0; i--) {
            if (positions[substring[i]] == null) {
                positions[substring[i]] = new ArrayList<>();
            }
            positions[substring[i]].add(i);
        }
        return positions;
    }

    private int getBadCharShift(List<Integer>[] positions, char badChar, int badPosition) {
        int newPosition = -1;
        List<Integer> charPositions = positions[badChar];
        if (charPositions != null) {
            for (int i = 0; i < charPositions.size(); i++) {
                if (charPositions.get(i) < badPosition) {
                    newPosition = charPositions.get(i);
                    break;
                }
            }
        }
        return badPosition - newPosition;
    }

    //------------------- Суффиксы --------------------------
    private int[] getSuffixesBorders(char[] string) {
        int[] bs = new int[string.length];
        for (int i=string.length-2; i>=0; i--) {
            int bsLeft = bs[0];
            while (bsLeft != 0 && string[i] != string[string.length - bsLeft - 1]) {
                bsLeft = bs[string.length - bsLeft];
            }
            if (string[i] == string[string.length - bsLeft - 1]) {
                bs[i] = bsLeft + 1;
            } else {
                bs[i] = 0;
            }
        }
        return bs;
    }

    private int[] getSuffixesBordersModified(int[] bs) {
        int[] bsm = new int[bs.length];
        bsm[0] = bs[0];
        for (int i=bs.length-1; i>0; i--) {
            if (bs[i] + 1 != bs[i - 1]) {
                bsm[i] = bs[i];
            }
        }
        return bsm;
    }

    private int[] getNearestSuffixes(int[] bs) {
        int[] ns = new int[bs.length];
        Arrays.fill(ns, -1);
        for(int i=0; i<bs.length; i++) {
            if (bs[i] != 0) {
                ns[bs.length - bs[i] - 1] = i;
            }
        }
        return ns;
    }

    private int[] getRestrictedBorders(int[] bs) {
        int[] br = new int[bs.length];
        int j = 0;
        int currentBorder = bs[0];
        while (currentBorder != 0) {
            for (; j < bs.length - currentBorder; j++) {
                br[j] = currentBorder;
            }
            currentBorder = bs[j];
        }
        return br;
    }

    private int getGoodSuffixShift(int[] nsh, int[] br, int badPosition, int substringLen) {
        if (badPosition == substringLen - 1) {
            return 1;
        }
        if (badPosition < 0) {
            return substringLen - br[0];
        }
        return nsh[badPosition] >= 0 ? badPosition - nsh[badPosition] + 1 : substringLen - br[badPosition];
    }


    //------------------- Карпа-Рабина --------------------------
    public List<Integer> findSubstringsKR(int[] substring, int[] text, int q) {
        List<Integer> results = new ArrayList<>();
        int n = text.length;
        text = Arrays.copyOf(text, text.length + 1);
        //2^(m-1) mod q
        int p2m = 1;
        for (int i=0; i< substring.length-1; i++) {
            p2m = (p2m * 2) % q;
        }

        int substringHash = gorner2Mod(substring, substring.length, q);
        int textSampleHash = gorner2Mod(text, substring.length, q);
        for (int j=0; j <= n - substring.length; j++) {
            if (substringHash == textSampleHash) {
                int k = 0;
                while (k < substring.length && substring[k] == text[j+k]) {
                    k++;
                }
                if (k == substring.length) {
                    results.add(j);
                }
            }
            textSampleHash = ((textSampleHash - p2m * text[j]) * 2 + text[j + substring.length]) % q;
            if (textSampleHash < 0) {
                textSampleHash += q;
            }
        }
        return results;
    }

    private int gorner2Mod(int[] s, int m, int q) {
        int res = 0;
        for(int i=0; i<m; i++) {
            res = (res * 2 + s[i]) % q;
        }
        return res;
    }


    //------------------- Карпа-Рабина --------------------------
    public List<Integer> findSubstringsShiftAnd(String sText, String sSubstring) {
        List<Integer> results = new ArrayList<>();
        char[] text = sText.toCharArray();
        char[] substring = sSubstring.toCharArray();
        int nA = 256;
        int[] B = new int[nA];
        for (int j = 0; j < substring.length; j++) {
            B[substring[j]] |= 1 << (substring.length - 1 - j);
        }
        int uHigh = 1 << (substring.length - 1);
        int M = 0;

        for (int i = 0; i < text.length; i++) {
            M = (M >> 1 | uHigh) & B[text[i]];
            if ((M & 1) != 0) {
                results.add(i - substring.length + 1);
            }
        }
        return results;
    }
}
