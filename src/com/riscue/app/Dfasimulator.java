package com.riscue.app;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

public class Dfasimulator {

	private ArrayList<Character> alphabetList = new ArrayList<Character>();
	private Set<String> finalStates = new HashSet<String>();
	private Map<String, Map<String, String>> Transition = new HashMap<String, Map<String, String>>();
	private String state;
	private int index;
	private String input;
	private WorkingArea workingPanel;
	private Component[] comps;
	private Connection lastConnection;

	public Dfasimulator(WorkingArea workingPanel) {
		this.workingPanel = workingPanel;
		comps = workingPanel.getComponents();
	}

	public boolean createMachine(String input, String alphabetRaw) {
		index = 0;
		this.input = input;
		for (int i = 0; i < alphabetRaw.length(); i++) {
			this.alphabetList.add(alphabetRaw.charAt(i));
		}
		state = findStartState();
		return state != "" && checkAlphabet() && checkOthers(alphabetRaw);
	}

	private boolean checkOthers(String alphabetRaw) {
		if (input.replaceAll("[" + alphabetRaw + "]", "").length() > 0) {
			JOptionPane.showMessageDialog(null, "There is unknown characters in input!");
			return false;
		}
		if (finalStates.size() < 1) {
			JOptionPane.showMessageDialog(null, "There is no final State in the Machine!");
			return false;
		}
		return true;
	}

	private String findStartState() {
		String temp = null;
		for (Component component : comps) {
			if (component.getClass() != RoundPanel.class)
				continue;
			if (((RoundPanel) component).first) {
				if (temp == null)
					temp = component.getName();
				else
					JOptionPane.showMessageDialog(null, "There is two many start state for the Machine!");
			}
		}
		if (temp == null)
			JOptionPane.showMessageDialog(null, "There is no start state for the Machine!");
		return temp;
	}

	private boolean checkAlphabet() {
		for (Component component : comps) {
			if (component.getClass() != RoundPanel.class)
				continue;
			if (!((RoundPanel) component).last) {
				Map<String, String> inputs = workingPanel.getPaths(component.getName());
				if (inputs == null) {
					JOptionPane.showMessageDialog(null, "There are too many same inputs for a State!");
					return false;
				} else if (inputs.size() != alphabetList.size()) {
					JOptionPane.showMessageDialog(null, "Not all paths for states filled!");
					return false;
				}
				Transition.put(component.getName(), inputs);
			} else {
				finalStates.add(component.getName());
			}
		}
		return true;
	}

	public Connection getConnection() {
		return lastConnection == null ? workingPanel.findStartConnection() : lastConnection;
	}

	public int stepMachine() {
		if (finalStates.contains(state))
			return -1;
		lastConnection = workingPanel.findConnection(state, input.charAt(index) + "");
		state = Transition.get(state).get(input.charAt(index) + "");
		index++;

		if (input.length() > index)
			return 0;

		if (finalStates.contains(state))
			return 1;
		else
			return -1;
	}

	public int getIndex() {
		return index;
	}

	public String getState() {
		return state;
	}
}