import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class A3q3 {
	
	class DecodeUnit
	{
		public String state;
	    public int    amount; 
	    public DecodeUnit() {
	    	state = "";
	    	amount = 0;
		}
	};
	
	static double[][] transitionTable = new double[256][256];
	static double[][] emissionTable = new double[256][256];
	
	final static Charset ENCODING = StandardCharsets.US_ASCII;
	final static String delims = "[ ]+";
	
	final static double NEGINF = Double.NEGATIVE_INFINITY / 2.0;
	
	String currentSequence;
	
	public A3q3 () throws IOException {
		transitionTable[0]['a'] = (double)1/3;
		transitionTable[0]['b'] = (double)1/3;
		transitionTable[0]['c'] = (double)1/3;
		transitionTable[0][0] = 0;
		transitionTable['a']['a'] = 0.9;
		transitionTable['a']['c'] = 0.1;
		transitionTable['a']['b'] = 0;
		transitionTable['b']['b'] = 0.89;
		transitionTable['b']['a'] = 0;
		transitionTable['b']['c'] = 0.11;
		transitionTable['c']['a'] = 0.1;
		transitionTable['c']['b'] = 0.11;
		transitionTable['c']['c'] = 0.79;

		initEmission("emission.txt");
	}
	
	private void initEmission(String matrix_name) throws IOException {
		
		for (int i=0; i<256; i++)
			for (int j=0; j<256; j++)
				emissionTable[i][j] = 0;
		Path path = Paths.get(matrix_name);
		try (Scanner scanner = new Scanner(path,ENCODING.name())) {
			String line;
			do {
				line = scanner.nextLine();
			} while (line.charAt(0)=='#');
			String[] letters = line.split(delims);
			int map[];
			map = new int[4];
			for (int j=1; j<=3; j++) {
				map[j] = letters[j].charAt(0);
				emissionTable[0][map[j]] = 1;
			}
			for (int i=0; i<20; i++) {
				String[] scores = scanner.nextLine().split(delims);
				int offset = 0;
				while (scores[offset].isEmpty())
					offset++;
				int row = scores[offset].charAt(0);
				for (int j=1; j<=3; j++) {
					emissionTable[row][map[j]] = Double.parseDouble(scores[offset+j]);
				}
			}
		}
	}
	
	public void readFile(String file_name) throws IOException {
		currentSequence = "";
		Path path = Paths.get(file_name);
		try (Scanner scanner = new Scanner(path,ENCODING.name())) {
			
			String line = "";
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				currentSequence = currentSequence + line;
			}
		}
	}
	
	public double LNEXP(double lnp, double lnq) {
		if ((lnp == -NEGINF) && (lnq == -NEGINF)) {
			return NEGINF;
		} else if (lnp < lnq) {
			return lnq + Math.log(1.0 + Math.exp(lnp-lnq));
		} else {
			return lnp + Math.log(1.0 + Math.exp(lnq-lnp));
		}
	}
	
	public String IndexToChar(int index) {
		switch (index) {
		case 1: 
			return "a";
		case 2:
			return "b";
		case 3:
			return "c";
		}
		return "";
	}
	
	public String IndexToString(int index) {
		switch (index) {
		case 1: 
			return "helix";
		case 2:
			return "strand";
		case 3:
			return "coil";
		}
		return "";
	}
	
	public void Viterbi() {
		double[][] v = new double[4][currentSequence.length()+1];
		int[][] ptr = new int[3][currentSequence.length()+1];
		
		// initialization
		v[0][0] = 0;
		v[1][0] = NEGINF;
		v[2][0] = NEGINF;
		v[3][0] = NEGINF;
		
		// Recursion
		for (int i = 1; i < currentSequence.length()+1; i++) {

			for (int j = 1; j < 4; j++) {
				String currentState = "";
				currentState = IndexToChar(j);
				
				double prev_s = v[0][i-1] +  Math.log(transitionTable[0][currentState.charAt(0)]);
				double prev_a = v[1][i-1] + Math.log(transitionTable['a'][currentState.charAt(0)]);
				double prev_b = v[2][i-1] + Math.log(transitionTable['b'][currentState.charAt(0)]);
				double prev_c = v[3][i-1] + Math.log(transitionTable['c'][currentState.charAt(0)]);
				double max = Math.max(Math.max(Math.max(prev_s, prev_a), prev_b), prev_c);
				
				if (prev_s == max) {
					ptr[j-1][i-1] = 0;
					v[j][i] = prev_s + Math.log(emissionTable[currentSequence.charAt(i-1)][currentState.charAt(0)]);
				} else if (prev_a == max) {
					ptr[j-1][i-1] = 1;
					v[j][i] = prev_a + Math.log(emissionTable[currentSequence.charAt(i-1)][currentState.charAt(0)]);
				} else if (prev_b == max) {
					ptr[j-1][i-1] = 2;
					v[j][i] = prev_b + Math.log(emissionTable[currentSequence.charAt(i-1)][currentState.charAt(0)]);
				} else if (prev_c == max) {
					ptr[j-1][i-1] = 3;
					v[j][i] = prev_c + Math.log(emissionTable[currentSequence.charAt(i-1)][currentState.charAt(0)]);
				}
				v[0][i] = NEGINF;
			}
		}
		
		// Termination
		double prev_a = v[1][currentSequence.length()];
		double prev_b = v[2][currentSequence.length()];
		double prev_c = v[3][currentSequence.length()];
		double max = Math.max(Math.max(prev_a, prev_b), prev_c);
		
		int endState = 0;
		if (prev_a == max) {
			ptr[0][currentSequence.length()] = 1;
			endState = 1;
		} else if (prev_b == max) {
			ptr[1][currentSequence.length()] = 2;
			endState = 2;
		} else if (prev_c == max) {
			ptr[2][currentSequence.length()] = 3;
			endState = 3;
		}
		
		System.out.println(max);
		
		// traceback...
		ArrayList<DecodeUnit> decoded = new ArrayList<DecodeUnit>();
		DecodeUnit currentDecode = new DecodeUnit();
		int currentState = endState;
		currentDecode.state = IndexToString(currentState);
		currentDecode.amount++;
		for (int i = currentSequence.length()-1; i > 0; i--) {
			if (ptr[currentState-1][i] == currentState) {
				currentDecode.amount++;
			} else {
				decoded.add(0, currentDecode);
				currentDecode = new DecodeUnit();
				currentState = ptr[currentState-1][i];
				currentDecode.state = IndexToString(currentState);
				currentDecode.amount++;
			}
		}
		decoded.add(0, currentDecode);
		
		System.out.println("Viterbi decoding");
		printDecode(decoded);
		
	}
	
	private void printDecode(ArrayList<DecodeUnit> decoded) {
		int counter = 1;
		for (int i = 0; i < decoded.size(); i++) {
			System.out.println(counter + "-" + ((counter += decoded.get(i).amount)-1) + " " + decoded.get(i).state);
		}
	}
	
	public void Posterior() {
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		A3q3 a3q3 = new A3q3();
		a3q3.readFile("P39758");
		a3q3.Viterbi();
		a3q3.Posterior();
	}
}
