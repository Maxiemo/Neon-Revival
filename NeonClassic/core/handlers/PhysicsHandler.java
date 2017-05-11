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

package neon.core.handlers;

import neon.core.Engine;
import neon.resources.RScript;
import neon.resources.ResourceManager;
import neon.systems.physics.PhysicsEvent;
import neon.systems.physics.PhysicsListener;

public class PhysicsHandler implements PhysicsListener {
	private ResourceManager resources;
	
	public PhysicsHandler(ResourceManager resources) {
		this.resources = resources;
	}
	
	// voorlopig alleen controleren of de player op een region staat die een script moet draaien
	@Override
	public void collisionOccured(PhysicsEvent event) {
		Object one = event.getObjectA();
		Object two = event.getObjectB();

		try {
			if(one.equals(Engine.getPlayer()) && two instanceof neon.maps.Region) {
				for(String s : ((neon.maps.Region)two).getScripts()) {
					RScript rs = (RScript)resources.getResource(s, "script");
					Engine.execute(rs.script);
				}
			} else if(one instanceof neon.maps.Region && two.equals(Engine.getPlayer())) {
				for(String s : ((neon.maps.Region)one).getScripts()) {
					RScript rs = (RScript)resources.getResource(s, "script");
					Engine.execute(rs.script);
				}
			}
		} catch(Exception e) { 
			e.printStackTrace();
		}
	}	
}
