package com.riscue.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Window extends JFrame {
	private static final long serialVersionUID = 8255319694373975038L;
	private static final int VERTICAL_OFFSET = 39;
	private static final int HORIZONTAL_OFFSET = 16;
	private static final int TOOLBOX_WIDTH = 202;
	private static final int TOOLBOX_HEIGHT = 52;
	private static final int DFADETAILS_HEIGHT = 70;
	private final Color COLOR_STATE = new Color(206, 156, 255);
	private final Color COLOR_FINAL = new Color(204, 254, 204);
	private JPanel panel = new JPanel(), toolboxPanel, dfaPanel, toolboxCover;
	private WorkingArea workingPanel;
	private JLabel addStateButton, addLineButton, deleteLineButton, clearScreenButton;
	private JButton dfaStep, dfaGo;
	private JTextField dfaInput;
	private Dfasimulator dfaSimulator;
	private JLabel dfaProgress;
	private String alphabetRaw;
	private JButton dfaSolve;
	private JButton dfaCancel;
	private Timer animationTimer = new Timer(1000, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Step();
		}
	});
	private JButton dfaRandom;

	public Window() {
		setTitle("DFA Simulator v1.0 || Riscue™");
		setSize(800, 600);
		setLocationRelativeTo(null);
		setLayout(null);
		setVisible(true);
		setContentPane(panel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		panel.setLayout(null);
		Initialize();

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				resizeWindow();
			}
		});
	}

	private void Initialize() {
		askForTheAlphabet();
		createDFADetailsArea();
		createWorkingArea();
		createToolBox();
		resizeWindow();

		Example(0, alphabetRaw.length());
		revalidate();
		repaint();
	}

	private void askForTheAlphabet() {
		JTextField raw = new JTextField("01");
		while (JOptionPane.showConfirmDialog(null,
				new JComponent[] {
						new JLabel("<html>List your alphabet's characters<br>without any white space:</html>"), raw },
				"Alphabet", JOptionPane.PLAIN_MESSAGE) == -1 || raw.getText().length() < 2) {
		}
		alphabetRaw = raw.getText();

		String tempStr = "";
		while (raw.getText().length() > 0) {
			tempStr += raw.getText().charAt(0);
			raw.setText(raw.getText().replaceAll("[" + raw.getText().charAt(0) + "]", ""));
		}

		if (alphabetRaw.length() != tempStr.length())
			askForTheAlphabet();
	}

	private void Example(int i, int j) {
		switch (i) {
		case 0:
			workingPanel.addState(150, 250, "q", true, false);
			workingPanel.addState(450, 100, "r", false, false);
			workingPanel.addState(650, 100, "s", false, true);
			workingPanel.addConnection("q");

			for (int k = 1; k < j; k++) {
				workingPanel.addConnection("q", "r", Color.BLACK, alphabetRaw.charAt(k) + "", false);
				workingPanel.addConnection("r", "s", Color.BLACK, alphabetRaw.charAt(k) + "", true);
			}

			workingPanel.addConnection("q", "r", Color.BLACK, alphabetRaw.charAt(0) + "", false);
			workingPanel.addConnection("r", "q", Color.BLACK, alphabetRaw.charAt(0) + "", false);
			break;

		default:
			break;
		}
	}

	private void addState() {
		if (!dfaInput.isVisible())
			return;
		JTextField name = new JTextField("D" + workingPanel.getComponentCount());
		JCheckBox isFirst = new JCheckBox("Start State");
		JCheckBox isLast = new JCheckBox("Stop State");
		JComponent[] inputs = new JComponent[] { new JLabel("State name:"), name, isFirst, isLast };
		if (JOptionPane.showConfirmDialog(null, inputs, "Add New State", JOptionPane.PLAIN_MESSAGE) == -1)
			return;

		if (workingPanel.findState(name.getText()) == null) {
			workingPanel.addState(10, 10, name.getText(), isFirst.isSelected(), isLast.isSelected());
			if (isFirst.isSelected())
				workingPanel.addConnection(name.getText());
			repaint();
			revalidate();
		} else {
			JOptionPane.showMessageDialog(null, "There is a State with same name!");
		}
	}

	private void addLine() {
		if (!dfaInput.isVisible())
			return;
		Component[] comps = workingPanel.getComponents();
		JComboBox<String> sState = new JComboBox<>();
		JComboBox<String> dState = new JComboBox<>();

		for (Component component : comps) {
			if (component.getClass() != RoundPanel.class)
				continue;
			sState.addItem(((RoundPanel) component).getName());
			dState.addItem(((RoundPanel) component).getName());
		}

		JComboBox<String> inputsCombo = new JComboBox<>();
		for (int i = 0; i < alphabetRaw.length(); i++) {
			inputsCombo.addItem(alphabetRaw.charAt(i) + "");
		}

		JRadioButton curve = new JRadioButton("Curve", true);
		JRadioButton straight = new JRadioButton("Straight");
		ButtonGroup RadioButtons = new ButtonGroup();
		RadioButtons.add(curve);
		RadioButtons.add(straight);
		JComponent[] inputs = new JComponent[] { new JLabel("Source State:"), sState, new JLabel("Destination State:"),
				dState, new JLabel("Input:"), inputsCombo, curve, straight };
		if (JOptionPane.showConfirmDialog(null, inputs, "Add New Line", JOptionPane.PLAIN_MESSAGE) == -1)
			return;

		if (dState.getSelectedIndex() == -1 || sState.getSelectedIndex() == -1 || inputsCombo.getSelectedIndex() == -1)
			return;

		if (sState.getSelectedItem() == dState.getSelectedItem() && straight.isSelected()) {
			JOptionPane.showMessageDialog(null, "Inpossible to link itself with straight line!");
			return;
		}

		if (workingPanel.findConnection(sState.getSelectedItem().toString(), dState.getSelectedItem().toString(),
				inputsCombo.getSelectedItem().toString()))
			JOptionPane.showMessageDialog(null, "There is a connection between those node with same input!");
		else
			workingPanel.addConnection(sState.getSelectedItem().toString(), dState.getSelectedItem().toString(),
					Color.BLACK, inputsCombo.getSelectedItem().toString(), straight.isSelected());

		repaint();
		revalidate();
	}

	private void removeLine() {
		if (!dfaInput.isVisible())
			return;
		Component[] comps = workingPanel.getComponents();
		JComboBox<String> sState = new JComboBox<>();
		JComboBox<String> dState = new JComboBox<>();

		for (Component component : comps) {
			if (component.getClass() != RoundPanel.class)
				continue;
			sState.addItem(((RoundPanel) component).getName());
			dState.addItem(((RoundPanel) component).getName());
		}

		JComboBox<String> inputsCombo = new JComboBox<>();
		for (int i = 0; i < alphabetRaw.length(); i++) {
			inputsCombo.addItem(alphabetRaw.charAt(i) + "");
		}

		JComponent[] inputs = new JComponent[] { new JLabel("Source State:"), sState, new JLabel("Destination State:"),
				dState, new JLabel("Input:"), inputsCombo };
		if (JOptionPane.showConfirmDialog(null, inputs, "Remove Line", JOptionPane.PLAIN_MESSAGE) == -1)
			return;

		if (sState.getSelectedIndex() == -1 || dState.getSelectedIndex() == -1)
			return;

		workingPanel.removeConnection(sState.getSelectedItem().toString(), dState.getSelectedItem().toString(),
				inputsCombo.getSelectedItem().toString());

		repaint();
		revalidate();
	}

	private void clearScreen() {
		if (!dfaInput.isVisible())
			return;
		workingPanel.removeAll();
		repaint();
		revalidate();
	}

	private void createToolBox() {
		toolboxPanel = new JPanel();
		toolboxPanel.setLayout(null);
		toolboxPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dfaPanel.add(toolboxPanel);

		toolboxCover = new JPanel();
		toolboxCover.setVisible(false);
		toolboxCover.setBackground(new Color(0, 0, 0, 100));
		dfaPanel.add(toolboxCover);
		dfaPanel.setComponentZOrder(toolboxCover, 0);

		addStateButton = new JLabel() {
			private static final long serialVersionUID = 5678227005419911653L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				graphics.setColor(COLOR_STATE);
				graphics.fillRoundRect(8, 8, getWidth() - 17, getHeight() - 17, getHeight(), getHeight());

				graphics.setColor(Color.BLACK);
				graphics.setStroke(new BasicStroke(2));
				graphics.drawRoundRect(8, 8, getWidth() - 17, getHeight() - 17, getHeight(), getHeight());
			}
		};
		addStateButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				addState();
			}
		});
		addStateButton.setOpaque(true);
		addStateButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		addStateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addStateButton.setBounds(1, 1, 50, 50);
		addStateButton.setBackground(COLOR_FINAL);
		toolboxPanel.add(addStateButton);

		addLineButton = new JLabel() {
			private static final long serialVersionUID = 2402010750578964861L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				graphics.setColor(Color.BLACK);
				graphics.setStroke(new BasicStroke(2));
				graphics.drawLine(10, 40, 40, 10);
			}
		};
		addLineButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				addLine();
			}
		});
		addLineButton.setOpaque(true);
		addLineButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		addLineButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addLineButton.setBounds(51, 1, 50, 50);
		addLineButton.setBackground(COLOR_FINAL);
		toolboxPanel.add(addLineButton);

		deleteLineButton = new JLabel() {
			private static final long serialVersionUID = 6937838232629992887L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				graphics.setColor(Color.BLACK);
				graphics.setStroke(new BasicStroke(2));
				graphics.drawLine(10, 40, 40, 10);

				graphics.setColor(Color.RED);
				graphics.setStroke(new BasicStroke(1));
				graphics.drawLine(22, 15, 28, 35);
				graphics.drawLine(28, 15, 22, 35);
			}
		};
		deleteLineButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				removeLine();
			}
		});
		deleteLineButton.setOpaque(true);
		deleteLineButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		deleteLineButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		deleteLineButton.setBounds(101, 1, 50, 50);
		deleteLineButton.setBackground(COLOR_FINAL);
		toolboxPanel.add(deleteLineButton);

		clearScreenButton = new JLabel() {
			private static final long serialVersionUID = 6104004126001226177L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				graphics.setColor(Color.RED);
				graphics.setStroke(new BasicStroke(5));
				graphics.drawLine(35, 35, 15, 15);
				graphics.drawLine(15, 35, 35, 15);
			}
		};
		clearScreenButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clearScreen();
			}
		});
		clearScreenButton.setOpaque(true);
		clearScreenButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		clearScreenButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		clearScreenButton.setBounds(151, 1, 50, 50);
		clearScreenButton.setBackground(COLOR_FINAL);
		toolboxPanel.add(clearScreenButton);
	}

	private void createWorkingArea() {
		workingPanel = new WorkingArea();
		workingPanel.setLayout(null);
		workingPanel.setBackground(Color.ORANGE);
		workingPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.add(workingPanel);
	}

	private void createDFADetailsArea() {
		dfaPanel = new JPanel();
		dfaPanel.setLayout(null);
		dfaPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.add(dfaPanel);

		JLabel dfaString = new JLabel("Input String:");
		dfaString.setBounds(10, 5, 150, 30);
		dfaPanel.add(dfaString);

		dfaInput = new JTextField("000010101001");
		dfaInput.setBounds(10, 31, 150, 30);
		dfaPanel.add(dfaInput);

		dfaSolve = new JButton("Solve");
		dfaSolve.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Solve();
			}
		});
		dfaSolve.setBounds(170, 5, 70, 25);
		dfaPanel.add(dfaSolve);

		dfaGo = new JButton("GO");
		dfaGo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Go();
			}
		});
		dfaGo.setBounds(170, 35, 70, 25);
		dfaPanel.add(dfaGo);

		dfaCancel = new JButton("Cancel");
		dfaCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				unlockScreen();
			}
		});
		dfaCancel.setBounds(250, 5, 75, 25);
		dfaPanel.add(dfaCancel);

		dfaStep = new JButton("Step");
		dfaStep.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Step();
			}
		});
		dfaStep.setBounds(250, 35, 75, 25);
		dfaPanel.add(dfaStep);

		dfaRandom = new JButton("Random");
		dfaRandom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				randomInput();
			}
		});
		dfaRandom.setBounds(79, 11, 81, 18);
		dfaPanel.add(dfaRandom);

		dfaProgress = new JLabel("", JLabel.CENTER);
		dfaProgress.setVisible(false);
		dfaProgress.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dfaProgress.setBounds(10, 31, 150, 30);
		dfaPanel.add(dfaProgress);
	}

	private void randomInput() {
		Random rand = new Random();
		String str = "";
		for (int i = 0; i < rand.nextInt(5) + 5; i++) {
			str += alphabetRaw.charAt(rand.nextInt(alphabetRaw.length()));
		}
		dfaInput.setText(str);
	}

	protected void Solve() {
		while (!Step()) {
		}
	}

	protected void Go() {
		dfaSolve.setEnabled(false);
		dfaGo.setEnabled(false);
		dfaStep.setEnabled(false);
		lockScreen();

		Step();

		animationTimer.start();
	}

	protected boolean Step() {
		if (dfaSimulator == null) {
			dfaSimulator = new Dfasimulator(workingPanel);
			if (!dfaSimulator.createMachine(dfaInput.getText(), alphabetRaw)) {
				dfaSimulator = null;
				return true;
			}
			lockScreen();
			drawStep();
			dfaProgress.setVisible(true);
			dfaInput.setVisible(false);
		} else {

			switch (dfaSimulator.stepMachine()) {
			case -1:
				drawStep();
				JOptionPane.showMessageDialog(null, "DFA reject given input!");

				unlockScreen();
				return true;
			case 0:
				drawStep();
				return false;
			case 1:
				drawStep();
				JOptionPane.showMessageDialog(null, "DFA completed successfully!");

				unlockScreen();
				return true;
			}
		}
		return false;
	}

	private void drawStep() {
		int index = dfaSimulator.getIndex();
		String progress = dfaInput.getText().substring(0, index) + "-"
				+ dfaInput.getText().substring(index, dfaInput.getText().length());
		dfaProgress.setText(progress);

		String state = dfaSimulator.getState();
		workingPanel.setSelectedState(state);

		Connection connection = dfaSimulator.getConnection();
		workingPanel.setSelectedConnection(connection);

		repaint();
	}

	private void lockScreen() {
		workingPanel.unlockScreen();

		toolboxCover.setVisible(true);

		dfaInput.setVisible(false);
		dfaProgress.setVisible(true);
		dfaRandom.setEnabled(false);
	}

	private void unlockScreen() {
		workingPanel.unlockScreen();

		toolboxCover.setVisible(false);

		dfaSimulator = null;

		dfaInput.setVisible(true);
		dfaProgress.setVisible(false);

		dfaGo.setEnabled(true);
		dfaSolve.setEnabled(true);
		dfaStep.setEnabled(true);
		dfaRandom.setEnabled(true);

		animationTimer.stop();

		repaint();
	}

	private void resizeWindow() {
		dfaPanel.setBounds(0, 0, getWidth() - HORIZONTAL_OFFSET, DFADETAILS_HEIGHT);
		toolboxPanel.setBounds(dfaPanel.getWidth() - TOOLBOX_WIDTH - 10, 10, TOOLBOX_WIDTH, TOOLBOX_HEIGHT);
		toolboxCover.setBounds(dfaPanel.getWidth() - TOOLBOX_WIDTH - 10, 10, TOOLBOX_WIDTH, TOOLBOX_HEIGHT);
		workingPanel.setBounds(0, dfaPanel.getHeight(), getWidth() - HORIZONTAL_OFFSET,
				getHeight() - dfaPanel.getHeight() - VERTICAL_OFFSET);
	}

}