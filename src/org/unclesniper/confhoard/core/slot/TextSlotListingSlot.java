package org.unclesniper.confhoard.core.slot;

import java.io.IOException;
import org.unclesniper.confhoard.core.Slot;
import java.io.UnsupportedEncodingException;
import org.unclesniper.confhoard.core.util.WriterUtils;
import org.unclesniper.confhoard.core.security.SlotAction;

public class TextSlotListingSlot extends AbstractSlotListingSlot {

	private static class TextSlotListingWriter extends StatefulSlotListingWriter {

		private boolean firstAction;

		public TextSlotListingWriter(String charset) throws UnsupportedEncodingException {
			super(charset);
		}

		@Override
		protected boolean writeBeginDocument() {
			return false;
		}

		@Override
		protected boolean writeBeginSlot(Slot slot) {
			firstAction = true;
			return false;
		}

		@Override
		protected boolean writeSlotKey(Slot slot) throws IOException {
			String key = slot.getKey();
			if(key.length() == 0)
				return false;
			intoChars.write(key);
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeActionKey(Slot slot, SlotAction action) throws IOException {
			char sep;
			if(firstAction) {
				sep = ':';
				firstAction = false;
			}
			else
				sep = ',';
			intoChars.write(sep + action.getActionProperty());
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeActionValue(Slot slot, SlotAction action, boolean permitted) throws IOException {
			intoChars.write(permitted ? "=true" : "=false");
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeMimeTypeKey(Slot slot) throws IOException {
			intoChars.write(",mimeType=");
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeMimeTypeValue(Slot slot) throws IOException {
			String mimeType = slot.getMimeType();
			if(mimeType == null || mimeType.length() == 0)
				return false;
			intoChars.write(mimeType);
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeDescriptionKey(Slot slot) throws IOException {
			intoChars.write(",description=");
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeDescriptionValue(Slot slot) throws IOException {
			String description = slot.getDescription();
			if(description == null || description.length() == 0)
				return false;
			intoChars.write(description);
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeEndSlot(Slot slot) throws IOException {
			WriterUtils.putEOL(intoChars);
			intoChars.flush();
			return true;
		}

		@Override
		protected boolean writeEndDocument() {
			return false;
		}

	}

	public TextSlotListingSlot(String key) {
		super(key);
	}

	@Override
	protected String getDefaultMimeType() {
		return "text/plain";
	}

	@Override
	protected AbstractSlotListingWriter newSlotListingWriter() throws UnsupportedEncodingException {
		return new TextSlotListingWriter(getCharset());
	}

}
