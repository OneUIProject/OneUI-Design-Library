<h2 align="center">

[![](https://img.shields.io/maven-central/v/io.github.yanndroid/oneui?label=maven%20central)](https://mvnrepository.com/artifact/io.github.yanndroid/oneui)
[![](https://img.shields.io/github/last-commit/Yanndroid/OneUI-Design-Library)](https://github.com/Yanndroid/OneUI-Design-Library/commits/master)
[![](https://img.shields.io/github/issues-raw/Yanndroid/OneUI-Design-Library?color=%23ff4400)](https://github.com/Yanndroid/OneUI-Design-Library/issues)
[![](https://img.shields.io/github/issues-pr-raw/Yanndroid/OneUI-Design-Library?color=%23bb00bb)](https://github.com/Yanndroid/OneUI-Design-Library/pulls)
[![](https://img.shields.io/github/contributors/Yanndroid/OneUI-Design-Library)](https://github.com/Yanndroid/OneUI-Design-Library/graphs/contributors)

</h2>
<p align="center">
<img loading="lazy" src="readme-resources/banner.png"/>
</p>

<img loading="lazy" src="https://github.com/Yanndroid/Yanndroid/blob/master/cats.gif" width="25" height="25" /> A library for Android, useful for creating Samsung's OneUI styled apps. This library contains a theme which will apply for most views (see [which](#Progress)) in your layout, and some custom OneUI views. The text which is in the custom views is translated to 90 languages, so you don't need to worry for these. Android 5.0 (api 21) and above are supported, the library also has Dark mode, Landscape, Tablet, DeX and RTL support. This library has been tested in Android Studio, but should work in other IDEs too. You can download and install the latest apk of the sample app [here](https://github.com/Yanndroid/OneUI-Design-Library/raw/master/app/release/app-release.apk). You can also check out my other apps for more examples on how to use this library. Suggestions, improvements and help are always welcome.

Huge thanks to [BlackMesa123](https://github.com/BlackMesa123) who has contributed a lot to this project.


- [Screenshots](#Screenshots)
- [Installation](#Installation)
- [Issues](#Issues)
- [Documentation](#Documentation)
- [Progress](#Progress)
- [Changelog](#Changelog)
- [More info](#More-info)
- [Special thanks](#Special-thanks-to)

## Screenshots

<p align="center"><img loading="lazy" src="readme-resources/screenshots/screenshot_1.jpg" height="350"/> <img loading="lazy" src="readme-resources/screenshots/screenshot_2.jpg" height="350"/> <img loading="lazy" src="readme-resources/screenshots/screenshot_3.jpg" height="350"/> <img loading="lazy" src="readme-resources/screenshots/screenshot_4.jpg" height="350"/>

[GIF version](https://github.com/Yanndroid/OneUI-Design-Library/blob/master/readme-resources/screenshots/screenrecording.gif)</p>

## Installation
v2.0.0 and future versions are (and only will be) available on mavenCentral. For older ones see below. (v1.3.0 was published to MavenCentral during development for testing purpose and should **not** be used as it's unstable and incomplete.)

1. Add the dependency to build.gradle (Module: ...)
```gradle
dependencies {
    implementation 'io.github.yanndroid:oneui:2.2.0'
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


<details>
<summary>Older versions</summary>

### with [Jitpack](https://jitpack.io/#Yanndroid/SamsungOneUi):
1. Add jitpack to build.gradle (Project: ...)
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
	}
}
```
2. Add the dependency to build.gradle (Module: ...)
```gradle
dependencies {
    implementation 'com.github.Yanndroid:OneUI-Design-Library:1.3.0'
    ...
}
```
3. Apply the main theme in AndroidManifest.xml
```xml
<application
    ...
    android:theme="@style/SamsungTheme"
    >
    ...
</application>
```


### with Github Packages:
1. Create a [new token](https://github.com/settings/tokens) with ```read:packages``` permission.
2. Add the dependency to build.gradle (Module: ...)
```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Yanndroid/OneUI-Design-Library")
            credentials {
                username = "your username"
                password = "your token"
            }
    }
}


dependencies {
    implementation 'de.dlyt.yanndroid:oneui:1.3.0'
    ...
}
```

3. Apply the main theme in AndroidManifest.xml
```xml
<application
    ...
    android:theme="@style/SamsungTheme"
    >
    ...
</application>
```

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

## Documentation
In general, most of the views are styled automatically when you apply ```android:theme="@style/OneUITheme"``` in AndroidManifest.xml, the usage of the custom views in the library however is needed to achieve the best results.

- [DrawerLayout](#DrawerLayout)
- [ToolbarLayout](#ToolbarLayout)
- [SplashView](#SplashView)
- [AboutPage](#AboutPage)
- [SwitchBarLayout](#SwitchBarLayout)
- [CoordinatorLayout](#CoordinatorLayout)
- [Round Layouts](#Round-Layouts)
- [NestedScrollView](#NestedScrollView)
- [RecyclerView](#RecyclerView)
- [SwipeRefreshLayout](#SwipeRefreshLayout)
- [Button](#Button)
- [SeekBar](#SeekBar)
- [ProgressBar](#ProgressBar)
- [SwitchBar](#SwitchBar)
- [Spinner](#Spinner)
- [OptionButton](#OptionButton)
- [OptionGroup](#OptionGroup)
- [RelatedCard](#RelatedCard)
- [BottomNavigationView](#BottomNavigationView)
- [TabLayout](#TabLayout)
- [ViewPager](#ViewPager)
- [AlertDialog](#AlertDialog)
- [ProgressDialog](#ProgressDialog)
- [ClassicColorPickerDialog](#ClassicColorPickerDialog)
- [DetailedColorPickerDialog](#DetailedColorPickerDialog)
- [Preferences](#Preferences)
- [PopupMenu](#PopupMenu)
- [Tooltip](#Tooltip)
- [Snackbar](#Snackbar)
- [Advanced](#Advanced)
- [Icons](#Icons)
- [Color theme](#Color-theme)
  - [Entire App](#1-entire-App)
  - [Single/Multiple activities](#2-singleMultiple-activities)
  - [Via Code](#3-Via-Code)
- [App Icon](#App-Icon)
- [OneUI 4](#OneUI-4)

### DrawerLayout
"Ready-to-go" DrawerLayout with included Samsung's AppBar.

<img loading="lazy" align="left" src="readme-resources/screenshots/drawerlayout.gif" width="150"/>

```xml
<de.dlyt.yanndroid.oneui.layout.DrawerLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout="..."
    app:drawer_icon="..."
    app:toolbar_title="..."
    app:toolbar_subtitle="..."
    app:toolbar_expanded="...">

</de.dlyt.yanndroid.oneui.layout.DrawerLayout>
```
The children of this view can be at five different location: on the **main screen**, in the **drawer**, in the **footer** (useful for views like BottomNavigationView), in the **appbar header** as a custom title or in the **root layout** (views like FAB). To specify the location of each child you can set the attribute ```app:layout_location``` of the child to either ```main_content``` (default), ```drawer_panel```, ```footer```, ```appbar_header``` or ```root```.

```app:toolbar_title``` and ```app:toolbar_subtitle``` can be used to set the title and subtitle of the AppBar and Toolbar. The AppBar status is set to expanded by default, you can simply set ```app:toolbar_expanded``` to false if you want it to be collapsed. On small screens/dpi the toolbar will not expand.

The drawable in ```app:drawer_icon="..."``` is the little icon in the header of the drawer panel. There are already some stock OneUI [icons](#Icons) included in the library you can use.

To make sure that AppBar scrolling behavior works correctly, the child set in ```main_content``` should either be [RecyclerView](#RecyclerView) or [NestedScrollView](#NestedScrollView) with ```app:layout_behavior``` set to ```@string/sesl_appbar_scrolling_view_behavior```. The stock RecyclerView and NestedScrollView might also work but probably won't behave correctly.

For further customization you can use ```android:layout``` to apply your own layout to this view, but keep in mind that you should add all the views and ids which are in the default layout, or your app might crash.
<br clear="left"/>

#### Methods
Return the [ToolbarLayout](#ToolbarLayout).
```java
public ToolbarLayout getToolbarLayout()
```
Manage the DrawerButton (icon in the top-right corner of the panel).
```java
public void setDrawerButtonIcon(Drawable drawerIcon)
public void setDrawerButtonTooltip(CharSequence tooltipText)
public void setDrawerButtonOnClickListener(OnClickListener listener)
```
Toolbar methods. (for more, use ```getToolbarLayout()``` and it's methods)
```java
public void setToolbarTitle(CharSequence title)
public void setToolbarTitle(CharSequence expandedTitle, CharSequence collapsedTitle)
public void setToolbarSubtitle(String subtitle)
public void setToolbarExpanded(boolean expanded, boolean animate)
```
Show badges on the DrawerButton and NavigationIcon (use ```ToolbarLayout.N_BADGE``` and ```DrawerLayout.N_BADGE``` or either a number).
```java
public void setButtonBadges(int navigationIcon, int drawerIcon)
public void setDrawerButtonBadge(int count) //only the drawerButton
```
Open/close the drawer panel with an optional animation.
```java
public void setDrawerOpen(Boolean open, Boolean animate)
```

### ToolbarLayout
"Ready-to-go" Samsung's AppBar.

<img loading="lazy" align="left" src="readme-resources/screenshots/toolbarlayout.gif" width="150"/>

```xml
<de.dlyt.yanndroid.oneui.layout.ToolbarLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout="..."
    app:title="..."
    app:subtitle="..."
    app:expandable="..."
    app:expanded="..."
    app:navigationIcon="...">

</de.dlyt.yanndroid.oneui.layout.ToolbarLayout>
```
The children of this view can be at four different location: on the **main screen**, in the **footer** (useful for views like BottomNavigationView), in the **appbar header** as a custom title or in the **root layout** (views like FAB). To specify the location of each child you can set the attribute ```app:layout_location``` of the child to either ```main_content``` (default), ```footer```, ```appbar_header``` or ```root```.

```app:title``` and ```app:subtitle``` can be used to set the title and subtitle of the AppBar and Toolbar. The AppBar status is set to expanded by default, you can simply set ```app:toolbar_expanded``` to false if you want it to be collapsed. You can also disable totally the CollapsingToolbar by setting ```app:toolbar_expandable``` to false. On small screens/dpi the toolbar will not expand anyway.

The drawable in ```app:navigationIcon="..."``` is the icon for the Toolbar Navigation Button. There are already a lot of stock OneUI [icons](#Icons) included in the library you can use.

To make sure that AppBar scrolling behavior works correctly, the child set in ```main_content``` should either be [RecyclerView](#RecyclerView) or [NestedScrollView](#NestedScrollView) with ```app:layout_behavior``` set to ```@string/sesl_appbar_scrolling_view_behavior```. The stock RecyclerView and NestedScrollView might also work but probably won't behave correctly.

For further customization you can use ```android:layout``` to apply your own layout to this view, but keep in mind that you should add all the views and ids which are in the default layout, or your app might crash.
<br clear="left"/>

#### Methods
Return the Toolbar.
```java
public MaterialToolbar getToolbar()
```
Set the title of the AppBar/Toolbar.
```java
public void setTitle(CharSequence title)
public void setTitle(CharSequence expandedTitle, CharSequence collapsedTitle)
```
Set the subtitle of the AppBar (the collapsed subtitle will show if ```app:toolbar_expandable``` is false or the screen/dpi is too small and the app bar can't expand).
```java
public void setSubtitle(CharSequence subtitle)
```
Expand or collapse the AppBar with an optional animation.
```java
public void setExpanded(boolean expanded, boolean animate)
public boolean isExpanded()
```
Methods for the NavigationButton. For the badge use a number or ```ToolbarLayout.N_BADGE``` for a "N". As a tooltip  you can use ```R.string.sesl_navigate_up``` ("Navigate Up"), which is the default in all Samsung apps and also translated to 90 languages.
```java
public void setNavigationButtonIcon(Drawable navigationIcon)
public void setNavigationButtonTooltip(CharSequence tooltipText)
public void setNavigationButtonVisible(boolean visible)
public void setNavigationButtonBadge(int count)
public void setNavigationOnClickListener(OnClickListener listener)
```
Manage the Toolbar Menu. In the Menu resource file use ```app:showAsAction="always"``` to show the item as a Action instead of in the popup menu.
```java
public void inflateToolbarMenu(Menu menu)
public void inflateToolbarMenu(@MenuRes int menuRes)
public Menu getToolbarMenu()
public void setOnToolbarMenuItemClickListener(OnMenuItemClickListener listener)
```
SelectMode. Changes the layout of the Toolbar to the one you can see in any Samsung app, when you long click a list item. This will show a "All" checkbox, "x selected" counter as the title and a bottom menu (see [screenshot](readme-resources/screenshots/toolbarlayout_selectmode.png)). In the Menu resource file for the bottom menu use ```app:showAsAction="always"``` to show the item as a Action instead of in the "more" menu.
```java
public void showSelectMode()
public void dismissSelectMode()

public void setSelectModeCount(int count)
public void setSelectModeAllChecked(boolean checked)
public void setSelectModeAllCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener)

public void setSelectModeBottomMenu(Menu menu, OnMenuItemClickListener listener)
public void setSelectModeBottomMenu(@MenuRes int menuRes, OnMenuItemClickListener listener)
public Menu getSelectModeBottomMenu()
```
SearchMode. Changes the layout of the Toolbar to a Search layout, with a text field and a voice input icon (see [screenshot](readme-resources/screenshots/toolbarlayout_searchmode.png)).
```java
public void showSearchMode()
public void dismissSearchMode()
public boolean isSearchMode()
public void setSearchModeListener(SearchModeListener listener)
```
:warning: For the voice input to work, you need to add this in your activity/fragment:
 ```java
 private ActivityResultLauncher<Intent> activityResultLauncher;
//onCreate:
activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> toolbarLayout.onSearchModeVoiceInputResult(result));
//setSearchModeListener - onVoiceInputClick
activityResultLauncher.launch(intent);
 ```
Also add this in your manifest for api 30+:
```xml
<queries>
    <intent>
        <action android:name="android.speech.action.RECOGNIZE_SPEECH" />
    </intent>
</queries>
```

### SplashView
The activity you are gonna use for SplashView has a different style than the rest of the application, so you need to add this ```android:theme="@style/OneUISplashTheme"``` to your splash activity in AndroidManifest.

This view comes in two different configurations:  
1) An animated Splash Screen View like the one in the Galaxy Store.

<img loading="lazy" align="left" src="readme-resources/screenshots/splash_animated.gif" width="150"/>

```xml
<de.dlyt.yanndroid.oneui.layout.SplashView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:animated="true"
    app:text="..."
    app:background_image="..."
    app:foreground_image="..."
    app:animation="..." />
```

Set ```app:background_image``` to the background part of your icon and ```app:foreground_image``` to the foreground. The foreground image will have a customizable animation via the ```app:animation``` attr (default animation will be Galaxy Store one). ```app:text="..."``` is the text under the icon. It has a custom font to match the one on the Galaxy Store splash screen.
<br clear="left"/>

#### Methods
Sets the icon foreground and background
```java
public void setImage(Drawable foreground, Drawable background)
```
Sets the text of the Splash TextView
```java
public void setText(String mText)
```
Returns the text of the Splash TextView
```java
public String getText()
```
Starts the animation of the foreground
```java
public void startSplashAnimation()
```
Clears the animation
```java
public void clearSplashAnimation()
```
Listener for the Splash Animation
```java
public void setSplashAnimationListener(Animation.AnimationListener listener)
```

2) A simple Splash View without animation. (Samsung apps use their own ```com.samsung.android.startingwindow.LAYOUT_RESID_FOR_MASS``` flag in manifest)

<img loading="lazy" align="left" src="readme-resources/screenshots/splash_simple.png" width="150"/>

```xml
<de.dlyt.yanndroid.oneui.layout.SplashView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:animated="false"
    app:text="..."
    app:image="..." />
```

Set the icon and text in ```app:image``` and ```app:text```.
<br clear="left"/>

#### Methods
Sets the icon Drawable
```java
public void setImage(Drawable mImage)
```
Sets the text of the Splash TextView
```java
public void setText(String mText)
```
Returns the text of the Splash TextView
```java
public String getText()
```

### AboutPage
A layout that looks like and has the same functions as the About Screen in any Samsung app. Like the [SplashView](#SplashView), the activity you're gonna use has a different style than the rest of the application, so you need to add this ```android:theme="@style/OneUIAboutTheme"``` to your About Activity in AndroidManifest.

<img loading="lazy" align="left" src="readme-resources/screenshots/aboutpage.gif" width="150"/>

```xml
<de.dlyt.yanndroid.oneui.layout.AboutPage
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:optional_text="..."
    app:update_state="...">

    <com.google.android.material.button.MaterialButton
        style="@style/ButtonStyle.AboutPage"
        android:text="..." />

    ...

</de.dlyt.yanndroid.oneui.layout.AboutPage>
```

The App Name and Version are automatically added to the view. The Info Button at the top right will redirect the user to the App Info in System Settings. ```app:optional_text``` is the text that can be added between the version and the status text. The status text will change according to the state you have set to the view programmatically (see below) ,or in the layout with ```app:update_state```.

You can use ```style="@style/ButtonStyle.AboutPage"``` for the buttons, which are shown at the bottom (layout_width and layout_height are also in this style).
<br clear="left"/>

#### Methods
Set the update state of the view to either ```AboutPage.LOADING```, ```AboutPage.NO_UPDATE```, ```AboutPage.UPDATE_AVAILABLE```, ```AboutPage.NOT_UPDATEABLE``` or ```AboutPage.NO_CONNECTION```. This will change the visibility of certain views and the status text.
```java
public void setUpdateState(@UpdateState int state)
```
Set the optional text between the version and the status text.
```java
public void setOptionalText(String text)
```
OnClickListener for the update and retry button.
```java
public void setUpdateButtonOnClickListener(OnClickListener listener)
public void setRetryButtonOnClickListener(OnClickListener listener)
```

### SwitchBarLayout
This is a extended [ToolbarLayout](#ToolbarLayout) with [SwitchBar](#SwitchBar). Useful for creating inner preferences layouts in pair with [SwitchPreferenceScreen](#SwitchPreferenceScreen).

<img loading="lazy" align="left" src="readme-resources/screenshots/switchbarlayout.png" width="150"/>

```xml
<de.dlyt.yanndroid.oneui.layout.SwitchBarLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout="..."
    app:toolbar_title="..."
    app:toolbar_subtitle="..."
    app:toolbar_expanded="...">

</de.dlyt.yanndroid.oneui.layout.SwitchBarLayout>
```

Manage the Toolbar with ```app:toolbar_title```, ```app:toolbar_subtitle``` and ```app:toolbar_expanded```.

For further customization you can use ```android:layout``` to apply your own layout to this view, but keep in mind that you should add all the views and ids which are in the default layout, or your app might crash.
<br clear="left"/>

#### Methods
Return the [ToolbarLayout](#ToolbarLayout).
```java
public ToolbarLayout getToolbarLayout()
```
Return the [SwitchBar](#SwitchBar).
```java
public SwitchBar getSwitchBar()
```
Set the Toolbar title and subtitle.
```java
public void setToolbarTitle(CharSequence title)
public void setToolbarTitle(CharSequence expandedTitle, CharSequence collapsedTitle)
public void setToolbarSubtitle(String subtitle)
```
Expand or collapse the toolbar with an optional animation.
```java
public void setToolbarExpanded(boolean expanded, boolean animate)
```

### CoordinatorLayout
Samsung's CoordinatorLayout
```xml
<de.dlyt.yanndroid.oneui.layout.CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

</de.dlyt.yanndroid.oneui.layout.CoordinatorLayout>
```

### Round Layouts
LinearLayout, FrameLayout and [NestedScrollView](#NestedScrollView) with rounded corners. Usage is the same as their parents view.
```xml
<de.dlyt.yanndroid.oneui.layout.RoundLinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:roundedCorners="...">

</de.dlyt.yanndroid.oneui.layout.RoundLinearLayout>

<de.dlyt.yanndroid.oneui.layout.RoundFrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:roundedCorners="...">

</de.dlyt.yanndroid.oneui.layout.RoundFrameLayout>

<de.dlyt.yanndroid.oneui.view.RoundNestedScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:roundedCorners="...">

</de.dlyt.yanndroid.oneui.view.RoundNestedScrollView>
```
To choose which corners are rounded use ```app:roundedCorners```. Available are ```all``` (default), ```none```, ```bottom_left```, ```bottom_right```, ```top_left``` and ```top_right``` (you can separate them with "|", to use multiple). Note that [DrawerLayout](#DrawerLayout), [ToolbarLayout](#ToolbarLayout) and [SwitchBarLayout](#SwitchBarLayout) already have rounded corners on their main content.

### NestedScrollView
Samsung's NestedScrollView, see [Round Layouts](#Round-Layouts) for the rounded corner version.

```xml
<de.dlyt.yanndroid.oneui.view.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent">

</de.dlyt.yanndroid.oneui.view.NestedScrollView>
```

If you want to use this as child for [DrawerLayout](#DrawerLayout) or [ToolbarLayout](#ToolbarLayout), don't forget to add ```app:layout_behavior="@string/sesl_appbar_scrolling_view_behavior"```.

### RecyclerView
Samsung's RecyclerView, heavily used in their apps.

```xml
<de.dlyt.yanndroid.oneui.view.RecyclerView
    android:layout_width="match_parent"
    android:layout_height="match_parent">
        
</de.dlyt.yanndroid.oneui.view.RecyclerView>
```
Attributes and usage are the same as Google's [RecyclerView](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView).

#### Methods
Enable Samsung's FastScroller. <img loading="lazy" src="readme-resources/screenshots/fastscroller.png" height="30"/>
```java
public void seslSetFastScrollerEnabled(boolean enabled)
public void seslSetFastScrollerEventListener(RecyclerView.SeslFastScrollerEventListener listener)
```
Enable Samsung's Go To Top button. <img loading="lazy" src="readme-resources/screenshots/gototop.png" height="30"/>
```java
public void seslSetGoToTopEnabled(boolean enabled)
public void seslSetGoToTopBottomPadding(int padding)
public void seslSetOnGoToTopClickListener(RecyclerView.SeslOnGoToTopClickListener listener)
```
Round the corners of the last item.
```java
public void seslSetLastRoundedCorner(boolean enabled)
```
Fill the background at the bottom after the last item with the app background color.
```java
public void seslSetFillBottomEnabled(boolean enabled)
public void seslSetFillBottomColor(int color)
```

Samsung also customized RecyclerView.ItemDecoration class by adding a call to **onDispatchDraw** method of the View. Overriding the ```seslOnDispatchDraw``` method lets you customize even more your list/grid view. You can find an example of it [here](https://github.com/Yanndroid/OneUI-Design-Library/blob/1e110958151a93647b71b80c68e54949a3a0691a/app/src/main/java/de/dlyt/yanndroid/oneuiexample/tabs/IconsTab.java#L298).

### SwipeRefreshLayout
Samsung's SwipeRefreshLayout.

<img loading="lazy" src="readme-resources/screenshots/swiperefreshlayout.jpg" width="300"/>

It's almost the same as Google's one, only difference is a different "pull-down" animation. 
```xml
<de.dlyt.yanndroid.oneui.layout.SwipeRefreshLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
        
</de.dlyt.yanndroid.oneui.layout.SwipeRefreshLayout>
```
Attributes and usage are the same as Google's [SwipeRefreshLayout](https://developer.android.com/reference/androidx/swiperefreshlayout/widget/SwipeRefreshLayout).

#### Methods
End the refresh status once the animation ends.
```java
public seslSetRefreshOnce(boolean once)
```

### Button
The Button has three styles which you can use, depending on your needs.

<img loading="lazy" src="readme-resources/screenshots/button.png" width="300"/>

```@style/ButtonStyle.Transparent```  
```@style/ButtonStyle.Colored``` 
```@style/ButtonStyle.Filled```

(There are also ```ButtonStyle.AboutPage``` and ```ButtonStyle.AboutPageUpdate``` for the [AboutPage](#AboutPage), you can also use.)

### SeekBar
Samsung's SeekBar.

<img loading="lazy" src="readme-resources/screenshots/seekbar.gif" width="300"/>

```xml
<de.dlyt.yanndroid.oneui.SeekBar
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:seslSeekBarMode="expand" />
```
With the ```app:seslSeekBarMode``` attribute, you can set the behavior of the SeekBar when pressed/tracking. Available values are:
- ```standard``` (default)
- ```expand``` (like the brightness slider in QS)
- ```vertical``` (standard but vertical)
- ```expand_vertical``` (expand but vertical)
- ```warning``` (turns orange when reaching the end)
- ```split``` (idk, ask samsung)

#### Methods
Set a warning at progress i.
```java
public void setOverlapPointForDualColor(int i)
```
Make the seekbar seamless.
```java
public void setSeamless(boolean var1)
```
Other methods are the same as the default [Seekbar](https://developer.android.com/reference/android/widget/SeekBar).

### ProgressBar
<img loading="lazy" src="readme-resources/screenshots/progressbar.gif" width="300"/>

```@style/ProgressBarStyle.Horizontal```  
```@style/ProgressBarStyle.Horizontal.Large```  
```@style/ProgressBarStyle.Circle.Large```  
```@style/ProgressBarStyle.Circle```  
```@style/ProgressBarStyle.Circle.Small```  
```@style/ProgressBarStyle.Circle.Title```

### SwitchBar
Samsung's SwitchBar, same as the one you find in OneUI System Settings app.

<img loading="lazy" src="readme-resources/screenshots/switchbar.gif" width="300"/>

```xml
<de.dlyt.yanndroid.oneui.SwitchBar
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

There is also the [SwitchBarLayout](#SwitchBarLayout) if you need. 

#### Methods
On and Off text resource id for the SwitchBar (default will be "On" and "Off").
```java
public void setSwitchBarText(int i, int i2)
```
Enable/disable the SwitchBar.
```java
public void setEnabled(boolean z)
```
Visibility of the ProgressBar in the SwitchBar.
```java
public void setProgressBarVisible(boolean z)
```
SwitchBar Listener.
```java
public void addOnSwitchChangeListener(OnSwitchChangeListener onSwitchChangeListener)
```

### Spinner

Spinner with rounded corners and custom selector.

<img loading="lazy" src="readme-resources/screenshots/spinner.png" width="260"/>

```xml
<androidx.appcompat.widget.SeslSpinner
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

Same usage as [Spinner](https://developer.android.com/reference/android/widget/Spinner).

### OptionButton
Create lists inside DrawerLayout without using RecyclerView with OptionButton.

<img loading="lazy" src="readme-resources/screenshots/optionbutton.png" width="260"/>

```xml
<de.dlyt.yanndroid.oneui.drawer.OptionButton
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:icon="..."
    app:text="..."
    app:selected="..."
    app:counter="..."
    app:counterEnabled="..." />
```

```app:icon="..."``` attribute is the button icon, and ```app:text="..."``` the text. ```app:selected="..."``` is to show the OptionButton as selected (colored and bold text), it's false by default. ```app:counterEnabled="..."``` and ```app:counter="..."``` can customize a counter at the end of the view, which is disabled by default. Make sure to enable the ```app:counterEnabled``` attribute or it won't show up.

#### Methods
Set/get the icon and text.
```java
public void setIcon(Drawable icon)
public String getText()
public void setText(String text)
```
Manage the counter.
```java
public Integer getCounter()
public void setCounter(Integer integer)
public void setCounterEnabled(Boolean enabled)
public void toggleCounterEnabled()
public Boolean isCounterEnabled()
```
Control the state (colored, bold text).
```java
public void setButtonSelected(Boolean selected)
public void toggleButtonSelected()
public Boolean isButtonSelected()
```
Enable/disable the OptionButton.
```java
public void setButtonEnabled(Boolean enabled)
```

### OptionGroup
[OptionButton](#OptionButton) and OptionGroup are working together like [RadioButton](https://developer.android.com/reference/android/widget/RadioButton) and [RadioGroup](https://developer.android.com/reference/android/widget/RadioGroup). It will select an OptionButton on click.

<img loading="lazy" src="readme-resources/screenshots/optiongroup.gif" width="260"/>

```xml
<de.dlyt.yanndroid.oneui.drawer.OptionGroup
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:selectedOptionButton="...">

    <de.dlyt.yanndroid.oneui.drawer.OptionButton
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:icon="..."
        app:text="..." />

    ...

</de.dlyt.yanndroid.oneui.drawer.OptionGroup>
```

```app:selectedOptionButton="..."``` will set the default selected OptionButton with this id. This view can also have other children, for example a divider:

<img loading="lazy" src="readme-resources/screenshots/drawerdivider.png" width="260"/>

```xml
<View style="@style/DrawerDividerStyle" />
```


#### Methods
Select an OptionButton with either the view, id or position.
```java
public void setSelectedOptionButton(OptionButton optionButton)
public void setSelectedOptionButton(Integer id)
public void setSelectedOptionButton(int position)
```
Get the currently selected OptionButton.
```java
public OptionButton getSelectedOptionButton()
```
Listener which will provide you view, id and position of the clicked OptionButton.
```java
public void setOnOptionButtonClickListener(OnOptionButtonClickListener listener)
```

### RelatedCard
Samsung's "Looking for something else?" Card you find in System Settings app.

<img loading="lazy" src="readme-resources/screenshots/relatedcard.png" width="300"/>  

(Depending on your screen right now you might not see it, but there's actually a light blue card around it.)

```xml
<de.dlyt.yanndroid.oneui.RelatedCard
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:title="...">

    <com.google.android.material.textview.MaterialTextView
        style="@style/RelatedButtonStyle"
        android:text="..." />


</de.dlyt.yanndroid.oneui.RelatedCard>
```
You can simply use ```style="@style/RelatedButtonStyle"``` for the child TextViews.

#### Methods
Get/set the title text.
```java
public String getTitle()
public void setTitle(String title)
```

### BottomNavigationView
Samsung's BottomNavigationView.

<img loading="lazy" src="readme-resources/screenshots/bottomnavigationview.jpg" width="300"/>

```xml
<de.dlyt.yanndroid.oneui.view.BottomNavigationView
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
        
</de.dlyt.yanndroid.oneui.view.BottomNavigationView>
```

If you want to set it up with a ViewPager you'll have to use the [ViewPager](#ViewPager) bundled in the library, the usage is the same as Google's [TabLayout](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout), but you'll have to call ```updateWidget(Activity activity)``` after you configured it.

#### Methods
Add a custom ImageButton like in Samsung's Gallery (as seen in screenshot).
```java
public void addTabCustomButton(Drawable icon, CustomButtonClickListener listener)
```

### TabLayout
Samsung's TabLayout.

<img loading="lazy" src="readme-resources/screenshots/tablayout.png" width="300"/>

```xml
<de.dlyt.yanndroid.oneui.view.TabLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
        
</de.dlyt.yanndroid.oneui.view.TabLayout>
```

If you want to set it up with a ViewPager you'll have to use the [ViewPager](#ViewPager) bundled in the library, the usage is the same as Google's [TabLayout](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout), but you'll have to call ```updateWidget()``` after you configured it. 

### ViewPager
ViewPager working with [BottomNavigationView](#BottomNavigationView) and [TabLayout](#TabLayout).

```xml
<de.dlyt.yanndroid.oneui.view.ViewPager
    android:layout_width="match_parent"
    android:layout_height="match_parent">

</de.dlyt.yanndroid.oneui.view.ViewPager>
```

The Adapter for this ViewPager has to extend [androidx.fragment.app.PagerAdapter](https://developer.android.com/reference/androidx/fragment/app/PagerAdapter) or a subclass.

### AlertDialog
Samsung's AlertDialogs.

<img loading="lazy" src="readme-resources/screenshots/alertdialog_2.png" width="300"/> <img loading="lazy" src="readme-resources/screenshots/alertdialog_3.png" width="300"/> <img loading="lazy" src="readme-resources/screenshots/alertdialog_1.png" width="300"/>

Usage is the same as the default [AlertDialog](https://developer.android.com/reference/android/app/AlertDialog), but you have to use ```de.dlyt.yanndroid.oneui.dialog.AlertDialog``` instead.

### ProgressDialog
Samsung's Alert Dialog with Progress.

<img loading="lazy" src="readme-resources/screenshots/progressdialog_1.png" width="300"/> <img loading="lazy" src="readme-resources/screenshots/progressdialog_2.png" width="153"/>

Usage is the same as [AlertDialog](#AlertDialog) but with additional methods.

#### Methods
Get/set the Progress drawables.
```java
public void setProgressDrawable(Drawable d)
public void setIndeterminateDrawable(Drawable d)
```

Get/set Progress indeterminate status.
```java
public boolean isIndeterminate()
public void setIndeterminate(boolean indeterminate)
```

Get/set Progress Style. Value can either be ```STYLE_SPINNER```, ```STYLE_HORIZONTAL``` or ```STYLE_CIRCLE_ONLY```.
```java
public void setProgressStyle(int style)
```

Get/set ProgressBar progress (in case you're using ```STYLE_HORIZONTAL```).
```java
public int getProgress()
public void setProgress(int value)
public int getSecondaryProgress()
public void setSecondaryProgress(int secondaryProgress)
public int getMax()
public void setMax(int max)
public void incrementProgressBy(int diff)
public void incrementSecondaryProgressBy(int diff)
public void setProgressNumberFormat(String format)
public void setProgressPercentFormat(NumberFormat format)
```

### ClassicColorPickerDialog
Samsung's Sesl Color Picker Dialog.

<img loading="lazy" src="readme-resources/screenshots/colorpicker_c.png" width="200"/>

Create the dialog with ColorPickerChangedListener, starting color, and recent colors.
```java
public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener)
public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener, int currentColor)
public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener, int currentColor, int[] recentColors)
public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener, int[] recentColors)
```
Set the current color.
```java
public void setNewColor(Integer color)
```
Show/hide the color transparency SeekBar.
```java
public void setTransparencyControlEnabled(boolean var1)
```
Show the dialog.
```java
public void show()
```
Dismiss the dialog.
```java
public void dismiss()
```
Close the dialog.
```java
public void close()
```
Example:
```java
ClassicColorPickerDialog mColorPickerDialog = new ClassicColorPickerDialog(this,
        new ClassicColorPickerDialog.ColorPickerChangedListener() {
            @Override
            public void onColorChanged(int i) {
                
            }
        },
        color);
mColorPickerDialog.show();
```

### DetailedColorPickerDialog
Samsung Notes app Color Picker Dialog, in case you need a more complete one.

<img loading="lazy" src="readme-resources/screenshots/colorpicker_1.png" width="200"/> <img loading="lazy" src="readme-resources/screenshots/colorpicker_2.png" width="200"/>

Create the dialog with mode (1 = Spectrum, 2 = Swatches) and fArr (starting color).
```java
public DetailedColorPickerDialog(Context context, int mode, float[] fArr)
public DetailedColorPickerDialog(Context context, float[] fArr)
```
Set the Color Change Listener.
```java
public void setColorPickerChangeListener(ColorPickerChangedListener colorPickerChangedListener)
```
Show the dialog.
```java
public void show()
```
Dismiss the dialog.
```java
public void dismiss()
```
Close the dialog.
```java
public void close()
```
Example:
```java
float[] scolor = new float[3];
Color.colorToHSV(Color.parseColor("#0381fe5"), scolor);

DetailedColorPickerDialog mColorPickerDialog = new DetailedColorPickerDialog(this, scolor);
mColorPickerDialog.setColorPickerChangeListener(new DetailedColorPickerDialog.ColorPickerChangedListener() {
    @Override
    public void onColorChanged(int i, float[] fArr) {
        
    }

    @Override
    public void onViewModeChanged(int i) {

    }
});
mColorPickerDialog.show();
```

### Preferences
Samsung's Preferences.

Attributes and usage are the same as Google's [PreferenceFragmentCompat](https://developer.android.com/reference/androidx/preference/PreferenceFragmentCompat) but you have to use ```de.dlyt.yanndroid.oneui.layout.PreferenceFragment``` instead. All the default preferences in androidx.preference are already included.

#### Methods
Enable/disable rounded corners.
```java
public void seslSetRoundedCorner(boolean enabled)
```
Create and add a [RelatedCard](#RelatedCard) to the bottom of the Preferences.
```java
PreferencesRelatedCard relatedCard = createRelatedCard(context);
relatedCard.addButton("This", this)
            .addButton("That", this)
            .addButton("There", this)
            .show(this);
```

##### Custom Preferences:

#### SwitchPreferenceScreen

<img loading="lazy" src="readme-resources/screenshots/switchpreferencescreen.png" width="300"/>

Clickable SwitchPreference used to contain inner preferences, can be used in combination with [SwitchBarLayout](#SwitchBarLayout).

```xml
<SwitchPreferenceScreen
    android:key="..."
    android:summary="..."
    android:title="..."/>
```

Both ```OnPreferenceClickListener``` and ```intent``` tag can be used to manage the behavior of the Preference when clicked.

#### TipsCard

<img loading="lazy" src="readme-resources/screenshots/tipcard.png" width="300"/>

```xml
<TipsCardViewPreference
    android:key="..."
    android:summary="..."
    android:title="..." />
```

#### ColorPickerPreference

<img loading="lazy" src="readme-resources/screenshots/colorpickerpreference.png" width="150"/> <img loading="lazy" src="readme-resources/screenshots/colorpicker_1.png" width="150"/> <img loading="lazy" src="readme-resources/screenshots/colorpicker_2.png" width="150"/>

```xml
<ColorPickerPreference
    android:defaultValue="..."
    android:key="..."
    android:title="..."
    app:pickerType="..."
    app:showAlphaSlider="..." />
```

```android:defaultValue```: default color string (ex. #FF2525)  
```app:pickerType```: classic or detailed  
```app:showAlphaSlider```: show transparency seekbar in classic picker type

#### HorizontalRadioPreference
Samsung's Radio Preferences used in Light/Dark mode Settings and Resolution Settings.

<img loading="lazy" src="readme-resources/screenshots/horizontalradiopreference.png" width="300"/>

```xml
<HorizontalRadioPreference
    android:key="..."
    android:title="..."
    app:entriesImage="..."
    app:entries="..."
    app:entriesSubtitle="..."
    app:entryValues="..."
    app:viewType="..." />
```

```app:entriesImage```: array with drawables (to be used only with **image** viewType)  
```app:entries```: string array for the names  
```app:entriesSubtitle```: string array for the subtitle (to be used only with **noImage** viewType) 
```app:entriesValues```: string array for the values  
```app:viewType```: image or noImage


### PopupMenu
Create a PopupWindow Menu with it's anchor.

<img loading="lazy" src="readme-resources/screenshots/popupmenu.jpg" width="150"/>

```java
//de.dlyt.yanndroid.oneui.menu.PopupMenu
PopupMenu popupMenu = new PopupMenu(view);
```

#### Methods
Inflate a menu resource or a Menu (de.dlyt.yanndroid.oneui.menu.Menu).
```java
public void inflate(@MenuRes int menuRes)
public void inflate(@MenuRes int menuRes, CharSequence title)
public void inflate(Menu menu)
public void inflate(Menu menu, CharSequence title)
```
Get the inflated menu.
```java
public Menu getMenu()
```
Set the menu item click and update listener.
```java
public void setPopupMenuListener(PopupMenuListener listener)
```
Show a divider between menu groups.
```java
public void setGroupDividerEnabled(boolean enabled)
```
Set a custom animation set.
```xml
<!-- styles.xml -->
<style name="MenuPopupAnimStyle" parent="@android:style/Animation">
    <item name="android:windowEnterAnimation">@anim/sesl_menu_popup_enter</item>
    <item name="android:windowExitAnimation">@anim/sesl_menu_popup_exit</item>
</style>

<style name="BottomMenuPopupAnimStyle" parent="@android:style/Animation">
...
```
```java
public void setAnimationStyle(int animationStyle)
```
Show and dismiss the popup.
```java
public void show()
public void show(int xoff, int yoff) //with offset
public void dismiss()
public boolean isShowing()
```

### Tooltip
Samsung's Tooltip.

<img loading="lazy" src="readme-resources/screenshots/tooltip.png" width="100"/>

Usage is the same as [TooltipCompat](https://developer.android.com/reference/androidx/appcompat/widget/TooltipCompat) but with additional methods. Please note this won't show up on default views and has to be added manually. Instead of using ```view.setTooltipText(text)```, use:
```java
Tooltip.setTooltipText(view, text);
SeslViewReflector.semSetHoverPopupType(view, 1 /* SemHoverPopupWindow.TYPE_TOOLTIP */);
```

#### Methods
```java
public static void seslSetTooltipForceActionBarPosX(boolean z)
public static void seslSetTooltipForceBelow(boolean z)
public static void seslSetTooltipNull(boolean z)
public static void seslSetTooltipPosition(int x, int y, int direction)
public void showPenPointEffect(MotionEvent event, boolean more)
```


### Snackbar
Samsung's Snackbar.

<img loading="lazy" src="readme-resources/screenshots/snackbar.png" width="300"/>

Same usage as the default [Snackbar](https://developer.android.com/reference/com/google/android/material/snackbar/Snackbar):
```java
//de.dlyt.yanndroid.oneui.view.Snackbar

Snackbar.make(view, "Text label", Snackbar.LENGTH_SHORT).setAction("Action", new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //onClick
    }
}).show();
```

### Advanced
For further customization or if you need something which isn't implemented in the custom views yet (I can't think of all possible uses), you can always access the views inside it with ```findViewById(int id)``` and change them directly. For the Ids please refer to the source code.

### Icons
There are also a lot of the stock icons you can find in Samsung apps included in this library, and we will add more over time. You can use them with ```@drawable/ic_samsung_...``` and ```R.drawable.ic_samsung_...```.

<img loading="lazy" src="readme-resources/screenshots/icons.png" width="350"/>

*not all icons are shown here because there are too much by now (230). They are all listed with their name in the Icons tab of the sample app.

### Color theme
The default color of the style is the same blue as Samsung (see [Screenshots](#Screenshots)). But like Samsung has different colors for different apps, you too can use other colors which will apply on the entire App and even on the [App Icon](#App-Icon). In this library there are three different ways to do that and all three can be used simultaneously:

#### 1. Entire App
This methode will apply the color theme on the entire app and on the app icon. You need to add these three colors in your ```colors.xml``` :
```xml
<color name="primary_color">...</color>
<color name="secondary_color">...</color>
<color name="primary_dark_color">...</color>
```
These colors should have approximately the same color but with a different brightness. ```secondary_color``` the brightest, then ```primary_color``` and the darkest ```primary_dark_color```.  

Here are some presets (if you want I can make more):
- ![#f3a425](https://via.placeholder.com/12/f3a425/000000?text=+) Yellow like MyFiles App (also used in [FreshHub](https://github.com/Yanndroid/FreshHub)):
```xml
<color name="primary_color">#fff3a425</color>
<color name="secondary_color">#ffffb949</color>
<color name="primary_dark_color">#ffbd7800</color>
```

- ![#008577](https://via.placeholder.com/12/008577/000000?text=+) Dark green like Calendar App:
```xml
<color name="primary_color">#ff008577</color>
<color name="secondary_color">#ff009e7c</color>
<color name="primary_dark_color">#ff00574b</color>
```

- ![#68b31a](https://via.placeholder.com/12/68b31a/000000?text=+) Light green like Calculator App:
```xml
<color name="primary_color">#ff68b31a</color>
<color name="secondary_color">#ff7fa87f</color>
<color name="primary_dark_color">#ff569415</color>
```

- ![#ff034A](https://via.placeholder.com/12/ff034A/000000?text=+) Light red which I personally like:
```xml
<color name="primary_color">#ffff034a</color>
<color name="secondary_color">#ffff3d67</color>
<color name="primary_dark_color">#ffde0043</color>
```

#### 2. Single/Multiple activities
If you want to use different colors for a single (or multiple, but not all) activities, this is also possible. The difference here is that this will only apply for the activities you want. Add the three colors (see [Entire App](#1.-Entire-App)) in a theme in ```themes.xml```:

```xml
<style name="ThemeName" parent="OneUITheme">
    <item name="colorPrimary">#fff3a425</item>
    <item name="colorSecondary">#ffffb949</item>
    <item name="colorPrimaryDark">#ffbd7800</item>
</style>
```
Then apply it on the activities you want with ```android:theme="@style/ThemeName"``` in ```AndroidManifest.xml```.

#### 3. Via Code
This method allows you to change the color of your theme dynamically within your app. It's based on [this idea](https://stackoverflow.com/a/48517223). In your activity onCreate add this line at the top **before** ```super.onCreate(...)```:
```java
new ThemeUtil(this);
```
This will apply the color theme at launch. If you want to change the color you can use these functions:
```java
ThemeUtil.setColor(Activity activity, int red, int green, int blue)
ThemeUtil.setColor(Activity activity, float red, float green, float blue)
ThemeUtil.setColor(Activity activity, float[] hsv)
```
The color you apply with these functions will apply on every activity with ```new ThemeUtil(this)``` at the top. If you are using ThemeUtil and you want to enable/disable Dark theme you'll have to do it with these methods:
```java
// mode: DARK_MODE_AUTO; DARK_MODE_DISABLED; DARK_MODE_ENABLED
public static void setDarkMode(AppCompatActivity activity, int mode)
public static int getDarkMode(Context context)
```

### App Icon
The most app icons of Samsung apps are made of one solid color as background and a white icon as foreground. Sometimes there is even a little detail of the foreground with a similar color as the background.

<img loading="lazy" src="readme-resources/app-icons/settings.png" width="50" height="50" />   <img loading="lazy" src="readme-resources/app-icons/notes.png" width="50" height="50" />   <img loading="lazy" src="readme-resources/app-icons/messages.png" width="50" height="50" />   <img loading="lazy" src="readme-resources/app-icons/camera.png" width="50" height="50" />   <img loading="lazy" src="readme-resources/app-icons/calculator.png" width="50" height="50" />   <img loading="lazy" src="readme-resources/app-icons/contacts.png" width="50" height="50" />   <img loading="lazy" src="readme-resources/app-icons/myfiles.png" width="50" height="50" />

 I would suggest you to use ```@color/primary_color``` for the background color and either ```@color/launcher_foreground_detail_color``` , ```@color/secondary_color``` or ```@color/primary_dark_color``` for the foreground "detail" color, so [your color theme](#Color-theme) applies for the app icon too.  
The sample app icon for example:

<img loading="lazy" src="readme-resources/app-icons/sample.png" width="50" height="50" />

### OneUI 4
Starting with v2.1.0, the new OneUI 4 design is being added to this library and v2.2.0 makes it the default theme of the library. If you still want to use the OneUI 3 style, instead of ```@style/OneUITheme```, ```@style/OneUIAboutTheme``` and ```@style/OneUISplashTheme``` use ```@style/OneUI3Theme```, ```@style/OneUI3AboutTheme``` and ```@style/OneUI3SplashTheme``` in your manifest file. All the views which don't have the new style yet will use the old one (OneUI 3) instead.

## Progress

- [x] CardView
- [x] CheckBox
- [x] Button
- [x] Switch 
- [x] RadioButton
- [x] ProgressBar
- [x] SeekBar
- [x] SwitchBar
- [x] RelatedCard
- [x] ScrollBar & FastScroll
- [x] PopupMenu
- [x] Dialog
- [x] TabLayout & ViewPager
- [x] Preferences
- [x] Tooltip
- [x] BottomNavigationView
- [x] About Screen
- [x] Splash Screen
- [x] ToolbarLayout
- [x] DrawerLayout
- [x] Drawer Divider
- [x] SnackBar
- [x] Color Picker Dialog
- [x] Spinner
- [x] Toolbar SearchMode and SelectMode
- [x] SwipeRefreshLayout
- [ ] BottomSheet

## Changelog

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
- [Samsung's EULA](https://www.samsung.com/sg/Legal/SamsungLegal-EULA/)

## Special thanks to:
- [Samsung](https://www.samsung.com/) for their awesome OneUI Design. :)
- [BlackMesa123](https://github.com/BlackMesa123) for a lot of OneUI stuff, more compatibility and his experience.
- [TenSeventy7](https://github.com/TenSeventy7) for some stuff and help.
- [leonbcode](https://github.com/leonbcode) for github actions, so this library is always up-to-date.
- All the [Contributors](https://github.com/Yanndroid/OneUI-Design-Library/graphs/contributors) and Issue Reporters.
