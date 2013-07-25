package org.kevoree.tools.aether.framework.android;

import org.kevoree.DeployUnit;
import org.kevoree.api.service.core.classloading.DeployUnitResolver;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.log.Log;
import org.kevoree.tools.aether.framework.AetherUtil;
import org.kevoree.tools.aether.framework.JCLContextHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by duke on 25/07/13.
 */
public class AndroidJCLContextHandler extends JCLContextHandler {

    private android.content.Context ctx;
    private ClassLoader parent;

    public AndroidJCLContextHandler(android.content.Context ctx, ClassLoader parent) {
        this.ctx = ctx;
        this.parent = parent;
    }

    public KevoreeJarClassLoader installDeployUnitNoFileInternals(DeployUnit du) {
        File resolvedFile = null;
        for (DeployUnitResolver resolver : getResolvers()) {
            try {
                resolvedFile = resolver.resolve(du);
                break;
            } catch (Exception e) {
            }
        }
        if (resolvedFile == null) {
            resolvedFile = AetherUtil.instance$.resolveDeployUnit(du);
        }
        if (resolvedFile != null) {
            return installDeployUnitInternals(du, resolvedFile);
        } else {
            Log.error("Error while resolving deploy unit " + du.getUnitName());
            return null;
        }
    }


    @Override
    public KevoreeJarClassLoader installDeployUnitInternals(DeployUnit du, File file) {
        KevoreeJarClassLoader previousKCL = getKCLInternals(du);
        KevoreeJarClassLoader res = null;
        if (previousKCL != null) {
            Log.debug("Take already installed {}", buildKEY(du));
            res = previousKCL;
        } else {
            String cleankey = buildKEY(du).replace(File.separator, "_");
            AndroidKevoreeJarClassLoader newcl = new AndroidKevoreeJarClassLoader(cleankey, ctx, parent);
            newcl.setLazyLoad(false);
            newcl.add(file.getAbsolutePath());
            getKcl_cache().put(buildKEY(du), newcl);
            getKcl_cache_file().put(buildKEY(du), file);
            //TRY TO RECOVER FAILED LINK
            //TRY TO RECOVER FAILED LINK
            if (getFailedLinks().containsKey(buildKEY(du))) {
                for (KevoreeJarClassLoader toLinkKCL : getFailedLinks().get(buildKEY(du))) {
                    toLinkKCL.addSubClassLoader(newcl);
                    newcl.addWeakClassLoader(toLinkKCL);
                    Log.debug("UnbreakLink " + du.getUnitName() + "->" + toLinkKCL.getLoadedURLs().get(0));

                }
                getFailedLinks().remove(buildKEY(du));
                Log.debug("Failed Link {} remain size : {}", du.getUnitName(), getFailedLinks().size());
            }
            for (DeployUnit rLib : du.getRequiredLibs()) {
                KevoreeJarClassLoader kcl = getKCLInternals(rLib);
                if (kcl != null) {
                    newcl.addSubClassLoader(kcl);
                    kcl.addWeakClassLoader(newcl);

                    for (DeployUnit rLibIn : du.getRequiredLibs()) {
                        if (rLibIn != rLib) {
                            KevoreeJarClassLoader kcl2 = getKCLInternals(rLibIn);
                            if (kcl2 != null) {
                                kcl.addWeakClassLoader(kcl2);
                            }
                        }
                    }
                } else {
                    Log.debug("Fail link ! Warning ");
                    List<KevoreeJarClassLoader> pendings = getFailedLinks().get(buildKEY(rLib));
                    if (pendings == null) {
                        pendings = new ArrayList<KevoreeJarClassLoader>();
                        getFailedLinks().put(buildKEY(rLib), pendings);
                    }
                    pendings.add(newcl);
                }
            }
            res = newcl;
        }
        return res;
    }


}
