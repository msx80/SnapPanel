package com.github.msx80.snappanel.components;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

public class SamplePanel extends JPanel {

	private static final long serialVersionUID = 157864L;
	private JTextField txtSampleText;

	/**
	 * Create the panel.
	 */
	public SamplePanel() {
		setLayout(new MigLayout("", "[][grow]", "[][][]"));
		
		JLabel lblNewLabel = new JLabel("Title label");
		add(lblNewLabel, "cell 1 0");
		
		txtSampleText = new JTextField();
		txtSampleText.setText("Sample text");
		add(txtSampleText, "cell 1 1,growx");
		txtSampleText.setColumns(10);
		
		JButton btnNewButton = new JButton("Sample button");
		add(btnNewButton, "cell 1 2");

	}

}
