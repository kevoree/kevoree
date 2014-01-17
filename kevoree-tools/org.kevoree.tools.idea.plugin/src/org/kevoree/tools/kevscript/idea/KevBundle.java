package org.kevoree.tools.kevscript.idea;

import com.intellij.CommonBundle;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.ResourceBundle;

/**
 * Created by duke on 16/01/2014.
 */
public class KevBundle {

    private static Reference<ResourceBundle> ourBundle;

    @NonNls
    private static final String BUNDLE = "org.kevoree.tools.kevscript.idea.KevBundle";

    public static String message(
            @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (ourBundle != null) bundle = ourBundle.get();
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }

}
