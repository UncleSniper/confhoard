package org.unclesniper.confhoard.core.slot;

import java.util.Iterator;
import java.io.InputStream;
import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.Slot;
import org.unclesniper.confhoard.core.Doom;
import java.io.UnsupportedEncodingException;
import org.unclesniper.confhoard.core.ConfStateBinding;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;
import org.unclesniper.confhoard.core.util.WriterInputStream;

public abstract class AbstractSlotListingSlot extends VirtualSlot {

	protected static abstract class AbstractSlotListingWriter extends WriterInputStream {

		public static final int GEN_CHARS_WRITTEN = 0001;
		public static final int GEN_SLOT_FINISHED = 0002;

		protected Credentials credentials;

		protected ConfStateBinding confState;

		protected Function<String, Object> parameters;

		private Iterator<Slot> slotIterator;

		private Slot currentSlot;

		private boolean inSlot;

		public AbstractSlotListingWriter(String charset) throws UnsupportedEncodingException {
			super(charset);
		}

		public void setCredentials(Credentials credentials) {
			this.credentials = credentials;
		}

		public void setConfState(ConfStateBinding confState) {
			this.confState = confState;
			slotIterator = confState == null ? null : confState.getSlots().iterator();
		}

		public void setParameters(Function<String, Object> parameters) {
			this.parameters = parameters;
		}

		protected abstract int writeSlot(Slot slot) throws IOException;

		@Override
		protected void generateMoreChars() throws IOException {
			int genFlags;
			do {
				if(!inSlot) {
					if(slotIterator == null)
						currentSlot = null;
					else if(slotIterator.hasNext())
						currentSlot = slotIterator.next();
					else {
						slotIterator = null;
						currentSlot = null;
					}
					inSlot = true;
				}
				genFlags = writeSlot(currentSlot);
				if(currentSlot != null && (genFlags & AbstractSlotListingWriter.GEN_SLOT_FINISHED) != 0)
					inSlot = false;
			} while(currentSlot != null && (genFlags & AbstractSlotListingWriter.GEN_CHARS_WRITTEN) == 0);
		}

	}

	protected static abstract class StatefulSlotListingWriter extends AbstractSlotListingWriter {

		private enum State {
			BEFORE_EVERYTHING,
			BEFORE_SLOT,
			AFTER_BEGIN,
			BEFORE_ACTION,
			AFTER_ACTION,
			BEFORE_MIME_KEY,
			AFTER_MIME_KEY,
			BEFORE_DESCRIPTION_KEY,
			AFTER_DESCRIPTION_KEY,
			BEFORE_END,
			AFTER_SLOT,
			AFTER_EVERYTHING
		}

		private static final SlotAction[] SLOT_ACTIONS = SlotAction.values();

		private State state = State.BEFORE_EVERYTHING;

		private int actionIndex;

		public StatefulSlotListingWriter(String charset) throws UnsupportedEncodingException {
			super(charset);
		}

		protected abstract boolean writeBeginDocument() throws IOException;

		protected abstract boolean writeBeginSlot(Slot slot) throws IOException;

		protected abstract boolean writeSlotKey(Slot slot) throws IOException;

		protected abstract boolean writeActionKey(Slot slot, SlotAction action) throws IOException;

		protected abstract boolean writeActionValue(Slot slot, SlotAction action, boolean permitted)
				throws IOException;

		protected abstract boolean writeMimeTypeKey(Slot slot) throws IOException;

		protected abstract boolean writeMimeTypeValue(Slot slot) throws IOException;

		protected abstract boolean writeDescriptionKey(Slot slot) throws IOException;

		protected abstract boolean writeDescriptionValue(Slot slot) throws IOException;

		protected abstract boolean writeEndSlot(Slot slot) throws IOException;

		protected abstract boolean writeEndDocument() throws IOException;

		@Override
		protected int writeSlot(Slot slot) throws IOException {
			if(state == State.AFTER_EVERYTHING)
				return AbstractSlotListingWriter.GEN_SLOT_FINISHED;
			boolean charsWritten = false;
			do {
				switch(state) {
					case BEFORE_EVERYTHING:
						charsWritten = writeBeginDocument();
						state = State.BEFORE_SLOT;
						if(charsWritten)
							break;
					case BEFORE_SLOT:
						if(slot == null) {
							charsWritten = writeEndDocument();
							state = State.AFTER_SLOT;
							break;
						}
						charsWritten = writeBeginSlot(slot);
						state = State.AFTER_BEGIN;
						if(charsWritten)
							break;
					case AFTER_BEGIN:
						charsWritten = writeSlotKey(slot);
						state = State.BEFORE_ACTION;
						actionIndex = 0;
						if(charsWritten)
							break;
					case BEFORE_ACTION:
						charsWritten = writeActionKey(slot, StatefulSlotListingWriter.SLOT_ACTIONS[actionIndex]);
						state = State.AFTER_ACTION;
						if(charsWritten)
							break;
					case AFTER_ACTION:
						{
							SlotAction action = StatefulSlotListingWriter.SLOT_ACTIONS[actionIndex];
							boolean permitted = slot.mayPerformAction(action, credentials);
							charsWritten = writeActionValue(slot, action, permitted);
						}
						if(++actionIndex >= StatefulSlotListingWriter.SLOT_ACTIONS.length) {
							state = State.BEFORE_MIME_KEY;
							if(charsWritten)
								break;
						}
						else {
							state = State.BEFORE_ACTION;
							break;
						}
					case BEFORE_MIME_KEY:
						charsWritten = writeMimeTypeKey(slot);
						state = State.AFTER_MIME_KEY;
						if(charsWritten)
							break;
					case AFTER_MIME_KEY:
						charsWritten = writeMimeTypeValue(slot);
						state = State.BEFORE_DESCRIPTION_KEY;
						if(charsWritten)
							break;
					case BEFORE_DESCRIPTION_KEY:
						charsWritten = writeDescriptionKey(slot);
						state = State.AFTER_DESCRIPTION_KEY;
						if(charsWritten)
							break;
					case AFTER_DESCRIPTION_KEY:
						charsWritten = writeDescriptionValue(slot);
						state = State.BEFORE_END;
						if(charsWritten)
							break;
					case BEFORE_END:
						charsWritten = writeEndSlot(slot);
						state = State.AFTER_SLOT;
						break;
					case AFTER_SLOT:
						break;
					case AFTER_EVERYTHING:
						throw new Doom("State AFTER_EVERYTHING reached inside loop");
					default:
						throw new Doom("Unrecognized state: " + state.name());
				}
			} while(!charsWritten && state != State.AFTER_SLOT);
			int genFlags = charsWritten ? AbstractSlotListingWriter.GEN_CHARS_WRITTEN : 0;
			if(state == State.AFTER_SLOT) {
				if(slot == null)
					state = State.AFTER_EVERYTHING;
				genFlags |= AbstractSlotListingWriter.GEN_SLOT_FINISHED;
			}
			return genFlags;
		}

	}

	private String charset;

	public AbstractSlotListingSlot(String key) {
		super(key);
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	protected abstract AbstractSlotListingWriter newSlotListingWriter() throws IOException;

	@Override
	protected InputStream retrieveFragment(Credentials credentials, ConfStateBinding state,
			Function<String, Object> parameters) throws IOException {
		AbstractSlotListingWriter writer = newSlotListingWriter();
		writer.setCredentials(credentials);
		writer.setConfState(state);
		writer.setParameters(parameters);
		return writer;
	}

}
