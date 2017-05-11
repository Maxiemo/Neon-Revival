/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2015 - Maarten Driesen
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

package neon.ui.custom;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import neon.resources.RSpell;

public class SpellTradeCellRenderer implements Callback<ListView<RSpell>, ListCell<RSpell>> {
	private String coin;
	
	public SpellTradeCellRenderer(String coin) {
		this.coin = coin;
	}
	
	@Override
	public ListCell<RSpell> call(ListView<RSpell> param) {
		return new SpellCell();
	}
	
	private class SpellCell extends ListCell<RSpell> {
		@Override
		public void updateItem(RSpell spell, boolean empty) {
			super.updateItem(spell, empty);
			if (spell != null) {
				setText(spell.id + " (" + spell.cost + " " + coin + ")");
			} else {
				setText(null);
			}
		}		
	}
}