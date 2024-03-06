import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of character data objects.
    HashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;

    // The random number generator used by this model.
    private Random randomGenerator;

    /**
     * Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the
     * same seed value will produce the same random texts. Good for debugging.
     */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /**
     * Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production.
     */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
      public void train(String fileName) {
        In inputFile = new In(fileName);
        String wholeFileString = "";
        wholeFileString = inputFile.readAll();
        for (int i = 0; i + windowLength < wholeFileString.length(); i++) {

            String key = wholeFileString.substring(i, i + windowLength);
            List value = CharDataMap.get(key);
            if (value != null) {
                if (value.indexOf(wholeFileString.charAt(i + windowLength)) != -1) {
                    value.update(wholeFileString.charAt(i + windowLength));

                } else {
                    value.addFirst(wholeFileString.charAt(i + windowLength));
                }
            } else {
                CharDataMap.put(key, new List());
                CharDataMap.get(key).addFirst(wholeFileString.charAt(i + windowLength));
            }
            calculateProbabilities(CharDataMap.get(key));
        }

    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list.
    public void calculateProbabilities(List probs) {
        int totalCount = 0;
        ListIterator it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData curr = it.next();
            totalCount += curr.count;
        }
        double cumulativeProbability = 0;
        it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData curr = it.next();
            curr.p = (double) curr.count / totalCount;
            cumulativeProbability += curr.p;
            curr.cp = cumulativeProbability;
        }
    }

    // Returns a random character from the given probabilities list.
    public char getRandomChar(List probs) {
        double randomValue = randomGenerator.nextDouble();
        CharData[] charDataArray = probs.toArray();
        for (CharData charData : charDataArray) {
            if (randomValue < charData.cp) {
                return charData.chr;
            }
        }
        return charDataArray[charDataArray.length - 1].chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during
     * training.
     *
     * @param initialText     - text to start with. If initialText's last substring
     *                        of size numberOfLetters
     *                        doesn't appear as a key in Map, we generate no text
     *                        and return only the initial text.
     * @param numberOfLetters - the size of text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        if (initialText.length() >= windowLength) {
            for (int i = 0; i < textLength; i++) {
                initialText += getRandomChar(
                        CharDataMap.get(initialText.substring(initialText.length() - windowLength)));
            }
        }
        return initialText;
    }

    /**
     * Returns a string representing the map of this language model.
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key).append(" : ").append(keyProbs).append("\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}