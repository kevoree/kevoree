package org.kevoree.tools.aether.framework

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.kevoree.resolver.MavenResolver

/**
 * User: ffouquet
 * Date: 25/07/11
 * Time: 15:06
 */

object AetherUtil : AetherFramework {
    override var resolver: MavenResolver = MavenResolver();
}