/*
 * Copyright (C) 2003, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package simplerMultiTouch.library;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Dispatches OSCMessages to registered listeners.
 *
 * @author Chandrasekhar Ramakrishnan
 */

public class OSCPacketDispatcher {

	private Map<String, OSCListener> addressToListener
			= new HashMap<String, OSCListener>();

	/**
	 *
	 */
	public OSCPacketDispatcher() {
	}

	public void addListener(String address, OSCListener listener) {
		addressToListener.put(address, listener);
	}

	public void dispatchPacket(OSCPacket packet) {
		if (packet instanceof OSCBundle) {
			dispatchBundle((OSCBundle) packet);
		} else {
			dispatchMessage((OSCMessage) packet);
		}
	}

	public void dispatchPacket(OSCPacket packet, Date timestamp) {
		if (packet instanceof OSCBundle) {
			dispatchBundle((OSCBundle) packet);
		} else {
			dispatchMessage((OSCMessage) packet, timestamp);
		}
	}

	private void dispatchBundle(OSCBundle bundle) {
		Date timestamp = bundle.getTimestamp();
		OSCPacket[] packets = bundle.getPackets();
		for (OSCPacket packet : packets) {
			dispatchPacket(packet, timestamp);
		}
	}

	private void dispatchMessage(OSCMessage message) {
		dispatchMessage(message, null);
	}

	private void dispatchMessage(OSCMessage message, Date time) {
		for (Entry<String, OSCListener> addrList : addressToListener.entrySet()) {
			if (message.getAddress().matches(addrList.getKey())) {
				addrList.getValue().acceptMessage(time, message);
			}
		}
	}
}
