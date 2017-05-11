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
import neon.resources.quest.Topic;
import neon.ui.states.DialogState;

public class TopicCellRenderer implements Callback<ListView<Topic>, ListCell<Topic>> {
	@Override
	public ListCell<Topic> call(ListView<Topic> param) {
		return new TopicCell();
	}
	
	private class TopicCell extends ListCell<Topic> {
		@Override
		public void updateItem(Topic topic, boolean empty) {
			super.updateItem(topic, empty);
			if (topic != null) {
				if(topic instanceof DialogState.DummyTopic) {
					setId("service");
				}  else {
					setId(null);
				}
				setText(topic.phrase);
			} else {
				setText(null);
			}
		}		
	}
}