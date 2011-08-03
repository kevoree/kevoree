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
package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2006-2008 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides utilities for working with collections.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: CollectionUtils.java 7031 2008-05-22 09:41:31Z mark $
 */
public final class CollectionUtils
{
    // constructors -----------------------------------------------------------

    private CollectionUtils()
    {
        throw new AssertionError();
    }

    // public methods ---------------------------------------------------------

    public static <T> Set<T> defaultSet( Set<T> set )
    {
        return defaultSet( set, Collections.<T>emptySet() );
    }

    public static <T> Set<T> defaultSet( Set<T> set, Set<T> defaultSet )
    {
        return isEmpty( set ) ? defaultSet : set;
    }

    public static boolean isEmpty( Collection<?> collection )
    {
        return ( collection == null ) || collection.isEmpty();
    }

    public static <E> Set<E> genericSet( Set<?> set, Class<E> elementType )
    {
        return genericCollection( set, elementType, new HashSet<E>() );
    }

    public static <E> List<E> genericList( List<?> list, Class<E> elementType )
    {
        return genericCollection( list, elementType, new ArrayList<E>() );
    }

    // private methods --------------------------------------------------------

    private static <T extends Collection<E>, E> T genericCollection( Collection<?> collection, Class<E> elementType,
                                                                     T genericCollection )
    {
        for ( Object element : collection )
        {
            genericCollection.add( elementType.cast( element ) );
        }

        return genericCollection;
    }
}