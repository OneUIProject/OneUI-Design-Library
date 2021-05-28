# Samsung OneUi Design
Samsung OneUI design library for AndroidStudio

Installation:

- build.gradle (Module: ...)
```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/yanndroid/SamsungOneUi")
        credentials {
            username = "Yanndroid"
            password = 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
        }
    }
}

dependencies {
    implementation 'de.dlyt.yanndroid:samsung:1.1.0'
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



Progress:

- [x] Cardview
- [x] Button (incomplete)
- [x] Checkbox
- [x] Switch 
- [x] Radiobutton
- [x] Switchbar
- [x] Progressbar circle
- [x] Progressbar Horizontal
- [x] Seekbar
- [x] Drawer
- [x] Drawer divider
- [x] SeslToggleSwitch
- [x] SeslProgressbar
- [x] SeslSwitchbar
- [x] SeslSeekbar
- [x] Collapsing Toolbar
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
- [ ] Color picker
- [ ] (Textview)
- [ ] (Edittext)
