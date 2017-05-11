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
import neon.resources.RRecipe;

public class PotionCellRenderer implements Callback<ListView<RRecipe>, ListCell<RRecipe>> {
	private String coin;
	
	public PotionCellRenderer(String coin) {
		this.coin = coin;
	}
	
	@Override
	public ListCell<RRecipe> call(ListView<RRecipe> param) {
		return new PotionCell();
	}
	
	private class PotionCell extends ListCell<RRecipe> {
		@Override
		public void updateItem(RRecipe recipe, boolean empty) {
			super.updateItem(recipe, empty);

			if (recipe != null) {
				StringBuffer text = new StringBuffer();
				for(String item : recipe.ingredients) {
					text.append(item + ", ");
				}
				setText(recipe + " (" + text + recipe.cost + " " + coin + ")");	
			} else {
				setText(null);
			}
		}		
	}
}