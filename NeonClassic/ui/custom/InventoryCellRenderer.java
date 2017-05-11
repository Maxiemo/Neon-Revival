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

import java.util.HashMap;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import neon.core.Engine;
import neon.entities.Item;

public class InventoryCellRenderer implements Callback<ListView<Item>, ListCell<Item>> {
	HashMap<String, Integer> listData;
	
	public InventoryCellRenderer(HashMap<String, Integer> listData) {
		this.listData = listData;
	}
	
	@Override
	public ListCell<Item> call(ListView<Item> param) {
		return new InventoryCell();
	}
	
	private class InventoryCell extends ListCell<Item> {
		@Override
		public void updateItem(Item item, boolean empty) {
			super.updateItem(item, empty);
			Font font = getFont();
			
			if (item != null) {
				String text = item.getID();
				if(listData.get(text) > 1) { 
					text = text + " (" + listData.get(text) + ")";
				}
				setText(text);
			    if(Engine.getPlayer().getInventoryComponent().hasEquiped(item.getUID())) {
					setFont(Font.font(font.getName(), FontWeight.BOLD, font.getSize()));
			    } else {
			    	setFont(Font.font(font.getName(), FontWeight.NORMAL, font.getSize()));
			    }
			} else {
				setText(null);
			}
		}		
	}
}