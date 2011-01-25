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
///**
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 	http://www.gnu.org/licenses/lgpl-3.0.txt
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package org.kevoree.remote.fileserver;
//
//
//import java.net.InetSocketAddress;
//import java.util.concurrent.Executors;
//import org.jboss.netty.bootstrap.ServerBootstrap;
//import org.jboss.netty.channel.ChannelException;
//import org.jboss.netty.channel.ChannelHandler;
//import org.jboss.netty.channel.ChannelPipeline;
//import org.jboss.netty.channel.ChannelPipelineFactory;
//import org.jboss.netty.channel.Channels;
//import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
//import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
//import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
//import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
//import org.jboss.netty.handler.stream.ChunkedWriteHandler;
//
///**
// *
// * @author ffouquet
// */
//public class ProvisioningServer {
//
//    public void start() {
//        if (System.getProperty("org.kevoree.remote.repository") != null) {
//            int nport = startServer(new ChunkedWriteHandler(), new FileServerHandler(System.getProperty("org.kevoree.remote.repository")));
//            System.out.println("Start provisioning server on port => " + nport);
//        }
//    }
//
//    public void stop() {
//        bootstrap.releaseExternalResources();
//    }
//    private int port;
//
//    public static class PipelineFactory implements ChannelPipelineFactory {
//
//        private ChannelHandler[] handlers;
//        private ChannelHandler first = null;
//
//        public PipelineFactory(ChannelHandler... handlers) {
//            this.handlers = handlers;
//        }
//
//        public void setFirst(ChannelHandler first) {
//            this.first = first;
//        }
//
//        public ChannelPipeline getPipeline() throws Exception {
//            ChannelPipeline pipeline = Channels.pipeline();
//
//            if (first != null) {
//                pipeline.addLast("first", first);
//            }
//            pipeline.addLast("decoder", new HttpRequestDecoder());
//            pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
//            pipeline.addLast("encoder", new HttpResponseEncoder());
//
//            for (ChannelHandler handler : handlers) {
//                pipeline.addLast("handler_" + handler.toString(), handler);
//            }
//
//            return pipeline;
//        }
//    }
//
//    private int startServer(ChannelHandler... handlers) {
//        return startServer(new PipelineFactory(handlers));
//    }
//    private ServerBootstrap bootstrap = null;
//
//    private int startServer(ChannelPipelineFactory factory) {
//        bootstrap = new ServerBootstrap(
//                new NioServerSocketChannelFactory(
//                Executors.newCachedThreadPool(),
//                Executors.newCachedThreadPool()));
//
//        bootstrap.setPipelineFactory(factory);
//
//        port = bindBootstrap(bootstrap, 0);
//        return port;
//    }
//
//    private int bindBootstrap(ServerBootstrap bootstrap, int retryCount) {
//        try {
//            bootstrap.bind(new InetSocketAddress("0.0.0.0", 18080 + retryCount));
//            System.out.println(18080 + retryCount);
//        } catch (ChannelException e) {
//            retryCount++;
//            if (retryCount < 100) {
//                return bindBootstrap(bootstrap, retryCount);
//            }
//        }
//
//        return 18080 + retryCount;
//    }
//}
