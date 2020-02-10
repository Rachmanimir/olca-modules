package org.openlca.geo.geojson;

import java.io.IOException;
import java.util.Map;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;

public class MsgPack {

	private MsgPack() {
	}

	public static byte[] pack(FeatureCollection coll) {
		if (coll == null)
			return null;
		try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
			packer.packMapHeader(2);
			packer.packString("type");
			packer.packString("FeatureCollection");
			packer.packString("features");
			packer.packArrayHeader(coll.features.size());
			for (Feature f : coll.features) {
				packFeature(f, packer);
			}
			return packer.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void packFeature(
			Feature f, MessagePacker packer) throws IOException {
		packer.packString("type");
		packer.packString("Feature");
	}

	static void packPoint(Point p, MessagePacker packer) {
		try {
			packer.packString("type");
			packer.packString("Point");
			packer.packString("coordinates");
			packer.packArrayHeader(2);
			packer.packDouble(p.x);
			packer.packDouble(p.y);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static FeatureCollection unpack(byte[] data) {
		if (data == null)
			return null;
		try {
			MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
			FeatureCollection coll = new FeatureCollection();
			if (!unpacker.hasNext())
				return coll;
			Value root = unpacker.unpackValue();
			if (!root.isMapValue())
				return coll;
			MapValue map = root.asMapValue();
			for (Map.Entry<Value, Value> entry : map.entrySet()) {
				Value keyValue = entry.getKey();
				if (!keyValue.isStringValue())
					continue;
				if (!"features".equals(keyValue.toString()))
					continue;
				Value value = entry.getValue();
				if (!value.isArrayValue())
					break;
			}
			return coll;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static Point unpackPoint(MessageUnpacker unpacker) {
		Point p = new Point();
		try {
			System.out.println(unpacker.unpackString());
			System.out.println(unpacker.unpackString());

			System.out.println(unpacker.unpackString()); // "coordinates"
			int n = unpacker.unpackArrayHeader();
			if (n > 0) {
				p.x = unpacker.unpackDouble();
			}
			if (n > 1) {
				p.y = unpacker.unpackDouble();
			}
			if (n > 2) {
				for (int i = 2; i < n; i++) {
					unpacker.unpackDouble();
				}
			}
			return p;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
