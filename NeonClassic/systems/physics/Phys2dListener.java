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

package neon.systems.physics;

import net.phys2d.raw.CollisionEvent;
import net.phys2d.raw.CollisionListener;

public class Phys2dListener implements CollisionListener {
	private PhysicsListener listener;
	
	public Phys2dListener(PhysicsListener pl) {
		listener = pl;
	}
	
	@Override
	public void collisionOccured(CollisionEvent ce) {
		listener.collisionOccured(new Phys2dEvent(ce));
	}
}
