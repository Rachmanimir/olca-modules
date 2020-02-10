package org.openlca.geo.jts;

import org.openlca.geo.geojson.GeometryCollection;
import org.openlca.geo.geojson.LineString;
import org.openlca.geo.geojson.MultiLineString;
import org.openlca.geo.geojson.MultiPoint;
import org.openlca.geo.geojson.MultiPolygon;
import org.openlca.geo.geojson.Point;
import org.openlca.geo.geojson.Polygon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Contains conversion methods for creating JTS geometries from GeoJSON and
 * converting them back to GeoJSON.
 */
public class JTS {

	/**
	 * Creates a JTS geometry from the given GeoJSON geometry.
	 */
	public static Geometry fromGeoJSON(org.openlca.geo.geojson.Geometry g) {
		if (g == null)
			return null;
		GeometryFactory gen = new GeometryFactory();

		if (g instanceof Point)
			return fromPoint((Point) g, gen);

		if (g instanceof MultiPoint)
			return fromMultiPoint((MultiPoint) g, gen);

		if (g instanceof LineString)
			return fromLineString((LineString) g, gen);

		if (g instanceof MultiLineString)
			return fromMultiLineString((MultiLineString) g, gen);

		if (g instanceof Polygon)
			return fromPolygon((Polygon) g, gen);

		if (g instanceof MultiPolygon)
			return fromMultiPolygon((MultiPolygon) g, gen);

		if (g instanceof GeometryCollection) {
			Geometry[] geoms = ((GeometryCollection) g).geometries.stream()
					.map(JTS::fromGeoJSON)
					.toArray(Geometry[]::new);
			return gen.createGeometryCollection(geoms);
		}

		return null;
	}

	private static Geometry fromPoint(Point p, GeometryFactory gen) {
		Coordinate coordinate = new Coordinate(p.x, p.y);
		return gen.createPoint(coordinate);
	}

	private static Geometry fromMultiPoint(MultiPoint mp, GeometryFactory gen) {
		Coordinate[] coordinates = mp.points.stream()
				.map(p -> new Coordinate(p.x, p.y))
				.toArray(Coordinate[]::new);
		return gen.createMultiPoint(coordinates);
	}

	private static com.vividsolutions.jts.geom.LineString fromLineString(
			LineString line, GeometryFactory gen) {
		Coordinate[] coordinates = line.points.stream()
				.map(p -> new Coordinate(p.x, p.y))
				.toArray(Coordinate[]::new);
		return gen.createLineString(coordinates);
	}

	private static Geometry fromMultiLineString(
			MultiLineString ml, GeometryFactory gen) {
		com.vividsolutions.jts.geom.LineString[] lines = ml.lineStrings.stream()
				.map(line -> fromLineString(line, gen))
				.toArray(com.vividsolutions.jts.geom.LineString[]::new);
		return gen.createMultiLineString(lines);
	}

	private static com.vividsolutions.jts.geom.Polygon fromPolygon(
			Polygon p, GeometryFactory gen) {
		if (p.rings.isEmpty())
			return null;
		Coordinate[] outer = p.rings.get(0).points.stream()
				.map(point -> new Coordinate(point.x, point.y))
				.toArray(Coordinate[]::new);
		LinearRing outerRing = gen.createLinearRing(outer);
		if (p.rings.size() == 1)
			return gen.createPolygon(outerRing);
		LinearRing[] innerRings = new LinearRing[p.rings.size() - 1];
		for (int i = 1; i < p.rings.size(); i++) {
			Coordinate[] ring = p.rings.get(i).points.stream()
					.map(point -> new Coordinate(point.x, point.y))
					.toArray(Coordinate[]::new);
			innerRings[i - 1] = gen.createLinearRing(ring);
		}
		return gen.createPolygon(outerRing, innerRings);
	}

	private static Geometry fromMultiPolygon(MultiPolygon mp, GeometryFactory gen) {
		return gen.createMultiPolygon(mp.polygons
				.stream()
				.map(p -> fromPolygon(p, gen))
				.toArray(com.vividsolutions.jts.geom.Polygon[]::new));
	}

	/**
	 * Converts the given JTS geometry to a GeoJSON geometry.
	 */
	public static org.openlca.geo.geojson.Geometry toGeoJSON(Geometry g) {
		if (g == null)
			return null;

		if (g instanceof com.vividsolutions.jts.geom.Point)
			return toPoint(g.getCoordinate());

		if (g instanceof com.vividsolutions.jts.geom.MultiPoint)
			return toMultiPoint(g);

		if (g instanceof com.vividsolutions.jts.geom.LineString)
			return toLineString((com.vividsolutions.jts.geom.LineString) g);

		if (g instanceof com.vividsolutions.jts.geom.MultiLineString)
			return toMultiLineString(g);

		if (g instanceof com.vividsolutions.jts.geom.Polygon)
			return toPolygon((com.vividsolutions.jts.geom.Polygon) g);

		if (g instanceof com.vividsolutions.jts.geom.MultiPolygon)
			return toMultiPolygon(g);

		if (g instanceof com.vividsolutions.jts.geom.GeometryCollection)
			return toGeometryCollection(g);

		return null;
	}

	private static MultiPoint toMultiPoint(Geometry g) {
		MultiPoint mp = new MultiPoint();
		for (int i = 0; i < g.getNumGeometries(); i++) {
			Geometry jts = g.getGeometryN(i);
			if (jts == null)
				continue;
			Point p = toPoint(jts.getCoordinate());
			if (p != null) {
				mp.points.add(p);
			}
		}
		return mp;
	}

	private static Point toPoint(Coordinate pos) {
		if (pos == null)
			return null;
		Point point = new Point();
		point.x = pos.x;
		point.y = pos.y;
		return point;
	}

	private static LineString toLineString(
			com.vividsolutions.jts.geom.LineString jts) {
		Coordinate[] coords = jts.getCoordinates();
		if (coords == null)
			return null;
		LineString line = new LineString();
		for (Coordinate coord : coords) {
			Point p = toPoint(coord);
			if (p != null) {
				line.points.add(p);
			}
		}
		return line;
	}

	private static MultiLineString toMultiLineString(Geometry g) {
		MultiLineString mls = new MultiLineString();
		for (int i = 0; i < g.getNumGeometries(); i++) {
			org.openlca.geo.geojson.Geometry line = toGeoJSON(g.getGeometryN(i));
			if (line instanceof LineString) {
				mls.lineStrings.add((LineString) line);
			}
		}
		return mls;
	}

	private static Polygon toPolygon(com.vividsolutions.jts.geom.Polygon jts) {
		Polygon polygon = new Polygon();
		LineString exterior = toLineString(jts.getExteriorRing());
		polygon.rings.add(exterior);
		for (int i = 0; i < jts.getNumInteriorRing()) {
			LineString interior = toLineString(jts.getInteriorRingN(i));
			if (interior != null) {
				polygon.rings.add(interior);
			}
		}
		return polygon;
	}

	private static MultiPolygon toMultiPolygon(Geometry g) {
		MultiPolygon gg = new MultiPolygon();
		return gg;
	}

	private static GeometryCollection toGeometryCollection(Geometry g) {
		GeometryCollection gg = new GeometryCollection();
		return gg;
	}
}