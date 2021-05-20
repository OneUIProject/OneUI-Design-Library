# SamsungDesign
Samsung OneUI design for AndroidStudio

Install:

- build.gradle (Module: ...)
```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/yanndroid/SamsungDesign")
        credentials {
            username = "Yanndroid"
            password = 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
        }
    }
}

dependencies {
    implementation 'de.dlyt.yanndroid:samsung:1.0.0'
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
