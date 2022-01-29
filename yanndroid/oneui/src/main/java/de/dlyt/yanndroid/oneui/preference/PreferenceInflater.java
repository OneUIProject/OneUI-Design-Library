package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

class PreferenceInflater {
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = new Class<?>[]{Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<?>> CONSTRUCTOR_MAP = new HashMap<>();
    private static final String INTENT_TAG_NAME = "intent";
    private static final String EXTRA_TAG_NAME = "extra";
    @NonNull
    private final Context mContext;
    private final Object[] mConstructorArgs = new Object[2];
    private PreferenceManager mPreferenceManager;
    private String[] mDefaultPackages;

    public PreferenceInflater(@NonNull Context context, PreferenceManager preferenceManager) {
        mContext = context;
        init(preferenceManager);
    }

    private void init(PreferenceManager preferenceManager) {
        mPreferenceManager = preferenceManager;

        setDefaultPackages(new String[]{Preference.class.getPackage().getName() + ".", SwitchPreference.class.getPackage().getName() + "."});
    }

    public void setDefaultPackages(String[] defaultPackage) {
        mDefaultPackages = defaultPackage;
    }

    public String[] getDefaultPackages() {
        return mDefaultPackages;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public Preference inflate(int resource, @Nullable PreferenceGroup root) {
        XmlResourceParser parser = getContext().getResources().getXml(resource);
        try {
            return inflate(parser, root);
        } finally {
            parser.close();
        }
    }

    @NonNull
    public Preference inflate(XmlPullParser parser, @Nullable PreferenceGroup root) {
        synchronized (mConstructorArgs) {
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            mConstructorArgs[0] = mContext;
            final Preference result;

            try {
                int type;
                do {
                    type = parser.next();
                } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);

                if (type != XmlPullParser.START_TAG) {
                    throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
                }

                Preference xmlRoot = createItemFromTag(parser.getName(), attrs);

                result = onMergeRoots(root, (PreferenceGroup) xmlRoot);

                rInflate(parser, result, attrs);

            } catch (InflateException e) {
                throw e;
            } catch (XmlPullParserException e) {
                final InflateException ex = new InflateException(e.getMessage());
                ex.initCause(e);
                throw ex;
            } catch (IOException e) {
                final InflateException ex = new InflateException(parser.getPositionDescription() + ": " + e.getMessage());
                ex.initCause(e);
                throw ex;
            }

            return result;
        }
    }

    private @NonNull PreferenceGroup onMergeRoots(PreferenceGroup givenRoot, @NonNull PreferenceGroup xmlRoot) {
        if (givenRoot == null) {
            xmlRoot.onAttachedToHierarchy(mPreferenceManager);
            return xmlRoot;
        } else {
            return givenRoot;
        }
    }

    private Preference createItem(@NonNull String name, @Nullable String[] prefixes, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        Constructor<?> constructor = CONSTRUCTOR_MAP.get(name);

        try {
            if (constructor == null) {
                final ClassLoader classLoader = mContext.getClassLoader();
                Class<?> clazz = null;
                if (prefixes == null || prefixes.length == 0) {
                    clazz = Class.forName(name, false, classLoader);
                } else {
                    ClassNotFoundException notFoundException = null;
                    for (final String prefix : prefixes) {
                        try {
                            clazz = Class.forName(prefix + name, false, classLoader);
                            break;
                        } catch (final ClassNotFoundException e) {
                            notFoundException = e;
                        }
                    }
                    if (clazz == null) {
                        if (notFoundException == null) {
                            throw new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
                        } else {
                            throw notFoundException;
                        }
                    }
                }
                constructor = clazz.getConstructor(CONSTRUCTOR_SIGNATURE);
                constructor.setAccessible(true);
                CONSTRUCTOR_MAP.put(name, constructor);
            }

            Object[] args = mConstructorArgs;
            args[1] = attrs;
            return (Preference) constructor.newInstance(args);

        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Exception e) {
            final InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(e);
            throw ie;
        }
    }

    protected Preference onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createItem(name, mDefaultPackages, attrs);
    }

    private Preference createItemFromTag(String name, AttributeSet attrs) {
        try {
            final Preference item;

            if (-1 == name.indexOf('.')) {
                item = onCreateItem(name, attrs);
            } else {
                item = createItem(name, null, attrs);
            }

            return item;

        } catch (InflateException e) {
            throw e;

        } catch (ClassNotFoundException e) {
            final InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class (not found)" + name);
            ie.initCause(e);
            throw ie;

        } catch (Exception e) {
            final InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(e);
            throw ie;
        }
    }

    private void rInflate(@NonNull XmlPullParser parser, Preference parent, final AttributeSet attrs) throws XmlPullParserException, IOException {
        final int depth = parser.getDepth();

        int type;
        while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            final String name = parser.getName();

            if (INTENT_TAG_NAME.equals(name)) {
                final Intent intent;

                try {
                    intent = Intent.parseIntent(getContext().getResources(), parser, attrs);
                } catch (IOException e) {
                    XmlPullParserException ex = new XmlPullParserException("Error parsing preference");
                    ex.initCause(e);
                    throw ex;
                }

                parent.setIntent(intent);
            } else if (EXTRA_TAG_NAME.equals(name)) {
                getContext().getResources().parseBundleExtra(EXTRA_TAG_NAME, attrs, parent.getExtras());
                try {
                    skipCurrentTag(parser);
                } catch (IOException e) {
                    XmlPullParserException ex = new XmlPullParserException("Error parsing preference");
                    ex.initCause(e);
                    throw ex;
                }
            } else {
                final Preference item = createItemFromTag(name, attrs);
                ((PreferenceGroup) parent).addItemFromInflater(item);
                rInflate(parser, item, attrs);
            }
        }

    }

    private static void skipCurrentTag(@NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        do {
            type = parser.next();
        } while (type != XmlPullParser.END_DOCUMENT && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth));
    }

}
