import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;


public class LanguageModel {
    
    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		// Your code goes here
        String window = "";
        char c;
        In in = new In(fileName);
        for (int i = 0; i < windowLength; i++) {
            window += in.readChar();
        }
        while (!in.isEmpty()) {
            // Gets the next character
            c = in.readChar();
            // Checks if the window is already in the map
            if (CharDataMap.containsKey(window)) {
                CharDataMap.get(window).update(c);
            }
            else{
                List list = new List();
                CharDataMap.put(window, list);
                list.update(c);
            }
            window = window.substring(1) + c;
            //code: tries to get the list of this window from the map.
            //Let’s call the retrieved list “probs” (it may be null)
            //// If the window was not found in the map
            //code: the if statement described above {
            //// Creates a new empty list, and adds (window,list) to the map
            //code: Performs the action described above.
            //Let’s call the newly created list “probs”
            }
            for (List probs : CharDataMap.values())
            calculateProbabilities(probs);
            
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		// Your code goes here
        Node pointer = probs.getFirst();
        int numOfChars = 0;
        while (pointer != null) {
            numOfChars += pointer.cp.count;
            pointer = pointer.next;
        }
        pointer = probs.getFirst();
        pointer.cp.p = (double)(pointer.cp.count)/numOfChars;
        pointer.cp.cp = pointer.cp.p;
        while (pointer.next != null) {
            pointer.next.cp.p = (double)(pointer.next.cp.count)/numOfChars;
            //System.out.println(pointer.next.cp.p + " + " + pointer.cp.cp + " = " + (pointer.next.cp.p + pointer.cp.cp));
            pointer.next.cp.cp = pointer.next.cp.p + pointer.cp.cp;
            pointer = pointer.next;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double random = randomGenerator.nextDouble();
        Node pointer = probs.getFirst();
		while (pointer != null) {
            if (pointer.cp.cp >= random) {
                return pointer.cp.chr;
            }
            pointer = pointer.next;
        }
        return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength || !CharDataMap.containsKey(initialText.substring(initialText.length() - windowLength))) {
            return initialText;
        }
        String geString = initialText;
        while (geString.length() <= textLength + initialText.length() - 1) {
            List probs = CharDataMap.get(geString.substring(geString.length() - windowLength));
            geString += getRandomChar(probs);
        }
        return geString;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {

        //LanguageModel model = new LanguageModel(1); // Assuming window length is 4
        //List list = new List();
        //list.update('_');list.update('e');list.update('e');list.update('t');list.update('t');list.update('i');list.update('m');list.update('m');list.update('o');list.update('c');
        //model.calculateProbabilities(list);
        //System.out.println(list);
        //model.train("text");
        //System.out.println(model);
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