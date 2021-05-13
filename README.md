# SamsungDesign
Samsung OneUI design for AndroidStudio

Install:

- gradle.properties
```
authToken=jp_4gl3t6mp4ncnvurik7qfa174ir
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
