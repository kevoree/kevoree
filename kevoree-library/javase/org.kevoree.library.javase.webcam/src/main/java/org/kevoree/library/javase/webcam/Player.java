package org.kevoree.library.javase.webcam;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/11/11
 * Time: 17:22
 *
 * @author Erwan Daubert
 * @version 1.0
 */
 @MessageTypes({
 		@MessageType(name = "percentType", elems = {@MsgElem(name = "percent", className = Integer.class)})
 })
 @Provides({
 		@ProvidedPort(name = "media", type = PortType.MESSAGE),
 		@ProvidedPort(name = "play", type = PortType.MESSAGE),
 		@ProvidedPort(name = "stop", type = PortType.MESSAGE),
 		@ProvidedPort(name = "pause", type = PortType.MESSAGE),
 		@ProvidedPort(name = "volume", type = PortType.MESSAGE, messageType = "percentType")
 })
@ComponentFragment
public abstract class Player extends AbstractComponentType {
	uk.co.caprica.vlcj.player.MediaPlayer mediaPlayer = null;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Port(name = "media")
		public void triggerMedia (final Object o) {
			if (o != null) {
				logger.debug("Run media mrl=" + o.toString());
				mediaPlayer.playMedia(o.toString());
			}
		}

		@Port(name = "play")
		public void triggerPlay (final Object o) {
			if (o != null) {
				logger.debug("play");
				mediaPlayer.play();
			}
		}

		@Port(name = "pause")
		public void triggerPause (final Object o) {
			if (o != null) {
				logger.debug("pause");
				mediaPlayer.pause();
			}
		}

		@Port(name = "stop")
		public void triggerStop (final Object o) {
			if (o != null) {
				logger.debug("stop");
				mediaPlayer.stop();
			}
		}

		@Port(name = "volume")
		public void triggerVolume (final Object o) {
			if (o != null && o instanceof Integer) {
				logger.debug("set Voume to " + o);
				mediaPlayer.setVolume((Integer)o);
			}
		}
}
