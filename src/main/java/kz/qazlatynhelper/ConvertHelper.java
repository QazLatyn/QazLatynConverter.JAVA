package kz.qazlatynhelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConvertHelper {
    enum Sound {
        Vowel, //Дауысты дыбыс
        Consonant, //Дауыссыз дыбыс
        Unknown //Белгісіз
    }
    private final static String[] cyrlChars = { "А", "Ә", "Ə", "Б", "В", "Г", "Ғ", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Қ", "Л", "М", "Н", "Ң", "О", "Ө", "Ɵ", "П", "Р", "С", "Т", "У", "Ұ", "Ү", "Ф", "Х", "Һ", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "І", "Ь", "Э", "Ю", "Я", "-" };
    private final static String[] latynChars = { "A", "Á", "B", "D", "E", "F", "G", "Ǵ", "H", "İ", "I", "J", "K", "L", "M", "N", "Ń", "O", "Ó", "P", "Q", "R", "S", "T", "U", "Ú", "V", "Y", "Ý", "Z", "-" };
    private final static String[] vowelChars = { "А", "Ә", "Ə", "Е", "И", "О", "Ө", "Ɵ", "Ұ", "Ү", "У", "Ы", "І", "Э" };
    private static Map<String, String> wordsPackDic = new HashMap<String, String>();
    private static Map<String, String> latyn2CyrlWordsPackDic = new HashMap<String, String>();

    static{
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();

        String words_pack_json = QApplication.readAssetsFile("words_pack.json");
        if(words_pack_json!=null && !words_pack_json.isEmpty()){
            wordsPackDic = gson.fromJson(words_pack_json, type);
        }

        String latyn2cyrl_words_pack_json = QApplication.readAssetsFile("latyn2cyrl_words_pack.json");
        if(latyn2cyrl_words_pack_json!=null && !latyn2cyrl_words_pack_json.isEmpty()){
            latyn2CyrlWordsPackDic = gson.fromJson(latyn2cyrl_words_pack_json, type);
        }
    }

    //region Сөздің бірінші әрпін үлкенге айналдыру +FirstCharToUpper(String input)
    private static String FirstCharToUpper(String input) {
        if (input == null || input.isEmpty()) return input;
        return QazLatynToUpper(input.substring(0, 1)) + input.substring(1);
    }
    //endregion

    //region Сөздерді үлкен-кіші жазылуына қарай сәйкестендіру +ConvertWord(String oldValue, String newValue)
    private static String ConvertWord(String oldValue, String newValue) {
        if (oldValue == null || oldValue.isEmpty()) return "";

        return oldValue.equals(QazLatynToUpper(oldValue)) ? QazLatynToUpper(newValue):
                oldValue.equals(FirstCharToUpper(oldValue)) ? FirstCharToUpper(newValue) : QazLatynToLower(newValue);
    }
    //endregion

    //region Жат кирлл әріптерін төл кирлларыпыне айналдыру +CopycatCyrlToOriginalCyrl(String cyrlText)
    public static String CopycatCyrlToOriginalCyrl(String cyrlText) {
        return cyrlText
                .replace("Ə", "Ә")
                .replace("ə", "ә")
                .replace("Ɵ", "Ө")
                .replace("ɵ", "ө");
    }
    //endregion

    //region Char тізбегін String тізбегіне айналдыру -CharArrayToStringArray(char[] c)
    private static String[] CharArrayToStringArray(char[] c){
        String[] s = new String[c.length];
        for(int i = 0; i < c.length; i++){
            s[i] = String.valueOf(c[i]);
        }
        return s;
    }
    //endregion
    //region Кирілді латынға айналдыру +Cyrl2Latyn(String cyrlText)
    public static String Cyrl2Latyn(String cyrlText) {
        cyrlText = CopycatCyrlToOriginalCyrl(cyrlText);
        cyrlText += ".";
        String[] chars = CharArrayToStringArray(cyrlText.toCharArray());
        int length = chars.length;
        String[] latynStrs = new String[length];
        boolean firstCharIsUpper = false;
        Sound prevSound = Sound.Unknown;
        String cyrlWord = "";
        for (int i = 0; i < length; i++) {

            if (!Arrays.asList(cyrlChars).contains(chars[i].toUpperCase())) {
                if (cyrlWord !=null && !cyrlWord.isEmpty()) {
                    int wordLength = cyrlWord.length();
                    int k = wordLength;
                    int j = (i - wordLength);
                    for (; k > 3 || (wordLength == 3 && k >= 3) || (wordLength == 2 && k >= 2); k--) {
                        String wpKey = cyrlWord.substring(0, k).toLowerCase();
                        if (wordsPackDic.containsKey(wpKey)) {
                            latynStrs[j] = ConvertWord(cyrlWord.substring(0, k), wordsPackDic.get(wpKey));
                            j += k;
                            break;
                        }
                    }
                    cyrlWord = cyrlWord.toLowerCase();
                    boolean lastIsUpper = false;
                    boolean prevIsC = false;
                    firstCharIsUpper = chars[j].toUpperCase().equals(chars[j]);
                    int lastStartIndex = j;
                    for (; j < i; j++) {
                        if (j > lastStartIndex) {
                            prevSound = Arrays.asList(vowelChars).contains(chars[j - 1].toUpperCase()) ? Sound.Vowel : Sound.Consonant;
                            prevIsC = chars[j - 1].toLowerCase() == "с";
                        }
                        if (j != length - 1 && Arrays.asList(cyrlChars).contains(chars[j + 1].toUpperCase()) && chars[j + 1] == chars[j + 1].toUpperCase()) {
                            lastIsUpper = true;
                        }
                        if (wordLength > 3) {
                            String key = chars[i - 3]+ chars[i - 2]+chars[i - 1];
                            switch (key.toLowerCase()) {
                                case "сть": { chars[i] = ""; chars[i - 1] = ""; } break;
                            }
                        }

                        if (j + 1 < length) {
                            String key = chars[j]+chars[j + 1];
                            switch (key.toLowerCase()) {
                                case "ия":
                                { latynStrs[j] = ConvertWord(key, "ıa"); j += 1; continue; }
                                case "йя":
                                { latynStrs[j] = ConvertWord(key, "ııa"); j += 1; continue; }
                                case "ию":
                                { latynStrs[j] = ConvertWord(key, "ıý"); j += 1; continue; }
                                case "йю":
                                { latynStrs[j] = ConvertWord(key, "ıý"); j += 1; continue; }
                                case "сц":
                                { latynStrs[j] = ConvertWord(key, "s"); j += 1; continue; }
                                case "тч":
                                { latynStrs[j] = ConvertWord(key, "ch"); j += 1; continue; }
                                case "ий":
                                { latynStrs[j] = ConvertWord(key, "ı"); j += 1; continue; }
                                case "ХХ": { latynStrs[j] = "ХХ"; j += 1; continue; }
                            }
                        }

                        switch (chars[j]) {
                            case "Я": { latynStrs[j] = prevSound == Sound.Consonant ? "Á" : "Ia"; } break;
                            case "я": { latynStrs[j] = prevSound == Sound.Consonant ? "á" : "ıa"; } break;
                            case "Ю": { latynStrs[j] = prevSound == Sound.Consonant ? "Ú" : "Iý"; } break;
                            case "ю": { latynStrs[j] = prevSound == Sound.Consonant ? "ú" : "ıý"; } break;
                            case "Щ": { latynStrs[j] = lastIsUpper ? "SH" : "Sh"; } break;
                            case "щ": { latynStrs[j] = "sh"; } break;
                            case "Э": { latynStrs[j] = "E"; } break;
                            case "э": { latynStrs[j] = "e"; } break;
                            case "А": { latynStrs[j] = "A"; } break;
                            case "а": { latynStrs[j] = "a"; } break;
                            case "Б": { latynStrs[j] = "B"; } break;
                            case "б": { latynStrs[j] = "b"; } break;
                            case "Ц": { latynStrs[j] = "S"; } break;
                            case "ц": { latynStrs[j] = "s"; } break;
                            case "Д": { latynStrs[j] = "D"; } break;
                            case "д": { latynStrs[j] = "d"; } break;
                            case "Е": { latynStrs[j] = "E"; } break;
                            case "е": { latynStrs[j] = "e"; } break;
                            case "Ф": { latynStrs[j] = "F"; } break;
                            case "ф": { latynStrs[j] = "f"; } break;
                            case "Г": { latynStrs[j] = "G"; } break;
                            case "г": { latynStrs[j] = "g"; } break;
                            case "Х": { latynStrs[j] = prevIsC ? "Q" : "H"; } break;
                            case "х": { latynStrs[j] = prevIsC ? "q" : "h"; } break;
                            case "Һ": { latynStrs[j] = "H"; } break;
                            case "һ": { latynStrs[j] = "h"; } break;
                            case "І": { latynStrs[j] = "İ"; } break;
                            case "і": { latynStrs[j] = "i"; } break;
                            case "И": { latynStrs[j] = "I"; } break;
                            case "и": { latynStrs[j] = "ı"; } break;
                            case "Й": { latynStrs[j] = "I"; } break;
                            case "й": { latynStrs[j] = "ı"; } break;
                            case "К": { latynStrs[j] = "K"; } break;
                            case "к": { latynStrs[j] = "k"; } break;
                            case "Л": { latynStrs[j] = "L"; } break;
                            case "л": { latynStrs[j] = "l"; } break;
                            case "М": { latynStrs[j] = "M"; } break;
                            case "м": { latynStrs[j] = "m"; } break;
                            case "Н": { latynStrs[j] = "N"; } break;
                            case "н": { latynStrs[j] = "n"; } break;
                            case "О": { latynStrs[j] = "O"; } break;
                            case "о": { latynStrs[j] = "o"; } break;
                            case "П": { latynStrs[j] = "P"; } break;
                            case "п": { latynStrs[j] = "p"; } break;
                            case "Қ": { latynStrs[j] = "Q"; } break;
                            case "қ": { latynStrs[j] = "q"; } break;
                            case "Р": { latynStrs[j] = "R"; } break;
                            case "р": { latynStrs[j] = "r"; } break;
                            case "С": { latynStrs[j] = "S"; } break;
                            case "с": { latynStrs[j] = "s"; } break;
                            case "Т": { latynStrs[j] = "T"; } break;
                            case "т": { latynStrs[j] = "t"; } break;
                            case "Ұ": { latynStrs[j] = "U"; } break;
                            case "ұ": { latynStrs[j] = "u"; } break;
                            case "В": { latynStrs[j] = "V"; } break;
                            case "в": { latynStrs[j] = "v"; } break;
                            case "У": { latynStrs[j] = "Ý"; } break;
                            case "у": { latynStrs[j] = "ý"; } break;
                            case "Ы": { latynStrs[j] = "Y"; } break;
                            case "ы": { latynStrs[j] = "y"; } break;
                            case "З": { latynStrs[j] = "Z"; } break;
                            case "з": { latynStrs[j] = "z"; } break;
                            case "Ә": { latynStrs[j] = "Á"; } break;
                            case "ә": { latynStrs[j] = "á"; } break;
                            case "Ё":
                            case "Ө": { latynStrs[j] = "Ó"; } break;
                            case "ё":
                            case "ө": { latynStrs[j] = "ó"; } break;
                            case "Ү": { latynStrs[j] = "Ú"; } break;
                            case "ү": { latynStrs[j] = "ú"; } break;
                            case "Ч": { latynStrs[j] = lastIsUpper ? "CH" : "Ch"; } break;
                            case "ч": { latynStrs[j] = "ch"; } break;
                            case "Ғ": { latynStrs[j] = "Ǵ"; } break;
                            case "ғ": { latynStrs[j] = "ǵ"; } break;
                            case "Ш": { latynStrs[j] = lastIsUpper ? "SH" : "Sh"; } break;
                            case "ш": { latynStrs[j] = "sh"; } break;
                            case "Ж": { latynStrs[j] = "J"; } break;
                            case "ж": { latynStrs[j] = "j"; } break;
                            case "Ң": { latynStrs[j] = "Ń"; } break;
                            case "ң": { latynStrs[j] = "ń"; } break;
                            case "ь": { latynStrs[j] = ""; } break;
                            case "Ь": { latynStrs[j] = ""; } break;
                            case "ъ": { latynStrs[j] = ""; } break;
                            case "Ъ": { latynStrs[j] = ""; } break;
                            case "¬": { latynStrs[j] = ""; } break;
                            default: { latynStrs[j] = chars[j] != "" ? chars[j] : ""; } break;
                        }
                    }
                    cyrlWord = "";
                }
                latynStrs[i] = chars[i];
                prevSound = Sound.Unknown;
                continue;
            }
            cyrlWord += chars[i];
            prevSound = Sound.Unknown;
        }

        latynStrs[length - 1] = "";
        return StringUtils.join(latynStrs);
    }
    //endregion

    //region Жат латын әріптерін төл латын әріпіне айналдыру +CopycatLatynToOriginalLatyn(String latynText)
    public static String CopycatLatynToOriginalLatyn(String latynText) {
        return latynText
                .replace("Á", "Á")
                .replace("á", "á")
                .replace("О́", "Ó")
                .replace("ó", "ó")
                .replace("Ú", "Ú")
                .replace("ú", "ú")
                .replace("Ń", "Ń")
                .replace("ń", "ń")
                .replace("Ǵ", "Ǵ")
                .replace("ǵ", "ǵ")
                .replace("Ý", "Ý")
                .replace("ý", "ý");
    }
    //endregion

    //region Қазақ латын әріптерін кіші әріпке айналдыру +QazLatynToLower(String latynText)
    private static String QazLatynToLower(String latynText) {
        return latynText.replace("I", "ı").replace("İ", "i").toLowerCase();
    }
    //endregion

    //region Қазақ латын әріптерін үлкен әріпке айналдыру +QazLatynToUpper(String latynText)
    private static String QazLatynToUpper(String latynText) {
        return latynText.replace("ı", "I").replace("i", "İ").toUpperCase();
    }
    //endregion

    //region Латында кирілге сәйкестендіру +Latyn2Cyrl(String latynText)

}
