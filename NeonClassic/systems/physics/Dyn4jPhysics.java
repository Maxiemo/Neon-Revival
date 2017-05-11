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

import java.util.HashMap;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Mass.Type;
import org.dyn4j.geometry.Rectangle;
import neon.entities.Entity;
import neon.maps.Region;

public class Dyn4jPhysics implements PhysicsSystem {
	private World world;
	private HashMap<PhysicsListener, CollisionListener> listeners = new HashMap<>();
	
	public Dyn4jPhysics() {
		world = new World();
	}
	
	@Override
	public void clear() {
		world.removeAllBodiesAndJoints();
	}

	@Override
	public void registerEntity(Entity entity) {
		Body body = new Body();
		body.setUserData(entity);
		java.awt.Rectangle bounds = entity.getShapeComponent();
		body.addFixture(new Rectangle(bounds.width, bounds.height));
		body.setMass();
		body.getTransform().setTranslation(bounds.getCenterX(), bounds.getCenterY());
		world.addBody(body);
	}

	@Override
	public void registerRegion(Region region) {
		Body body = new Body();
		body.setUserData(region);
		java.awt.Rectangle bounds = region.getBounds();
		body.addFixture(new BodyFixture(new Rectangle(bounds.width, bounds.height)));
		body.setMass(Type.INFINITE);
		body.getTransform().setTranslation(bounds.getCenterX(), bounds.getCenterY());
		world.addBody(body);
	}

	@Override
	public void addListener(PhysicsListener pl) {
		CollisionListener listener = new Dyn4jListener(pl);
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
		for(Body body : world.getBodies()) {
			if(body.getUserData() instanceof Entity) {
				Entity entity = (Entity)body.getUserData();
				java.awt.Rectangle bounds = entity.getShapeComponent();
				body.getTransform().setTranslation(bounds.getCenterX(), bounds.getCenterY());
			}
		}
		world.step(1);
	}
}
