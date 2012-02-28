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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.ILoggerFactory;

import android.util.Log;

/**
 * An implementation of {@link ILoggerFactory} which always returns
 * {@link AndroidLogger} instances.
 *
 * @author Thorsten M&ouml;ler
 * @version $Rev:$; $Author:$; $Date:$
 */
public class AndroidLoggerFactory implements ILoggerFactory
{
	private final Map<String, AndroidLogger> loggerMap;

	static final int TAG_MAX_LENGTH = 23; // tag names cannot be longer on Android platform
                                         // see also android/system/core/include/cutils/property.h
                                         // and android/frameworks/base/core/jni/android_util_Log.cpp

	public AndroidLoggerFactory()
	{
		loggerMap = new HashMap<String, AndroidLogger>();
	}

	/* @see org.slf4j.ILoggerFactory#getLogger(java.lang.String) */
	public AndroidLogger getLogger(final String name)
	{
		final String actualName = forceValidName(name); // fix for bug #173

		AndroidLogger slogger = null;
		// protect against concurrent access of the loggerMap
		synchronized (this)
		{
			slogger = loggerMap.get(actualName);
			if (slogger == null)
			{
				if (!actualName.equals(name)) Log.i(AndroidLoggerFactory.class.getSimpleName(),
					"Logger name '" + name + "' exceeds maximum length of " + TAG_MAX_LENGTH +
					" characters, using '" + actualName + "' instead.");

				slogger = new AndroidLogger(actualName);
				loggerMap.put(actualName, slogger);
			}
		}
		return slogger;
	}

	/**
	 * Trim name in case it exceeds maximum length of {@value #TAG_MAX_LENGTH} characters.
	 */
	private final String forceValidName(String name)
	{
		if (name != null && name.length() > TAG_MAX_LENGTH)
		{
			final StringTokenizer st = new StringTokenizer(name, ".");
			if (st.hasMoreTokens()) // note that empty tokens are skipped, i.e., "aa..bb" has tokens "aa", "bb"
			{
				final StringBuilder sb = new StringBuilder();
				String token;
				do
				{
					token = st.nextToken();
					if (token.length() == 1) // token of one character appended as is
					{
						sb.append(token);
						sb.append('.');
					}
					else if (st.hasMoreTokens()) // truncate all but the last token
					{
						sb.append(token.charAt(0));
						sb.append("*.");
					}
					else // last token (usually class name) appended as is
					{
						sb.append(token);
					}
				} while (st.hasMoreTokens());

				name = sb.toString();
			}

			// Either we had no useful dot location at all or name still too long.
			// Take leading part and append '*' to indicate that it was truncated
			if (name.length() > TAG_MAX_LENGTH)
			{
				name = name.substring(0, TAG_MAX_LENGTH - 1) + '*';
			}
		}
		return name;
	}
}
