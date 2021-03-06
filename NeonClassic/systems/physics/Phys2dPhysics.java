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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import neon.entities.Entity;
import neon.maps.Region;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.*;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.strategies.QuadSpaceStrategy;

public class Phys2dPhysics implements PhysicsSystem {
	private World world;
	private HashMap<PhysicsListener, CollisionListener> listeners = new HashMap<>();
	private ArrayList<Body> bodies = new ArrayList<>();
	
	public Phys2dPhysics() {
		world = new World(new Vector2f(0,0), 1, new QuadSpaceStrategy(50, 15));
	}
	
	@Override
	public void clear() {
		world.clear();
		bodies.clear();
	}
	
	@Override
	public void registerEntity(Entity entity) {
		Rectangle bounds = entity.getShapeComponent();
		Body body = new Body(new Box(bounds.width, bounds.height), 1);
		body.setEnabled(true);
		body.setUserData(entity);
		body.move(bounds.x + bounds.width/2, bounds.y + bounds.height/2);			
		world.add(body);
		bodies.add(body);
	}
	
	@Override
	public void registerRegion(Region region) {
		// -1 en -0.5f om afrondingsfouten met floats te voorkomen
		Rectangle bounds = region.getBounds();
		Box box = new Box(bounds.width - 1, bounds.height - 1);
		Body body = new StaticBody(box);
		body.setUserData(region);
		body.setPosition((float)bounds.getCenterX() - 0.5f, (float)bounds.getCenterY() - 0.5f);
		world.add(body);
	}
	
	@Override
	public void addListener(PhysicsListener pl) {
		Phys2dListener listener = new Phys2dListener(pl);
		listeners.put(pl, listener);
		world.addListener(listener);
	}
	
	@Override
	public void removeListener(PhysicsListener pl) {
		world.removeListener(listeners.get(pl));
		listeners.remove(pl);
	}

	@Override
	public void update() {
		for(Body body : bodies) {
			Entity entity = (Entity)body.getUserData();
			Rectangle bounds = entity.getShapeComponent();
			body.move(bounds.x + bounds.width/2, bounds.y + bounds.height/2);			
		}

		world.step();
	}
}