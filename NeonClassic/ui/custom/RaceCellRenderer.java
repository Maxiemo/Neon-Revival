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
import neon.resources.RCreature;
import neon.util.ColorFactory;

public class RaceCellRenderer implements Callback<ListView<RCreature>, ListCell<RCreature>> {	
	@Override
	public ListCell<RCreature> call(ListView<RCreature> param) {
		return new RaceCell();
	}
	
	private class RaceCell extends ListCell<RCreature> {
		@Override
		public void updateItem(RCreature race, boolean empty) {
			super.updateItem(race, empty);

			if (race != null) {
				setText(race.id);	
				java.awt.Color color = ColorFactory.getColor(race.color);
				setTextFill(javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue(), 1));
			} else {
				setText(null);
			}
		}		
	}
}