# Samsung OneUi Design
A library for Android, which make your app look like Samsung's OneUI.
Tested in AndroidStudio.

Excuse my bad english, if you want you can correct it :)

## Screenshots


## Installation

- add the dependencies to build.gradle (Module: ...)
```gradle
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

- apply the theme in AndroidManifest.xml
```xml
<application
    ...
    android:theme="@style/SamsungTheme"
    >
    ...
</application>
```

## Usage
### DrawerLayout
```xml
<de.dlyt.yanndroid.samsung.layout.DrawerLayout 
    android:id="@+id/drawer_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:drawer_icon="..."
    app:drawer_viewId="@id/viewindrawer"
    app:toolbar_subtitle="..."
    app:toolbar_title="...">

    <View
        android:id="@+id/viewindrawer"
        ... />

    <!--other views-->

</de.dlyt.yanndroid.samsung.layout.DrawerLayout>

```
The view with the ID specified in ```app:drawer_viewId="..."``` will be shown in the drawer and the rest of the children on the main screen.  

```app:toolbar_title="..."``` and ```app:toolbar_subtitle="..."``` are setting the title and subtitle in the toolbar. If nothing is set for the subtitle, the toolbar will adjust the title position to match the space.  

The drawable in ```app:drawer_icon="..."``` is the little icon at the top right in the drawer pane. There are already some stock Samsung icon included in the library (see <todo>).




## Progress

- [x] Cardview
- [x] Checkbox
- [x] Switch 
- [x] Radiobutton
- [x] Switchbar
- [x] Progressbar circle
- [x] Progressbar horizontal
- [x] Seekbar
- [x] Drawer
- [x] Drawer divider
- [x] SeslToggleSwitch
- [x] SeslProgressbar
- [x] SeslSwitchbar
- [x] SeslSeekbar
- [x] Collapsing Toolbar
- [x] Button (incomplete)
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
