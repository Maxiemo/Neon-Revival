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
import neon.resources.RCraft;

public class CraftCellRenderer implements Callback<ListView<RCraft>, ListCell<RCraft>> {
	private String coin;
	
	public CraftCellRenderer(String coin) {
		this.coin = coin;
	}
	
	@Override
	public ListCell<RCraft> call(ListView<RCraft> param) {
		return new CraftCell();
	}
	
	private class CraftCell extends ListCell<RCraft> {
		@Override
		public void updateItem(RCraft craft, boolean empty) {
			super.updateItem(craft, empty);
			if (craft != null) {
				setText(craft + " (" + craft.amount + " " + craft.raw  + 
		    			", " + craft.cost + " " + coin + ")");	
			} else {
				setText(null);
			}
		}		
	}
}