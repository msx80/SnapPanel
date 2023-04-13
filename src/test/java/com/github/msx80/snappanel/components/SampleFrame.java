package com.github.msx80.snappanel.components;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.msx80.snappanel.SnapListener;
import com.github.msx80.snappanel.SnapPanel;

public class SampleFrame extends JFrame implements SnapListener {

	private static final long serialVersionUID = 3127870749373946723L;
	private JComponent contentPane;

	public static void launch() throws Exception {
		CompletableFuture<Void> cf = new CompletableFuture<>();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(FlatLightLaf.class.getName());
					SampleFrame frame = new SampleFrame();
					frame.setVisible(true);
					
					frame.addWindowListener(new WindowAdapter()
					{
					    @Override
					    public void windowClosing(WindowEvent e)
					    {
					      cf.complete(null);
					    }
					});
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		cf.get();
	}

	/**
	 * Create the frame.
	 */
	public SampleFrame() {
        
		setTitle("Test Snap");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		contentPane.setLayout(null);

		SnapPanel panel1 = new SnapPanel();
		panel1.setLinkableTop(false);
		panel1.setLocation(100, 100);
		panel1.setOnSnap(this);
		panel1.setContent(new JLabel("This can only be at top"));
		contentPane.add(panel1);

		SnapPanel panel2 = new SnapPanel();
		panel2.setLinkableBottom(false);
		panel2.setLocation(300, 100);
		panel2.setOnSnap(this);
		panel2.setContent(new JLabel("This can only be at bottom"));
		contentPane.add(panel2);

		SnapPanel panel3 = new SnapPanel();
		panel3.setLocation(100, 170);
		panel3.setOnSnap(this);
		panel3.setContent(new SamplePanel());
		contentPane.add(panel3);

		SnapPanel panel4 = new SnapPanel();
		panel4.setLocation(200, 270);
		panel4.setOnSnap(this);
		panel4.setContent(new SamplePanel());
		contentPane.add(panel4);

		// add room for scrolling
		contentPane.setPreferredSize(new Dimension(2000, 2000));
		
		JScrollPane sp = new JScrollPane();
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setViewportView(contentPane);
		setContentPane(sp);
	}

	@Override
	public void snapped(SnapPanel top, SnapPanel bottom) {
		System.out.println("Snapped "+top+" "+bottom);
	}

	@Override
	public void unlinked(SnapPanel top, SnapPanel bottom) {
		System.out.println("Unlinked "+top+" "+bottom);
		
	}
}
