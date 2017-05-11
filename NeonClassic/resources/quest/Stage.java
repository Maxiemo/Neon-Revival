/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2014 - Maarten Driesen
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

package neon.resources.quest;

import org.jdom2.Element;

/**
 * A quest stage.
 * 
 * @author mdriesen
 *
 */
public class Stage {
	/** The resource ID of the quest this stage belongs to. */
	public final String questID;
	private int index;
	private String entry, action;
	
	/**
	 * Initializes a quest stage.
	 * 
	 * @param questID	the resouce ID of the quest this stage belongs to
	 */
	public Stage(String questID, Element properties) {
		this.questID = questID;
		index = Integer.parseInt(properties.getAttributeValue("index"));
		if(properties.getChild("entry") != null) {
			entry = properties.getChildText("entry");
		}
		if(properties.getChild("action") != null) {
			action = properties.getChildText("action");
		}
	}
	
	/**
	 * @return	the stage index
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * @return	the journal entry for this stage
	 */
	public String getJournalEntry() {
		return entry;
	}
	
	/**
	 * @return	the script action for this stage
	 */
	public String getAction() {
		return action;
	}
}
