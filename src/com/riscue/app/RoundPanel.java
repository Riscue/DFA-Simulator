package com.riscue.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class RoundPanel extends JPanel {
	private static final long serialVersionUID = 4476086614081666611L;
	private final Color COLOR_NODE = new Color(206, 156, 255);
	private final Color COLOR_FINAL = new Color(204, 254, 204);
	public boolean first, last;
	private JLabel lblNewLabel;

	public RoundPanel(int x, int y, String name, boolean f, boolean l) {
		setLayout(null);
		setOpaque(false);
		setBounds(x, y, 50, 50);
		first = f;
		last = l;

		lblNewLabel = new JLabel(name, JLabel.CENTER);
		lblNewLabel.setBounds(0, 0, 50, 50);
		lblNewLabel.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize() + 5));
		add(lblNewLabel);
	}

	public String getName() {
		return lblNewLabel.getText();
	}

	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		graphics.setColor(last ? COLOR_FINAL : COLOR_NODE);
		graphics.fillRoundRect(0, 0, width - 1, height - 1, getHeight(), getHeight());

		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(2));
		graphics.drawRoundRect(0, 0, width - 1, height - 1, getHeight(), getHeight());
		if (last)
			graphics.drawRoundRect(4, 4, width - 9, height - 9, getHeight(), getHeight());
	}
}