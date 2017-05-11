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

package neon.ui.graphics;

import java.awt.Graphics2D;
import java.awt.TexturePaint;
import neon.maps.Region;
import neon.util.TextureFactory;

public class RegionRenderer {
	public static void paint(Region region, Graphics2D graphics, float zoomf, boolean isSelected) {
		int zoom = (int)zoomf;
		TexturePaint paint = TextureFactory.getTexture(region.getTextureType(), zoom, region.getColor());
		graphics.setPaint(paint);
		graphics.fillRect(region.getX()*zoom, region.getY()*zoom, region.getWidth()*zoom, region.getHeight()*zoom);
	}
}
