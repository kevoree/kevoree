package org.kevoree.library.javase.webserver.wordpress;

import com.caucho.quercus.*;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.QuercusValueException;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.lib.*;
import com.caucho.quercus.lib.date.DateModule;
import com.caucho.quercus.lib.db.MysqlModule;
import com.caucho.quercus.lib.db.MysqliModule;
import com.caucho.quercus.lib.file.FileModule;
import com.caucho.quercus.lib.regexp.RegexpModule;
import com.caucho.quercus.lib.string.StringModule;
import com.caucho.quercus.page.QuercusPage;
import com.caucho.vfs.FilePath;
import com.caucho.vfs.Path;
import com.caucho.vfs.Vfs;
import com.caucho.vfs.WriteStream;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.webserver.*;
import org.kevoree.library.javase.webserver.servlet.LocalServletRegistry;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/12/11
 * Time: 23:05
 * To change this template use File | Settings | File Templates.
 */
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "mysql_host"),
        @DictionaryAttribute(name = "mysql_db_name"),
        @DictionaryAttribute(name = "mysql_login"),
        @DictionaryAttribute(name = "mysql_pass")
})
public class WordPressPage extends AbstractPage {

    private File rootDir = null;
    private LocalServletRegistry servletRepository = null;// new LocalServletRegistry();

