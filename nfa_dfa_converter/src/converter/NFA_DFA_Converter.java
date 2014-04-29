package converter;

import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.JFrame;

public class NFA_DFA_Converter {

	/* TreeSets that will have all the states of the DFA */

	private static TreeSet<String> states_dfa = new TreeSet<String>();
	private static TreeSet<String> control = new TreeSet<String>();

	/* Method to remove repeated states and order them in a String of states */

	protected static String no_repeat(String s) {
		return new TreeSet<String>(Arrays.asList(s.split(","))).toString()
				.replace("[", "").replaceAll("\\s+", "").replace("]", "");
	}

	/* Calculates the ECLOSE for a single symbol */

	protected static String eclose_simple(String state, String[][] enfa) {
		String eclose = state;
		String reference_1 = state;
		String reference_2 = eclose;
		for (int i = 0; i < enfa.length; i++) {
			if (enfa[i][0].contains(reference_1) && enfa[i][1].contains("q")) {
				eclose = state + "," + enfa[i][1];
				reference_1 = enfa[i][1];
				do{
					reference_2 = eclose;
					for (int j=0; j<enfa.length; j++){
						if(enfa[j][0].contains(reference_1) && enfa[j][1].contains("q")){
							eclose += "," + enfa[j][1];
							reference_1 = enfa[j][1];
						}
					}
				}while (reference_2 != eclose);
			}
		}
		return eclose;
	}

	/* C�lculo de eclose para o caso geral - m�todo de refer�ncia */

	protected static String eclose_completa(String estado, String[][] enfa) {
		String eclose = estado;
		if (!estado.contains(",")) {
			eclose = eclose_simple(eclose, enfa);
		}
		String[] dividida = eclose.split(",");
		for (int i = 0; i < dividida.length; i++) {
			if (eclose_simple(dividida[i], enfa) != ""
					&& eclose_simple(dividida[i], enfa) != null)
				eclose = eclose + "," + eclose_simple(dividida[i], enfa);
		}
		return no_repeat(eclose);
	}

	/* Para descobrir o estado inicial de um NFA */

	protected static String estado_inicial(String[][] enfa) {
		String com_seta = "";
		for (int i = 0; i < enfa.length; i++) {
			if (enfa[i][0].contains("-->")) {
				if (com_seta == "")
					com_seta = enfa[i][0].replace("-->", "");
				else
					com_seta = com_seta + "," + enfa[i][0].replace("-->", "");
			}
		}
		return com_seta;
	}

	/* Para descobrir o estado final de um NFA */

	protected static String estado_final(String[][] enfa) {
		String com_asterisco = "";
		for (int i = 0; i < enfa.length; i++) {
			if (enfa[i][0].contains("*")) {
				if (com_asterisco == "")
					com_asterisco = enfa[i][0].replace("*", "");
				else
					com_asterisco = com_asterisco + ","
							+ enfa[i][0].replace("*", "");
			}
		}
		return com_asterisco;
	}

	/* Dada uma string, verificar ent�o se ela � o in�cio de um DFA ou n�o */

	protected static boolean eh_dfa_inicial(String candidata, String[][] enfa) {
		String inicio = estado_inicial(enfa);
		boolean possui_seta = false;
		if (candidata.contains(",")) {
			String[] dividida = candidata.split(",");
			for (int i = 0; i < dividida.length; i++) {
				if (inicio.contains(dividida[i]))
					possui_seta = true;
			}
		} else {
			if (inicio.contains(candidata))
				possui_seta = true;
		}
		return possui_seta;
	}

	/* Dada uma string, verificar ent�o se ela � o final de um DFA ou n�o */

	protected static boolean eh_dfa_final(String candidata, String[][] enfa) {
		String fim = estado_final(enfa);
		boolean possui_asterisco = false;
		if (candidata.contains(",")) {
			String[] dividida = candidata.split(",");
			for (int i = 0; i < dividida.length; i++) {
				if (fim.contains(dividida[i]))
					possui_asterisco = true;
			}
		} else {
			if (fim.contains(candidata))
				possui_asterisco = true;
		}
		return possui_asterisco;
	}

	/*
	 * Para um dado valor na primeira coluna, acrescentar "-->" se for inicial
	 * ou "*" se for final
	 */

