package org.openlca.io.ilcd.output;

import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.io.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Out {

	private Out() {
	}

	public static XMLGregorianCalendar getTimestamp(CategorizedEntity e) {
		if (e == null)
			return null;
		long time = e.lastChange;
		if (time <= 0L)
			return null;
		try {
			GregorianCalendar gCal = new GregorianCalendar();
			gCal.setTimeInMillis(time);
			XMLGregorianCalendar xml = Xml.datatypeFactory
					.newXMLGregorianCalendar(gCal);
			return xml;
		} catch (Exception ex) {
			Logger log = LoggerFactory.getLogger(Out.class);
			log.error("failed to create XML calendar for time " + time, ex);
			return null;
		}
	}

}