    @Override
    public void startPage() {
        servletRepository = new LocalServletRegistry((Bundle)this.getDictionary().get("osgi.bundle"));
        super.startPage();
        InputStream zipStream = this.getClass().getClassLoader().getResourceAsStream("wordpress-3.3-fr_FR.zip");
        if (zipStream != null) {
            rootDir = new File(ZipHelper.unzipToTempDir(zipStream).getAbsolutePath() + File.separator + "wordpress");
            logger.debug("Install base wordpress in " + rootDir.getAbsolutePath());
            File outFile = new File(rootDir.getAbsolutePath() + File.separator + "wp-config.php");
            TemplateHelper.copyAndReplace(this.getClass().getClassLoader().getResourceAsStream("wp-config.tphp"), outFile, this);
            servletRepository.registerServlet("php", new PhpWrapperServlet());
        }
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {

        String url = request.getUrl();
        if (url.trim().endsWith("/")) {
            url = url + "index.php";
        }

        if (servletRepository.tryURL(url, request, response)) {
            return response;
        }
        if (FileServiceHelper.checkStaticFileFromDir("index.html", this, request, response, rootDir.getAbsolutePath())) {
            return response;
        }
        response.setContent("Bad request");
        return response;
    }

    class PhpWrapperServlet extends HttpServlet {
        // private final L10N L = new L10N(PhpWrapperServlet.class);
        private final Logger log = LoggerFactory.getLogger(this.getClass());
        protected QuercusContext _quercus;
        protected ServletConfig _config;
        protected ServletContext _servletContext;

        /**
         * initialize the script manager.
         */
        public void init(ServletConfig config)
                throws ServletException {
            _config = config;
            _servletContext = config.getServletContext();
            Path pwd = new FilePath(rootDir.getAbsolutePath());
            getQuercus().setPwd(pwd);
            getQuercus().init();
            getQuercus().start();
        }

        protected void initImpl(ServletConfig config)
                throws ServletException {
        }

        /**
         * Sets the profiling mode
         */
        public void setProfileProbability(double probability) {
        }

        /**
         * Service.
         */
        public void service(HttpServletRequest request,
                            HttpServletResponse response)
                throws ServletException, IOException {
            Env env = null;
            WriteStream ws = null;
            try {
                QuercusPage page = null;
                try {
                    String urlpath = getLastParam(request.getRequestURI());

                    if (urlpath.trim().endsWith("/")) {
                        urlpath = urlpath + "index.php";
                    }
                    if(urlpath.equals("")){
                        urlpath = "index.php";
                    }
                    logger.debug("-->"+urlpath);


                    if (urlpath != null && urlpath != "" && urlpath != "/") {
                        Path path = new FilePath(rootDir.getAbsolutePath() + File.separator + urlpath);
                        page = getQuercus().parse(path);
                    } else {
                        Path path = new FilePath(rootDir.getAbsolutePath() + File.separator + "index.php");
                        page = getQuercus().parse(path);
                    }
                } catch (FileNotFoundException ex) {
                    // php/2001
                    log.debug("File not found ", ex);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                ws = openWrite(response);
                // php/6006
                ws.setNewlineString("\n");
                QuercusContext quercus = getQuercus();
                env = quercus.createEnv(page, ws, request, response);
                quercus.setServletContext(_servletContext);
                try {
                    env.start();
                    // php/2030, php/2032, php/2033
                    // Jetty hides server classes from web-app
                    // http://docs.codehaus.org/display/JETTY/Classloading
                    // env.setGlobalValue("request", env.wrapJava(request));
                    // env.setGlobalValue("response", env.wrapJava(response));
                    // env.setGlobalValue("servletContext", env.wrapJava(_servletContext));
                    StringValue prepend = quercus.getIniValue("auto_prepend_file").toStringValue(env);
                    if (prepend.length() > 0) {
                        Path prependPath = env.lookup(prepend);
                        if (prependPath == null)
                            env.error("auto_prepend_file '{0}' not found." + prepend);
                        else {
                            QuercusPage prependPage = getQuercus().parse(prependPath);
                            prependPage.executeTop(env);
                        }
                    }
                    env.executeTop();
                    StringValue append
                            = quercus.getIniValue("auto_append_file").toStringValue(env);
                    if (append.length() > 0) {
                        Path appendPath = env.lookup(append);

                        if (appendPath == null)
                            env.error("auto_append_file '{0}' not found." + append);
                        else {
                            QuercusPage appendPage = getQuercus().parse(appendPath);
                            appendPage.executeTop(env);
                        }
                    }
                    //   return;
                } catch (QuercusExitException e) {
                    throw e;
                } catch (QuercusErrorException e) {
                    throw e;
                } catch (QuercusLineRuntimeException e) {
                    logger.debug("", e);
                    ws.println(e.getMessage());
                    //  return;
                } catch (QuercusValueException e) {
                    logger.debug("", e);
                    ws.println(e.toString());
                    //  return;
                } catch (Throwable e) {
                    if (response.isCommitted())
                        e.printStackTrace(ws.getPrintWriter());
                    ws = null;
                    throw e;
                } finally {
                    if (env != null)
                        env.close();
                    if (ws != null && env.getDuplex() == null)
                        ws.close();
                }
            } catch (QuercusDieException e) {
                // normal exit
                //logger.debug("", e);
            } catch (QuercusExitException e) {
                // normal exit
                //logger.debug("", e);
            } catch (QuercusErrorException e) {
                // error exit
                logger.debug("Php interpreter error ", e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new ServletException(e);
            }
        }

        protected WriteStream openWrite(HttpServletResponse response)
                throws IOException {
            WriteStream ws;
            OutputStream out = response.getOutputStream();
            ws = Vfs.openWrite(out);
            return ws;
        }

        protected QuercusContext getQuercus() {
            synchronized (this) {
                if (_quercus == null)
                    _quercus = new QuercusContext();
                _quercus.addModule(new DateModule());
                _quercus.addModule(new StringModule());
                _quercus.addModule(new FileModule());
                _quercus.addModule(new VariableModule());
                _quercus.addModule(new MiscModule());
                _quercus.addModule(new NetworkModule());
                _quercus.addModule(new OptionsModule());
                _quercus.addModule(new OutputModule());
                _quercus.addModule(new TokenModule());
                _quercus.addModule(new UrlModule());
                _quercus.addModule(new ExifModule());
                _quercus.addModule(new FunctionModule());
                _quercus.addModule(new HashModule());
                _quercus.addModule(new HtmlModule());
                _quercus.addModule(new HttpModule());
                _quercus.addModule(new ImageModule());
                _quercus.addModule(new JavaModule());
                _quercus.addModule(new MathModule());
                _quercus.addModule(new MhashModule());
                _quercus.addModule(new ApacheModule());
                _quercus.addModule(new ApcModule());
                _quercus.addModule(new ArrayModule());
                _quercus.addModule(new BcmathModule());
                _quercus.addModule(new ClassesModule());
                _quercus.addModule(new CtypeModule());
                _quercus.addModule(new ErrorModule());
                _quercus.addModule(new RegexpModule());
                _quercus.addModule(new MysqlModule());
                _quercus.addModule(new MysqliModule());
            }

            return _quercus;
        }

        /**
         * Destroys the quercus instance.
         */
        public void destroy() {
            _quercus.close();
        }
    }

}