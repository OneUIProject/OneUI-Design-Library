# SamsungDesign
Samsung OneUI design for AndroidStudio

Install:
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

```
dependencies {
    implementation 'com.github.Yanndroid:SamsungDesign:1.0.0'
    ...
}
```

```
<application
        ...
        android:theme="@style/SamsungTheme"
        >
        ...
    </application>
```
