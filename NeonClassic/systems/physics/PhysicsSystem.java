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

import neon.entities.Entity;
import neon.maps.Region;

public interface PhysicsSystem {
	/**
	 * Removes all entities from the physics system.
	 */
	public void clear();
	
	/**
	 * Register an {@code Entity} to be tracked by the physics system.
	 * 
	 * @param entity
	 */
	public void registerEntity(Entity entity);
	
	public void registerRegion(Region region);	
	public void addListener(PhysicsListener pl);
	public void removeListener(PhysicsListener pl);
	public void update();
}

