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

import org.dyn4j.collision.manifold.Manifold;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionListener;
import org.dyn4j.dynamics.contact.ContactConstraint;

public class Dyn4jListener implements CollisionListener {
	private PhysicsListener pl;
	
	public Dyn4jListener(PhysicsListener pl) {
		this.pl = pl;
	}
	
	@Override
	public boolean collision(ContactConstraint cc) {
		return false;
	}

	@Override
	public boolean collision(Body one, Body two) {
		return true;
	}

	@Override
	public boolean collision(Body one, BodyFixture bf1, Body two, BodyFixture bf2, Penetration p) {
		if(p.getDepth() > 0.5) {
			pl.collisionOccured(new Dyn4jEvent(one.getUserData(), two.getUserData()));
		}
		return false;
	}

	@Override
	public boolean collision(Body one, BodyFixture bf1, Body two, BodyFixture bf2, Manifold m) {
		return false;
	}
}