	protected static String primeira_coluna(String em_questao, String[][] enfa) {
		if (eh_dfa_inicial(em_questao, enfa)){
			if (eh_dfa_final(em_questao, enfa))
				return "-->*" + em_questao;
			else
				return "-->" + em_questao;
		}
		else {
			if (eh_dfa_final(em_questao, enfa))
				return "*" + em_questao;
			else
				return em_questao;
		}
	}

	/*
	 * Determinar o conjunto de estados atingidos numa transi��o de um
	 * determinado s�mbolo
	 */

	protected static String destino(String origem, int posicao, String[][] enfa) {
		String resultante = "";
		String eclose = eclose_completa(origem, enfa);
		String[] dividida = eclose.split(",");
		for (String estado : dividida) {
			for (int i = 0; i < enfa.length; i++) {
				if (enfa[i][0].contains(estado)) {
					if (enfa[i][posicao].contains("q")) {
						if (!resultante.contains("q"))
							resultante = enfa[i][posicao];
						else
							resultante += "," + enfa[i][posicao];
					}
				}
			}
		}
		if (!resultante.startsWith("q"))
			return "";
		else {
			String proxima = eclose_completa(resultante, enfa);
			if (!states_dfa.contains(proxima))
				states_dfa.add(proxima);
			return proxima;
		}
	}

	/*
	 * Determina os estados atingidos por uma determinada transi��o
	 * iterativamente
	 */

	protected static void destinos(String estados, String[][] enfa) {
		if (!states_dfa.contains(estados))
			states_dfa.add(estados);
		control.add(estados);
		for (int j = 2; j < enfa[0].length; j++) {
			String destino = destino(estados, j, enfa);
			if (destino != "" && destino != null
					&& (!control.contains(destino)))
				destinos(destino, enfa);
		}
	}

	/* In�cio da determina��o acima, partindo do estado inicial da DFA */

	protected static void estados_da_dfa(String[][] enfa) {
		String primeiro_estado = eclose_completa(estado_inicial(enfa), enfa);
		destinos(primeiro_estado, enfa);
	}

	/* Constru��o efetiva do DFA a partir de um NFA */

	protected static String[][] dfa(String[][] enfa) {
		estados_da_dfa(enfa);
		int linha = 0;
		String[][] dfa = new String[states_dfa.size()][enfa[0].length - 1];
		for (String s : states_dfa) {
			dfa[linha][0] = primeira_coluna(s, enfa);
			for (int j = 2; j < enfa[0].length; j++)
				dfa[linha][j - 1] = destino(s, j, enfa);
			linha++;
		}
		return dfa;
	}

	protected static String[][] nfa1 = { { "-->q0", "q1", "q1", "", "" },
			{ "q1", "", "", "q2", "q1,q4" }, { "q2", "", "", "", "q3" },
			{ "q3", "q5", "", "", "q3" }, { "q4", "", "", "q3", "" },
			{ "*q5", "      ", "", "", "" } };

	protected static String[][] nfa2 = { { "-->q0", "q1", "q0", "", "" },
			{ "q1", "q2", "", "q1", "" }, { "*q2", "", "", "", "q2" } };

	protected static String[][] nfa3 = {
			{ "-->q0", "q1", "q2", "q3", "q1,q4" },
			{ "q1", "q2", "", "q3", "" }, { "q2", "", "", "q3,q4", "q2" },
			{ "q3", "q1", "", "q4", "" }, { "*q4", "", "", "", "" } };

	protected static String[] nome_coluna1 = { "Estado", "�psilon", "a", "b",
			"c" };
	protected static String[] nome_coluna2 = { "Estado", "a", "b", "c" };

	protected static Transition_Table gui1 = new Transition_Table(nome_coluna1, nfa1);       //nfa1, nfa2, nfa3
	protected static Transition_Table gui2 = new Transition_Table(nome_coluna2, dfa(nfa1));  //nfa1, nfa2, nfa3

	public static void main(String args[]) {
		gui1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui1.setSize(600, 180);
		gui1.setVisible(true);
		gui1.setTitle("Tabela de transi��es do �psilon-NFA 3");  //1, 2, 3
		gui2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui2.setSize(600, 180);
		gui2.setVisible(true);
		gui2.setTitle("Tabela de transi��es do DFA 3");   //1, 2, 3

	}

}