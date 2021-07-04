package cn.nukkit.lang;

import io.netty.util.internal.EmptyArrays;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class TranslationContainer extends TextContainer implements Cloneable {

    protected String[] params;

    public TranslationContainer(String text) {
        this(text, EmptyArrays.EMPTY_STRINGS);
    }

    public TranslationContainer(String text, String params) {
        super(text);
        this.setParameters(new String[]{params});
    }

    public TranslationContainer(String text, String... params) {
        super(text);
        this.setParameters(params);
    }

    public TranslationContainer(TranslationKey message) {
        this(message.getCode());
    }

    public TranslationContainer(TranslationKey message, String params) {
        this(message.getCode(), params);
    }

    public TranslationContainer(TranslationKey message, String... params) {
        this(message.getCode(), params);
    }

    public String[] getParameters() {
        return params;
    }

    public void setParameters(String[] params) {
        this.params = params;
    }

    public String getParameter(int i) {
        return (i >= 0 && i < this.params.length) ? this.params[i] : null;
    }

    public void setParameter(int i, String str) {
        if (i >= 0 && i < this.params.length) {
            this.params[i] = str;
        }
    }

    @Override
    public TranslationContainer clone() {
        TranslationContainer container = (TranslationContainer) super.clone();
        container.params = this.params.clone();
        return container;
    }
}
