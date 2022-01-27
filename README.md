<h2 align="center">

[![](https://img.shields.io/maven-central/v/io.github.yanndroid/oneui?label=maven%20central)](https://mvnrepository.com/artifact/io.github.yanndroid/oneui)

</h2>
<p align="center">
<img loading="lazy" src="readme-resources/banner.png"/>
</p>

<img loading="lazy" src="https://github.com/Yanndroid/Yanndroid/blob/master/cats.gif" width="25" height="25" /> A library for Android, useful for creating Samsung's OneUI styled apps. This library contains a theme which will apply for most views (see [which](#Progress)) in your layout, and some custom OneUI views. The text which is in the custom views is translated to 90 languages, so you don't need to worry about these. Android 5.0 (api 21) and above are supported, the library also has Dark mode, Landscape, Tablet, DeX and RTL support. This library has been tested in Android Studio, but should work in other IDEs too. You can download and install the latest apk of the sample app [here](https://github.com/Yanndroid/OneUI-Design-Library/raw/master/app/release/app-release.apk). You can also check out my other apps for more examples on how to use this library. Suggestions, improvements and help are always welcome.

Huge thanks to [BlackMesa123](https://github.com/BlackMesa123) who has contributed a lot to this project.


- [Screenshots](#Screenshots)
- [Installation](#Installation)
- [Issues](#Issues)
- [Documentation](../../wiki)
- [Changelog](#Changelog)
- [More info](#More-info)
- [Special thanks](#Special-thanks-to)

## Screenshots

<p align="center"><img loading="lazy" src="readme-resources/screenshots/screenshot_1.jpg" height="350"/> <img loading="lazy" src="readme-resources/screenshots/screenshot_2.jpg" height="350"/> <img loading="lazy" src="readme-resources/screenshots/screenshot_3.jpg" height="350"/> <img loading="lazy" src="readme-resources/screenshots/screenshot_4.jpg" height="350"/></p>

[GIF version](https://github.com/Yanndroid/OneUI-Design-Library/blob/master/readme-resources/screenshots/screenrecording.gif)

## Installation

1. Add the dependency to build.gradle (Module: ...)
```gradle
dependencies {
    implementation 'io.github.yanndroid:oneui:2.2.1'
    ...
}
```
2. Apply the main theme in AndroidManifest.xml
```xml
<application
    ...
    android:theme="@style/OneUITheme"
    >
    ...
</application>
```

<details>
<summary>If you're building apps specifically for Samsung devices, also check this out.</summary>

In order to enable even more UI elements, two flags can be used:
- In AndroidManifest.xml, add the following flags inside the "application" tag:
```xml
<application
    ...>
        <!-- enable Samsung UI elements -->
        <meta-data android:name="SamsungBasicInteraction" android:value="SEP10"/>
        <!-- disable icon squircle container -->
        <meta-data android:name="com.samsung.android.icon_container.has_icon_container" android:value="true"/>
        <!-- Samsung adaptive-icon (?) -->
        <meta-data android:name="com.samsung.android.icon_container.feature_appicon" android:value="ADAPTIVEICON_SHADOW"/>
        ...
</application>
```
- This one requires decompiling your app manually with [apktool](https://github.com/iBotPeaches/Apktool); replace manually the parent in ```Platform.V21.AppCompat``` and ```Platform.V25.AppCompat``` themes in the following files:
```
res/values/styles.xml
res/values-v25/styles.xml
```
...with ```Theme.DeviceDefault.NoActionBar```.

</details>

## Issues

### Proguard

If you encounter problems with Proguard (missing classes), see this [Issue](https://github.com/Yanndroid/OneUI-Design-Library/issues/53) by [AlirezaIvaz](https://github.com/AlirezaIvaz).

### Preview render problem
Some of the custom views might not render in the preview, because this library is using a font (```sec-roboto-light```), which Android Studio (and other IDEs probably too) don't know. To temporarily bypass this problem you can simply add 
```xml
<string name="sesl_font_family_regular">sans-serif</string>
```
to your strings.xml. But don't forget to remove it afterwards for your release. Thanks to [roynatech2544](https://github.com/roynatech2544), for reporting this issue.

## Changelog

<details>
<summary>2.2.1</summary>

- OneUI4++ (views, colors, fonts, themes)
- PopupMenu improvements
- fixes & minor changes

</details>

<details>
<summary>2.2.0</summary>

- OneUI4++ (huge thanks to [BlackMesa123](https://github.com/BlackMesa123))
    - AppBar/Toolbar
    - Edge effect
    - TabLayout/BottomNavigationView
    - SwipeRefreshLayout
    - EditText
    - and more...
- ToolbarLayout & PopupMenu/Menu improvements
- icons++ (OneUI4)
- fixes & minor changes

</details>

<details>
<summary>2.1.1</summary>

- ThemeColor > ThemeUtil
- dark mode fix (ThemeUtil)
- Button/AboutPage improvements
- preference notifyChanged()
- ToolbarLayout getToolbarMenuItemView()
- OneUI4 Switch
- minor changes

</details>

<details>
<summary>2.1.0</summary>

- seekbar vertical and improved
- toolbar improvements 
    - SearchMode
    - SelectMode
    - custom Title & collapsed subtitle
    - toolbar menu
- swipeRefreshLayout
- PopupMenu improvements
- some OneUI 4 stuff
- crash fixes
- icons++

</details>

<details>
<summary>2.0.1</summary>

- fixed:
    - scroll behavior on clickable views
    - aboutpage button font and ripple 
    - popupmenu going beyond screen
    - drawerLayout back click closes drawer
    - crash on small screens/dpi
- icons++
- Spinner
- BottomNavigationView improvements

</details>

<details>
<summary>2.0.0</summary>

- contributions from BlackMesa123:
    - AppBarLayout (& friends)
    - Preferences
    - Classic Color Picker
    - Dialogs
    - Tooltip
    - TabLayout
    - BottomNavigationView
    - Layouts rework
    - NestedScrollView, RecyclerView, Round Layouts
    - and much more (most of the stuff in this release)
- a lot of fixes and improvements
- support for api 21+
- Snackbar & PopupMenu
- more icons
- now available on mavencentral

</details>

<details>
<summary>1.3.0</summary>

- renamed library
- getView methode added
- splash screen display size fix
- minor changes

</details>

<details>
<summary>1.2.2</summary>

- Scrollbar
- AboutPage
- RelatedCard
- corner fix
- language update
- customizable splash animation
- expanded attribute for toolbar
- fixed landscape toolbar height
- improved orientation switching
- button text fix
- status & navigation bar dim on drawer slide
- added changelog to readme

</details>

<details>
<summary>1.2.1</summary>

- landscape support
- tablet support
- dex support
- expandable attribute for toolbar
- toolbar subtitle color
- added Header style

</details>

<details>
<summary>1.2.0</summary>

- colorPicker
- color Changer
- readme finished
- much more icons
- rtl support
- translated to 90 languages

</details>

<details>
<summary>1.1.3 - 1.0.0</summary>

- initial release/publish
- most of the stuff (I don't remember anymore...)

</details>
<br/>

## More info
- [Official OneUI Design Guide](https://design.samsung.com/global/contents/one-ui/download/oneui_design_guide_eng.pdf)
- [Optimizing for DeX](https://developer.samsung.com/samsung-dex/modify-optimizing.html)
- [Samsung's EULA](https://www.samsung.com/sg/Legal/SamsungLegal-EULA/)

## Special thanks to:
- [Samsung](https://www.samsung.com/) for their awesome OneUI Design. :)
- [BlackMesa123](https://github.com/BlackMesa123) for a lot of OneUI stuff, more compatibility and his experience.
- [TenSeventy7](https://github.com/TenSeventy7) for some stuff and help.
- [leonbcode](https://github.com/leonbcode) for github actions, so this library is always up-to-date.
- All the [Contributors](https://github.com/Yanndroid/OneUI-Design-Library/graphs/contributors) and Issue Reporters.
