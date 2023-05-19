package org.pytorch.helloworld

import java.io.File
import java.util.*

class Tokenizer(filename: String?) {
    init {
        tokenTool = TokenTool()
        val file = filename?.let { File(it) }
        val s = Scanner(file)
        encode_map = HashMap()
        merges = ArrayList()
        vocab = ArrayList()
        encoder = HashMap()
        bpe_ranks = HashMap()
        cache = HashMap()
        // Read the file and add each line as a vocabulary item
        while (s.hasNext()) {
            vocab.add(s.nextLine())
        }

        // Create an encoder map with each vocabulary item as a key and its index as a value
        for (i in vocab.indices) {
            encoder[vocab[i]] = i
        }
    }

    // Generate vocabulary by adding special tokens to the existing vocabulary
    fun Generate_vocab() {
        val t_vocab: List<Char> = ArrayList(encode_map.values)
        val tt_vocab: MutableList<String> = ArrayList()

        // Add </w> to each vocabulary item and add them to the vocabulary list
        for (i in t_vocab) {
            vocab.add(i.toString())
        }
        for (i in vocab) {
            tt_vocab.add("$i</w>")
        }
        vocab.addAll(tt_vocab)

        // Add each merge to the vocabulary list
        for (i in merges) {
            var t = String()
            for (j in i) {
                t += j
            }
            vocab.add(t)
        }
        vocab.add("<|startoftext|>")
        vocab.add("<|endoftext|>")
    }

    companion object {
        // Initialize necessary data structures as static variables
        var tokenTool: TokenTool = TokenTool()
        var encode_map: Map<Int, Char> = HashMap()
        var merges: List<Array<String>> = ArrayList()
        var vocab: MutableList<String> = ArrayList()
        var encoder: MutableMap<String, Int> = HashMap()
        var bpe_ranks: Map<Array<String>, Int> = HashMap()
        var cache: Map<String, String> = HashMap()

        // Encode a given text by converting it into BPE tokens
        fun encode(text: String): IntArray {
            var text = text

            // Clean the text and convert it to lowercase
            text = tokenTool.whitespace_clean(text).toLowerCase()

            // Split the text into individual tokens and convert each token to a BPE token
            val texts = text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val bpe_token = IntArray(texts.size)
            for (i in texts.indices) {
                val t_token = encoder[texts[i] + "</w>"]!!
                bpe_token[i] = t_token
            }
            return bpe_token
        }
    }
}

class TokenTool {
    // Converts bytes to unicode characters using a mapping of byte codes to unicode characters
    fun bytes_to_unicode(): Map<Int, Char> {
        val encode_map: MutableMap<Int, Char> = HashMap()
        val bs: MutableList<Int> = ArrayList()
        // Add all the printable ASCII characters to the byte list
        for (i in '!'.toInt()..'~'.toInt()) {
            bs.add(i)
        }
        // Add all the extended ASCII characters to the byte list
        for (i in '¡'.toInt()..'¬'.toInt()) {
            bs.add(i)
        }
        for (i in '®'.toInt()..'ÿ'.toInt()) {
            bs.add(i)
        }
        // Add non-printable ASCII characters to the byte list
        val cs: MutableList<Int> = ArrayList()
        for (i in bs) {
            cs.add(i)
        }
        var n = 0
        // Add other byte codes that are not in the byte list to the byte list and their corresponding unicode characters to the character list
        for (i in 0..255) {
            if (!bs.contains(i)) {
                bs.add(i)
                cs.add(256 + n)
                n += 1
            }
        }
        // Convert character codes to unicode characters and store the byte to unicode character mapping
        val Cs: MutableList<Char> = ArrayList()
        for (i in cs.indices) {
            Cs.add(cs[i].toChar())
        }
        for (i in Cs.indices) {
            encode_map[bs[i]] = Cs[i]
        }
        return encode_map
    }

    // Cleans the input text by removing extra whitespace characters and replacing multiple spaces with a single space
    fun whitespace_clean(text: String): String {
        // Replace multiple whitespace characters with a single space
        text.replace("\\s+".toRegex(), " ")
        return text
    }
}
