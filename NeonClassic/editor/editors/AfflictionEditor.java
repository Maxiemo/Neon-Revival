/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2012 - Maarten Driesen
 * 
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neon.editor.editors;

import java.awt.event.*;
import javax.swing.*;

import java.awt.*;

import javax.swing.border.*;

import neon.editor.Editor;
import neon.editor.NeonFormat;
import neon.editor.help.HelpLabels;
import neon.magic.Effect;
import neon.resources.RSpell;

public class AfflictionEditor extends ObjectEditor implements ActionListener {
	private JTextField nameField;
	private JFormattedTextField sizeField;
	private JComboBox<Effect> effectBox;
	private JTextArea scriptArea;
	private RSpell data;
	
	public AfflictionEditor(JFrame parent, RSpell data) {
		super(parent, "Affliction: " + data.id);
		this.data = data;
		
		JPanel props = new JPanel();
		GroupLayout layout = new GroupLayout(props);
		props.setLayout(layout);
		layout.setAutoCreateGaps(true);
		props.setBorder(new TitledBorder("Properties"));
	
		JLabel nameLabel = new JLabel("Name: ");
		JLabel effectLabel = new JLabel("Effect: ");
		JLabel sizeLabel = new JLabel("Magnitude: ");
		nameField = new JTextField(15);
		effectBox = new JComboBox<Effect>(Effect.values());
		effectBox.addActionListener(this);
		sizeField = new JFormattedTextField(NeonFormat.getIntegerInstance());
		JLabel nameHelpLabel = HelpLabels.getNameHelpLabel();
		JLabel effectHelpLabel = HelpLabels.getEffectHelpLabel();
		JLabel sizeHelpLabel = HelpLabels.getSpellSizeHelpLabel();
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(nameLabel).addComponent(nameField).addComponent(nameHelpLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(effectLabel).addComponent(effectBox).addComponent(effectHelpLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(sizeLabel).addComponent(sizeField).addComponent(sizeHelpLabel))
				.addGap(10));
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(nameLabel).addComponent(effectLabel).addComponent(sizeLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(effectBox).addComponent(sizeField))
				.addGap(10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(nameHelpLabel).addComponent(effectHelpLabel).addComponent(sizeHelpLabel)));
		frame.add(props, BorderLayout.PAGE_START);
		
		scriptArea = new JTextArea(6, 0);
		scriptArea.setDisabledTextColor(Color.red);
		JScrollPane scriptScroller = new JScrollPane(scriptArea);
		scriptScroller.setBorder(new TitledBorder("Script"));
		frame.add(scriptScroller, BorderLayout.CENTER);
	}
	
	protected void load() {
		nameField.setText(data.name);
		effectBox.setSelectedItem(data.effect);
		sizeField.setValue(data.size);
		scriptArea.setText(data.script);
		scriptArea.setEditable(data.effect == Effect.SCRIPTED);
	}

	protected void save() {
		data.name = nameField.getText();
		data.size = Integer.parseInt(sizeField.getText());
		data.effect = effectBox.getItemAt(effectBox.getSelectedIndex());
		data.script = scriptArea.getText();
		data.setPath(Editor.getStore().getActive().get("id"));
	}

	public void actionPerformed(ActionEvent e) {
		scriptArea.setEditable(effectBox.getSelectedItem() == Effect.SCRIPTED);
	}
}
