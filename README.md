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




- [x] Cardview
- [x] Button
- [x] Checkbox
- [x] Switch 
- [x] Radiobutton
- [x] Switchbar
- [x] Progressbar circle
- [x] Progressbar Horizontal
- [x] Seekbar
- [x] Drawer
- [x] Drawer divider
- [ ] Color picker
- [x] Collapsing Toolbar
- [ ] Textview
- [ ] Edittext
- [ ] Menu
- [ ] Dialog
- [ ] Bottomsheet
- [ ] Snackbar
- [ ] Spinner
- [ ] Tablayout
- [ ] Viewpager
- [ ] Landscape 
- [ ] Preferences
- [ ] Tooltip
- [x] SeslToggleSwitch
- [x] SeslProgressbar
- [x] SeslSwitchbar
- [x] SeslSeekbar
