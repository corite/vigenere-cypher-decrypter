import java.io.File
import kotlin.streams.toList

fun main() {
    val text1 = readFile("Lorem1.txt")
    println(getKeyLength(text1))
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
    val numberOfOccurrences =text.chars().filter{ it == character }.distinct().count()
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
    println(lengthCoincidenceMap.entries.sortedByDescending { it.value })
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