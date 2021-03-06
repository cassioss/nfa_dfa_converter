package converter;

import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public  class Transition_Table extends JFrame {
	private JTable tabela;

	public Transition_Table(String[] columns_names, String[][] data) {
		setLayout(new FlowLayout());
		tabela = new JTable(data, columns_names);
		tabela.setFillsViewportHeight(true);

		JScrollPane scrollPane = new JScrollPane(tabela);
		add(scrollPane);
	}
}