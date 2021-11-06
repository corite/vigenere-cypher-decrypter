import java.io.File
import kotlin.math.absoluteValue
import kotlin.streams.toList

fun main() {
    val text1 = readFile("Lorem1.txt")
    val text2 = readFile("Lorem2.txt")
    val text3 = readFile("Lorem3.txt")

    decryptAndPrint(text1, text2, text3)

}

fun decryptAndPrint(vararg texts:String) {
    for (text in texts) {
        decryptAndPrint(text, mostCommonChar = ' ')
    }
}

fun decryptAndPrint(text:String,mostCommonChar: Char) {
    val keyLength = getKeyLength(text)
    println("key length: $keyLength")
    val keys = getKeys(text, keyLength,mostCommonChar)
    println("keys: $keys")
    val decryptedText = decrypt(text,keys)
    println("decrypted text: $decryptedText")
}



fun readFile(fileName: String) = File(fileName).readText(Charsets.US_ASCII)

fun getCoincidenceIndex( text:String) : Double {
    val n = text.length
    val distinctValues = text.chars().distinct().toList()
    //distinct characters in the text

    return 1.0/(n*(n-1)) * distinctValues.sumOf { getAbsoluteFrequency(text, it) * (getAbsoluteFrequency(text, it) - 1) }
    //simply the formula applied to the given values
}

fun getCharFrequencyMap(text:String) :Map<Int,Double> {
    val distinctChars = text.chars().distinct().toList()
    return distinctChars.associateWith { getRelativeFrequency(text,it) }
    //all chars associated with their relative frequency in the map
}

fun getRelativeFrequency(text: String, character:Int) :Double {
    val numberOfOccurrences = getAbsoluteFrequency(text,character)
    return numberOfOccurrences / text.length.toDouble()
}

fun getAbsoluteFrequency(text: String, character: Int) :Long{
    return text.chars().filter { it == character }.count()
    //how often a char is present in a text
}

fun getKeyLength(text: String) :Int{
    val lengthCoincidenceMap:MutableMap<Int,Double> = mutableMapOf()
    for (keyLength in 1..100) {
        //iterating over all possible key-lengths
        val coincidences: MutableList<Double> = mutableListOf()
        for (remainder in 0 until keyLength) {
            //for each key-length go through all the residue classes
            val textSnippet = text.chars().toArray().filterIndexed { index, _ -> index % keyLength == remainder  }.map { it.toChar() }.joinToString("")
            coincidences += getCoincidenceIndex(textSnippet)
        }
        lengthCoincidenceMap += keyLength to coincidences.average()
        //only save the average of all residue classes for one key-length
    }
    return findCorrectKeyLength(lengthCoincidenceMap.toList().sortedByDescending { it.second}.take(5).toMap())
    //only taking the top 5 for consideration (only a rule of thumb, it worked in my cases)
}
fun findCorrectKeyLength(lengthCoincidenceMap: Map<Int,Double>):Int {
    val lengthsToConsider = lengthCoincidenceMap.entries.filter { (it.value-0.06).absoluteValue < 0.015 }.map { it.key }.toList()
    //only take into account the values that have approximately the desired coincidence-index
    //this is especially important when the keys are long, because then there are not (many) multiples of the key-length present (Lorem3.txt)
    return gcd(lengthsToConsider)
    //get the greatest common divisor of the numbers in order to filter out multiples of the desired key-length
}

/**
 * returns the greatest common divisor of the numbers
 */
fun gcd(aInt:Int,bInt:Int):Int {
    var a=aInt
    var b=bInt
    //this is only done because the input parameters are static and cannot be reassigned
    while (b>0) {
        val temp = b
        b = a%b
        a=temp
    }
    return a
}

/**
 * returns the greatest common divisor of the numbers
 */
fun gcd(numbers:List<Int>):Int {
    var result = numbers[0]
    for (i in 1 until numbers.size) {
        result = gcd(result,numbers[i])
    }
    return result
}
fun getKeys(text:String, keyLength:Int, mostCommonChar: Char = ' '):List<Int> {
    val encryptedTexts = getEncryptedTextsWithSameKey(text,keyLength)
    //get all text snippets that have been encrypted with the same key
    return encryptedTexts.map { findKey(it,mostCommonChar) }.toList()
}

fun getEncryptedTextsWithSameKey(text:String, keyLength:Int):List<String> {
    val textsWithSameKey:MutableList<String> = mutableListOf()
    for (i in 0 until keyLength) {
        textsWithSameKey += text.chars().toArray().filterIndexed { index, _ -> index%keyLength==i }.map { it.toChar() }.joinToString("")
        //gets all chars from each residue class %key-length and puts them in an array of texts
    }
    return textsWithSameKey
}

/**
 * gets the key to a string that has been encrypted with one key (a simple additive cipher)
 */
fun findKey(text:String, mostFrequentChar:Char):Int {
    val charFrequencyMap = getCharFrequencyMap(text)
    val mostFrequentEncryptedChar = charFrequencyMap.entries.sortedByDescending { it.value }.map { it.key }[0]
    return (mostFrequentEncryptedChar-mostFrequentChar.code+128)%128
}

/**
 * decrypts a vigenère cipher with the given list of keys
 */
fun decrypt(text: String, keys:List<Int>):String {
    var decryptedText = String()
    for (i in text.indices) {
        val currentKey = keys[i % keys.size]
        //decrypt each char with its corresponding key in the key-set
        decryptedText += decrypt(text[i],currentKey)
    }
    return decryptedText
}

fun decrypt(character: Char, key:Int):Char {
    return ((character.code-key+128)%128).toChar()
}