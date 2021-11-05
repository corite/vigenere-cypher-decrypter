import java.io.File
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
    val keyLength = getKeyLength(text);
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


    return 1.0/(n*(n-1)) * distinctValues.sumOf { getAbsoluteFrequency(text, it) * (getAbsoluteFrequency(text, it) - 1) }
}

fun getCharFrequencyMap(text:String) :Map<Int,Double> {
    val distinctChars = text.chars().distinct().toList()
    return distinctChars.associateWith { getRelativeFrequency(text,it) }
}

fun getRelativeFrequency(text: String, character:Int) :Double {
    val numberOfOccurrences =text.chars().filter{ it == character }.count()
    return numberOfOccurrences / text.length.toDouble()
}

fun getAbsoluteFrequency(text: String, character: Int) :Long{
    return text.chars().filter { it == character }.count()
}

fun getKeyLength(text: String) :Int{
    var lengthCoincidenceMap:MutableMap<Int,Double> = mutableMapOf()
    for (keyLength in 2..100) {
        val coincidences: MutableList<Double> = mutableListOf()
        for (remainder in 0 until keyLength) {
            val textSnippet = text.chars().toArray().filterIndexed { index, _ -> index % keyLength == remainder  }.map { it.toChar() }.joinToString("")
            coincidences += getCoincidenceIndex(textSnippet)
        }
        lengthCoincidenceMap += keyLength to coincidences.average()
    }
    val highestCoincidences = lengthCoincidenceMap.entries.sortedByDescending { it.value }.map { it.key }.take(5)
    return gcd(highestCoincidences)
}

fun gcd(aInt:Int,bInt:Int):Int {
    var a=aInt
    var b=bInt
    while (b>0) {
        val temp = b
        b = a%b
        a=temp
    }
    return a
}

fun gcd(numbers:List<Int>):Int {
    var result = numbers[0]
    for (i in 1 until numbers.size) {
        result = gcd(result,numbers[i])
    }
    return result
}
fun getKeys(text:String, keyLength:Int, mostCommonChar: Char = ' '):List<Int> {
    val encryptedTexts = getEncryptedTextsWithSameKey(text,keyLength)
    return encryptedTexts.map { findKey(it,mostCommonChar) }.toList()
}

fun getEncryptedTextsWithSameKey(text:String, keyLength:Int):List<String> {
    var textsWithSameKey:MutableList<String> = mutableListOf()
    for (i in 0 until keyLength) {
        textsWithSameKey += text.chars().toArray().filterIndexed { index, _ -> index%keyLength==i }.map { it.toChar() }.joinToString("")
    }
    return textsWithSameKey
}
fun findKey(text:String, mostFrequentChar:Char):Int {
    val charFrequencyMap = getCharFrequencyMap(text)
    val mostFrequentEncryptedChar = charFrequencyMap.entries.sortedByDescending { it.value }.map { it.key }[0]
    return (mostFrequentEncryptedChar-mostFrequentChar.code+128)%128
}
fun decrypt(text: String, keys:List<Int>):String {
    var decryptedText = String()
    for (i in text.indices) {
        var currentKey = keys[i % keys.size]
        decryptedText += decrypt(text[i],currentKey)
    }
    return decryptedText
}

fun decrypt(character: Char, key:Int):Char {
    return ((character.code-key+128)%128).toChar()
}