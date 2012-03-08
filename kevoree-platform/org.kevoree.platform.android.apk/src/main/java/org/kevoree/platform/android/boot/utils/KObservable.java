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
package org.kevoree.platform.android.boot.utils;

import java.util.ArrayList;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 11:33
 */

public class KObservable<T> implements IKObservable<T> {

	private final ArrayList<OnChangeListener<T>> listeners = new ArrayList<OnChangeListener<T>>();

	public void addListener(OnChangeListener<T> listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	public void removeListener(OnChangeListener<T> listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void notifyObservers(final T model) {
		synchronized (listeners) {
			for (OnChangeListener<T> listener : listeners) {
				listener.onChange(model);
			}
		}
	}

}