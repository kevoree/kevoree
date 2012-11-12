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
package jexxus.common;

/**
 * Used for the requirements of message delivery.
 * 
 * @author Jason
 * 
 */
public class Delivery {

	/**
	 * These messages will always reach the destination unless there is a
	 * network failure.
	 */
	public static final Delivery RELIABLE = new Delivery("TCP");

	/**
	 * These messages are not guaranteed to reach the destination, but they have
	 * the advantage of being faster.
	 */
	public static final Delivery UNRELIABLE = new Delivery("UDP");

	private final String type;

	private Delivery(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
