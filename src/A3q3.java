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
				System.out.println(line);
				if (line.charAt(0) != '>')
					currentSequence = currentSequence + line;
			}
		}
	}
	
	public double LNEXP(double lnp, double lnq) {
		if ((lnp == NEGINF) && (lnq == NEGINF)) {
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
		System.out.println("\nViterbi decoding");
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
		
		printDecode(decoded);
		
	}
	
	private void printDecode(ArrayList<DecodeUnit> decoded) {
		int counter = 1;
		for (int i = 0; i < decoded.size(); i++) {
			System.out.println(counter + "-" + ((counter += decoded.get(i).amount)-1) + " " + decoded.get(i).state);
		}
	}
	
	public void Posterior() {
		System.out.println("\nPosterior Decoding");
		double[][] f = new double[4][currentSequence.length()+1];
		double[][] b = new double[4][currentSequence.length()+1];
		
		// Forward
		// Initialization
		f[0][0] = 0;
		f[1][0] = NEGINF;
		f[2][0] = NEGINF;
		f[3][0] = NEGINF;
		
		// Recursion
		for (int i = 1; i < currentSequence.length()+1; i++) {
			for (int j = 1; j < 4; j++) {
				char currentState = IndexToChar(j).charAt(0);
				double prev_s = f[0][i-1] + Math.log(transitionTable[0][currentState]); 
				double prev_a = f[1][i-1] + Math.log(transitionTable['a'][currentState]); 
				double prev_b = f[2][i-1] + Math.log(transitionTable['b'][currentState]); 
				double prev_c = f[3][i-1] + Math.log(transitionTable['c'][currentState]); 
				double sum = LNEXP(LNEXP(LNEXP(prev_s, prev_a), prev_b), prev_c);
				f[j][i] = sum + Math.log(emissionTable[currentSequence.charAt(i-1)][currentState]);
			}
			f[0][i] = NEGINF;
		}
		
		// Termination
		
		double prob = LNEXP(LNEXP(f[1][currentSequence.length()], f[2][currentSequence.length()]), f[3][currentSequence.length()]);
		System.out.println(prob);
		
		// Backwards
		// Initialization
		
		int L = currentSequence.length();
		b[0][L] = 0;
		b[1][L] = 0;
		b[2][L] = 0;
		b[3][L] = 0;
		
		// Recursion
		
		for (int i = L-1; i > 0; i--) {
			for (int j = 1; j < 4; j++) {
				char currentState = IndexToChar(j).charAt(0);
				double prev_s = b[0][i+1] + Math.log(transitionTable[currentState][0]) + Math.log(emissionTable[currentSequence.charAt(i)][0]);
				double prev_a = b[1][i+1] + Math.log(transitionTable[currentState]['a']) + Math.log(emissionTable[currentSequence.charAt(i)]['a']);
				double prev_b = b[2][i+1] + Math.log(transitionTable[currentState]['b']) + Math.log(emissionTable[currentSequence.charAt(i)]['b']); 
				double prev_c = b[3][i+1] + Math.log(transitionTable[currentState]['c']) + Math.log(emissionTable[currentSequence.charAt(i)]['c']); 
				double sum = LNEXP(LNEXP(LNEXP(prev_s, prev_a), prev_b), prev_c);
				b[j][i] = sum;
			}
			b[0][i] = NEGINF;
		}
		
		// Termination
		// Not necessary
		
		// Posterior Decoding
		
		double pos_a = (f[1][1] + b[1][1]) - prob;
		double pos_b = (f[2][1] + b[2][1]) - prob;
		double pos_c = (f[3][1] + b[3][1]) - prob;
		double max = Math.max(Math.max(pos_a, pos_b), pos_c);
		
		int currentState = 0;
		if (pos_a == max) {
			currentState = 1;
		} else if (pos_b == max) {
			currentState = 2;
		} else if (pos_c == max) {
			currentState = 3;
		}
	
		ArrayList<DecodeUnit> decoded = new ArrayList<DecodeUnit>();
		DecodeUnit currentDecode = new DecodeUnit();
		
		currentDecode.state = IndexToString(currentState);
		currentDecode.amount++;
		for (int i = 2; i < L+1; i++) {
			
			pos_a = (f[1][i] + b[1][i]) - prob;
			pos_b = (f[2][i] + b[2][i]) - prob;
			pos_c = (f[3][i] + b[3][i]) - prob;
			
			max = Math.max(Math.max(pos_a, pos_b), pos_c);
			
			int tempState = 0;
			if (pos_a == max) {
				tempState = 1;
			} else if (pos_b == max) {
				tempState = 2;
			} else if (pos_c == max) {
				tempState = 3;
			}
			
			if (tempState == currentState) {
				currentDecode.amount++;
			} else {
				decoded.add(currentDecode);
				currentDecode = new DecodeUnit();
				currentState = tempState;
				currentDecode.state = IndexToString(currentState);
				currentDecode.amount++;
			}
		}
		decoded.add(currentDecode);
		
		printDecode(decoded);
		
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		A3q3 a3q3 = new A3q3();
		a3q3.readFile("P39758.fasta");
		a3q3.Viterbi();
		a3q3.Posterior();
		a3q3.readFile("Q0VCA5.fasta");
		a3q3.Viterbi();
		a3q3.Posterior();
	}
}
