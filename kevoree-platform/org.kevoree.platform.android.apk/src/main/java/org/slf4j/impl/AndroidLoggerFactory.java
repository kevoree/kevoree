/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.slf4j.impl;

import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;
import org.slf4j.ILoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * An implementation of {@link org.slf4j.ILoggerFactory} which always returns
 * {@link org.slf4j.impl.AndroidLogger} instances.
 *
 * @author Thorsten M&ouml;ler
 * @version $Rev:$; $Author:$; $Date:$
 */
public class AndroidLoggerFactory implements ILoggerFactory, KevoreeLogService {
	private final Map<String, AndroidLogger> loggerMap;

	static final int TAG_MAX_LENGTH = 23; // tag names cannot be longer on Android platform
	// see also android/system/core/include/cutils/property.h
	// and android/frameworks/base/core/jni/android_util_Log.cpp

	public AndroidLoggerFactory () {
		loggerMap = new HashMap<String, AndroidLogger>();
	}

	private KevoreeLogLevel coreLogLevel = KevoreeLogLevel.INFO;
	private KevoreeLogLevel userLogLevel = KevoreeLogLevel.INFO;

	/* @see org.slf4j.ILoggerFactory#getLogger(java.lang.String) */
	public AndroidLogger getLogger (final String name) {
		final String actualName = forceValidName(name); // fix for bug #173

		AndroidLogger slogger = null;
		// protect against concurrent access of the loggerMap
		synchronized (this) {
			slogger = loggerMap.get(actualName);
			if (slogger == null) {
				/*if (!actualName.equals(name)) {
					Log.v(AndroidLoggerFactory.class.getSimpleName(),
							"Logger name '" + name + "' exceeds maximum length of " + TAG_MAX_LENGTH +
									" characters, using '" + actualName + "' instead.");
				}*/

				slogger = new AndroidLogger(actualName);
				loggerMap.put(name, slogger);
				setLogLevel(name);
			}
		}
		return slogger;
	}

	/**
	 * Trim name in case it exceeds maximum length of {@value #TAG_MAX_LENGTH} characters.
	 */
	private final String forceValidName (String name) {
		if (name != null && name.length() > TAG_MAX_LENGTH) {
			final StringTokenizer st = new StringTokenizer(name, ".");
			if (st.hasMoreTokens()) // note that empty tokens are skipped, i.e., "aa..bb" has tokens "aa", "bb"
			{
				final StringBuilder sb = new StringBuilder();
				String token;
				do {
					token = st.nextToken();
					if (token.length() == 1) // token of one character appended as is
					{
						sb.append(token);
						sb.append('.');
					} else if (st.hasMoreTokens()) // truncate all but the last token
					{
						sb.append(token.charAt(0));
						sb.append("*.");
					} else // last token (usually class name) appended as is
					{
						sb.append(token);
					}
				} while (st.hasMoreTokens());

				name = sb.toString();
			}

			// Either we had no useful dot location at all or name still too long.
			// Take leading part and append '*' to indicate that it was truncated
			if (name.length() > TAG_MAX_LENGTH) {
				name = name.substring(0, TAG_MAX_LENGTH - 1) + '*';
			}
		}
		return name;
	}

	private void setLogLevel (String loggerName) {
		if (loggerName.startsWith("org.kevoree.framework")
				|| loggerName.startsWith("org.kevoree.core")
				|| loggerName.startsWith("org.kevoree.baseChecker")
				|| loggerName.startsWith("org.kevoree.kcl")
				|| loggerName.startsWith("org.kevoree.merger")
				|| loggerName.startsWith("org.kevoree.model")
				|| loggerName.startsWith("org.kevoree.api")
				|| loggerName.startsWith("org.kevoree.tools.aether")) {
			loggerMap.get(loggerName).setLevel(coreLogLevel);
		} else {
			loggerMap.get(loggerName).setLevel(userLogLevel);
		}
	}

	@Override
	public void setCoreLogLevel (KevoreeLogLevel kevoreeLogLevel) {
		coreLogLevel = kevoreeLogLevel;
		for (String loggerName : loggerMap.keySet()) {
			if (loggerName.startsWith("org.kevoree.framework")
					|| loggerName.startsWith("org.kevoree.core")
					|| loggerName.startsWith("org.kevoree.baseChecker")
					|| loggerName.startsWith("org.kevoree.kcl")
					|| loggerName.startsWith("org.kevoree.merger")
					|| loggerName.startsWith("org.kevoree.model")
					|| loggerName.startsWith("org.kevoree.api")
					|| loggerName.startsWith("org.kevoree.tools.aether")) {
				loggerMap.get(loggerName).setLevel(kevoreeLogLevel);
			}
		}
	}

	@Override
	public void setUserLogLevel (KevoreeLogLevel kevoreeLogLevel) {
		userLogLevel = kevoreeLogLevel;
		for (String loggerName : loggerMap.keySet()) {
			if (!loggerName.startsWith("org.kevoree.framework")
					&& !loggerName.startsWith("org.kevoree.core")
					&& !loggerName.startsWith("org.kevoree.baseChecker")
					&& !loggerName.startsWith("org.kevoree.kcl")
					&& !loggerName.startsWith("org.kevoree.merger")
					&& !loggerName.startsWith("org.kevoree.model")
					&& !loggerName.startsWith("org.kevoree.api")
					&& !loggerName.startsWith("org.kevoree.tools.aether")) {
				loggerMap.get(loggerName).setLevel(kevoreeLogLevel);
			}
		}
	}

	@Override
	public void setLogLevel (String s, KevoreeLogLevel kevoreeLogLevel) {
		for (String loggerName : loggerMap.keySet()) {
			if (loggerName.equals(s)) {
				loggerMap.get(loggerName).setLevel(kevoreeLogLevel);
			}
		}
	}

	@Override
	public KevoreeLogLevel getCoreLogLevel () {
		for (String loggerName : loggerMap.keySet()) {
			if (loggerName.startsWith("org.kevoree.framework")
					|| loggerName.startsWith("org.kevoree.core")
					|| loggerName.startsWith("org.kevoree.baseChecker")
					|| loggerName.startsWith("org.kevoree.kcl")
					|| loggerName.startsWith("org.kevoree.merger")
					|| loggerName.startsWith("org.kevoree.model")
					|| loggerName.startsWith("org.kevoree.api")
					|| loggerName.startsWith("org.kevoree.tools.aether")) {
				return loggerMap.get(loggerName).getLevel();
			}
		}
		return null;
	}

	@Override
	public KevoreeLogLevel getUserLogLevel () {
		for (String loggerName : loggerMap.keySet()) {
			if (!loggerName.startsWith("org.kevoree.framework")
					&& !loggerName.startsWith("org.kevoree.core")
					&& !loggerName.startsWith("org.kevoree.baseChecker")
					&& !loggerName.startsWith("org.kevoree.kcl")
					&& !loggerName.startsWith("org.kevoree.merger")
					&& !loggerName.startsWith("org.kevoree.model")
					&& !loggerName.startsWith("org.kevoree.api")
					&& !loggerName.startsWith("org.kevoree.tools.aether")) {
				return loggerMap.get(loggerName).getLevel();
			}
		}
		return null;
	}

	@Override
	public KevoreeLogLevel getLogLevel (String s) {
		for (String loggerName : loggerMap.keySet()) {
			if (loggerName.equals(s)) {
				return loggerMap.get(loggerName).getLevel();
			}
		}
		return null;
	}

}
