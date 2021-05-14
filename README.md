# SamsungDesign
Samsung OneUI design for AndroidStudio

Install:

- gradle.properties
```
authToken=.....
```



- build.gradle (Project: ...)
```
allprojects {
    repositories {
        ...
        maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }
    }
}
```

- build.gradle (Module: ...)
```
dependencies {
    implementation 'com.github.Yanndroid:SamsungDesign:1.0.0'
    ...
}
```

- AndroidManifest.xml
```
<application
        ...
        android:theme="@style/SamsungTheme"
        >
        ...
    </application>
```




[v] Cardview
[v] Button
[v] Checkbox
[v] Switch 
[v] Radiobutton
[v] Switchbar
[v] Progressbar circle
[v] Progressbar Horizontal
[v] Seekbar
[v] Seekbar (smusic)
[v] Drawer
[v] Drawer divider
[  ] Color picker
[v] Collapsing Toolbar
[v] Textview
[  ] Edittext
[  ] Menu
[  ] Dialog
[  ] Bottomsheet
[  ] (Snackbar)
[  ] Spinner
[  ] Tablayout (snote)
[  ] Viewpager
[  ] Landscape 
[  ] Preferences
[  ] Tooltip
Sesl:
[v] ToggleSwitch
[v] Progressbar
[v] Switchbar
[v] Seekbar
