/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2011-2014 - Maarten Driesen
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

package neon.util.spatial;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A Quadtree to store rectangular objects.
 * 
 * @author mdriesen
 * @param <E>
 */
public class QuadTree<E> {
	private Node root;
	private Rectangle2D bounds;
	private int capacity;
	private HashMap<E, NodeElement> map;
	
	/**
	 * 
	 * @param capacity	maximum number of elements in a node
	 * @param maxDepth	maximum depth of the tree
	 * @param bounds	initial bounds of the tree
	 */
	public QuadTree(int capacity, int maxDepth, Rectangle2D bounds) {
		this.capacity = capacity;
		this.bounds = bounds;
		map = new HashMap<>();
		root = new Node(null, bounds);
	}
	
	/**
	 * 
	 * @param capacity	maximum number of elements in a node
	 * @param maxDepth	maximum depth of the tree
	 */
	public QuadTree(int capacity, int maxDepth) {
		this(capacity, maxDepth, new Rectangle2D.Double());
	}
	
	/**
	 * @return	all elements in this tree
	 */
	public List<E> getElements() {
		return root.getValues(bounds);
	}

	/**
	 * @param bounds
	 * @return	all elements within the given bounds
	 */
	public List<E> getElements(Rectangle2D bounds) {
		return root.getValues(bounds);
	}

	public List<E> getElements(Point2D point) {
		return root.getValues(new Rectangle2D.Double(point.getX(), point.getY(), 1, 1));
	}
	
	/**
	 * Inserts an element into this tree.
	 * 
	 * @param e			the element
	 * @param bounds	the bounds of the element
	 */
	public void insert(E e, Rectangle2D bounds) {
//		System.out.println("insert");
		NodeElement element = new NodeElement(e, bounds);

		// eerst kijken of element nog in tree past, zoniet de tree groter maken
		if(!this.bounds.contains(bounds)) {
			this.bounds = this.bounds.createUnion(bounds);
			root = new Node(null, this.bounds);
			
			for(NodeElement ne : map.values()) {
				root.insert(ne);
			}
		}
		
		map.put(e, element);
		root.insert(element);
	}

	/**
	 * Removes an element from this tree.
	 * 
	 * @param e
	 */
	public void remove(E e) {
		if(map.containsKey(e)) {
			NodeElement ne = map.get(e);
			map.remove(e);
			ne.getParent().remove(ne);
		} else {
			throw new IllegalArgumentException("Element " + e + " not found in tree.");
		}
	}

	/**
	 * Moves an element to the given point.
	 * 
	 * @param e
	 * @param p
	 */
	public void move(E e, Point p) {
		
	}
	
	public void resize(E e, Dimension d) {
		
	}
	
	public void clear() {
		root = new Node(null, bounds);
	}

	public double getWidth() {
		return bounds.getWidth();
	}

	public double getHeight() {
		return bounds.getHeight();
	}
	
	private class Node {
		private ArrayList<NodeElement> elements = new ArrayList<>();
		private Rectangle2D bounds;
		private Type type = Type.LEAF;
		private Node parent;
		
		private Node NW;
		private Node NE;
		private Node SW;
		private Node SE;
		
		private Node(Node parent, Rectangle2D bounds) {
			this.parent = parent;
			this.bounds = bounds;
		}
		
		/*
		 * Inserts an element in this node.
		 */
		private boolean insert(NodeElement ne) {
			// kijken of element in bounds past
			if(!bounds.contains(ne.bounds)) {
				return false;
			}
			
			// kijken of er nog plaats is in deze node.
			if(elements.size() < capacity && type.equals(Type.LEAF)) {
				elements.add(ne);
				ne.setParent(this);
				return true;
			} else {	// geen plaats, dus in child node
				if(!type.equals(Type.BRANCH)) {
					split();
				}

				// kijken of element in een child node past
				if(!NW.insert(ne) && !NE.insert(ne) && !SW.insert(ne) && !SE.insert(ne)) {
					// element overlapt waarschijnlijk met twee of meer subnodes, dus in deze node opslaan
					elements.add(ne);
					ne.setParent(this);
				}
				
				return true;
			}			
		}
		
		private void split() {
//			System.out.println("split");
			// child nodes aanmaken
			double halfX = bounds.getX() + bounds.getWidth()/2;
			double halfY = bounds.getY() + bounds.getHeight()/2;
			double halfW = bounds.getWidth()/2;
			double halfH = bounds.getHeight()/2;
			
			NW = new Node(this, new Rectangle2D.Double(bounds.getX(), bounds.getY(), halfW, halfH));
			NE = new Node(this, new Rectangle2D.Double(halfX, bounds.getY(), halfW, halfH));
			SW = new Node(this, new Rectangle2D.Double(bounds.getX(), halfY, halfW, halfH));
			SE = new Node(this, new Rectangle2D.Double(halfX, halfY, halfW, halfH));
			
			// dit is nu een branch node
			type = Type.BRANCH;
			
			// elementen verdelen
			List<NodeElement> buffer = new ArrayList<>(elements);	// efkens kopie maken
			for(NodeElement ne : buffer) {
				if(NW.insert(ne) || NE.insert(ne) || SW.insert(ne) || SE.insert(ne)) {
					// element paste in een leaf node, dus verwijderen uit deze node
					elements.remove(ne);
				}				
			}
		}
	
		private List<E> getValues(Rectangle2D bounds) {
			ArrayList<E> values = new ArrayList<>();
			
			if(bounds.intersects(this.bounds)) {
				for(NodeElement ne : elements) {
					if(bounds.intersects(ne.bounds)) {
						values.add(ne.value);
					}
				}

				if(type.equals(Type.BRANCH)) {
					values.addAll(NW.getValues(bounds));
					values.addAll(NE.getValues(bounds));
					values.addAll(SW.getValues(bounds));
					values.addAll(SE.getValues(bounds));
				}
			}

			return values;
		}
		
		private void remove(NodeElement ne) {
			elements.remove(ne);
			parent.checkElements();
		}
		
		private void checkElements() {
			// TODO: nakijken of node niet moet ingekrompen worden
		}
	}
	
	private class NodeElement {
		private Rectangle2D bounds;
		private E value;
		private Node parent;
		
		private NodeElement(E e, Rectangle2D bounds) {
			this.bounds = bounds;
			this.value = e;
		}
		
		private void setParent(Node parent) {
			this.parent = parent;
		}
		
		private Node getParent() {
			return parent;
		}
	}
	
	private enum Type {
		LEAF, BRANCH;
	}
}
